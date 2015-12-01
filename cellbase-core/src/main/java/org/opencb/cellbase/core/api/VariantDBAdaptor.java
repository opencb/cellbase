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

/**
 * Created by imedina on 26/11/15.
 */
public interface VariantDBAdaptor<Variation> extends FeatureDBAdaptor<Variation> {

    enum QueryParams implements QueryParam {
        ID("id", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        GENE("gene", TEXT_ARRAY, ""),
        CONSEQUENCE_TYPE("consequenceType", TEXT_ARRAY, ""),
        TRANSCRIPT_CONSEQUENCE_TYPE("transcriptVariations.consequenceTypes", TEXT_ARRAY, ""),
        XREFS("xrefs", TEXT_ARRAY, "");

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

//    List<String> CONSEQUENCE_TYPES = Arrays.asList(
//            "transcript_ablation",
//            "splice_acceptor_variant",
//            "splice_donor_variant",
//            "stop_gained",
//            "frameshift_variant",
//            "stop_lost",
//            "start_lost",
//            "transcript_amplification",
//            "inframe_insertion",
//            "inframe_deletion",
//            "missense_variant",
//            "protein_altering_variant",
//            "splice_region_variant",
//            "incomplete_terminal_codon_variant",
//            "stop_retained_variant",
//            "synonymous_variant",
//            "coding_sequence_variant",
//            "mature_miRNA_variant",
//            "5_prime_UTR_variant",
//            "3_prime_UTR_variant",
//            "non_coding_transcript_exon_variant",
//            "intron_variant",
//            "NMD_transcript_variant",
//            "non_coding_transcript_variant",
//            "upstream_gene_variant",
//            "downstream_gene_variant",
//            "TFBS_ablation",
//            "TFBS_amplification",
//            "TF_binding_site_variant",
//            "regulatory_region_ablation",
//            "regulatory_region_amplification",
//            "feature_elongation",
//            "regulatory_region_variant",
//            "feature_truncation",
//            "intergenic_variant");

}
