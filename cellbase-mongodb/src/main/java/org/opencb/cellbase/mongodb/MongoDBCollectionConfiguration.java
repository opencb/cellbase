/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.mongodb;

/**
 * Created by imedina on 19/03/15.
 */
public class MongoDBCollectionConfiguration {

    public static final int GENE_CHUNK_SIZE = 5000;
    public static final int VARIATION_CHUNK_SIZE = 2000;
    public static final int GENOME_SEQUENCE_CHUNK_SIZE = 2000;
    public static final int CONSERVATION_CHUNK_SIZE = 2000;
    public static final int REGULATORY_REGION_CHUNK_SIZE = 2000;
    public static final int VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE = 1000;
}
