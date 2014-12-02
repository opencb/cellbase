package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.formats.clinical.Cosmic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author by jpflorido on 26/05/14.
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CosmicParser extends CellBaseParser {
    
    private final Path cosmicFilePath;
    private static final String CHROMOSOME = "CHR";
    private static final String START = "START";
    private static final String END = "END";
    private static final String REF = "REF";
    private static final String ALT = "ALT";
    private Pattern mutationGRCh37GenomePositionPattern;
    private Pattern snvPattern;
    private long invalidPositionLines;
    private long invalidSubstitutionLines;
    private long invalidInsertionLines;
    private long invalidDeletionLines;
    private long invalidDuplicationLines;
    private long invalidMutationCDSOtherReason;

    public CosmicParser(Path cosmicFilePath, CellBaseSerializer serializer){
        super(serializer);
        this.cosmicFilePath = cosmicFilePath;
        this.compileRegularExpressionPaterns();
    }

    private void compileRegularExpressionPaterns() {
        mutationGRCh37GenomePositionPattern = Pattern.compile("(?<"+CHROMOSOME+">\\S+):(?<"+START+">\\d+)-(?<"+END+">\\d+)");
        snvPattern = Pattern.compile("c\\.\\d+(_\\d+)?(?<"+REF+">(A|C|T|G)+)>(?<"+ALT+">(A|C|T|G)+)");
    }

    public void parse() {
        logger.info("Parsing cosmic file ...");
        long processedCosmicLines = 0, ignoredCosmicLines = 0;

        try {
            BufferedReader cosmicReader = new BufferedReader(new InputStreamReader(new FileInputStream(cosmicFilePath.toFile())));

            String line;
            cosmicReader.readLine(); // First line is the header -> ignore it

            while ((line = cosmicReader.readLine()) != null) {
                Cosmic cosmic = buildCosmic(line);

                if (parseChromosomeStartAndEnd(cosmic) && parseVariant(cosmic))  {
                    serializer.serialize(cosmic);
                } else {
                    ignoredCosmicLines++;
                }
                processedCosmicLines++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            logger.info("Done");
            this.printSummary(processedCosmicLines, ignoredCosmicLines);
        }
    }

    private Cosmic buildCosmic(String line) {
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
        // 17 Mutation GRCh37 genome position
        // 18 Mutation GRCh37 strand
        // 19 Snp
        // 20 FATHMM Prediction
        // 21 Mutation somatic status
        // 22 PubMed PMID
        // 23 ID STUDY
        // 24 Sample source
        // 25 Tumour origin
        // 26 Age
        // 27 Comments
        String[] fields = line.split("\t", -1); // -1 argument make split return also empty fields
        Cosmic cosmic = new Cosmic();
        cosmic.setGeneName(fields[0]);
        cosmic.setAccessionNumber(fields[1]);
        cosmic.setGeneCDSLength(Integer.parseInt(fields[2]));
        cosmic.setHgncId(fields[3]);
        cosmic.setSampleName(fields[4]);
        cosmic.setIdSample(fields[5]);
        cosmic.setID_tumour(fields[6]);
        cosmic.setPrimarySite(fields[7]);
        cosmic.setSiteSubtype(fields[8]);
        cosmic.setPrimaryHistology(fields[9]);
        cosmic.setHistologySubtype(fields[10]);
        cosmic.setGenomeWideScreen(fields[11]);
        cosmic.setMutationID(fields[12]);
        cosmic.setMutationCDS(fields[13]);
        cosmic.setMutationAA(fields[14]);
        cosmic.setMutationDescription(fields[15]);
        cosmic.setMutationZygosity(fields[16]);
        cosmic.setMutationGRCh37GenomePosition(fields[17]);
        cosmic.setMutationGRCh37Strand(fields[18]);
        if(!fields[19].isEmpty() && fields[19].equalsIgnoreCase("y")){
            cosmic.setSnp(true);
        }
        cosmic.setFathmmPrediction(fields[20]);
        cosmic.setMutationSomaticStatus(fields[21]);
        cosmic.setPubmedPMID(fields[22]);
        if (!fields[23].isEmpty() && !fields[23].equals("NS")) {
            cosmic.setIdStudy(Integer.parseInt(fields[23]));
        }
        cosmic.setSampleSource(fields[24]);
        cosmic.setTumourOrigin(fields[25]);
        if(!fields[26].isEmpty() && !fields[26].equals("NS")) {
            cosmic.setAge(Float.parseFloat(fields[26]));
        }
        cosmic.setComments(fields[27]);
        return cosmic;
    }

    public boolean parseChromosomeStartAndEnd(Cosmic cosmic) {
        boolean success = false;
        if(cosmic.getMutationGRCh37GenomePosition() != null && !cosmic.getMutationGRCh37GenomePosition().isEmpty()){
            Matcher matcher = mutationGRCh37GenomePositionPattern.matcher(cosmic.getMutationGRCh37GenomePosition());
            if (matcher.matches()) {
                setCosmicChromosome(matcher.group(CHROMOSOME), cosmic);
                cosmic.setStart(Integer.parseInt(matcher.group(START)));
                cosmic.setEnd(Integer.parseInt(matcher.group(END)));
                success = true;
            }
        }
        if (!success) {
            this.invalidPositionLines++;
        }
        return success;
    }

    private void setCosmicChromosome(String chromosome, Cosmic cosmic) {
        switch (chromosome) {
            case "23":
                cosmic.setChromosome("X");
                break;
            case "24":
                cosmic.setChromosome("Y");
                break;
            case "25":
                cosmic.setChromosome("MT");
                break;
            default:
                cosmic.setChromosome(chromosome);
        }
    }

//    public boolean calculateAltAndRef(Cosmic cosmic){
//        if (cosmic.getMutationCDS().contains(">")) {
//            // Change (one or more nucleotides). Get number of nucleotides of alternative
//            cosmic.setAlternate(cosmic.getMutationCDS().split(">")[1]);
//            cosmic.setReference(cosmic.getMutationCDS().split(">")[0]);
//        } else if (cosmic.getMutationCDS().contains("del")) {
//            // Deletion
//            cosmic.setReference(cosmic.getMutationCDS().split("del")[1]);
//            cosmic.setAlternate("-");
//        }  else if (cosmic.getMutationCDS().contains("ins")) {
//            // Insertion
//            cosmic.setReference("-");
//            cosmic.setAlternate(cosmic.getMutationCDS().split("ins")[1]);
//        }
//
//        // Check strand
//        // TODO: MutationCDS equals '-' ? Strand is stored in the field 'Mutation CRCh47 strand'
//        if (cosmic.getMutationCDS().equals("-")) {
//            // Negative strand
//            if (!cosmic.getAlternate().equals("-")){
//                cosmic.setAlternate(DNASequenceUtils.reverseComplement(cosmic.getAlternate()));
//            } if (!cosmic.getReference().equals("-")){
//                cosmic.setReference(DNASequenceUtils.reverseComplement(cosmic.getReference()));
//            }
//        }
//    }

    private void printSummary(long processedCosmicLines, long ignoredCosmicLines) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(processedCosmicLines) + " cosmic lines");
        logger.info("Serialized " + formatter.format(processedCosmicLines - ignoredCosmicLines) + " cosmic objects");
        logger.info(formatter.format(ignoredCosmicLines) + " cosmic lines ignored: ");
        if (invalidPositionLines > 0) {
            logger.info("\t-" +  formatter.format(invalidPositionLines) + " lines by invalid position");
        }
        if (invalidSubstitutionLines > 0) {
            logger.info("\t-" +  formatter.format(invalidSubstitutionLines) + " lines by invalid substitution CDS");
        }
        if (invalidInsertionLines > 0) {
            logger.info("\t-" +  formatter.format(invalidInsertionLines) + " lines by invalid insertion CDS");
        }
        if (invalidDeletionLines > 0) {
            logger.info("\t-" +  formatter.format(invalidDeletionLines) + " lines by invalid deletion CDS");
        }
        if (invalidDuplicationLines > 0) {
            logger.info("\t-" +  formatter.format(invalidDuplicationLines) + " lines because mutation CDS is a duplication");
        }
        if (invalidMutationCDSOtherReason > 0) {
            logger.info("\t-" +  formatter.format(invalidMutationCDSOtherReason) + " lines because mutation CDS is invalid for other reasons");
        }
    }

    /**
     * Check whether the variant is valid and parse it
     * @return true if valid mutation, false otherwise
     */
    private boolean parseVariant(Cosmic cosmic) {
        boolean validVariant = true;

        String mutationCds = cosmic.getMutationCDS();
        if(mutationCds.contains(">")) {
            validVariant = parseSnv(mutationCds, cosmic);
            if(!validVariant){
                invalidSubstitutionLines++;
            }
        } else if(mutationCds.contains("del")) {
            validVariant = parseDeletion(mutationCds, cosmic);
            if(!validVariant){
                invalidDeletionLines++;
            }
        } else if(mutationCds.contains("ins")) {
            validVariant = parseInsertion(mutationCds, cosmic);
            if (!validVariant) {
               invalidInsertionLines++;
            }
        } else if(mutationCds.contains("dup")) {
            validVariant = parseDuplication(mutationCds);
            if(!validVariant){
                invalidDuplicationLines++;
            }
        } else {
        	validVariant = false;
            invalidMutationCDSOtherReason++;
        }

        return validVariant;
    }
    
    private boolean parseDuplication(String dup){
    	// TODO: The only Duplication in Cosmic V70 is a structural variation that is not going to be serialized
    	return false;
    }

    private boolean parseInsertion(String mutationCds, Cosmic cosmic) {
        boolean validVariant = true;
        String insertedNucleotides = mutationCds.split("ins")[1];
        if (insertedNucleotides.matches("\\d+")) {
            //c.503_508ins30
            validVariant = false;
        } else {
            cosmic.setReference("-");
            cosmic.setAlternate(insertedNucleotides);
        }

        return validVariant;
    }

    private boolean parseDeletion(String mutationCds, Cosmic cosmic) {
        boolean validVariant = true;
        String[] mutationCDSArray = mutationCds.split("del");

        // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        if(mutationCDSArray.length < 2) { // c.503_508del (usually, deletions of several nucleotides)
            // TODO: allow these variants
            validVariant = false;
        } else if(mutationCDSArray[1].matches("\\d+")) { //  c.503_508del30
            validVariant = false;
        } else {
            cosmic.setReference(mutationCDSArray[1]);
            cosmic.setAlternate("-");
        }

        return validVariant;
    }

    private boolean parseSnv(String mutation_CDS, Cosmic cosmic) {
        boolean validVariant = true;
        Matcher snvMatcher = snvPattern.matcher(mutation_CDS);

        if (snvMatcher.matches()) {
            cosmic.setReference(snvMatcher.group(REF));
            cosmic.setAlternate(snvMatcher.group(ALT));
        } else {
            validVariant = false;
        }

        return validVariant;
    }
}