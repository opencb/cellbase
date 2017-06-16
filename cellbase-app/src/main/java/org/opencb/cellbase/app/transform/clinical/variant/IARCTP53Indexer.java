package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.biodata.tools.sequence.FastaIndexManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjlopez on 21/11/16.
 */
public class IARCTP53Indexer extends ClinicalIndexer {

    private static final String IARCTP53_NAME = "IARCTP53";
    private static final String VARIANT_STRING_PATTERN = "[ACGT]*";

    private static final String REF = "REF";
    private static final String ALT = "ALT";

    private static final Map<String, String> INHERITANCE_TAGS_MAP = new HashMap<>(4);

    static {
        INHERITANCE_TAGS_MAP.put("P", "paternal");
        INHERITANCE_TAGS_MAP.put("M", "maternal");
        INHERITANCE_TAGS_MAP.put("P&M", "maternal and paternal");
        INHERITANCE_TAGS_MAP.put("de novo", "de novo");
        INHERITANCE_TAGS_MAP.put("na", "not known");
    }

    private final RocksDB rdb;
    private final Path germlineFile;
    private final Path somaticFile;
    private final String assembly;
    private final Pattern snvPattern;
    private final Path germlineReferencesFile;
    private final Path somaticReferencesFile;
    private final Path genomeSequenceFilePath;
    private final Pattern kbSizePattern;
    private final Pattern mbSizePattern;
    private final Pattern smallSizePattern;
    private int ignoredRecords = 0;
    private int invalidSubstitutionLines = 0;
    private int invalidDeletionLines = 0;
    private int invalidInsertionLines = 0;
    private int invalidgDescriptionOtherReason = 0;
    private int nDuplications = 0;

    public IARCTP53Indexer(Path germlineFile, Path germlineReferencesFile, Path somaticFile,
                           Path somaticReferencesFile, Path genomeSequenceFilePath, String assembly, RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.assembly = assembly;
        this.germlineFile = germlineFile;
        this.germlineReferencesFile = germlineReferencesFile;
        this.somaticFile = somaticFile;
        this.somaticReferencesFile = somaticReferencesFile;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        snvPattern = Pattern.compile("g\\.\\d+(_\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");
        kbSizePattern = Pattern.compile("\\d+((kb)|(Kb)|(KB))");
        mbSizePattern = Pattern.compile("\\d+((mb)|(Mb)|(MB))");
        smallSizePattern = Pattern.compile("\\d+");
    }

    public void index() throws RocksDBException {
        index(germlineFile, germlineReferencesFile, true);
        index(somaticFile, somaticReferencesFile, false);
        this.printSummary();
    }

