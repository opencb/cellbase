package org.opencb.cellbase.app.transform.variation;

import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationSynonymFile extends AbstractVariationFile {

    public static final String VARIATION_SYNONYM_FILENAME = "variation_synonym.txt";
    public static final String PREPROCESSED_VARIATION_SYNONYM_FILENAME = "variation_synonym.sorted.txt";

    static final int VARIATION_ID_COLUMN_INDEX = 1;
    static final int RS_COLUMN_INDEX = 4;

    public VariationSynonymFile(Path variationDirectory) {
        super(variationDirectory, VARIATION_SYNONYM_FILENAME, PREPROCESSED_VARIATION_SYNONYM_FILENAME,
                VARIATION_ID_COLUMN_INDEX);
    }
}
