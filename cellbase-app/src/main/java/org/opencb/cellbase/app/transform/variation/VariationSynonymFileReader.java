package org.opencb.cellbase.app.transform.variation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationSynonymFileReader extends VariationFileReader {
    static final int VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE = 1;
    public static final String PREPROCESSED_VARIATION_SYNONYM_FILENAME = "variation_synonym.sorted.txt";
    public VariationSynonymFileReader(Path variationDirectory) throws IOException {
        super(variationDirectory, PREPROCESSED_VARIATION_SYNONYM_FILENAME, VARIATION_ID_COLUMN_INDEX_IN_VARIATION_SYNONYM_FILE);
    }
}
