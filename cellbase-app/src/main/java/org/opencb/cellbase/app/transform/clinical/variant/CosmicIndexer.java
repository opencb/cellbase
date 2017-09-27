package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjlopez on 04/10/16.
 */
public class CosmicIndexer extends ClinicalIndexer {

    private static final String COSMIC_NAME = "cosmic";
    private static final int PRIMARY_SITE_COLUMN = 7;
    private static final int SITE_SUBTYPE_COLUMN = 8;
    private static final int PRIMARY_HISTOLOGY_COLUMN = 11;
    private static final int HISTOLOGY_SUBTYPE_COLUMN = 12;
    private static final int ID_COLUMN = 16;
    private static final String MUTATION_SOMATIC_STATUS_IN_SOURCE_FILE = "mutationSomaticStatus_in_source_file";
    private static final int GENE_NAMES_COLUMN = 0;
    private static final int HGNC_COLUMN = 3;
    private final Path cosmicFile;
    private final int mutationSomaticStatusColumn;
    private final int pubmedPMIDColumn;
    private final int sampleSourceColumn;
    private final int tumourOriginColumn;
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

    public CosmicIndexer(Path cosmicFile, String assembly, RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.cosmicFile = cosmicFile;
        this.compileRegularExpressionPatterns();

        // COSMIC v78 GRCh38 includes one more column at position 27 (1-based) "Resistance Mutation" which is not
        // provided in the GRCh37 file
        if (assembly.equalsIgnoreCase("grch37")) {
            this.mutationSomaticStatusColumn = 29;
            this.pubmedPMIDColumn = 30;
            this.sampleSourceColumn = 32;
            this.tumourOriginColumn = 33;
        } else {
            this.mutationSomaticStatusColumn = 29;
            this.pubmedPMIDColumn = 30;
            this.sampleSourceColumn = 32;
            this.tumourOriginColumn = 33;
        }
    }

    private void compileRegularExpressionPatterns() {
        mutationGRCh37GenomePositionPattern = Pattern.compile("(?<" + CHROMOSOME + ">\\S+):(?<" + START + ">\\d+)-(?<" + END + ">\\d+)");
        snvPattern = Pattern.compile("c\\.\\d+(_\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");
    }

    public void index() throws RocksDBException {

        logger.info("Parsing cosmic file ...");

        try {
            ProgressLogger progressLogger = new ProgressLogger("Parsed COSMIC lines:",
                    () -> EtlCommons.countFileLines(cosmicFile), 200).setBatchSize(10000);

            BufferedReader cosmicReader = FileUtils.newBufferedReader(cosmicFile);
            String line;
            cosmicReader.readLine(); // First line is the header -> ignore it
            while ((line = cosmicReader.readLine()) != null) {
                logger.debug(line);
                EvidenceEntry evidenceEntry = buildCosmic(line);
                SequenceLocation sequenceLocation = new SequenceLocation();
                if (parsePosition(sequenceLocation, line) && parseVariant(sequenceLocation, line)) {
                    updateRocksDB(sequenceLocation, evidenceEntry);
                    numberIndexedRecords++;
                } else {
                    ignoredCosmicLines++;
                }
                totalNumberRecords++;
                progressLogger.increment(1);
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
        logger.info("Number of indexed Cosmic records: {}", numberIndexedRecords);
        logger.info("Number of new variants in Cosmic not previously indexed in RocksDB: {}", numberNewVariants);
        logger.info("Number of updated variants during Cosmic indexing: {}", numberVariantUpdates);

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

    private void updateRocksDB(SequenceLocation sequenceLocation, EvidenceEntry evidenceEntry) throws RocksDBException, IOException {

        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(),
                sequenceLocation.getAlternate()).getBytes();
        VariantAnnotation variantAnnotation = getVariantAnnotation(key);
//        List<EvidenceEntry> evidenceEntryList = getVariantAnnotation(key);
        addNewEntry(variantAnnotation, evidenceEntry);
        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantAnnotation));
    }

    private void addNewEntry(VariantAnnotation variantAnnotation, EvidenceEntry evidenceEntry) {
        List<EvidenceEntry> evidenceEntryList = variantAnnotation.getTraitAssociation();
        // There are cosmic records which share all the fields but the bibliography. In some occassions (COSM12600)
        // the redundancy is such that the document becomes much bigger than 16MB and cannot be loaded into MongoDB.
        // This merge reduces redundancy.
        int i = 0;
        boolean merged = false;
        while (i < evidenceEntryList.size() && !merged) {
            if (sameSomaticDocument(evidenceEntryList.get(i), evidenceEntry)) {
                if (evidenceEntryList.get(i).getBibliography() != null) {
                    if (evidenceEntry.getBibliography() != null) {
                        Set<String> bibliographySet = new HashSet<>(evidenceEntryList.get(i).getBibliography());
                        bibliographySet.addAll(new HashSet<>(evidenceEntry.getBibliography()));
                        evidenceEntryList.get(i).setBibliography(new ArrayList<>(bibliographySet));
                    }
                } else {
                    evidenceEntryList.get(i).setBibliography(evidenceEntry.getBibliography());
                }
                merged = true;
            }
            i++;
        }
        if (!merged) {
            evidenceEntryList.add(evidenceEntry);
        }
    }

