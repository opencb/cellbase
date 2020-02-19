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

package org.opencb.cellbase.lib.queries;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

public class GeneQuery {

    private QueryOptions queryOptions;
    private String id;
    private String name;
    private String biotype;
    private String region;
    private String transcriptsBiotype;
    private String transcriptsXrefs;
    private String transcriptsId;
    private String transcriptsName;
    private String transcriptsAnnotationFlags;
    private String transcriptsTfbsName;
    private String annotationDiseasesId;
    private String annotationDiseasesName;
    private String annotationExpressionGene;
    private String annotationExpressionTissue;
    private String annotationexpressionValue;
    private String annotationDrugsName;
    private String annotationDrugsGene;

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
                addQueryOption(QueryOptions.EXCLUDE, value);
            } else if (QueryOptions.INCLUDE.equals(key)) {
                addQueryOption(QueryOptions.INCLUDE, value);
            } else {
                if ("id".equals(key)) {
                    setId(value);
                } else if ("name".equals(key)) {
                    setName(value);
                } else if ("biotype".equals(key)) {
                    setBiotype(value);
                } else if ("region".equals(key)) {
                    setRegion(value);
                } else if ("transcriptsBiotype".equals(key)) {
                    setTranscriptsBiotype(value);
                } else if ("transcriptsXrefs".equals(key)) {
                    setTranscriptsXrefs(value);
                } else if ("transcriptsId".equals(key)) {
                    setTranscriptsId(value);
                } else if ("transcriptsName".equals(key)) {
                    setTranscriptsName(value);
                } else if ("transcriptsAnnotationFlags".equals(key)) {
                    setTranscriptsAnnotationFlags(value);
                } else if ("transcriptsTfbsName".equals(key)) {
                    setTranscriptsTfbsName(value);
                } else if ("annotationDiseasesId".equals(key)) {
                    setAnnotationDiseasesId(value);
                } else if ("annotationDiseasesName".equals(key)) {
                    setAnnotationDiseasesName(value);
                } else if ("annotationExpressionGene".equals(key)) {
                    setAnnotationExpressionGene(value);
                } else if ("annotationExpressionTissue".equals(key)) {
                    setAnnotationExpressionTissue(value);
                } else if ("annotationexpressionValue".equals(key)) {
                    setAnnotationexpressionValue(value);
                } else if ("annotationDrugsName".equals(key)) {
                    setAnnotationDrugsName(value);
                } else if ("annotationDrugsGene".equals(key)) {
                    setAnnotationDrugsGene(value);
                } else {
                    throw new CellbaseException("invalid parameter " + key);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public GeneQuery setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public GeneQuery setName(String name) {
        this.name = name;
        return this;
    }

    public String getBiotype() {
        return biotype;
    }

    public GeneQuery setBiotype(String biotype) {
        this.biotype = biotype;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public GeneQuery setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getTranscriptsBiotype() {
        return transcriptsBiotype;
    }

    public GeneQuery setTranscriptsBiotype(String transcriptsBiotype) {
        this.transcriptsBiotype = transcriptsBiotype;
        return this;
    }

    public String getTranscriptsXrefs() {
        return transcriptsXrefs;
    }

    public GeneQuery setTranscriptsXrefs(String transcriptsXrefs) {
        this.transcriptsXrefs = transcriptsXrefs;
        return this;
    }

    public String getTranscriptsId() {
        return transcriptsId;
    }

    public GeneQuery setTranscriptsId(String transcriptsId) {
        this.transcriptsId = transcriptsId;
        return this;
    }

    public String getTranscriptsName() {
        return transcriptsName;
    }

    public GeneQuery setTranscriptsName(String transcriptsName) {
        this.transcriptsName = transcriptsName;
        return this;
    }

    public String getTranscriptsAnnotationFlags() {
        return transcriptsAnnotationFlags;
    }

    public GeneQuery setTranscriptsAnnotationFlags(String transcriptsAnnotationFlags) {
        this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
        return this;
    }

    public String getTranscriptsTfbsName() {
        return transcriptsTfbsName;
    }

    public GeneQuery setTranscriptsTfbsName(String transcriptsTfbsName) {
        this.transcriptsTfbsName = transcriptsTfbsName;
        return this;
    }

    public String getAnnotationDiseasesId() {
        return annotationDiseasesId;
    }

    public GeneQuery setAnnotationDiseasesId(String annotationDiseasesId) {
        this.annotationDiseasesId = annotationDiseasesId;
        return this;
    }

    public String getAnnotationDiseasesName() {
        return annotationDiseasesName;
    }

    public GeneQuery setAnnotationDiseasesName(String annotationDiseasesName) {
        this.annotationDiseasesName = annotationDiseasesName;
        return this;
    }

    public String getAnnotationExpressionGene() {
        return annotationExpressionGene;
    }

    public GeneQuery setAnnotationExpressionGene(String annotationExpressionGene) {
        this.annotationExpressionGene = annotationExpressionGene;
        return this;
    }

    public String getAnnotationExpressionTissue() {
        return annotationExpressionTissue;
    }

    public GeneQuery setAnnotationExpressionTissue(String annotationExpressionTissue) {
        this.annotationExpressionTissue = annotationExpressionTissue;
        return this;
    }

    public String getAnnotationexpressionValue() {
        return annotationexpressionValue;
    }

    public GeneQuery setAnnotationexpressionValue(String annotationexpressionValue) {
        this.annotationexpressionValue = annotationexpressionValue;
        return this;
    }

    public String getAnnotationDrugsName() {
        return annotationDrugsName;
    }

    public GeneQuery setAnnotationDrugsName(String annotationDrugsName) {
        this.annotationDrugsName = annotationDrugsName;
        return this;
    }

    public String getAnnotationDrugsGene() {
        return annotationDrugsGene;
    }

    public GeneQuery setAnnotationDrugsGene(String annotationDrugsGene) {
        this.annotationDrugsGene = annotationDrugsGene;
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

    public Query getQuery() {
        Query query = new Query();
        if (StringUtils.isNotEmpty(id)) {
            query.put("id", id);
        }
        if (StringUtils.isNotEmpty(name)) {
            query.put("name", name);
        }
        if (StringUtils.isNotEmpty(biotype)) {
            query.put("biotype", biotype);
        }
        if (StringUtils.isNotEmpty(region)) {
            query.put("region", region);
        }
        if (StringUtils.isNotEmpty(transcriptsBiotype)) {
            query.put("transcripts.biotype", transcriptsBiotype);
        }
        if (StringUtils.isNotEmpty(transcriptsXrefs)) {
            query.put("transcripts.xrefs", transcriptsXrefs);
        }
        if (StringUtils.isNotEmpty(transcriptsId)) {
            query.put("transcripts.id", transcriptsId);
        }
        if (StringUtils.isNotEmpty(transcriptsName)) {
            query.put("transcripts.name", transcriptsName);
        }
        if (StringUtils.isNotEmpty(transcriptsAnnotationFlags)) {
            query.put("transcripts.annotationFlags", transcriptsAnnotationFlags);
        }
        if (StringUtils.isNotEmpty(transcriptsTfbsName)) {
            query.put("transcripts.tfbs.name", transcriptsTfbsName);
        }
        if (StringUtils.isNotEmpty(annotationDiseasesId)) {
            query.put("annotation.diseases.id", annotationDiseasesId);
        }
        if (StringUtils.isNotEmpty(annotationDiseasesName)) {
            query.put("annotation.diseases.name", annotationDiseasesName);
        }
        if (StringUtils.isNotEmpty(annotationExpressionGene)) {
            query.put("annotation.expression.gene", annotationExpressionGene);
        }
        if (StringUtils.isNotEmpty(annotationExpressionTissue)) {
            query.put("annotation.expression.tissue", annotationExpressionTissue);
        }
        if (StringUtils.isNotEmpty(annotationexpressionValue)) {
            query.put("annotation.expression.value", annotationexpressionValue);
        }
        if (StringUtils.isNotEmpty(annotationDrugsName)) {
            query.put("annotation.drugs.name", annotationDrugsName);
        }
        if (StringUtils.isNotEmpty(annotationDrugsGene)) {
            query.put("annotation.drugs.gene", annotationDrugsGene);
        }
        return query;
    }

    public static class Builder {
        private String id;
        private String name;
        private String biotype;
        private String region;
        private String transcriptsBiotype;
        private String transcriptsXrefs;
        private String transcriptsId;
        private String transcriptsName;
        private String transcriptsAnnotationFlags;
        private String transcriptsTfbsName;
        private String annotationDiseasesId;
        private String annotationDiseasesName;
        private String annotationExpressionGene;
        private String annotationExpressionTissue;
        private String annotationexpressionValue;
        private String annotationDrugsName;
        private String annotationDrugsGene;

        public Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withBiotype(String biotype) {
            this.biotype = biotype;
            return this;
        }

        public Builder inRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder withTranscriptsBiotype(String transcriptsBiotype) {
            this.transcriptsBiotype = transcriptsBiotype;
            return this;
        }

        public Builder withTranscriptsXrefs(String transcriptsXrefs) {
            this.transcriptsXrefs = transcriptsXrefs;
            return this;
        }

        public Builder withTranscriptsId(String transcriptsId) {
            this.transcriptsId = transcriptsId;
            return this;
        }

        public Builder withTranscriptsName(String transcriptsName) {
            this.transcriptsName = transcriptsName;
            return this;
        }

        public Builder withTranscriptsAnnotationFlags(String transcriptsAnnotationFlags) {
            this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
            return this;
        }

        public Builder withTranscriptsTfbsName(String transcriptsTfbsName) {
            this.transcriptsTfbsName = transcriptsTfbsName;
            return this;
        }

        public Builder withAnnotationDiseasesId(String annotationDiseasesId) {
            this.annotationDiseasesId = annotationDiseasesId;
            return this;
        }

        public Builder withAnnotationDiseasesName(String annotationDiseasesName) {
            this.annotationDiseasesName = annotationDiseasesName;
            return this;
        }

        public Builder withAnnotationExpressionGene(String annotationExpressionGene) {
            this.annotationExpressionGene = annotationExpressionGene;
            return this;
        }

        public Builder withAnnotationExpressionTissue(String annotationExpressionTissue) {
            this.annotationExpressionTissue = annotationExpressionTissue;
            return this;
        }

        public Builder withAnnotationexpressionValue(String annotationexpressionValue) {
            this.annotationexpressionValue = annotationexpressionValue;
            return this;
        }

        public Builder withAnnotationDrugsName(String annotationDrugsName) {
            this.annotationDrugsName = annotationDrugsName;
            return this;
        }

        public Builder withAnnotationDrugsGene(String annotationDrugsGene) {
            this.annotationDrugsGene = annotationDrugsGene;
            return this;
        }

        public GeneQuery build() {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.id = this.id;
            geneQuery.name = this.name;
            geneQuery.biotype = this.biotype;
            geneQuery.region = this.region;
            geneQuery.transcriptsBiotype = this.transcriptsBiotype;
            geneQuery.transcriptsXrefs = this.transcriptsXrefs;
            geneQuery.transcriptsId = this.transcriptsId;
            geneQuery.transcriptsName = this.transcriptsName;
            geneQuery.transcriptsAnnotationFlags = this.transcriptsAnnotationFlags;
            geneQuery.transcriptsTfbsName = this.transcriptsTfbsName;
            geneQuery.annotationDiseasesId = this.annotationDiseasesId;
            geneQuery.annotationDiseasesName = this.annotationDiseasesName;
            geneQuery.annotationExpressionGene = this.annotationExpressionGene;
            geneQuery.annotationExpressionTissue = this.annotationExpressionTissue;
            geneQuery.annotationexpressionValue = this.annotationexpressionValue;
            geneQuery.annotationDrugsName = this.annotationDrugsName;
            geneQuery.annotationDrugsGene = this.annotationDrugsGene;
            return geneQuery;
        }
    }
}