    private void index(Path filePath, Path referencesFilePath, boolean isGermline) throws RocksDBException {

        // Preparing the fasta file for fast accessing
        FastaIndexManager fastaIndexManager = null;
        try {
            fastaIndexManager = new FastaIndexManager(genomeSequenceFilePath, true);
            if (!fastaIndexManager.isConnected()) {
                fastaIndexManager.index();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        logger.info("Parsing {} ...", filePath.toString());
        int variantIdColumnIndex = isGermline ? 9 : 1;

        try {
            BufferedReader reader = FileUtils.newBufferedReader(filePath);
            String line;
            reader.readLine(); // First line is the header -> ignore it
            String previousVariantId = null;
            SequenceLocation sequenceLocation = null;
            VariantTraitAssociation variantTraitAssociation = null;
            Map<String, String> references = loadReferences(referencesFilePath, isGermline);
            boolean skip = false;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);  // -1 argument make split return also empty fields
                logger.debug(line);
                // One variant may appear in multiple lines - one for each individual in which was observed. fields[9]
                // contains MUT_ID
                if (!fields[variantIdColumnIndex].equals(previousVariantId)) {
                    totalNumberRecords++;
                    // Do not update RocksDB on first iteration
                    if (previousVariantId != null && !skip) {
                        updateRocksDB(sequenceLocation, variantTraitAssociation);
                        numberIndexedRecords++;
                    }
                    sequenceLocation = parseVariant(fields, fastaIndexManager, isGermline);
                    if (sequenceLocation != null) {
                        variantTraitAssociation = new VariantTraitAssociation();
                        // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//                        variantTraitAssociation.setGermline(new ArrayList<>());
//                        variantTraitAssociation.setSomatic(new ArrayList<>());
                        skip = false;
                    } else {
                        skip = true;
                        ignoredRecords++;
                    }
                    previousVariantId = fields[variantIdColumnIndex];

                }

                if (!skip) {
                    List<String> bibliography = parseBibliography(fields, references, isGermline);
                    // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//                    if (isGermline) {
//                        Germline germlineObject = buildGermline(fields);
//                        if (bibliography != null) {
//                            germlineObject.setBibliography(bibliography);
//                        }
//                        variantTraitAssociation.getGermline().add(germlineObject);
//                    } else {
//                        Somatic somaticObject = buildSomatic(fields);
//                        if (bibliography != null) {
//                            somaticObject.setBibliography(bibliography);
//                        }
//                        variantTraitAssociation.getSomatic().add(somaticObject);
//                    }
                }
            }

            // Write last variant
            // Do not update RocksDB on first iteration
            if (previousVariantId != null && !skip) {
                updateRocksDB(sequenceLocation, variantTraitAssociation);
                numberIndexedRecords++;
            }
        } catch (RocksDBException e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing IARCTP53");
            throw e;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            logger.info("Done");
        }

    }

    private List<String> parseBibliography(String[] fields, Map<String, String> references, boolean isGermline) {
        List<String> bibliography = null;
        int bibliographyColumnIndex = isGermline ? 53 : 64;
        // fields[53] may contain bibliography ids
        if (!fields[bibliographyColumnIndex].isEmpty() && !fields[bibliographyColumnIndex].equalsIgnoreCase("na")) {
            bibliography = new ArrayList<>();
            // - 1 since ids in the file are assigned 1-based while indexing in the references array is
            // 0 based
            bibliography.add(references.get(fields[bibliographyColumnIndex]));
        }

        return bibliography;
    }

    private Map<String, String> loadReferences(Path filePath, boolean isGermline) throws IOException {
        BufferedReader reader = FileUtils.newBufferedReader(filePath);

        reader.readLine(); // Skip header on the first line
        logger.info("Loading references from {} ", filePath.toString());
        Map<String, String> references = new HashMap<>(300);
        int pubmedIdPosition = isGermline ? 8 : 9;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t", -1); // -1 argument make split return also empty fields
            references.put(fields[0], "PMID:" + fields[pubmedIdPosition]);

        }
        logger.info("{} references loaded", references.size());