    public boolean sameSomaticDocument(EvidenceEntry evidenceEntry1, EvidenceEntry evidenceEntry2) {

        if (evidenceEntry1 == evidenceEntry2) {
            return true;
        }
        if (evidenceEntry2 == null || evidenceEntry1.getClass() != evidenceEntry2.getClass()) {
            return false;
        }

        if (evidenceEntry1.getSource() != null ? !evidenceEntry1.getSource().equals(evidenceEntry2.getSource())
                : evidenceEntry2.getSource() != null) {
            return false;
        }
        if (evidenceEntry1.getSomaticInformation() != null
                ? !evidenceEntry1.getSomaticInformation().equals(evidenceEntry2.getSomaticInformation())
                : evidenceEntry2.getSomaticInformation() != null) {
            return false;
        }
        if (evidenceEntry1.getId() != null
                ? !evidenceEntry1.getId().equals(evidenceEntry2.getId()) : evidenceEntry2.getId() != null) {
            return false;
        }
        if (evidenceEntry1.getAlleleOrigin() != null
                ? !evidenceEntry1.getAlleleOrigin().equals(evidenceEntry2.getAlleleOrigin())
                : evidenceEntry2.getAlleleOrigin() != null) {
            return false;
        }
        if (evidenceEntry1.getGenomicFeatures() != null
                ? !evidenceEntry1.getGenomicFeatures().equals(evidenceEntry2.getGenomicFeatures())
                : evidenceEntry2.getGenomicFeatures() != null) {
            return false;
        }
        if (evidenceEntry1.getAdditionalProperties() != null
                ? !evidenceEntry1.getAdditionalProperties().equals(evidenceEntry2.getAdditionalProperties())
                : evidenceEntry2.getAdditionalProperties() != null) {
            return false;
        }

        return true;
    }

//    /**
//     * Checks whether all fields but the bibliography list, are exactly the same in two somatic records.
//     * @param somatic1 Somatic object
//     * @param somatic2 Somatic object
//     * @return true if all fields but the bibliography are exaclty the same in both records. false otherwise
//     */
//    private boolean sameSomaticDocument(EvidenceEntry evidenceEntry1, EvidenceEntry evidenceEntry2) {
//        // Check gene name list
//        boolean equalSource = (somatic1.getSource() == null
//                && somatic2.getSource() == null)
//                || (somatic1.getSource().equalsIgnoreCase(somatic2.getSource()));
//
//        if (equalSource) {
//            boolean equalAccession = (somatic1.getAccession() == null && somatic2.getAccession() == null)
//                    || (somatic1.getAccession().equals(somatic2.getAccession()));
//            if (equalAccession) {
//                boolean equalGeneList = (somatic1.getGeneNames() == null && somatic2.getGeneNames() == null)
//                        || (new HashSet<>(somatic1.getGeneNames()).equals(new HashSet<>(somatic2.getGeneNames())));
//                if (equalGeneList) {
//                    boolean equalMutationSomaticStatus = (somatic1.getMutationSomaticStatus() == null
//                            && somatic2.getMutationSomaticStatus() == null)
//                            || (somatic1.getMutationSomaticStatus().equalsIgnoreCase(somatic2.getMutationSomaticStatus()));
//                    if (equalMutationSomaticStatus) {
//                        boolean equalPrimarySite = (somatic1.getPrimarySite() == null
//                                && somatic2.getPrimarySite() == null)
//                                || (somatic1.getPrimarySite().equalsIgnoreCase(somatic2.getPrimarySite()));
//                        if (equalPrimarySite) {
//                            boolean equalSiteSubtype = (somatic1.getSiteSubtype() == null
//                                    && somatic2.getSiteSubtype() == null)
//                                    || (somatic1.getSiteSubtype().equalsIgnoreCase(somatic2.getSiteSubtype()));
//                            if (equalSiteSubtype) {
//                                boolean equalPrimaryHistology = (somatic1.getPrimaryHistology() == null
//                                        && somatic2.getPrimaryHistology() == null)
//                                        || (somatic1.getPrimaryHistology().equalsIgnoreCase(somatic2.getPrimaryHistology()));
//                                if (equalPrimaryHistology) {
//                                    boolean equalHistologySubtype = (somatic1.getHistologySubtype() == null
//                                            && somatic2.getHistologySubtype() == null)
//                                            || (somatic1.getHistologySubtype().equalsIgnoreCase(somatic2.getHistologySubtype()));
//                                    if (equalHistologySubtype) {
//                                        boolean equalSampleSource = (somatic1.getSampleSource() == null
//                                                && somatic2.getSampleSource() == null)
//                                                || (somatic1.getSampleSource().equalsIgnoreCase(somatic2.getSampleSource()));
//                                        if (equalSampleSource) {
//                                            boolean equalTumourOrigin = (somatic1.getTumourOrigin() == null
//                                                    && somatic2.getTumourOrigin() == null)
//                                                    || (somatic1.getTumourOrigin().equalsIgnoreCase(somatic2.getTumourOrigin()));
//                                            return equalTumourOrigin;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

