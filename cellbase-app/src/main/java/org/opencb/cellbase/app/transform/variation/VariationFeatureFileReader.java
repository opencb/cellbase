package org.opencb.cellbase.app.transform.variation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationFeatureFileReader extends VariationFileReader {
    public static final String PREPROCESSED_VARIATION_FEATURE_FILENAME = "variation_feature.sorted.txt";
    public static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE = 5;
    public VariationFeatureFileReader(Path variationDirectory) throws IOException {
        super(variationDirectory, PREPROCESSED_VARIATION_FEATURE_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE);
    }
}
