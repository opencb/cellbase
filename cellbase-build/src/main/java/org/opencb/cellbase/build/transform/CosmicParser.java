package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.variant.clinical.Cosmic;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author by jpflorido on 26/05/14.
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CosmicParser extends CellBaseParser {

    private final CellBaseSerializer serializer;
    private final Path cosmicFilePath;

    public CosmicParser(CellBaseSerializer serializer, Path cosmicFilePath){
        super(serializer);
        this.serializer = serializer;
        this.cosmicFilePath = cosmicFilePath;
    }

    public void parse() {
        // COSMIC file is a tab-delimited file with the following fields (columns)
        // 0 Gene name
        // 1 Accession Number
        // 2 Gene CDS length
        // 3 HGNC ID
        // 4 Sample name
        // 5 ID sample
        // 6 ID tumour
        // 7 Primary site
        // 8 Site subtype
        // 9 Primary histology
        // 10 Histology subtype
        // 11 Genome-wide screen
        // 12 Mutation ID
        // 13 Mutation CDS
        // 14 Mutation AA
        // 15 Mutation Description
        // 16 Mutation zygosity
        // 17 Mutation NCBI36 genome position
        // 18 Mutation NCBI36 strand
        // 19 Mutation GRCh37 genome position
        // 20 Mutation GRCh37 strand
        // 21 Mutation somatic status
        // 22 PubMed PMID
        // 23 Sample source
        // 24 Tumour origin
        // 25 Age
        // 26 Comments

        try {
            BufferedReader cosmicReader = new BufferedReader(new InputStreamReader(new FileInputStream(cosmicFilePath.toFile())));
            String line;

            cosmicReader.readLine(); // First line is the header -> ignore it

            // TODO: remove the list, serialize created variants
            List<Cosmic> cosmicList = new ArrayList<>();

            while ((line = cosmicReader.readLine()) != null) {
                String[] fields = line.split("\t", 27);

                // For each variant contained, check out the sign of the strand
                if (checkValidVariant(fields[19], fields[13], 1)){ // This function filters complex changes
                    // Create new COSMIC object
                    cosmicList.add(new Cosmic(fields));
                }
            }

            // Sort objects by chromosome and position
            // TODO: sort here?
            //Collections.sort(cosmicList); // Sort function extends Comparable (which has been overrided in Cosmic.java class)

            // Move through the ordered list and save each variant grouped by chromosome (a file for a given chromosome)
            for (Cosmic cosmicVariant : cosmicList) {
                // TODO: not efficient. Transform the chromosome when creating each cosmic to avoid browse the list
                // In COSMIC database, X is referred as chr 23, Y as chr 24 and MT as chr 25. This is useful when sorting objects (see above).
                // However, when printing to JSON, each object is transformed to that 23 becomes X, 24 becomes Y and 25 becomes MT
                if (cosmicVariant.getChromosome().equals("23")){
                    cosmicVariant.setChromosome("X");
                    calculateMutation_NCBI36(cosmicVariant);
                } else if (cosmicVariant.getChromosome().equals("24")) {
                    cosmicVariant.setChromosome("Y");
                    calculateMutation_NCBI36(cosmicVariant);
                } else if (cosmicVariant.getChromosome().equals("25")) {
                    cosmicVariant.setChromosome("MT");
                    calculateMutation_NCBI36(cosmicVariant);
                }


                // serialize cosmic object
                serialize(cosmicVariant);
            }

        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Check whether the variant is valid in this version: unknown variants (?) and a deletion of more than one nucleotide are avoided in this version
     * @param mutation_CDS: string containing the change: c.8668C>G, c.6902_6903insA,c.503_508delTCTCTG
     * @param genomePosition: genome position of the variant (GRCh37)
     * @param deletionLength: maximum number of nucleotides allowed in deletions
     * @return true if valid mutation, false otherwise
     */
    private boolean checkValidVariant(String genomePosition, String mutation_CDS, int deletionLength) {
        boolean validVariant = true;

        if (genomePosition.equals(("")) || mutation_CDS.contains("?")) {
            validVariant = false;
        } else if(mutation_CDS.contains(">")) {
            validVariant = checkValidSustitution(mutation_CDS);
        } else if(mutation_CDS.contains("del")) {
            validVariant = checkValidDeletion(mutation_CDS, deletionLength);
        } else if(mutation_CDS.contains("ins")) {
            validVariant = checkValidInsertion(mutation_CDS.split("ins")[1]);
        } else if(mutation_CDS.contains("dup") || mutation_CDS.contains("Intronic")) {
            // TODO: transform duplication
            validVariant = false;
        }

        return validVariant;
    }

    private boolean checkValidInsertion(String ins) {
        boolean validVariant = true;
        if (ins.matches("\\d+")) {
            //c.503_508ins30
            validVariant = false;
        }
        return validVariant;
    }

    private boolean checkValidDeletion(String mutation_CDS, int deletionLength) {
        boolean validVariant = true;
        // TODO: split is being done multiple times (not efficient)
        // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        if(mutation_CDS.split("del").length < 2) { // c.503_508del (usually, deletions of several nucleotides)
            validVariant = false;
        } else if(mutation_CDS.split("del")[1].matches("\\d+")) { //  c.503_508del30
            validVariant = false;
        } else if(mutation_CDS.split("del")[1].length() > deletionLength) {// c.503_508delCCT and deletionLength=1 (for example)
            validVariant = false;
        }
        return validVariant;
    }

    private boolean checkValidSustitution(String mutation_CDS) {
        boolean validVariant = true;
        // Avoid changes of type c.8668CC>G, c.8668CC>GG, c.8668CC>GGG, c.8668CSSSSSC>G, etc
        String ref = null;
        String alt = mutation_CDS.split(">")[1];
        String refAux = mutation_CDS.split(">")[0];
        // TODO: pattern should be compiled once
        Matcher matcher = Pattern.compile("((A|C|G|T)+)").matcher(refAux);

        if(matcher.find()) {// Either change or deletion
            ref = matcher.group(); // Get the first group (entire pattern -> group() is equivalente to group(0)
        }
        if(ref == null || ref.isEmpty() || alt.isEmpty() || ref.length() > 1 || alt.length() > 1) {
            // Avoid variants with more than a single change (in either ref or alt)
            validVariant = false; // for example,c.8668CC>G
        }
        return validVariant;
    }

    private void calculateMutation_NCBI36(Cosmic cosmicVariant){
        // GRCh37 position
        String genomePosition = cosmicVariant.getMutation_GRCh37_genome_position();
        String newGenomePosition = cosmicVariant.getChromosome() + ":" + genomePosition.split(":")[1];
        cosmicVariant.setMutation_GRCh37_genome_position(newGenomePosition);
        // TODO: NCBI 36 == HG37??
        // NCBI position
        genomePosition = cosmicVariant.getMutation_NCBI36_genome_position();
        if (!genomePosition.isEmpty()) {
            newGenomePosition = cosmicVariant.getChromosome() + ":" + genomePosition.split(":")[1];
            cosmicVariant.setMutation_NCBI36_genome_position(newGenomePosition);
        }
    }
}