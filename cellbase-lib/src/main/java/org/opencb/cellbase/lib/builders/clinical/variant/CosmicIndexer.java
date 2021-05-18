/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.builders.clinical.variant;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
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


public class CosmicIndexer extends ClinicalIndexer {

    private final Path cosmicFile;
    private final String assembly;
    private Pattern mutationGRCh37GenomePositionPattern;
    private Pattern snvPattern;

    private static final String COSMIC_VERSION = "v92";

    private static final int GENE_NAMES_COLUMN = 0;
    private static final int HGNC_COLUMN = 3;
    private static final int PRIMARY_SITE_COLUMN = 7;
    private static final int SITE_SUBTYPE_COLUMN = 8;
    private static final int PRIMARY_HISTOLOGY_COLUMN = 11;
    private static final int HISTOLOGY_SUBTYPE_COLUMN = 12;
    private static final int ID_COLUMN = 16;
    private static final int COSM_ID_COLUMN = 17;
    private static final int HGVS_COLUMN = 19;
    private static final int MUTATION_DESCRIPTION_COLUMN = 21;
    private static final int MUTATION_ZYGOSITY_COLUMN = 22;
    private static final int FATHMM_PREDICTION_COLUMN = 29;
    private static final int FATHMM_SCORE_COLUMN = 30;
    private static final int MUTATION_SOMATIC_STATUS_COLUMN = 31;
    private static final int PUBMED_PMID_COLUMN = 32;
    private static final int SAMPLE_SOURCE_COLUMN = 34;
    private static final int TUMOUR_ORIGIN_COLUMN = 35;

    private static final String HGVS_INSERTION_TAG = "ins";
    private static final String HGVS_SNV_CHANGE_SYMBOL = ">";
    private static final String HGVS_DELETION_TAG = "del";
    private static final String HGVS_DUPLICATION_TAG = "dup";
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
    private long normaliseTime = 0;
    private int rocksDBNewVariants = 0;
    private int rocksDBUpdateVariants = 0;

    public CosmicIndexer(Path cosmicFile, boolean normalize, Path genomeSequenceFilePath, String assembly, RocksDB rdb) throws IOException {
        super(genomeSequenceFilePath);

        this.cosmicFile = cosmicFile;
        this.normalize = normalize;
        this.assembly = assembly;
        this.rdb = rdb;

        this.init();
    }

    private void init() {
        mutationGRCh37GenomePositionPattern = Pattern.compile("(?<" + CHROMOSOME + ">\\S+):(?<" + START + ">\\d+)-(?<" + END + ">\\d+)");
        snvPattern = Pattern.compile("c\\.\\d+((\\+|\\-|_)\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");
    }

