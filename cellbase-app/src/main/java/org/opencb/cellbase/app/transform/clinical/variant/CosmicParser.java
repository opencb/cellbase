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

package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.cellbase.app.transform.CellBaseParser;
import org.opencb.cellbase.core.common.clinical.Cosmic;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

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
@Deprecated
public class CosmicParser extends CellBaseParser {

    private final Path cosmicFilePath;
    private static final String CHROMOSOME = "CHR";
    private static final String START = "START";
    private static final String END = "END";
    private static final String REF = "REF";
    private static final String ALT = "ALT";
    private final int mutationSomaticStatusColumn;
    private final int pubmedPMIDColumn;
    private final int idStudyColumn;
    private final int sampleSourceColumn;
    private final int tumourOriginColumn;
    private final int ageColumn;
    private Pattern mutationGRCh37GenomePositionPattern;
    private Pattern snvPattern;
    private long invalidPositionLines;
    private long invalidSubstitutionLines;
    private long invalidInsertionLines;
    private long invalidDeletionLines;
    private long invalidDuplicationLines;
    private long invalidMutationCDSOtherReason;

    private static final String VARIANT_STRING_PATTERN = "[ACGT]*";

    public CosmicParser(Path cosmicFilePath, CellBaseSerializer serializer, String assembly) {
        super(serializer);
        this.cosmicFilePath = cosmicFilePath;
        this.compileRegularExpressionPatterns();

        // COSMIC v78 GRCh38 includes one more column at position 27 (1-based) "Resistance Mutation" which is not
        // provided in the GRCh37 file
        if (assembly.equalsIgnoreCase("grch37")) {
            this.mutationSomaticStatusColumn = 29;
            this.pubmedPMIDColumn = 30;
            this.idStudyColumn = 31;
            this.sampleSourceColumn = 32;
            this.tumourOriginColumn = 33;
            this.ageColumn = 34;
        } else {
            this.mutationSomaticStatusColumn = 29;
            this.pubmedPMIDColumn = 30;
            this.idStudyColumn = 31;
            this.sampleSourceColumn = 32;
            this.tumourOriginColumn = 33;
            this.ageColumn = 34;
        }

    }

    private void compileRegularExpressionPatterns() {
        mutationGRCh37GenomePositionPattern = Pattern.compile("(?<" + CHROMOSOME + ">\\S+):(?<" + START + ">\\d+)-(?<" + END + ">\\d+)");
        snvPattern = Pattern.compile("c\\.\\d+(_\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");
    }

