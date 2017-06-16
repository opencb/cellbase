package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fjlopez on 04/10/16.
 */
public class CosmicIndexer extends ClinicalIndexer {

    private static final String COSMIC_NAME = "cosmic";
    private final RocksDB rdb;
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

// FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//        try {
//            BufferedReader cosmicReader = FileUtils.newBufferedReader(cosmicFile);
//            String line;
//            cosmicReader.readLine(); // First line is the header -> ignore it
//            while ((line = cosmicReader.readLine()) != null) {
//                logger.debug(line);
//                Somatic somatic = buildCosmic(line);
//                SequenceLocation sequenceLocation = new SequenceLocation();
//                if (parsePosition(sequenceLocation, line) && parseVariant(sequenceLocation, line)) {
//                    updateRocksDB(sequenceLocation, somatic);
//                    numberIndexedRecords++;
//                } else {
//                    ignoredCosmicLines++;
//                }
//                totalNumberRecords++;
//
//                if (totalNumberRecords % 1000 == 0) {
//                    logger.info("{} records parsed", totalNumberRecords);
//                }
//            }
//        } catch (RocksDBException e) {
//            logger.error("Error reading/writing from/to the RocksDB index while indexing Cosmic");
//            throw e;
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } finally {
//            logger.info("Done");
//            this.printSummary();
//        }

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

    // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//    private void updateRocksDB(SequenceLocation sequenceLocation, Somatic somatic) throws RocksDBException, IOException {
//
//        byte[] key = VariantAnnotationUtils.buildVariantId(sequenceLocation.getChromosome(),
//                sequenceLocation.getStart(), sequenceLocation.getReference(),
//                sequenceLocation.getAlternate()).getBytes();
//        byte[] dbContent = rdb.get(key);
//        VariantTraitAssociation variantTraitAssociation;
//        if (dbContent == null) {
//            variantTraitAssociation = new VariantTraitAssociation();
//            variantTraitAssociation.setGermline(Collections.emptyList());
//            variantTraitAssociation.setSomatic(Collections.singletonList(somatic));
//            numberNewVariants++;
//        } else {
//            variantTraitAssociation = mapper.readValue(dbContent, VariantTraitAssociation.class);
//            // There are cosmic records which share all the fields but the bibliography. In some occassions (COSM12600)
//            // the redundancy is such that the document becomes much bigger than 16MB and cannot be loaded into MongoDB.
//            // This merge reduces redundancy.
//            mergeSomaticDocument(variantTraitAssociation.getSomatic(), somatic);
//            numberVariantUpdates++;
//        }
//        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantTraitAssociation));
//    }
// FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//    private void mergeSomaticDocument(List<Somatic> somaticList, Somatic somatic) {
//        int i = 0;
//        boolean merged = false;
//        while (i < somaticList.size() && !merged) {
//            if (sameSomaticDocument(somaticList.get(i), somatic)) {
//                if (somaticList.get(i).getBibliography() != null) {
//                    if (somatic.getBibliography() != null) {
//                        Set<String> bibliographySet = new HashSet<>(somaticList.get(i).getBibliography());
//                        bibliographySet.addAll(new HashSet<>(somatic.getBibliography()));
//                        somaticList.get(i).setBibliography(new ArrayList<>(bibliographySet));
//                    }
//                } else {
//                    somaticList.get(i).setBibliography(somatic.getBibliography());
//                }
//                merged = true;
//            }
//            i++;
//        }
//        if (!merged) {
//            somaticList.add(somatic);
//        }
//    }

    // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
    /**
     * Checks whether all fields but the bibliography list, are exactly the same in two somatic records.
     * @param somatic1 Somatic object
     * @param somatic2 Somatic object
     * @return true if all fields but the bibliography are exaclty the same in both records. false otherwise
     */
//    private boolean sameSomaticDocument(Somatic somatic1, Somatic somatic2) {
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

    // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//    private Somatic buildCosmic(String line) {
//        String[] fields = line.split("\t", -1); // -1 argument make split return also empty fields
//        Somatic cosmic = new Somatic();
//        cosmic.setSource(COSMIC_NAME);
//        cosmic.setGeneNames(new ArrayList<>(Arrays.asList(fields[0])));
//        cosmic.setAccession(fields[16]);
//        if (!fields[3].equalsIgnoreCase(fields[0]) && !fields[3].isEmpty()) {
//            cosmic.getGeneNames().add(fields[3]);
//        }
//        cosmic.setPrimarySite(fields[7]);
//        cosmic.setSiteSubtype(fields[8]);
//        cosmic.setPrimaryHistology(fields[11]);
//        cosmic.setHistologySubtype(fields[12]);
//        cosmic.setMutationSomaticStatus(fields[mutationSomaticStatusColumn]);
//        if (!fields[pubmedPMIDColumn].isEmpty()
//                && !fields[pubmedPMIDColumn].replace(" ", "").replace("NA", "").replace("NULL", "").replace("\t", "")
//                .replace(".", "").replace("-", "").isEmpty()) {
//            cosmic.setBibliography(Collections.singletonList("PMID:" + fields[pubmedPMIDColumn]));
//        }
//        cosmic.setSampleSource(fields[sampleSourceColumn]);
//        cosmic.setTumourOrigin(fields[tumourOriginColumn]);
//
//        return cosmic;
//    }

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