    public void index() throws RocksDBException {
        logger.info("Parsing cosmic file ...");

        try {
            ProgressLogger progressLogger = new ProgressLogger("Parsed COSMIC lines:",
                    () -> EtlCommons.countFileLines(cosmicFile), 200).setBatchSize(10000);

            long t0, t1 = 0, t2 = 0;
            List<EvidenceEntry> evidenceEntries = new ArrayList<>();
            SequenceLocation old = null;

            BufferedReader cosmicReader = FileUtils.newBufferedReader(cosmicFile);
            cosmicReader.readLine(); // First line is the header -> ignore it
            String line;
            while ((line = cosmicReader.readLine()) != null) {
                String[] fields = line.split("\t", -1);

                t0 = System.currentTimeMillis();
                EvidenceEntry evidenceEntry = buildCosmic(fields);
                t1 += System.currentTimeMillis() - t0;

                SequenceLocation sequenceLocation = parseLocation(fields);
                if (old == null) {
                    old = sequenceLocation;
                }

                if (sequenceLocation != null && parseVariant(sequenceLocation, fields)) {
                    if (sequenceLocation.getStart() == old.getStart() && sequenceLocation.getAlternate().equals(old.getAlternate())) {
                        evidenceEntries.add(evidenceEntry);
                    } else {
                        boolean success = updateRocksDB(old, evidenceEntries);
                        t2 += System.currentTimeMillis() - t0;
                        // updateRocksDB may fail (false) if normalisation process fails
                        if (success) {
                            numberIndexedRecords += evidenceEntries.size();
                        } else {
                            ignoredCosmicLines += evidenceEntries.size();
                        }
                        old = sequenceLocation;
                        evidenceEntries.clear();
                        evidenceEntries.add(evidenceEntry);
                    }
                } else {
                    ignoredCosmicLines++;
                }
                totalNumberRecords++;
                progressLogger.increment(1);

                if (totalNumberRecords % 10000 == 0) {
                    System.out.println("totalNumberRecords = " + totalNumberRecords);
                    System.out.println("numberIndexedRecords = " + numberIndexedRecords + " ("
                            + (numberIndexedRecords * 100 / totalNumberRecords) + "%)");
                    System.out.println("ignoredCosmicLines = " + ignoredCosmicLines);
                    System.out.println("buildCosmic = " + t1);

                    System.out.println("updateRocksDB = " + t2);
                    System.out.println("\tnormaliseTime = " + normaliseTime);
                    System.out.println("\trocksDBNewVariants = " + (numberNewVariants - rocksDBNewVariants));
                    System.out.println("\trocksDBUpdateVariants = " + (numberVariantUpdates - rocksDBUpdateVariants));
                    System.out.println("");

                    t1 = 0;
                    t2 = 0;
                    normaliseTime = 0;
                    rocksDBNewVariants = numberNewVariants;
                    rocksDBUpdateVariants = numberVariantUpdates;
                }
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

    private boolean updateRocksDB(SequenceLocation sequenceLocation, List<EvidenceEntry> evidenceEntries)
            throws RocksDBException, IOException {
        // More than one variant being returned from the normalisation process would mean it's and MNV which has been decomposed
        List<String> normalisedVariantStringList = getNormalisedVariantString(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(), sequenceLocation.getAlternate());
        if (normalisedVariantStringList != null) {
            for (String normalisedVariantString : normalisedVariantStringList) {
                VariantAnnotation variantAnnotation = getVariantAnnotation(normalisedVariantString.getBytes());
                List<EvidenceEntry> mergedEvidenceEntries = mergeEvidenceEntries(evidenceEntries);
                addHaplotypeProperty(mergedEvidenceEntries, normalisedVariantStringList);
                variantAnnotation.setTraitAssociation(mergedEvidenceEntries);
                rdb.put(normalisedVariantString.getBytes(), jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            }
            return true;
        }
        return false;
    }

    private List<EvidenceEntry> mergeEvidenceEntries(List<EvidenceEntry> evidenceEntries) {
        List<EvidenceEntry> mergedEvidenceEntries = new ArrayList<>();
        if (evidenceEntries.size() > 0) {
            mergedEvidenceEntries.add(evidenceEntries.get(0));
            // For each evidence entry ...
            for (int i = 1; i < evidenceEntries.size(); i++) {
                boolean merged = false;
                // ... check if it matches a existing evidence entry
                for (EvidenceEntry mergedEvidenceEntry : mergedEvidenceEntries) {
                    if (sameSomaticDocument(evidenceEntries.get(i), mergedEvidenceEntry)) {
                        // Merge Transcripts
                        if (mergedEvidenceEntry.getGenomicFeatures() != null) {
                            if (evidenceEntries.get(i).getGenomicFeatures() != null) {
                                for (GenomicFeature newGenomicFeature : evidenceEntries.get(i).getGenomicFeatures()) {
                                    if (newGenomicFeature.getFeatureType().equals(FeatureTypes.transcript)) {
                                        boolean found = false;
                                        for (GenomicFeature feature : mergedEvidenceEntry.getGenomicFeatures()) {
                                            if (feature.getXrefs().get(SYMBOL).equals(newGenomicFeature.getXrefs().get(SYMBOL))) {
                                                found = true;
                                            }
                                        }
                                        if (!found) {
                                            mergedEvidenceEntry.getGenomicFeatures().add(newGenomicFeature);
                                        }
                                    }
                                }
                            }
                        } else {
                            mergedEvidenceEntry.setGenomicFeatures(evidenceEntries.get(i).getGenomicFeatures());
                        }

                        // Merge Bibliography
                        // There are cosmic records which share all the fields but the bibliography. In some occassions (COSM12600)
                        // the redundancy is such that the document becomes much bigger than 16MB and cannot be loaded into MongoDB.
                        // This merge reduces redundancy.
                        if (mergedEvidenceEntry.getBibliography() != null) {
                            if (evidenceEntries.get(i).getBibliography() != null) {
                                Set<String> bibliographySet = new HashSet<>(mergedEvidenceEntry.getBibliography());
                                bibliographySet.addAll(new HashSet<>(evidenceEntries.get(i).getBibliography()));
                                mergedEvidenceEntry.setBibliography(new ArrayList<>(bibliographySet));
                            }
                        } else {
                            mergedEvidenceEntry.setBibliography(evidenceEntries.get(i).getBibliography());
                        }

                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    mergedEvidenceEntries.add(evidenceEntries.get(i));
                }
            }
        }

        return mergedEvidenceEntries;
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

    /**
     * Check whether the variant is valid and parse it.
     *
     * @return true if valid mutation, false otherwise
     */
    private boolean parseVariant(SequenceLocation sequenceLocation, String[] fields) {
        boolean validVariant = false;
        String mutationCds = fields[HGVS_COLUMN];
        VariantType variantType = getVariantType(mutationCds);
        if (variantType != null) {
            switch (variantType) {
                case SNV:
                    validVariant = parseSnv(mutationCds, sequenceLocation);
                    if (!validVariant) {
                        invalidSubstitutionLines++;
                    }
                    break;
                case DELETION:
                    validVariant = parseDeletion(mutationCds, sequenceLocation);
                    if (!validVariant) {
                        invalidDeletionLines++;
                    }
                    break;
                case INSERTION:
                    validVariant = parseInsertion(mutationCds, sequenceLocation);
                    if (!validVariant) {
                        invalidInsertionLines++;
                    }
                    break;
                case DUPLICATION:
                    validVariant = parseDuplication(mutationCds);
                    if (!validVariant) {
                        invalidDuplicationLines++;
                    }
                    break;
                default:
                    System.out.println("variantType = " + variantType);
                    validVariant = false;
                    invalidMutationCDSOtherReason++;
            }
        }

        return validVariant;
    }

    private VariantType getVariantType(String mutationCds) {
        if (mutationCds.contains(HGVS_SNV_CHANGE_SYMBOL)) {
            return VariantType.SNV;
        } else if (mutationCds.contains(HGVS_DELETION_TAG)) {
            return VariantType.DELETION;
        } else if (mutationCds.contains(HGVS_INSERTION_TAG)) {
            return VariantType.INSERTION;
        } else if (mutationCds.contains(HGVS_DUPLICATION_TAG)) {
            return VariantType.DUPLICATION;
        } else {
            return null;
        }
    }

    private boolean parseDuplication(String dup) {
        // TODO: The only Duplication in Cosmic V70 is a structural variation that is not going to be serialized
        return false;
    }

    private boolean parseInsertion(String mutationCds, SequenceLocation sequenceLocation) {
        boolean validVariant = true;
        String[] insParts = mutationCds.split("ins");

        if (insParts.length > 1) {
            String insertedNucleotides = insParts[1];
            if (insertedNucleotides.matches("\\d+") || !insertedNucleotides.matches(VARIANT_STRING_PATTERN)) {
                //c.503_508ins30
                validVariant = false;
            } else {
                sequenceLocation.setReference("");
                sequenceLocation.setAlternate(getPositiveStrandString(insertedNucleotides, sequenceLocation.getStrand()));
            }
        } else {
            validVariant = false;
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

    private EvidenceEntry buildCosmic(String[] fields) {
        String id = fields[ID_COLUMN];
        String url = "https://cancer.sanger.ac.uk/cosmic/search?q=" + id;

        EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.COSMIC_DATA, COSMIC_VERSION, null);
        SomaticInformation somaticInformation = getSomaticInformation(fields);
        List<GenomicFeature> genomicFeatureList = getGenomicFeature(fields);

        List<Property> additionalProperties = new ArrayList<>();
        additionalProperties.add(new Property("COSM_ID", "Legacy COSM ID", fields[COSM_ID_COLUMN]));
        additionalProperties.add(new Property("MUTATION_DESCRIPTION", "Description", fields[MUTATION_DESCRIPTION_COLUMN]));
        if (StringUtils.isNotEmpty(fields[MUTATION_ZYGOSITY_COLUMN])) {
            additionalProperties.add(new Property("MUTATION_ZYGOSITY", "Mutation Zygosity", fields[MUTATION_ZYGOSITY_COLUMN]));
        }
        additionalProperties.add(new Property("FATHMM_PREDICTION", "FATHMM Prediction", fields[FATHMM_PREDICTION_COLUMN]));
        additionalProperties.add(new Property("FATHMM_SCORE", "FATHMM Score", "0" + fields[FATHMM_SCORE_COLUMN]));
        additionalProperties.add(new Property("MUTATION_SOMATIC_STATUS", "Mutation Somatic Status",
                fields[MUTATION_SOMATIC_STATUS_COLUMN]));

        List<String> bibliography = getBibliography(fields[PUBMED_PMID_COLUMN]);

        return new EvidenceEntry(evidenceSource, Collections.emptyList(), somaticInformation,
                url, id, assembly,
                getAlleleOriginList(Collections.singletonList(fields[MUTATION_SOMATIC_STATUS_COLUMN])),
                Collections.emptyList(), genomicFeatureList, null, null, null, null,
                EthnicCategory.Z, null, null, null, additionalProperties, bibliography);
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
        if (!EtlCommons.isMissing(fields[TUMOUR_ORIGIN_COLUMN])) {
            tumourOrigin = fields[TUMOUR_ORIGIN_COLUMN].replace("_", " ");
        }
        String sampleSource = null;
        if (!EtlCommons.isMissing(fields[SAMPLE_SOURCE_COLUMN])) {
            sampleSource = fields[SAMPLE_SOURCE_COLUMN].replace("_", " ");
        }

        return new SomaticInformation(primarySite, siteSubtype, primaryHistology, histologySubtype, tumourOrigin, sampleSource);
    }

    private List<String> getBibliography(String bibliographyString) {
        if (!EtlCommons.isMissing(bibliographyString)) {
            return Collections.singletonList("PMID:" + bibliographyString);
        }

        return Collections.emptyList();
    }

    private List<GenomicFeature> getGenomicFeature(String[] fields) {
        List<GenomicFeature> genomicFeatureList = new ArrayList<>(5);
        if (fields[GENE_NAMES_COLUMN].contains("_")) {
            genomicFeatureList.add(createGeneGenomicFeature(fields[GENE_NAMES_COLUMN].split("_")[0]));
        }
        // Add transcript ID
        if (StringUtils.isNotEmpty(fields[1])) {
            genomicFeatureList.add(createGeneGenomicFeature(fields[1], FeatureTypes.transcript));
        }
        if (!fields[HGNC_COLUMN].equalsIgnoreCase(fields[GENE_NAMES_COLUMN]) && !EtlCommons.isMissing(fields[HGNC_COLUMN])) {
            genomicFeatureList.add(createGeneGenomicFeature(fields[HGNC_COLUMN]));
        }

        return genomicFeatureList;
    }

    public SequenceLocation parseLocation(String[] fields) {
        SequenceLocation sequenceLocation = null;
        String locationString = fields[25];
        if (StringUtils.isNotEmpty(locationString)) {
            Matcher matcher = mutationGRCh37GenomePositionPattern.matcher(locationString);
            if (matcher.matches()) {
                sequenceLocation = new SequenceLocation();
                sequenceLocation.setChromosome(getCosmicChromosome(matcher.group(CHROMOSOME)));
                sequenceLocation.setStrand(fields[26]);

                String mutationCds = fields[HGVS_COLUMN];
                VariantType variantType = getVariantType(mutationCds);
                if (VariantType.INSERTION.equals(variantType)) {
                    sequenceLocation.setEnd(Integer.parseInt(matcher.group(START)));
                    sequenceLocation.setStart(Integer.parseInt(matcher.group(END)));
                } else {
                    sequenceLocation.setStart(Integer.parseInt(matcher.group(START)));
                    sequenceLocation.setEnd(Integer.parseInt(matcher.group(END)));
                }
            }
        }
        if (sequenceLocation == null) {
            this.invalidPositionLines++;
        }
        return sequenceLocation;
    }

    private String getCosmicChromosome(String chromosome) {
        switch (chromosome) {
            case "23":
                return "X";
            case "24":
                return "Y";
            case "25":
                return "MT";
            default:
                return chromosome;
        }
    }

}
