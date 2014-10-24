package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.variant.clinical.Cosmic;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
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
    private Pattern pattern;

    public CosmicParser(CellBaseSerializer serializer, Path cosmicFilePath){
        super(serializer);
        this.serializer = serializer;
        this.cosmicFilePath = cosmicFilePath;
        pattern = Pattern.compile("((A|C|G|T)+)");
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

            while ((line = cosmicReader.readLine()) != null) {
                String[] fields = line.split("\t", 27);

                // For each variant contained, check out the sign of the strand
                if (checkValidVariant(fields[19], fields[13], 1)){ // This function filters complex changes
                    // Create new COSMIC object
                    Cosmic c = new Cosmic(fields);
                    serialize(c);
                    System.out.println("Objeto: "+c);
                }
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
        } else if(mutation_CDS.contains("dup")) {
            validVariant = checkValidDuplication(mutation_CDS);
        } else if(mutation_CDS.contains("Intronic")){
        	validVariant = false;
        }

        return validVariant;
    }
    
    private boolean checkValidDuplication(String dup){
    	boolean validVariant = false;
    	
    	/*
    	 * TODO: The only Duplication in Cosmic V68 is a structural variation.
    	 * we are not going to modify a variation of more than one nucleotide 
    	 */
    	
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
        String[] mutationCDSArray = mutation_CDS.split("del");

        // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        if(mutationCDSArray.length < 2) { // c.503_508del (usually, deletions of several nucleotides)
            validVariant = false;
        } else if(mutationCDSArray[1].matches("\\d+")) { //  c.503_508del30
            validVariant = false;
        } else if(mutationCDSArray[1].length() > deletionLength) {// c.503_508delCCT and deletionLength=1 (for example)
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

        Matcher matcher = pattern.matcher(refAux);

        if(matcher.find()) {// Either change or deletion
            ref = matcher.group(); // Get the first group (entire pattern -> group() is equivalente to group(0)
        }
        if(ref == null || ref.isEmpty() || alt.isEmpty() || ref.length() > 1 || alt.length() > 1) {
            // Avoid variants with more than a single change (in either ref or alt)
            validVariant = false; // for example,c.8668CC>G
        }
        
        return validVariant;
    }
}