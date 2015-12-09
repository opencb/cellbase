package org.opencb.cellbase.app.transform.variation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by parce on 09/12/15.
 */
public class VariationTranscriptFileReader extends VariationFileReader {
    public static final String PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME = "transcript_variation.includingVariationId.txt";
    public VariationTranscriptFileReader(Path variationDirectory, int variationIdColumnIndexInFile) throws IOException {
        super(variationDirectory, PREPROCESSED_TRANSCRIPT_VARIATION_FILENAME, variationIdColumnIndexInFile);
    }
}
