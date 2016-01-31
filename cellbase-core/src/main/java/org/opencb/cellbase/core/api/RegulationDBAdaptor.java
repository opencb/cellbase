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

package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;
import static org.opencb.commons.datastore.core.QueryParam.Type.DECIMAL;

/**
 * Created by imedina on 07/12/15.
 */
public interface RegulationDBAdaptor<T> extends FeatureDBAdaptor<T> {

    enum FeatureType {
        CTCF_binding_site("CTCF_binding_site"),
        ChIP_seq_region("ChIP_seq_region"),
        H3K14_acetylation_site("H3K14_acetylation_site"),
        H3K18_acetylation_site("H3K18_acetylation_site"),
        H3K23_acylation_site("H3K23_acylation_site"),
        H3K27_acylation_site("H3K27_acylation_site"),
        H3K27_trimethylation_site("H3K27_trimethylation_site"),
        H3K36_trimethylation_site("H3K36_trimethylation_site"),
        H3K4_dimethylation_site("H3K4_dimethylation_site"),
        H3K4_monomethylation_site("H3K4_monomethylation_site"),
        H3K4_trimethylation_site("H3K4_trimethylation_site"),
        H3K79_dimethylation_site("H3K79_dimethylation_site"),
        H3K79_monomethylation_site("H3K79_monomethylation_site"),
        H3K9_acetylation_site("H3K9_acetylation_site"),
        H3K9_monomethylation_site("H3K9_monomethylation_site"),
        H3K9_trimethylation_site("H3K9_trimethylation_site"),
        H4K20_monomethylation_site("H4K20_monomethylation_site"),
        H4K5_acylation_site("H4K5_acylation_site"),
        TF_binding_site("TF_binding_site"),
        TF_binding_site_motif("TF_binding_site_motif"),
        enhancer("enhancer"),
        histone_acetylation_site("histone_acetylation_site"),
        histone_binding_site("histone_binding_site"),
        histone_methylation_site("histone_methylation_site"),
        mirna_target("mirna_target"),
        open_chromatin_region("open_chromatin_region"),
        promoter("promoter"),
        promoter_flanking_region("promoter_flanking_region");

        private final String key;

        FeatureType(String key) {
            this.key = key;
        }
    }

    enum QueryParams implements QueryParam {
        NAME("name", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        FEATURE_TYPE("featureType", TEXT_ARRAY, ""),
        FEATURE_CLASS("featureClass", TEXT_ARRAY, ""),
        CELL_TYPES("cellTypes", TEXT_ARRAY, ""),
        SCORE("score", DECIMAL, "");


        QueryParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        private final String key;
        private Type type;
        private String description;

        @Override
        public String key() {
            return key;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
