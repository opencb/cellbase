package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.variant.clinvar.v24jaxb.PublicSetType;
import org.opencb.biodata.models.variant.avro.Germline;
import org.opencb.biodata.models.variant.avro.Somatic;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.cellbase.core.common.clinical.Cosmic;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjlopez on 04/10/16.
 */
public class CosmicIndexer extends ClinicalIndexer {

    private final RocksDB rdb;
    private final Path cosmicFile;
    private Pattern mutationGRCh37GenomePositionPattern;
    private Pattern snvPattern;
    private static final String CHROMOSOME = "CHR";
    private static final String START = "START";
    private static final String END = "END";
    private static final String REF = "REF";
    private static final String ALT = "ALT";
    private int invalidPositionLines = 0;
    private int invalidSubstitutionLines = 0;
    private int invalidDeletionLines = 0;
    private int invalidInsertionLines = 0;
    private int invalidDuplicationLines = 0;
    private int invalidMutationCDSOtherReason = 0;

    private static final String VARIANT_STRING_PATTERN = "[ACGT]*";
    private int ignoredCosmicLines = 0;

    public CosmicIndexer(Path cosmicFile, RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.cosmicFile = cosmicFile;
        this.compileRegularExpressionPatterns();
    }

    private void compileRegularExpressionPatterns() {
        mutationGRCh37GenomePositionPattern = Pattern.compile("(?<" + CHROMOSOME + ">\\S+):(?<" + START + ">\\d+)-(?<" + END + ">\\d+)");
        snvPattern = Pattern.compile("c\\.\\d+(_\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");
    }

    public void index() throws RocksDBException {

        logger.info("Parsing cosmic file ...");

        try {
            BufferedReader cosmicReader = new BufferedReader(new InputStreamReader(new FileInputStream(cosmicFile.toFile())));
            String line;
            cosmicReader.readLine(); // First line is the header -> ignore it
            while ((line = cosmicReader.readLine()) != null) {
                logger.debug(line);
                Somatic somatic = buildCosmic(line);
                SequenceLocation sequenceLocation = new SequenceLocation();
                if (parsePosition(sequenceLocation, line) && parseVariant(sequenceLocation, line)) {
                    updateRocksDB(sequenceLocation, somatic);
                    numberIndexedRecords++;
                } else {
                    ignoredCosmicLines++;
                }
                totalNumberRecords++;
            }
        } catch (RocksDBException e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing Cosmic");
            throw e;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            logger.info("Done");
            this.printSummary();
        }

    }

    private void printSummary() {
        logger.info("Total number of parsed Cosmic records: {}", totalNumberRecords);
        logger.info("Number of indexed Clinvar records: {}", numberIndexedRecords);
        logger.info("Number of new variants in ClinVar not previously indexed in RocksDB: {}", numberNewVariants);
        logger.info("Number of updated variants during ClinVar indexing: {}", numberVariantUpdates);

        NumberFormat formatter = NumberFormat.getInstance();
        logger.info(formatter.format(ignoredCosmicLines) + " cosmic lines ignored: ");
        if (invalidPositionLines > 0) {
            logger.info("\t-" + formatter.format(invalidPositionLines) + " lines by invalid position");
        }
        if (invalidSubstitutionLines > 0) {
            logger.info("\t-" + formatter.format(invalidSubstitutionLines) + " lines by invalid substitution CDS");
        }
        if (invalidInsertionLines > 0) {
            logger.info("\t-" + formatter.format(invalidInsertionLines) + " lines by invalid insertion CDS");
        }
        if (invalidDeletionLines > 0) {
            logger.info("\t-" + formatter.format(invalidDeletionLines) + " lines by invalid deletion CDS");
        }
        if (invalidDuplicationLines > 0) {
            logger.info("\t-" + formatter.format(invalidDuplicationLines) + " lines because mutation CDS is a duplication");
        }
        if (invalidMutationCDSOtherReason > 0) {
            logger.info("\t-" + formatter.format(invalidMutationCDSOtherReason)
                    + " lines because mutation CDS is invalid for other reasons");
        }
    }

