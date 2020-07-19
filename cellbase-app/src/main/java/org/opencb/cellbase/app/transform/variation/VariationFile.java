package org.opencb.cellbase.app.transform.variation;

import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationFile extends AbstractVariationFile {

    public static final String VARIATION_FILENAME = "variation.txt";
    public static final String PREPROCESSED_VARIATION_FILENAME = "variation.sorted.txt";

    public static final int VARIATION_ID_COLUMN_INDEX = 0;
    public static final int RS_COLUMN_INDEX = 2;

    public VariationFile(Path variationDirectory) {
        super(variationDirectory, VARIATION_FILENAME, PREPROCESSED_VARIATION_FILENAME, VARIATION_ID_COLUMN_INDEX);
    }

    public BufferedReader getBufferedReader() throws IOException {
        Path inputFile;
        if (Files.exists(variationDirectory.resolve(PREPROCESSED_VARIATION_FILENAME))) {
            inputFile = variationDirectory.resolve(PREPROCESSED_VARIATION_FILENAME);
        } else {
            inputFile = variationDirectory.resolve(PREPROCESSED_VARIATION_FILENAME + ".gz");
        }
        return FileUtils.newBufferedReader(inputFile);
    }
}