        return references;

    }

    private void printSummary() {
        logger.info("Total number of parsed IARCTP53 records: {}", totalNumberRecords);
        logger.info("Number of indexed IARCTP53 records: {}", numberIndexedRecords);
        logger.info("Number of new variants in IARCTP53 not previously indexed in RocksDB: {}", numberNewVariants);
        logger.info("Number of updated variants during IARCTP53 indexing: {}", numberVariantUpdates);

        NumberFormat formatter = NumberFormat.getInstance();
        logger.info(formatter.format(ignoredRecords) + " IARCTP53 records ignored: ");
        if (invalidSubstitutionLines > 0) {
            logger.info("\t-" + formatter.format(invalidSubstitutionLines) + " lines by invalid substitution");
        }
        if (invalidInsertionLines > 0) {
            logger.info("\t-" + formatter.format(invalidInsertionLines) + " lines by invalid insertion");
        }
        if (invalidDeletionLines > 0) {
            logger.info("\t-" + formatter.format(invalidDeletionLines) + " lines by invalid deletion");
        }
        if (nDuplications > 0) {
            logger.info("\t-" + formatter.format(nDuplications) + " lines by duplication");
        }
        if (invalidgDescriptionOtherReason > 0) {
            logger.info("\t-" + formatter.format(invalidgDescriptionOtherReason)
                    + " lines because g. description is invalid for other reasons");
        }
    }

    private void updateRocksDB(SequenceLocation sequenceLocation, VariantTraitAssociation variantTraitAssociation)
            throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        byte[] dbContent = rdb.get(key);
        if (dbContent == null) {
            rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
            numberNewVariants++;
        } else {
            VariantTraitAssociation existingVariantTraitAssociation = mapper.readValue(dbContent, VariantTraitAssociation.class);
            // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//            existingVariantTraitAssociation.getGermline().addAll(variantTraitAssociation.getGermline());
//            existingVariantTraitAssociation.getSomatic().addAll(variantTraitAssociation.getSomatic());
            rdb.put(key, jsonObjectWriter.writeValueAsBytes(existingVariantTraitAssociation));
            numberVariantUpdates++;
        }

    }

    /**
     * Check whether the variant is valid and parse it.
     *
     * @return true if valid mutation, false otherwise
     */
    private SequenceLocation parseVariant(String[] fields, FastaIndexManager fastaIndexManager,
                                          boolean isGermline) throws RocksDBException {

        SequenceLocation sequenceLocation = parsePosition(fields, isGermline);
        int gDescriptionColumnIndex = isGermline ? 19 : 10;
        String gDescription = fields[gDescriptionColumnIndex];

        boolean validVariant = true;
        if (gDescription.contains(">")) {
            validVariant = parseSnv(gDescription, sequenceLocation);
            if (!validVariant) {
                invalidSubstitutionLines++;
            }
        } else if (gDescription.contains("del")) {
            validVariant = parseDeletion(gDescription, sequenceLocation, fastaIndexManager);
            if (!validVariant) {
                invalidDeletionLines++;
            }
        } else if (gDescription.contains("ins")) {
            validVariant = parseInsertion(gDescription, sequenceLocation);
            if (!validVariant) {
                invalidInsertionLines++;
            }
        } else if (gDescription.contains("dup")) {
            parseDuplication(gDescription);
            nDuplications++;
            validVariant = false;
        } else {
            validVariant = false;
            invalidgDescriptionOtherReason++;
        }

        return validVariant ? sequenceLocation : null;
    }

    private void parseDuplication(String dup) {
        // TODO: No duplications seen so far
        logger.warn("Duplication found when parsing the IARC TP53 file: {}. No action currently "
                + "implemented. Variant will be skipped.", dup);
    }

    private boolean parseInsertion(String mutationCds, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        String insertedNucleotides = mutationCds.split("ins")[1];
        if (insertedNucleotides.matches("\\d+") || !insertedNucleotides.matches(VARIANT_STRING_PATTERN)) {
            //c.503_508ins30
            validVariant = false;
        } else {
            sequenceLocation.setReference("");
            sequenceLocation.setAlternate(insertedNucleotides);
        }

        return validVariant;
    }

    private boolean parseDeletion(String gDescription, SequenceLocation sequenceLocation,
                                  FastaIndexManager fastaIndexManager) throws RocksDBException {
        boolean validVariant = true;
        String[] gDescriptionArray = gDescription.split("del");

        // For deletions, only deletions of, at most, deletionLength nucleotide are allowed
        if (gDescriptionArray.length < 2) { // c.503_508del (usually, deletions of several nucleotides)
            // TODO: allow these variants
            validVariant = false;
        } else if (gDescriptionArray[1].matches("\\d+")) { // Expecting number of deleted nts here
            sequenceLocation.setReference(fastaIndexManager.query("17", sequenceLocation.getStart(),
                    sequenceLocation.getEnd()));
            sequenceLocation.setAlternate("");
        } else if (gDescriptionArray[1].matches(VARIANT_STRING_PATTERN)) { // Avoid allele strings containing Ns, for example
            sequenceLocation.setReference(gDescriptionArray[1]);
            validVariant = true;
        } else {
            validVariant = false;
        }

        return validVariant;
    }

    private boolean parseSnv(String gDescription, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        Matcher snvMatcher = snvPattern.matcher(gDescription);

        if (snvMatcher.matches()) {
            String ref = snvMatcher.group(REF);
            String alt = snvMatcher.group(ALT);
            if (!ref.equalsIgnoreCase("N") && !alt.equalsIgnoreCase("N")) {
                sequenceLocation.setReference(ref);
                sequenceLocation.setAlternate(alt);
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

    // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//    private Germline buildGermline(String[] fields) {
//        // IARC TP53 Germline file is a tab-delimited file with the following fields (columns)
////        1 Family_ID
////        2 Family_code
////        3 Country
////        4 Population
////        5 Region
////        6 Development
////        7 Class
////        8 Generations_analyzed
////        9 Germline_mutation
////        10 MUT_ID
////        11 hg18_Chr17_coordinates
////        12 hg19_Chr17_coordinates
////        13 hg38_Chr17_coordinates
////        14 ExonIntron
////        15 Genomic_nt
////        16 Codon_number
////        17 Type
////        18 Description
////        19 c_description
////        20 g_description
////        21 WT_nucleotide
////        22 Mutant_nucleotide
////        23 WT_codon
////        24 Mutant_codon
////        25 CpG_site
////        26 Splice_site
////        27 WT_AA
////        28 Mutant_AA
////        29 Effect
////        30 AGVGDClass
////        31 SIFTClass
////        32 Polyphen2
////        33 TransactivationClass
////        34 DNEclass
////        35 ProtDescription
////        36 Domain_function
////        37 Residue_function
////        38 Individual_ID
////        39 Individual_code
////        40 FamilyCase
////        41 FamilyCase_group
////        42 Generation
////        43 Sex
////        44 Germline_carrier
////        45 Mode_of_inheritance
////        46 Dead
////        47 Unaffected
////        48 Age
////        49 Tumor_ID
////        50 Topography
////        51 Short_topo
////        52 Morphology
////        53 Age_at_diagnosis
////        54 Ref_ID
////        55 Other_infos
////        56 p53mut_ID
//
//        Germline germline = new Germline();
//
//        germline.setSource(IARCTP53_NAME);
//        germline.setAccession(fields[9]);
//        germline.setPhenotype(Collections.singletonList(fields[49]));
//        germline.setDisease(Collections.singletonList(fields[51]));
//        germline.setGeneNames(Collections.singletonList("TP53"));
//        germline.setInheritanceModel(Collections.singletonList(INHERITANCE_TAGS_MAP.get(fields[44])));
//
//
//        return germline;
//    }
//
//    private Somatic buildSomatic(String[] fields) {
//        // IARC TP53 Germline file is a tab-delimited file with the following fields (columns)
////      1 Mutation_ID
////      2 MUT_ID
////      3 hg18_Chr17_coordinates
////      4 hg19_Chr17_coordinates
////      5 hg38_Chr17_coordinates
////      6 ExonIntron
////      7 Genomic_nt
////      8 Codon_number
////      9 Description
////     10 c_description
////     11 g_description
////     12 WT_nucleotide
////     13 Mutant_nucleotide
////     14 Splice_site
////     15 CpG_site
////     16 Context_coding_3
////     17 Type
////     18 Mut_rate
////     19 WT_codon
////     20 Mutant_codon
////     21 WT_AA
////     22 Mutant_AA
////     23 ProtDescription
////     24 Mut_rateAA
////     25 Effect
////     26 SIFTClass
////     27 Polyphen2
////     28 TransactivationClass
////     29 DNEclass
////     30 Structural_motif
////     31 Sample_Name
////     32 Sample_ID
////     33 Sample_source
////     34 Tumor_origin
////     35 Topography
////     36 Short_topo
////     37 Topo_code
////     38 Sub_topography
////     39 Morphology
////     40 Morpho_code
////     41 Grade
////     42 Stage
////     43 TNM
////     44 p53_IHC
////     45 KRAS_status
////     46 Other_mutations
////     47 Other_associations
////     48 Add_Info
////     49 Individual_ID
////     50 Sex
////     51 Age
////     52 Ethnicity
////     53 Geo_area
////     54 Country
////     55 Development
////     56 Population
////     57 Region
////     58 TP53polymorphism
////     59 Germline_mutation
////     60 Family_history
////     61 Tobacco
////     62 Alcohol
////     63 Exposure
////     64 Infectious_agent
////     65 Ref_ID
////     66 Cross_Ref_ID
////     67 PubMed
////     68 Exclude_analysis
////     69 WGS_WXS
//
//        Somatic somatic = new Somatic();
//        somatic.setSource(IARCTP53_NAME);
//        somatic.setAccession(fields[1]);
//        somatic.setPrimarySite(fields[34]);
//        somatic.setSiteSubtype(fields[33]);
//        somatic.setHistologySubtype(fields[38]);
//        somatic.setSampleSource(fields[32]);
//        somatic.setTumourOrigin(fields[33]);
//        somatic.setGeneNames(Collections.singletonList("TP53"));
//
//        return somatic;
//    }

    public SequenceLocation parsePosition(String[] fields, boolean isGermline) {
        SequenceLocation sequenceLocation = new SequenceLocation();
        sequenceLocation.setChromosome("17"); // all variants in this database appear in the same gene

        int grch37ColumnPosition = isGermline ? 11 : 3;
        int grch38ColumnPosition = isGermline ? 12 : 4;
        if ("grch37".equalsIgnoreCase(assembly)) {
            sequenceLocation.setStart(Integer.valueOf(fields[grch37ColumnPosition]));
        } else if ("grch38".equalsIgnoreCase(assembly)) {
            sequenceLocation.setStart(Integer.valueOf(fields[grch38ColumnPosition]));
        }

        int hgvsColumnPosition = isGermline ? 17 : 10;
        if (fields[hgvsColumnPosition].contains("del")) {
            String[] deletionParts = fields[hgvsColumnPosition].split("del");
            if (deletionParts.length == 2) {
                Integer deletionSize = parseDeletionSize(fields[hgvsColumnPosition].split("del")[1]);
                if (deletionSize != null) {
                    sequenceLocation.setEnd(sequenceLocation.getStart() + deletionSize - 1);
                } else {
                    logger.warn("Deletion size format not recognized: \"{}\"", fields[hgvsColumnPosition].split("del")[1]);
                    sequenceLocation = null;
                }
            } else {
                logger.warn("Deletion format not recognized: \"{}\"", fields[hgvsColumnPosition]);
                sequenceLocation = null;
            }
        } else if (fields[hgvsColumnPosition].contains("ins")) {
            sequenceLocation.setEnd(sequenceLocation.getStart() - 1);
        } else {
            sequenceLocation.setEnd(sequenceLocation.getStart());
        }

        return sequenceLocation;
    }

    private Integer parseDeletionSize(String sizeString) {
        // Size of the deletion may be provided as 45kb for example
        if (smallSizePattern.matcher(sizeString).matches()) {
            return Integer.valueOf(sizeString);
        } else if (kbSizePattern.matcher(sizeString).matches()) {
//            return Integer.valueOf(sizeString.substring(0, sizeString.length() - 2)) * 1000;
            // TODO: appropriately parse big deletions
            return null;
        } else if (mbSizePattern.matcher(sizeString).matches()) {
//            return Integer.valueOf(sizeString.substring(0, sizeString.length() - 2)) * 1000000;
            // TODO: appropriately parse big deletions
            return null;
        } else if (sizeString.matches(VARIANT_STRING_PATTERN)) {
            return sizeString.length();
        } else {
            logger.warn("Deletion size string format not recognized: \"{}\"", sizeString);
            return null;
        }
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
