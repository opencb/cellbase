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

package org.opencb.cellbase.core.queries;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.QueryOptions;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneQuery extends AbstractQuery {

    private QueryOptions queryOptions;
    private List<String> ids;
    private List<String> names;
    private List<String> biotypes;
    private List<String> regions;
    private List<String> transcriptBiotypes;
    private List<String> transcriptXrefs;
    private List<String> transcriptIds;
    private List<String> transcriptNames;
    private List<String> transcriptAnnotationFlags;
    private List<String> transcriptTfbsNames;
    private List<String> annotationDiseaseIds;
    private List<String> annotationDiseaseNames;
    private List<String> annotationExpressionGenes;
    private List<String> annotationExpressionTissues;
    private List<String> annotationExpressionValues;
    private List<String> annotationDrugNames;
    private List<String> annotationDrugGenes;

    public GeneQuery() {}

    public GeneQuery(MultivaluedMap<String, String> multivaluedMap) throws CellbaseException {
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue().get(0);

            if (QueryOptions.COUNT.equals(key)) {
                addQueryOption(QueryOptions.COUNT, Boolean.parseBoolean(value));
            } else if (QueryOptions.SKIP.equals(key)) {
                int skip;
                try {
                    skip = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    throw new CellbaseException("Invalid skip value, not a valid number: " + value);
                }
                addQueryOption(QueryOptions.SKIP, skip);
            } else if (QueryOptions.LIMIT.equals(key)) {
                int limit;
                try {
                    limit = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    throw new CellbaseException("Invalid limit value, not a valid number: " + value);
                }
                addQueryOption(QueryOptions.LIMIT, limit);
            } else if (QueryOptions.EXCLUDE.equals(key)) {
                queryOptions.getAsStringList(QueryOptions.EXCLUDE).addAll(Splitter.on(",").splitToList(value));
            } else if (QueryOptions.INCLUDE.equals(key)) {
                queryOptions.getAsStringList(QueryOptions.INCLUDE).addAll(Splitter.on(",").splitToList(value));
            } else {
                if ("id".equals(key)) {
                    setId(value);
                } else if ("name".equals(key)) {
                    setNames(value);
                } else if ("biotype".equals(key)) {
                    setBiotypes(value);
                } else if ("region".equals(key)) {
                    setRegions(value);
                } else if ("transcriptsBiotype".equals(key)) {
                    setTranscriptBiotypes(value);
                } else if ("transcriptsXrefs".equals(key)) {
                    setTranscriptXrefs(value);
                } else if ("transcriptsId".equals(key)) {
                    setTranscriptIds(value);
                } else if ("transcriptsName".equals(key)) {
                    setTranscriptNames(value);
                } else if ("transcriptsAnnotationFlags".equals(key)) {
                    setTranscriptAnnotationFlags(value);
                } else if ("transcriptsTfbsName".equals(key)) {
                    setTranscriptTfbsNames(value);
                } else if ("annotationDiseasesId".equals(key)) {
                    setAnnotationDiseaseIds(value);
                } else if ("annotationDiseasesName".equals(key)) {
                    setAnnotationDiseaseNames(value);
                } else if ("annotationExpressionGene".equals(key)) {
                    setAnnotationExpressionGenes(value);
                } else if ("annotationExpressionTissue".equals(key)) {
                    setAnnotationExpressionTissues(value);
                } else if ("annotationexpressionValue".equals(key)) {
                    setAnnotationExpressionValues(value);
                } else if ("annotationDrugsName".equals(key)) {
                    setAnnotationDrugNames(value);
                } else if ("annotationDrugsGene".equals(key)) {
                    setAnnotationDrugGenes(value);
                } else {
                    throw new CellbaseException("invalid parameter " + key);
                }
            }
        }
    }

    public List<String> getIds() {
        return ids;
    }

    public GeneQuery setId(String ids) {
        this.ids.addAll(Arrays.asList(ids.split(",")));
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public GeneQuery setNames(String names) {
        this.names.addAll(Arrays.asList(names.split(",")));
        return this;
    }

    public List<String> getBiotypes() {
        return biotypes;
    }

    public GeneQuery setBiotypes(String biotypes) {
        this.biotypes.addAll(Arrays.asList(biotypes.split(",")));
        return this;
    }

    public List<String> getRegions() {
        return regions;
    }

    public GeneQuery setRegions(String regions) {
        this.regions.addAll(Arrays.asList(regions.split(",")));
        return this;
    }

    public List<String> getTranscriptBiotypes() {
        return transcriptBiotypes;
    }

    public GeneQuery setTranscriptBiotypes(String transcriptBiotypes) {
        this.transcriptBiotypes.addAll(Arrays.asList(transcriptBiotypes.split(",")));
        return this;
    }

    public List<String> getTranscriptXrefs() {
        return transcriptXrefs;
    }

    public GeneQuery setTranscriptXrefs(String transcriptXrefs) {
        this.transcriptXrefs.addAll(Arrays.asList(transcriptXrefs.split(",")));
        return this;
    }

    public List<String> getTranscriptIds() {
        return transcriptIds;
    }

    public GeneQuery setTranscriptIds(String transcriptIds) {
        this.transcriptIds.addAll(Arrays.asList(transcriptIds.split(",")));
        return this;
    }

    public List<String> getTranscriptNames() {
        return transcriptNames;
    }

    public GeneQuery setTranscriptNames(String transcriptNames) {
        this.transcriptNames.addAll(Arrays.asList(transcriptNames.split(",")));
        return this;
    }

    public List<String> getTranscriptAnnotationFlags() {
        return transcriptAnnotationFlags;
    }

    public GeneQuery setTranscriptAnnotationFlags(String transcriptAnnotationFlags) {
        this.transcriptAnnotationFlags.addAll(Arrays.asList(transcriptAnnotationFlags.split(",")));
        return this;
    }

    public List<String> getTranscriptTfbsNames() {
        return transcriptTfbsNames;
    }

    public GeneQuery setTranscriptTfbsNames(String transcriptTfbsNames) {
        this.transcriptTfbsNames.addAll(Arrays.asList(transcriptTfbsNames.split(",")));
        return this;
    }

    public List<String> getAnnotationDiseaseIds() {
        return annotationDiseaseIds;
    }

    public GeneQuery setAnnotationDiseaseIds(String annotationDiseaseIds) {
        this.annotationDiseaseIds.addAll(Arrays.asList(annotationDiseaseIds.split(",")));
        return this;
    }

    public List<String> getAnnotationDiseaseNames() {
        return annotationDiseaseNames;
    }

    public GeneQuery setAnnotationDiseaseNames(String annotationDiseaseNames) {
        this.annotationDiseaseNames.addAll(Arrays.asList(annotationDiseaseNames.split(",")));
        return this;
    }

    public List<String> getAnnotationExpressionGenes() {
        return annotationExpressionGenes;
    }

    public GeneQuery setAnnotationExpressionGenes(String annotationExpressionGenes) {
        this.annotationExpressionGenes.addAll(Arrays.asList(annotationExpressionGenes.split(",")));
        return this;
    }

    public List<String> getAnnotationExpressionTissues() {
        return annotationExpressionTissues;
    }

    public GeneQuery setAnnotationExpressionTissues(String annotationExpressionTissues) {
        this.annotationExpressionTissues.addAll(Arrays.asList(annotationExpressionTissues.split(",")));
        return this;
    }

    public List<String> getAnnotationExpressionValues() {
        return annotationExpressionValues;
    }

    public GeneQuery setAnnotationExpressionValues(String annotationExpressionValues) {
        this.annotationExpressionValues.addAll(Arrays.asList(annotationExpressionValues.split(",")));
        return this;
    }

    public List<String> getAnnotationDrugNames() {
        return annotationDrugNames;
    }

    public GeneQuery setAnnotationDrugNames(String annotationDrugNames) {
        this.annotationDrugNames.addAll(Arrays.asList(annotationDrugNames.split(",")));
        return this;
    }

    public List<String> getAnnotationDrugGenes() {
        return annotationDrugGenes;
    }

    public GeneQuery setAnnotationDrugGenes(String annotationDrugGenes) {
        this.annotationDrugGenes.addAll(Arrays.asList(annotationDrugGenes.split(",")));
        return this;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public GeneQuery addQueryOption(String key, Object value) {
        queryOptions.put(key, value);
        return this;
    }

    public GeneQuery setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
        return this;
    }

    public static class Builder {
        private QueryOptions queryOptions;
        private List<String> ids;
        private List<String> names;
        private List<String> biotypes;
        private List<String> regions;
        private List<String> transcriptBiotypes;
        private List<String> transcriptXrefs;
        private List<String> transcriptIds;
        private List<String> transcriptNames;
        private List<String> transcriptAnnotationFlags;
        private List<String> transcriptTfbsNames;
        private List<String> annotationDiseaseIds;
        private List<String> annotationDiseaseNames;
        private List<String> annotationExpressionGenes;
        private List<String> annotationExpressionTissues;
        private List<String> annotationExpressionValues;
        private List<String> annotationDrugNames;
        private List<String> annotationDrugGenes;

        public Builder() {
        }

        public Builder withIds(String ids) {
            this.ids.addAll(Arrays.asList(ids.split(",")));
            return this;
        }

        public Builder withNames(String names) {
            this.names.addAll(Arrays.asList(names.split(",")));
            return this;
        }

        public Builder withBiotypes(String biotypes) {
            this.biotypes.addAll(Arrays.asList(biotypes.split(",")));
            return this;
        }

        public Builder inRegions(String regions) {
            this.regions.addAll(Arrays.asList(regions.split(",")));
            return this;
        }

        public Builder withTranscriptBiotype(String transcriptBiotypes) {
            this.transcriptBiotypes.addAll(Arrays.asList(transcriptBiotypes.split(",")));
            return this;
        }

        public Builder withTranscriptXrefs(String transcriptXrefs) {
            this.transcriptXrefs.addAll(Arrays.asList(transcriptXrefs.split(",")));
            return this;
        }

        public Builder withTranscriptIds(String transcriptIds) {
            this.transcriptIds.addAll(Arrays.asList(transcriptIds.split(",")));
            return this;
        }

        public Builder withTranscriptNames(String transcriptNames) {
            this.transcriptNames.addAll(Arrays.asList(transcriptNames.split(",")));
            return this;
        }

        public Builder withTranscriptAnnotationFlags(String transcriptAnnotationFlags) {
            this.transcriptAnnotationFlags.addAll(Arrays.asList(transcriptAnnotationFlags.split(",")));
            return this;
        }

        public Builder withTranscriptTfbsNames(String transcriptTfbsNames) {
            this.transcriptTfbsNames.addAll(Arrays.asList(transcriptTfbsNames.split(",")));
            return this;
        }

        public Builder withAnnotationDiseaseIds(String annotationDiseaseIds) {
            this.annotationDiseaseIds.addAll(Arrays.asList(annotationDiseaseIds.split(",")));
            return this;
        }

        public Builder withAnnotationDiseaseNames(String annotationDiseaseNames) {
            this.annotationDiseaseNames.addAll(Arrays.asList(annotationDiseaseNames.split(",")));
            return this;
        }

        public Builder withAnnotationExpressionGenes(String annotationExpressionGenes) {
            this.annotationExpressionGenes.addAll(Arrays.asList(annotationExpressionGenes.split(",")));
            return this;
        }

        public Builder withAnnotationExpressionTissues(String annotationExpressionTissues) {
            this.annotationExpressionTissues.addAll(Arrays.asList(annotationExpressionTissues.split(",")));
            return this;
        }

        public Builder withAnnotationExpressionValues(String annotationExpressionValues) {
            this.annotationExpressionValues.addAll(Arrays.asList(annotationExpressionValues.split(",")));
            return this;
        }

        public Builder withAnnotationDrugNames(String annotationDrugNames) {
            this.annotationDrugNames.addAll(Arrays.asList(annotationDrugNames.split(",")));
            return this;
        }

        public Builder withAnnotationDrugGenes(String annotationDrugGenes) {
            this.annotationDrugGenes.addAll(Arrays.asList(annotationDrugGenes.split(",")));
            return this;
        }

        public GeneQuery build() {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.ids = this.ids;
            geneQuery.names = this.names;
            geneQuery.biotypes = this.biotypes;
            geneQuery.regions = this.regions;
            geneQuery.transcriptBiotypes = this.transcriptBiotypes;
            geneQuery.transcriptXrefs = this.transcriptXrefs;
            geneQuery.transcriptIds = this.transcriptIds;
            geneQuery.transcriptNames = this.transcriptNames;
            geneQuery.transcriptAnnotationFlags = this.transcriptAnnotationFlags;
            geneQuery.transcriptTfbsNames = this.transcriptTfbsNames;
            geneQuery.annotationDiseaseIds = this.annotationDiseaseIds;
            geneQuery.annotationDiseaseNames = this.annotationDiseaseNames;
            geneQuery.annotationExpressionGenes = this.annotationExpressionGenes;
            geneQuery.annotationExpressionTissues = this.annotationExpressionTissues;
            geneQuery.annotationExpressionValues = this.annotationExpressionValues;
            geneQuery.annotationDrugNames = this.annotationDrugNames;
            geneQuery.annotationDrugGenes = this.annotationDrugGenes;
            return geneQuery;
        }
    }
}
