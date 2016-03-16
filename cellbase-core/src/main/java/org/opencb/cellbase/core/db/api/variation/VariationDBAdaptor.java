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

package org.opencb.cellbase.core.db.api.variation;

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Arrays;
import java.util.List;


public interface VariationDBAdaptor extends FeatureDBAdaptor {

    List<String> CONSEQUENCE_TYPES = Arrays.asList(
            "transcript_ablation",
            "splice_acceptor_variant",
            "splice_donor_variant",
            "stop_gained",
            "frameshift_variant",
            "stop_lost",
            "start_lost",
            "transcript_amplification",
            "inframe_insertion",
            "inframe_deletion",
            "missense_variant",
            "protein_altering_variant",
            "splice_region_variant",
            "incomplete_terminal_codon_variant",
            "stop_retained_variant",
            "synonymous_variant",
            "coding_sequence_variant",
            "mature_miRNA_variant",
            "5_prime_UTR_variant",
            "3_prime_UTR_variant",
            "non_coding_transcript_exon_variant",
            "intron_variant",
            "NMD_transcript_variant",
            "non_coding_transcript_variant",
            "upstream_gene_variant",
            "downstream_gene_variant",
            "TFBS_ablation",
            "TFBS_amplification",
            "TF_binding_site_variant",
            "regulatory_region_ablation",
            "regulatory_region_amplification",
            "feature_elongation",
            "regulatory_region_variant",
            "feature_truncation",
            "intergenic_variant");

    QueryResult next(String id, QueryOptions options);

    QueryResult getById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    QueryResult getAllConsequenceTypes(QueryOptions options);


    QueryResult getByGeneId(String id, QueryOptions options);

    List<QueryResult> getAllByGeneIdList(List<String> idList, QueryOptions options);

    QueryResult getByTranscriptId(String id, QueryOptions options);

    List<QueryResult> getAllByTranscriptIdList(List<String> idList, QueryOptions options);


//    QueryResult getAllPhenotypes(QueryOptions options);
//
//    List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options);
//
//    QueryResult getAllByPhenotype(String phenotype, QueryOptions options);
//
//    List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options);
//
//    QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options);
//
//    List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options);

    // public List<SnpPopulationFrequency> getAllSnpPopulationFrequency(String name);
    // public List<List<SnpPopulationFrequency>> getAllSnpPopulationFrequencyList(List<String> nameList);

    QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);

    List<QueryResult> getIdByVariantList(List<Variant> variations, QueryOptions options);

    List<QueryResult> getAllByVariantList(List<Variant> variations, QueryOptions options);

}
