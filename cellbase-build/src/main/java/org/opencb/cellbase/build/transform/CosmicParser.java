package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.variant.clinical.Cosmic;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;


import java.io.*;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;


/**
 * @author by jpflorido on 26/05/14.
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CosmicParser {

    private final CellBaseSerializer serializer;

    public CosmicParser(CellBaseSerializer serializer){
        this.serializer = serializer;
    }
	
    /**
     * Function that converts a nucleotide string into its complementary in reverse order
     * @param nucleotides string of nucleotides
     * @return complementary string of nucleotides
     */
    private String getCDNA(String nucleotides) {
        StringBuffer cDNA = new StringBuffer("");

        // For each nucleotide, get its complement base
        for(int i=nucleotides.length()-1; i>=0;i--) {
            switch(nucleotides.charAt(i)){
                case 'A': cDNA.append("T");
                    break;
                case 'C': cDNA.append("G");
                    break;
                case 'G': cDNA.append("C");
                    break;
                case 'T': cDNA.append("A");
                    break;
            }
        }

        return cDNA.toString();
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

        if(genomePosition.equals(("")))
            validVariant = false;
        else if(mutation_CDS.contains(">")) // Avoid changes of type c.8668CC>G, c.8668CC>GG, c.8668CC>GGG, c.8668CSSSSSC>G, etc
        {
            String ref="";
            String alt="";
            alt = mutation_CDS.split(">")[1];
            String refAux = mutation_CDS.split(">")[0];
            Matcher matcher=Pattern.compile("((A|C|G|T)+)").matcher(refAux);

            if(matcher.find()) // Either change or deletion
                ref=matcher.group(); // Get the first group (entire pattern -> group() is equivalente to group(0)

            if(ref.length()>1 || alt.length()>1 || ref.equals("") || alt.equals("")) // Avoid variants with more than a single change (in either ref or alt)
            	validVariant=false; // for example,c.8668CC>G
        }
        else if(mutation_CDS.contains("?"))
        	validVariant=false;
        else if(mutation_CDS.contains("del")) // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        {
        	if(mutation_CDS.split("del").length<2) // c.503_508del (usually, deletions of several nucleotides)
        		validVariant=false;
        	else if(mutation_CDS.split("del")[1].matches("\\d+")) //  c.503_508del30
        		validVariant=false;
        	else if(mutation_CDS.split("del")[1].length() > deletionLength) // c.503_508delCCT and deletionLength=1 (for example)
        		validVariant=false;
        } else if(mutation_CDS.contains("ins")) {
            if(mutation_CDS.split("ins")[1].matches("\\d+")) //c.503_508ins30
                validVariant=false;
        } else if(mutation_CDS.contains("dup")) {
            validVariant=false;
        } else if(mutation_CDS.contains("Intronic")) {
            validVariant=false;
        }

        return validVariant;
    }

    /**
     * Function that parses a cosmic file and generates a Json file
     * @param cosmicFilePath Path of input file (COSMIC file)
     */
    public void parse(Path cosmicFilePath) {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cosmicFilePath.toFile())));
            String line = "";

            reader.readLine(); // First line is the header -> ignore it

            List<Cosmic> myList = new ArrayList<>(); // All Cosmic objects will be stored in a list

            while ((line = reader.readLine()) != null) {
                String ref = "";
                String alt = "";
                Float age = null;

                String[] fields = line.split("\t",27);

                // For each variant contained, check out the sign of the strand
                String Mutation_GRCh37_genome_position = fields[19];
                String Mutation_CDS = fields[13];
                if (checkValidVariant(Mutation_GRCh37_genome_position,Mutation_CDS,1)){ // This function filters complex changes
                    // Get chr and pos from Genomic position
                    String chr = Mutation_GRCh37_genome_position.split(":")[0];
                    String initEnd = Mutation_GRCh37_genome_position.split(":")[1];
                    String pos = initEnd.split("-")[0];

                    String Gene_name = fields[0];
                    String Accession_Number = fields[1];
                    int gene_CDS_length = Integer.parseInt(fields[2]);
                    String HGNC_id = fields[3];
                    String Sample_name = fields[4];
                    String ID_sample = fields[5];
                    String ID_tumour = fields[6];
                    String Primary_site = fields[7];
                    String Site_subtype = fields[8];
                    String Primary_histology = fields[9];
                    String Histology_subtype = fields[10];
                    String Genome_wide_screen = fields[11];
                    String Mutation_ID = fields[12];

                    String Mutation_AA = fields[14];
                    String Mutation_Description = fields[15];
                    String Mutation_zygosity = fields[16];
                    String Mutation_NCBI36_genome_position = fields[17];
                    String Mutation_NCBI36_strand = fields[18];
                    String Mutation_GRCh37_strand = fields[20];
                    String Mutation_somatic_status = fields[21];
                    String Pubmed_PMID = fields[22];
                    String Sample_source = fields[23];
                    String Tumour_origin = fields[24];
                    if(!fields[25].equals(""))
                        age = Float.parseFloat(fields[25]);
                    else
                        age=null;
                    String comments = fields[26];

                    // Work on Mutation_CDS to extract alt and ref
                    //Check type of variant (SNP or indel)

                    // http://www.hgvs.org/mutnomen/nucleotide.html Check format of this field
                    if (Mutation_CDS.contains(">")) // Change (one or more nucleotides)
                    {

                        // Get number of nucleotides of alternative
                        alt = Mutation_CDS.split(">")[1];
                        String refAux = Mutation_CDS.split(">")[0];
                        Matcher matcher=Pattern.compile("((A|C|G|T)+)").matcher(refAux); // Although more than one nucleotide is allowed, in this version just one nucleotide change is allowed

                        if(matcher.find()) // Either change or deletion
                            ref = matcher.group(); // Get the first group (entire pattern -> group() is equivalente to group(0)

                    } else if (Mutation_CDS.contains("del")) {
                        // Deletion
                        ref = Mutation_CDS.split("del")[1];
                        alt = "-";
                    } else {
                        // Insertion
                        ref = "-";
                        alt = Mutation_CDS.split("ins")[1];
                    }

                    // Check strand
                    if (Mutation_GRCh37_strand.equals("-")) // Negative strand
                    {
                        if (!alt.equals("-")){
                            alt = getCDNA(alt);
                        } if (!ref.equals("-")){
                            ref = getCDNA(ref);
                        }
                    }

                    // Create new COSMIC object
                    Cosmic cosmicVariant = new Cosmic(alt, ref, chr, Integer.parseInt(pos), Integer.parseInt(pos),
                            Gene_name, Mutation_GRCh37_strand, Primary_site, Mutation_zygosity, Mutation_AA,
                            Tumour_origin, Histology_subtype, Sample_source, Accession_Number, Mutation_ID,
                            Mutation_CDS, Sample_name, Primary_histology, Mutation_GRCh37_genome_position,
                            Mutation_Description, Genome_wide_screen, ID_tumour, ID_sample,
                            Mutation_somatic_status, Site_subtype, Mutation_NCBI36_strand,
                            Mutation_NCBI36_genome_position, gene_CDS_length, HGNC_id, Pubmed_PMID, age, comments);
                    myList.add(cosmicVariant);
                }

            }

            // Sort objects by chromosome and position
            Collections.sort(myList); // Sort function extends Comparable (which has been overrided in Cosmic.java class)

            // Move through the ordered list and save each variant grouped by chromosome (a file for a given chromosome)
            String genomePosition="";
            String newGenomePosition="";
            for (Cosmic cosmicVariant : myList) {
                // In COSMIC database, X is referred as chr 23, Y as chr 24 and MT as chr 25. This is useful when sorting objects (see above).
                // However, when printing to JSON, each object is transformed to that 23 becomes X, 24 becomes Y and 25 becomes MT
                if (cosmicVariant.getChromosome().equals("23") ||
                        cosmicVariant.getChromosome().equals("24") ||
                        cosmicVariant.getChromosome().equals("25")) {

                    if (cosmicVariant.getChromosome().equals("23")){
                        cosmicVariant.setChromosome("X");
                    } else if (cosmicVariant.getChromosome().equals("24")) {
                        cosmicVariant.setChromosome("Y");
                    } else { // Chromosome 25
                        cosmicVariant.setChromosome("MT");
                    }

                    // GRCh37 position
                    genomePosition = cosmicVariant.getMutation_GRCh37_genome_position();
                    newGenomePosition = cosmicVariant.getChromosome() + ":" + genomePosition.split(":")[1];
                    cosmicVariant.setMutation_GRCh37_genome_position(newGenomePosition);

                    // NCBI position
                    genomePosition = cosmicVariant.getMutation_NCBI36_genome_position();
                    if (!genomePosition.isEmpty()) {
                        newGenomePosition = cosmicVariant.getChromosome() + ":" + genomePosition.split(":")[1];
                        cosmicVariant.setMutation_NCBI36_genome_position(newGenomePosition);
                    }
                }

                // serialize cosmic object
                serializer.serializeObject(cosmicVariant);
            }

        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}