package org.opencb.cellbase.app.transform.variation;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by parce on 08/12/15.
 */
public abstract class AbstractVariationFile {

    protected Logger logger;

    protected Path variationDirectory;

    protected String unprocessedFileName;
    protected String preprocessedFileName;

    protected BufferedReader fileReader;

    protected int variationIdColumnIndex;
    private boolean endOfFile;
    private String[] lastReadLine;
    private int lastReadVariationId;

    public AbstractVariationFile(Path variationDirectory, String unprocessedFileName, String preprocessedFileName) {
        this.variationDirectory = variationDirectory;
        this.unprocessedFileName = unprocessedFileName;
        this.preprocessedFileName = preprocessedFileName;
        endOfFile = false;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public AbstractVariationFile(Path variationDirectory, String unprocessedFileName, String preprocessedFileName,
                                 int variationIdColumnIndexInFile)  {
        this(variationDirectory, unprocessedFileName, preprocessedFileName);
        this.variationIdColumnIndex = variationIdColumnIndexInFile;
    }

    public void createBufferedReader() throws IOException {
        this.fileReader = getBufferedReader(preprocessedFileName);
    }

    protected BufferedReader getBufferedReader(String fileName) throws IOException {
        Path inputFile;
        if (Files.exists(variationDirectory.resolve(fileName))) {
            inputFile = variationDirectory.resolve(fileName);
        } else {
            inputFile = variationDirectory.resolve(fileName + ".gz");
        }
        return FileUtils.newBufferedReader(inputFile);
    }

    protected BufferedReader getUnprocessedFileBufferedReader() throws IOException {
        return getBufferedReader(unprocessedFileName);
    }

    public List<String[]> getVariationRelatedLines(int variationId) throws IOException {
        List<String[]> variationRelatedLines;

        readFileLinesUntilReachVariation(variationId);
        if (endOfFile || variationIdExceededInFile(variationId)) {
            variationRelatedLines = Collections.EMPTY_LIST;
        } else {
            variationRelatedLines = new ArrayList<>();
            while (!endOfFile && !variationIdExceededInFile(variationId)) {
                variationRelatedLines.add(lastReadLine);
                readLineInVariationRelatedFile();
            }
        }
        return variationRelatedLines;
    }

    private void readFileLinesUntilReachVariation(int variationId) throws IOException {
        while (!endOfFile && !variationReachedInFile(variationId)) {
            readLineInVariationRelatedFile();
        }
    }

    private void readLineInVariationRelatedFile() throws IOException {
        String line = fileReader.readLine();
        if (line == null) {
            endOfFile = true;
        } else {
            lastReadLine = line.split("\t", -1);
            lastReadVariationId = getVariationIdFromLastLineInVariationRelatedFile();
        }
    }

    private int getVariationIdFromLastLineInVariationRelatedFile() {
        return Integer.parseInt(lastReadLine[variationIdColumnIndex]);
    }

    private boolean variationReachedInFile(int variationId) {
        return lastReadVariationId != -1 && lastReadVariationId >= variationId;
    }

    private boolean variationIdExceededInFile(int variationId) {
        return lastReadVariationId > variationId;
    }

    public void gunzip() throws IOException, InterruptedException {
        if (!existsZippedOrUnzippedFile(preprocessedFileName)) {
            // unzip variation file name for preprocess it later
            gunzipFileIfNeeded(variationDirectory, unprocessedFileName);
        }
    }

    protected boolean existsZippedOrUnzippedFile(String baseFilename) {
        return Files.exists(variationDirectory.resolve(baseFilename))
                || Files.exists(variationDirectory.resolve(baseFilename + ".gz"));
    }

    private void gunzipFileIfNeeded(Path directory, String fileName) throws IOException, InterruptedException {
        Path zippedFile = directory.resolve(fileName + ".gz");
        if (Files.exists(zippedFile)) {
            logger.info("Unzipping {} ...", zippedFile);
            Process process = Runtime.getRuntime().exec("gunzip " + zippedFile.toAbsolutePath());
            process.waitFor();
        } else {
            Path unzippedFile = directory.resolve(fileName);
            if (Files.exists(unzippedFile)) {
                logger.info("File {} was previously unzipped: skipping 'gunzip' for this file ...", unzippedFile);
            } else {
                throw new FileNotFoundException("File " + zippedFile + " doesn't exist");
            }
        }
    }

    public void sort() throws IOException, InterruptedException {
        if (!existsZippedOrUnzippedFile(preprocessedFileName)) {
            Path sortedFile = variationDirectory.resolve(preprocessedFileName);
            Path unsortedFile = variationDirectory.resolve(unprocessedFileName);
            sortFileByNumericColumn(unsortedFile, sortedFile, variationIdColumnIndex);
        }
    }

    protected void sortFileByNumericColumn(Path inputFile, Path outputFile, int columnIndex) throws InterruptedException, IOException {
        logger.info("Sorting file {} into {} ...", inputFile, outputFile);

        // increment column index by 1, beacause Java indexes are 0-based and 'sort' command uses 1-based indexes
        columnIndex++;
        ProcessBuilder pb = new ProcessBuilder("sort", "-t", "\t", "-k", Integer.toString(columnIndex),
                "-n", "--stable", inputFile.toAbsolutePath().toString(), "-T", variationDirectory.toString(),
                "-o", outputFile.toAbsolutePath().toString());
        logger.debug("Executing '{}' ...", StringUtils.join(pb.command(), " "));
        Stopwatch stopwatch = Stopwatch.createStarted();
        Process process = pb.start();
        process.waitFor();

        int returnedValue = process.exitValue();
        if (returnedValue != 0) {
            String errorMessage = IOUtils.toString(process.getErrorStream());
            logger.error("Error sorting {}", inputFile);
            logger.error(errorMessage);
            throw new RuntimeException("Error sorting " + inputFile);
        }
        logger.info("Sorted");
        logger.debug("Elapsed time sorting file: {}", stopwatch);
    }

    public void gzip() throws IOException, InterruptedException {
        gzipFile(unprocessedFileName);
        gzipFile(preprocessedFileName);
    }

    private void gzipFile(String fileName) throws IOException, InterruptedException {
        Path unzippedFile = variationDirectory.resolve(fileName);
        if (Files.exists(unzippedFile)) {
            logger.info("Compressing {}", unzippedFile.toAbsolutePath());
            Process process = Runtime.getRuntime().exec("gzip " + unzippedFile.toAbsolutePath());
            process.waitFor();
        }
    }

    public boolean existsZippedOrUnzippedFile() {
        return Files.exists(variationDirectory.resolve(unprocessedFileName))
                || Files.exists(variationDirectory.resolve(unprocessedFileName + ".gz"));
    }

    public boolean isEmpty() throws IOException {
        if (Files.exists(variationDirectory.resolve(unprocessedFileName))) {
            return Files.size(variationDirectory.resolve(unprocessedFileName)) == 0;
        } else {
            return Files.size(variationDirectory.resolve(unprocessedFileName + ".gz")) == 0;
        }
    }
}