    public void parse() {
        logger.info("Parsing cosmic file ...");
        long processedCosmicLines = 0, ignoredCosmicLines = 0;

        try {
            BufferedReader cosmicReader = new BufferedReader(new InputStreamReader(new FileInputStream(cosmicFilePath.toFile())));

            String line;
            cosmicReader.readLine(); // First line is the header -> ignore it

            while ((line = cosmicReader.readLine()) != null) {
                logger.debug(line);
                Cosmic cosmic = buildCosmic(line);

                if (parseChromosomeStartAndEnd(cosmic) && parseVariant(cosmic)) {
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
        Cosmic cosmic = new Cosmic();
        cosmic.setGeneName(fields[0]);
        cosmic.setAccessionNumber(fields[1]);
        cosmic.setGeneCDSLength(Integer.parseInt(fields[2]));
        cosmic.setHgncId(fields[3]);
        cosmic.setSampleName(fields[4]);
        cosmic.setIdSample(fields[5]);
        cosmic.setIdTumour(fields[6]);
        cosmic.setPrimarySite(fields[7]);
        cosmic.setSiteSubtype(fields[8]);
        cosmic.setPrimaryHistology(fields[11]);
        cosmic.setHistologySubtype(fields[12]);
        cosmic.setGenomeWideScreen(fields[15]);
        cosmic.setMutationID(fields[16]);
        cosmic.setMutationCDS(fields[17]);
        cosmic.setMutationAA(fields[18]);
        cosmic.setMutationDescription(fields[19]);
        cosmic.setMutationZygosity(fields[20]);
        cosmic.setMutationGRCh37GenomePosition(fields[23]);
        cosmic.setMutationGRCh37Strand(fields[24]);
        if (!fields[25].isEmpty() && fields[25].equalsIgnoreCase("y")) {
            cosmic.setSnp(true);
        }
        cosmic.setFathmmPrediction(fields[27]);
        cosmic.setMutationSomaticStatus(fields[mutationSomaticStatusColumn]);
        cosmic.setPubmedPMID(fields[pubmedPMIDColumn]);
        if (!fields[idStudyColumn].isEmpty() && !fields[idStudyColumn].equals("NS")) {
            cosmic.setIdStudy(Integer.parseInt(fields[idStudyColumn]));
        }
        cosmic.setSampleSource(fields[sampleSourceColumn]);
        cosmic.setTumourOrigin(fields[tumourOriginColumn]);
        if (!fields[ageColumn].isEmpty() && !fields[ageColumn].equals("NS")) {
            cosmic.setAge(Float.parseFloat(fields[ageColumn]));
        }
//        cosmic.setComments(fields[34]);
        return cosmic;
    }

    public boolean parseChromosomeStartAndEnd(Cosmic cosmic) {
        boolean success = false;
        if (cosmic.getMutationGRCh37GenomePosition() != null && !cosmic.getMutationGRCh37GenomePosition().isEmpty()) {
            Matcher matcher = mutationGRCh37GenomePositionPattern.matcher(cosmic.getMutationGRCh37GenomePosition());
            if (matcher.matches()) {
                setCosmicChromosome(matcher.group(CHROMOSOME), cosmic);
                cosmic.setStart(getStart(Integer.parseInt(matcher.group(START)), cosmic.getMutationCDS()));
                cosmic.setEnd(Integer.parseInt(matcher.group(END)));
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

    /**
     * Check whether the variant is valid and parse it.
     *
     * @return true if valid mutation, false otherwise
     */
    private boolean parseVariant(Cosmic cosmic) {
        boolean validVariant;

        String mutationCds = cosmic.getMutationCDS();
        if (mutationCds.contains(">")) {
            validVariant = parseSnv(mutationCds, cosmic);
            if (!validVariant) {
                invalidSubstitutionLines++;
            }
        } else if (mutationCds.contains("del")) {
            validVariant = parseDeletion(mutationCds, cosmic);
            if (!validVariant) {
                invalidDeletionLines++;
            }
        } else if (mutationCds.contains("ins")) {
            validVariant = parseInsertion(mutationCds, cosmic);
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

    private boolean parseInsertion(String mutationCds, Cosmic cosmic) {
        boolean validVariant = true;
        String insertedNucleotides = mutationCds.split("ins")[1];
        if (insertedNucleotides.matches("\\d+") || !insertedNucleotides.matches(VARIANT_STRING_PATTERN)) {
            //c.503_508ins30
            validVariant = false;
        } else {
            cosmic.setReference("");
            cosmic.setAlternate(getPositiveStrandString(insertedNucleotides, cosmic.getMutationGRCh37Strand()));
        }

        return validVariant;
    }

    private boolean parseDeletion(String mutationCds, Cosmic cosmic) {
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
            cosmic.setReference(getPositiveStrandString(mutationCDSArray[1], cosmic.getMutationGRCh37Strand()));
            cosmic.setAlternate("");
        }

        return validVariant;
    }

    private boolean parseSnv(String mutationCds, Cosmic cosmic) {
        boolean validVariant = true;
        Matcher snvMatcher = snvPattern.matcher(mutationCds);

        if (snvMatcher.matches()) {
            String ref = snvMatcher.group(REF);
            String alt = snvMatcher.group(ALT);
            if (!ref.equalsIgnoreCase("N") && !alt.equalsIgnoreCase("N")) {
                cosmic.setReference(getPositiveStrandString(ref, cosmic.getMutationGRCh37Strand()));
                cosmic.setAlternate(getPositiveStrandString(alt, cosmic.getMutationGRCh37Strand()));
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

    private void printSummary(long processedCosmicLines, long ignoredCosmicLines) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(processedCosmicLines) + " cosmic lines");
        logger.info("Serialized " + formatter.format(processedCosmicLines - ignoredCosmicLines) + " cosmic objects");
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
}