    private void updateRocksDB(SequenceLocation sequenceLocation, Somatic somatic) throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        byte[] dbContent = rdb.get(key);
        VariantTraitAssociation variantTraitAssociation;
        ObjectMapper mapper = new ObjectMapper();
        if (dbContent == null) {
            variantTraitAssociation = new VariantTraitAssociation();
            variantTraitAssociation.setGermline(Collections.emptyList());
            variantTraitAssociation.setSomatic(Collections.singletonList(somatic));
            numberNewVariants++;
        } else {
            variantTraitAssociation = mapper.readValue(dbContent, VariantTraitAssociation.class);
            variantTraitAssociation.getSomatic().add(somatic);
            numberVariantUpdates++;
        }
        ObjectWriter jsonObjectWriter = mapper.writer();
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
    }

    /**
     * Check whether the variant is valid and parse it.
     *
     * @return true if valid mutation, false otherwise
     */
    private boolean parseVariant(SequenceLocation sequenceLocation, String line) {
        boolean validVariant;
        String[] fields = line.split("\t", -1);
        String mutationCds = fields[27];

        if (mutationCds.contains(">")) {
            validVariant = parseSnv(mutationCds, sequenceLocation);
            if (!validVariant) {
                invalidSubstitutionLines++;
            }
        } else if (mutationCds.contains("del")) {
            validVariant = parseDeletion(mutationCds, sequenceLocation);
            if (!validVariant) {
                invalidDeletionLines++;
            }
        } else if (mutationCds.contains("ins")) {
            validVariant = parseInsertion(mutationCds, sequenceLocation);
            if (!validVariant) {
                invalidInsertionLines++;
            }
        } else if (mutationCds.contains("dup")) {
            validVariant = parseDuplication(mutationCds);
            if (!validVariant) {
                invalidDuplicationLines++;
            }
        } else {
            validVariant = false;
            invalidMutationCDSOtherReason++;
        }

        return validVariant;
    }

    private boolean parseDuplication(String dup) {
        // TODO: The only Duplication in Cosmic V70 is a structural variation that is not going to be serialized
        return false;
    }

    private boolean parseInsertion(String mutationCds, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        String insertedNucleotides = mutationCds.split("ins")[1];
        if (insertedNucleotides.matches("\\d+") || !insertedNucleotides.matches(VARIANT_STRING_PATTERN)) {
            //c.503_508ins30
            validVariant = false;
        } else {
            sequenceLocation.setReference("");
            sequenceLocation.setAlternate(getPositiveStrandString(insertedNucleotides, sequenceLocation.getStrand()));
        }

        return validVariant;
    }

    private boolean parseDeletion(String mutationCds, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        String[] mutationCDSArray = mutationCds.split("del");

        // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        if (mutationCDSArray.length < 2) { // c.503_508del (usually, deletions of several nucleotides)
            // TODO: allow these variants
            validVariant = false;
        } else if (mutationCDSArray[1].matches("\\d+")
                || !mutationCDSArray[1].matches(VARIANT_STRING_PATTERN)) { // Avoid allele strings containing Ns, for example
            validVariant = false;
        } else {
            sequenceLocation.setReference(getPositiveStrandString(mutationCDSArray[1], sequenceLocation.getStrand()));
            sequenceLocation.setAlternate("");
        }

        return validVariant;
    }

    private boolean parseSnv(String mutationCds, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        Matcher snvMatcher = snvPattern.matcher(mutationCds);

        if (snvMatcher.matches()) {
            String ref = snvMatcher.group(REF);
            String alt = snvMatcher.group(ALT);
            if (!ref.equalsIgnoreCase("N") && !alt.equalsIgnoreCase("N")) {
                sequenceLocation.setReference(getPositiveStrandString(ref, sequenceLocation.getStrand()));
                sequenceLocation.setAlternate(getPositiveStrandString(alt, sequenceLocation.getStrand()));
            } else {
                validVariant = false;
            }
        } else {
            validVariant = false;
        }

        return validVariant;
    }

    private String getPositiveStrandString(String alleleString, String strand) {
        if (strand.equals("-")) {
            return reverseComplementary(alleleString);
        } else {
            return alleleString;
        }
    }

    private String reverseComplementary(String alleleString) {
        char[] reverseAlleleString = new StringBuilder(alleleString).reverse().toString().toCharArray();
        for (int i = 0; i < reverseAlleleString.length; i++) {
            reverseAlleleString[i] = VariantAnnotationUtils.COMPLEMENTARY_NT.get(reverseAlleleString[i]);
        }

        return String.valueOf(reverseAlleleString);
    }

    private Somatic buildCosmic(String line) {
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
        // 11 Primary histology
        // 12 Histology subtype
        // 15 Genome-wide screen
        // 16 Mutation ID
        // 17 Mutation CDS
        // 18 Mutation AA
        // 19 Mutation Description
        // 29 Mutation zygosity
        // 23 Mutation GRCh37 genome position
        // 24 Mutation GRCh37 strand
        // 25 Snp
        // 26 FATHMM Prediction
        // 28 Mutation somatic status
        // 29 PubMed PMID
        // 30 ID STUDY
        // 31 Sample source
        // 32 Tumour origin
        // 33 Age
        // 34 Comments
        String[] fields = line.split("\t", -1); // -1 argument make split return also empty fields
        Somatic cosmic = new Somatic();
        cosmic.setGeneNames(Collections.singletonList(fields[0]));
        cosmic.setAccession(fields[1]);
        if (!fields[3].equalsIgnoreCase(fields[0])) {
            cosmic.getGeneNames().add(fields[3]);
        }
        cosmic.setPrimarySite(fields[7]);
        cosmic.setSiteSubtype(fields[8]);
        cosmic.setPrimaryHistology(fields[11]);
        cosmic.setHistologySubtype(fields[12]);
        cosmic.setAccession(fields[16]);
//        cosmic.setMutationGRCh37GenomePosition(fields[23]);
//        cosmic.setMutationGRCh37Strand(fields[24]);
        cosmic.setMutationSomaticStatus(fields[28]);
        cosmic.setBibliography(Collections.singletonList(fields[29]));
        cosmic.setSampleSource(fields[31]);
        cosmic.setTumourOrigin(fields[32]);

        return cosmic;
    }

    public boolean parsePosition(SequenceLocation sequenceLocation, String line) {
        boolean success = false;

        String[] fields = line.split("\t", -1);
        String positionString = fields[23];
        sequenceLocation.setStrand(fields[24]);
        if (positionString != null && !positionString.isEmpty()) {
            Matcher matcher = mutationGRCh37GenomePositionPattern.matcher(positionString);
            if (matcher.matches()) {
                setCosmicChromosome(matcher.group(CHROMOSOME), sequenceLocation);
                sequenceLocation.setStart(getStart(Integer.parseInt(matcher.group(START)), fields[27]));
                sequenceLocation.setEnd(Integer.parseInt(matcher.group(END)));
                success = true;
            }
        }
        if (!success) {
            this.invalidPositionLines++;
        }
        return success;
    }

    private Integer getStart(Integer readPosition, String mutationCDS) {
        // In order to agree with the Variant model and what it's stored in variation, the start must be incremented in
        // 1 for insertions given what is provided in the COSMIC file
        if (mutationCDS.contains("ins")) {
            return readPosition + 1;
        } else {
            return readPosition;
        }
    }

    private void setCosmicChromosome(String chromosome, SequenceLocation sequenceLocation) {
        switch (chromosome) {
            case "23":
                sequenceLocation.setChromosome("X");
                break;
            case "24":
                sequenceLocation.setChromosome("Y");
                break;
            case "25":
                sequenceLocation.setChromosome("MT");
                break;
            default:
                sequenceLocation.setChromosome(chromosome);
        }
    }

}
