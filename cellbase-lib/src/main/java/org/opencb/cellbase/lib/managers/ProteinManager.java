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

package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptMissenseVariantFunctionalScore;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.ProteinQuery;
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.MissenseVariationFunctionalScoreMongoDBAdaptor;
import org.opencb.cellbase.lib.impl.core.ProteinMongoDBAdaptor;
import org.opencb.cellbase.lib.impl.core.TranscriptMongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProteinManager extends AbstractManager implements AggregationApi<ProteinQuery, Entry> {

    private ProteinMongoDBAdaptor proteinDBAdaptor;
    private TranscriptMongoDBAdaptor transcriptDBAdaptor;
    private MissenseVariationFunctionalScoreMongoDBAdaptor missenseVariationFunctionalScoreMongoDBAdaptor;

    public ProteinManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        missenseVariationFunctionalScoreMongoDBAdaptor = dbAdaptorFactory.getMissenseVariationFunctionalScoreMongoDBAdaptor(species,
                assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return proteinDBAdaptor;
    }

    public CellBaseDataResult getSubstitutionScores(TranscriptQuery query, Integer position, String aa)
            throws JsonProcessingException {
        // Fetch Ensembl transcriptId to query substiturion scores
        logger.info("Searching transcripts for {}", query.getTranscriptsXrefs());
        CellBaseDataResult<Transcript> queryResult = transcriptDBAdaptor.query(query);
        logger.info("{} transcripts found", queryResult.getNumResults());
        // Get substitution scores for fetched transcript
        if (queryResult.getNumResults() > 0) {
            String transcriptId = queryResult.getResults().get(0).getId();
            query.setTranscriptsId(Collections.singletonList(transcriptId));
            CellBaseDataResult<Score> scoresCellBaseDataResult = proteinDBAdaptor.getSubstitutionScores(query, position, aa);
            scoresCellBaseDataResult.setId(transcriptId);
            return scoresCellBaseDataResult;
        } else {
            return queryResult;
        }
    }

    public CellBaseDataResult<String> getSequence(ProteinQuery query) {
        List<String> includes = new ArrayList<>();
        includes.add("sequence.value");
        query.setIncludes(includes);
        CellBaseDataResult<Entry> proteinDataResult = proteinDBAdaptor.query(query);
        if (proteinDataResult == null) {
            return null;
        }
        CellBaseDataResult<String> result = new CellBaseDataResult<>(proteinDataResult.getId(), proteinDataResult.getTime(),
                proteinDataResult.getEvents(), proteinDataResult.getNumResults(), Collections.emptyList(), 1);
        result.setResults(Collections.singletonList(proteinDataResult.getResults().get(0).getSequence().getValue()));
        return result;
    }

    public CellBaseDataResult<ProteinVariantAnnotation> getVariantAnnotation(Variant variant, String ensemblTranscriptId, int aaPosition,
                                                                             String aaReference, String aaAlternate, QueryOptions options) {
        CellBaseDataResult<ProteinVariantAnnotation> proteinVariantAnnotation = proteinDBAdaptor.getVariantAnnotation(ensemblTranscriptId,
                aaPosition, aaReference, aaAlternate, options);
        CellBaseDataResult<TranscriptMissenseVariantFunctionalScore> revelResults =
                missenseVariationFunctionalScoreMongoDBAdaptor.getScores(
                        variant.getChromosome(), variant.getStart(), variant.getReference(), variant.getAlternate(),
                        aaReference, aaAlternate);
        if (proteinVariantAnnotation.getResults() != null && revelResults.getResults() != null) {
            proteinVariantAnnotation.getResults().get(0).getSubstitutionScores().add(
                    new Score(revelResults.first().getScore(), "revel", ""));
        }
        return proteinVariantAnnotation;
    }
}


