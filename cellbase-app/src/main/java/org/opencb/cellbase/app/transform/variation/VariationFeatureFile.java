package org.opencb.cellbase.app.transform.variation;

import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationFeatureFile extends AbstractVariationFile {

    public static final String VARIATION_FEATURE_FILENAME = "variation_feature.txt";
    public static final String PREPROCESSED_VARIATION_FEATURE_FILENAME = "variation_feature.sorted.txt";

    public static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE = 5;

    public VariationFeatureFile(Path variationDirectory) {
        super(variationDirectory, VARIATION_FEATURE_FILENAME, PREPROCESSED_VARIATION_FEATURE_FILENAME,
                VARIATION_ID_COLUMN_INDEX_IN_VARIATION_FEATURE_FILE);
    }
}
