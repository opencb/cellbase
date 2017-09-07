package org.opencb.cellbase.app.transform;

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by fjlopez on 05/05/17.
 */
public class RepeatsParser extends CellBaseParser {
    private static final String TRF = "trf";
    private static final String GSD = "genomicSuperDup";
    private static final String WM = "windowMasker";
    private final Path filesDir;

    public RepeatsParser(Path filesDir, CellBaseFileSerializer serializer) {
        super(serializer);
        this.filesDir = filesDir;
    }


    @Override
    public void parse() throws Exception {

        logger.info("Parsing repeats...");
        if (Files.exists(filesDir.resolve(EtlCommons.TRF_FILE))) {
            parseTrfFile(filesDir.resolve(EtlCommons.TRF_FILE));
        } else {
            logger.warn("No TRF file found {}", EtlCommons.TRF_FILE);
            logger.warn("Skipping TRF file parsing. TRF data models will not be built.");
        }

        if (Files.exists(filesDir.resolve(EtlCommons.GSD_FILE))) {
            parseGsdFile(filesDir.resolve(EtlCommons.GSD_FILE));
        } else {
            logger.warn("No Genomic Super Duplications file found {}", EtlCommons.GSD_FILE);
            logger.warn("Skipping Genomic Super Duplications file parsing. "
                    + "Genomic Super Duplications data models will not be built.");
        }

        if (Files.exists(filesDir.resolve(EtlCommons.WM_FILE))) {
            parseWmFile(filesDir.resolve(EtlCommons.WM_FILE));
        } else {
            logger.warn("No WindowMasker file found {}", EtlCommons.WM_FILE);
            logger.warn("Skipping WindowMasker file parsing. WindowMasker data models will not be built.");
        }
        logger.info("Done.");

    }

    private void parseTrfFile(Path filePath) throws IOException {
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger("Parsed TRF lines:",
                    () -> EtlCommons.countFileLines(filePath), 200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseTrfLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
    }

    private Repeat parseTrfLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(null, Region.normalizeChromosome(parts[1]), Integer.valueOf(parts[2]) + 1, Integer.valueOf(parts[3]),
                Integer.valueOf(parts[5]), Float.valueOf(parts[6]), Float.valueOf(parts[8]) / 100, Float.valueOf(parts[10]),
                parts[16], TRF);
    }

    private void parseGsdFile(Path filePath) throws IOException {
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger("Parsed GSD lines:",
                    () -> EtlCommons.countFileLines(filePath), 200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseGSDLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
    }

    private Repeat parseGSDLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(parts[11], Region.normalizeChromosome(parts[1]), Integer.valueOf(parts[2]) + 1,
                Integer.valueOf(parts[3]), null, 2f, Float.valueOf(parts[26]), null,
                null, GSD);

    }

    private void parseWmFile(Path filePath) throws IOException {
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(filePath)) {
            String line = bufferedReader.readLine();

            ProgressLogger progressLogger = new ProgressLogger("Parsed WM lines:",
                    () -> EtlCommons.countFileLines(filePath), 200).setBatchSize(10000);
            while (line != null) {
                serializer.serialize(parseWmLine(line));
                line = bufferedReader.readLine();
                progressLogger.increment(1);
            }
        }
    }

    private Repeat parseWmLine(String line) {
        String[] parts = line.split("\t");

        return new Repeat(null, Region.normalizeChromosome(parts[1]), Integer.valueOf(parts[2]) + 1,
                Integer.valueOf(parts[3]), null, null, null, null, null, WM);
    }
}
