package org.opencb.cellbase.app.transform;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.StructuralVariantType;
import org.opencb.biodata.models.variant.avro.StructuralVariation;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by fjlopez on 08/05/17.
 */
public class DgvParser extends CellBaseParser {

    private static final int VARIANT_SUBTYPE_COLUMN = 5;
    private static final int CHR_COLUMN = 1;
    private static final int START_COLUMN = 2;
    private static final int END_COLUMN = 3;
    private static final int ACCESSION_COLUMN = 0;
    private static final int SUPPORTING_VARIANTS_COLUMN = 11;
    private static final String UNKNOWN_NT = "N";
    private static final String CNV_STR = "<CNV>";
    private static final String DELETION = "deletion";
    private static final String INSERTION = "insertion";
    private static final String MOBILE_ELEMENT_INSERTION = "mobile element insertion";
    private static final String NOVEL_SEQUENCE_INSERTION = "novel sequence insertion";
    private static final String INVERSION = "inversion";
    private static final String DUPLICATION = "duplication";
    private static final String TANDEM_DUPLICATION = "tandem duplication";
    private static final String GAIN = "gain";
    private static final String LOSS = "loss";
    private static final String DELETION_ALTERNATE_STR = "<DEL>";
    private static final String DUPLICATION_ALTERNATE_STR = "<DUP>";
    private static final String INSERTION_ALTERNATE_STR = "<INS>";
    private static final String INVERSION_ALTERNATE_STR = "<INV>";
    private final Path file;
    private static final Map<String, StructuralVariantType> DGV_SUBTYPE_TO_SV_SUBTYPE = new HashMap<>(4);

    static {
        DGV_SUBTYPE_TO_SV_SUBTYPE.put("loss", StructuralVariantType.COPY_NUMBER_LOSS);
        DGV_SUBTYPE_TO_SV_SUBTYPE.put("deletion", StructuralVariantType.COPY_NUMBER_LOSS);
        DGV_SUBTYPE_TO_SV_SUBTYPE.put("duplication", StructuralVariantType.COPY_NUMBER_GAIN);
        DGV_SUBTYPE_TO_SV_SUBTYPE.put("gain", StructuralVariantType.COPY_NUMBER_GAIN);
        DGV_SUBTYPE_TO_SV_SUBTYPE.put("insertion", StructuralVariantType.COPY_NUMBER_GAIN);
    }

    private Map<String, Integer> unexpectedVariantSubtype;

    public DgvParser(Path file, CellBaseSerializer serializer) {
        super(serializer);
        this.file = file;
    }

    private Long countFileLines(Path filePath) throws IOException {
        try (BufferedReader bufferedReader1 = FileUtils.newBufferedReader(filePath)) {
            long nLines = 0;
            String line1 = bufferedReader1.readLine();
            while (line1 != null) {
                nLines++;
                line1 = bufferedReader1.readLine();
            }
            return nLines;
        }

    }

    @Override
    public void parse() throws Exception {
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(file)) {
            // Skip header
            bufferedReader.readLine();

            unexpectedVariantSubtype = new HashMap<>();
            ProgressLogger progressLogger = new ProgressLogger("Parsed DGV lines:",
                    () -> countFileLines(file), 200).setBatchSize(10000);
            String line = bufferedReader.readLine();
            while (line != null) {
                List<Variant> variantList = parseVariants(line);
                line = bufferedReader.readLine();
                for (Variant variant : variantList) {
                    serializer.serialize(variant.getImpl());
                }
                progressLogger.increment(1);
            }
        }
        printSummary();
        logger.info("Done.");

    }

    private void printSummary() {
        for (String subtype : unexpectedVariantSubtype.keySet()) {
            logger.info("{} variants skipped because of unexpected subtype '{}'", unexpectedVariantSubtype.get(subtype),
                    subtype);
        }
    }

    private List<Variant> parseVariants(String line) {
        String[] fields = line.split("\t");
        String[] variantSubtypes = fields[VARIANT_SUBTYPE_COLUMN].split("\\+");
        List<Variant> variantList = new ArrayList<>(variantSubtypes.length);

        for (String subtype : variantSubtypes) {
            Variant variant;
            StructuralVariation structuralVariation = new StructuralVariation();
            switch (subtype) {
                case DELETION:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, DELETION_ALTERNATE_STR);
                    break;
                case MOBILE_ELEMENT_INSERTION:
                case NOVEL_SEQUENCE_INSERTION:
                case INSERTION:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, INSERTION_ALTERNATE_STR);
                    break;

                case INVERSION:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, INVERSION_ALTERNATE_STR);
                    break;
                case DUPLICATION:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, DUPLICATION_ALTERNATE_STR);
                    break;
                case TANDEM_DUPLICATION:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, DUPLICATION_ALTERNATE_STR);
                    structuralVariation.setType(StructuralVariantType.TANDEM_DUPLICATION);
                    break;
                case GAIN:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, CNV_STR);
                    structuralVariation.setType(StructuralVariantType.COPY_NUMBER_GAIN);
                    break;
                case LOSS:
                    variant = new Variant(fields[CHR_COLUMN], Integer.valueOf(fields[START_COLUMN]),
                            Integer.valueOf(fields[END_COLUMN]), UNKNOWN_NT, CNV_STR);
                    structuralVariation.setType(StructuralVariantType.COPY_NUMBER_LOSS);
                    break;
                default:
                    logger.debug("Unexpected VariantSubtype found '{}'", subtype);
                    logger.debug("Complete line {}", line);
                    logger.debug("Skipping variant subtype parsing");
                    if (unexpectedVariantSubtype.containsKey(subtype)) {
                        unexpectedVariantSubtype.put(subtype, unexpectedVariantSubtype.get(subtype) + 1);
                    } else {
                        unexpectedVariantSubtype.put(subtype, 1);
                    }
                    continue;
            }

            // Imprecise fields are set to the same exact values variant.start variant.end in order for imprecise
            // queries to work properly
            structuralVariation.setCiStartLeft(variant.getStart());
            structuralVariation.setCiStartRight(variant.getStart());
            structuralVariation.setCiEndLeft(variant.getEnd());
            structuralVariation.setCiEndRight(variant.getEnd());

            variant.setSv(structuralVariation);
            variant.setId(fields[ACCESSION_COLUMN]);
            variant.setNames(Arrays.asList(fields[SUPPORTING_VARIANTS_COLUMN].split(",")));
            variantList.add(variant);
        }

        return variantList;
    }
}
