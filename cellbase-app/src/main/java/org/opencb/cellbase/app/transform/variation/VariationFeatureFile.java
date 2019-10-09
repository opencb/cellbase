/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
