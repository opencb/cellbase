package org.opencb.cellbase.app.transform.variation;

import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by parce on 08/12/15.
 */
public abstract class VariationFileReader {
    private BufferedReader fileReader;
    private int variationIdColumnIndex;
    private boolean endOfFile;
    private String[] lastReadLine;
    private int lastReadVariationId;

    public VariationFileReader(Path variationDirectory, String fileName, int variationIdColumnIndexInFile) throws IOException {
        this.fileReader = getBufferedReader(variationDirectory, fileName);
        this.variationIdColumnIndex = variationIdColumnIndexInFile;
        endOfFile = false;
    }

    private BufferedReader getBufferedReader(Path variationDirectory, String fileName) throws IOException {
        Path inputFile;
        if (Files.exists(variationDirectory.resolve(fileName))) {
            inputFile = variationDirectory.resolve(fileName);
        } else {
            inputFile = variationDirectory.resolve(fileName + ".gz");
        }
        return FileUtils.newBufferedReader(inputFile);
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
}