    /**
     * Check whether the variant is valid and parse it.
     *
     * @return true if valid mutation, false otherwise
     */
    private boolean parseVariant(SequenceLocation sequenceLocation, String line) {
        boolean validVariant;
        String[] fields = line.split("\t", -1);
        String mutationCds = fields[17];

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

    private EvidenceEntry buildCosmic(String line) {
        String[] fields = line.split("\t", -1); // -1 argument make split return also empty fields

        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.COSMIC_DATA, null, null);
        SomaticInformation somaticInformation = getSomaticInformation(fields);

        List<GenomicFeature> genomicFeatureList = getGenomicFeature(fields);

        List<Property> additionalProperties = Collections.singletonList(new Property(null,
                MUTATION_SOMATIC_STATUS_IN_SOURCE_FILE, fields[mutationSomaticStatusColumn]));

        List<String> bibliography = getBibliography(fields[pubmedPMIDColumn]);

        EvidenceEntry evidenceEntry = new EvidenceEntry(evidenceSource, null, somaticInformation, null,
                fields[ID_COLUMN], null,
                getAlleleOriginList(Collections.singletonList(fields[mutationSomaticStatusColumn])), null,
                genomicFeatureList, null, null, null, null,
                null, null, null, null, additionalProperties,
                bibliography);

        return evidenceEntry;
    }

    private SomaticInformation getSomaticInformation(String[] fields) {
        String primarySite = null;
        if (!EtlCommons.isMissing(fields[PRIMARY_SITE_COLUMN])) {
            primarySite = fields[PRIMARY_SITE_COLUMN].replace("_", " ");
        }
        String siteSubtype = null;
        if (!EtlCommons.isMissing(fields[SITE_SUBTYPE_COLUMN])) {
            siteSubtype = fields[SITE_SUBTYPE_COLUMN].replace("_", " ");
        }
        String primaryHistology = null;
        if (!EtlCommons.isMissing(fields[PRIMARY_HISTOLOGY_COLUMN])) {
            primaryHistology = fields[PRIMARY_HISTOLOGY_COLUMN].replace("_", " ");
        }
        String histologySubtype = null;
        if (!EtlCommons.isMissing(fields[HISTOLOGY_SUBTYPE_COLUMN])) {
            histologySubtype = fields[HISTOLOGY_SUBTYPE_COLUMN].replace("_", " ");
        }
        String tumourOrigin = null;
        if (!EtlCommons.isMissing(fields[tumourOriginColumn])) {
            tumourOrigin = fields[tumourOriginColumn].replace("_", " ");
        }
        String sampleSource = null;
        if (!EtlCommons.isMissing(fields[sampleSourceColumn])) {
            sampleSource = fields[sampleSourceColumn].replace("_", " ");
        }

        return new SomaticInformation(primarySite, siteSubtype, primaryHistology, histologySubtype, tumourOrigin,
                sampleSource);
    }

    private List<String> getBibliography(String bibliographyString) {
        if (!EtlCommons.isMissing(bibliographyString)) {
            return Collections.singletonList("PMID:" + bibliographyString);
        }

        return null;
    }

    private List<GenomicFeature> getGenomicFeature(String[] fields) {
        List<GenomicFeature> genomicFeatureList = new ArrayList<>(2);
        genomicFeatureList.add(createGeneGenomicFeature(fields[GENE_NAMES_COLUMN]));
        if (!fields[HGNC_COLUMN].equalsIgnoreCase(fields[GENE_NAMES_COLUMN])
                && !EtlCommons.isMissing(fields[HGNC_COLUMN])) {
            genomicFeatureList.add(createGeneGenomicFeature(fields[HGNC_COLUMN]));
        }

        return genomicFeatureList;
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
