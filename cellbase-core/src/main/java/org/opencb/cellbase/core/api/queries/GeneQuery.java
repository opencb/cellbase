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

package org.opencb.cellbase.core.api.queries;

import org.opencb.biodata.models.core.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneQuery extends FeatureQuery {

    private List<String> ids;
    private List<String> names;
    private List<String> biotypes;
    private List<Region> regions;
    private List<String> transcriptsBiotype;
    private List<String> transcriptsXrefs;
    private List<String> transcriptsId;
    private List<String> transcriptsName;
    private List<String> transcriptsAnnotationFlags;
    private List<String> transcriptsTfbsName;
    private List<String> annotationDiseasesId;
    private List<String> annotationDiseasesName;
    private List<String> annotationExpressionGene;
    private List<String> annotationExpressionTissue;
    private List<String> annotationExpressionValue;
    private List<String> annotationDrugsName;
    private List<String> annotationDrugsGene;

    public GeneQuery() {
    }

    public GeneQuery(Map<String, String> params) {
        super(params);
    }

    public List<String> getIds() {
        return ids;
    }

    public GeneQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public GeneQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getBiotypes() {
        return biotypes;
    }

    public GeneQuery setBiotypes(List<String> biotypes) {
        this.biotypes = biotypes;
        return this;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public GeneQuery setRegions(List<Region> regions) {
        this.regions = new ArrayList();
        this.regions = regions;
        return this;
    }

    public List<String> getTranscriptsBiotype() {
        return transcriptsBiotype;
    }

    public GeneQuery setTranscriptsBiotype(List<String> transcriptsBiotype) {
        this.transcriptsBiotype = transcriptsBiotype;
        return this;
    }

    public List<String> getTranscriptsXrefs() {
        return transcriptsXrefs;
    }

    public GeneQuery setTranscriptsXrefs(List<String> transcriptsXrefs) {
        this.transcriptsXrefs = transcriptsXrefs;
        return this;
    }

    public List<String> getTranscriptsId() {
        return transcriptsId;
    }

    public GeneQuery setTranscriptsId(List<String> transcriptsId) {
        this.transcriptsId = transcriptsId;
        return this;
    }

    public List<String> getTranscriptsName() {
        return transcriptsName;
    }

    public GeneQuery setTranscriptsName(List<String> transcriptsName) {
        this.transcriptsName = transcriptsName;
        return this;
    }

    public List<String> getTranscriptsAnnotationFlags() {
        return transcriptsAnnotationFlags;
    }

    public GeneQuery setTranscriptsAnnotationFlags(List<String> transcriptsAnnotationFlags) {
        this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
        return this;
    }

    public List<String> getTranscriptsTfbsName() {
        return transcriptsTfbsName;
    }

    public GeneQuery setTranscriptsTfbsName(List<String> transcriptsTfbsName) {
        this.transcriptsTfbsName = transcriptsTfbsName;
        return this;
    }

    public List<String> getAnnotationDiseasesId() {
        return annotationDiseasesId;
    }

    public GeneQuery setAnnotationDiseasesId(List<String> annotationDiseasesId) {
        this.annotationDiseasesId = annotationDiseasesId;
        return this;
    }

    public List<String> getAnnotationDiseasesName() {
        return annotationDiseasesName;
    }

    public GeneQuery setAnnotationDiseasesName(List<String> annotationDiseasesName) {
        this.annotationDiseasesName = annotationDiseasesName;
        return this;
    }

    public List<String> getAnnotationExpressionGene() {
        return annotationExpressionGene;
    }

    public GeneQuery setAnnotationExpressionGene(List<String> annotationExpressionGene) {
        this.annotationExpressionGene = annotationExpressionGene;
        return this;
    }

    public List<String> getAnnotationExpressionTissue() {
        return annotationExpressionTissue;
    }

    public GeneQuery setAnnotationExpressionTissue(List<String> annotationExpressionTissue) {
        this.annotationExpressionTissue = annotationExpressionTissue;
        return this;
    }

    public List<String> getAnnotationExpressionValue() {
        return annotationExpressionValue;
    }

    public GeneQuery setAnnotationExpressionValue(List<String> annotationExpressionValue) {
        this.annotationExpressionValue = annotationExpressionValue;
        return this;
    }

    public List<String> getAnnotationDrugsName() {
        return annotationDrugsName;
    }

    public GeneQuery setAnnotationDrugsName(List<String> annotationDrugsName) {
        this.annotationDrugsName = annotationDrugsName;
        return this;
    }

    public List<String> getAnnotationDrugsGene() {
        return annotationDrugsGene;
    }

    public GeneQuery setAnnotationDrugsGene(List<String> annotationDrugsGene) {
        this.annotationDrugsGene = annotationDrugsGene;
        return this;
    }

    @Override
    public String toString() {
        return "GeneQuery{"
                + "ids=" + ids
                + ", names=" + names
                + ", biotypes=" + biotypes
                + ", regions=" + regions
                + ", transcriptsBiotype=" + transcriptsBiotype
                + ", transcriptsXrefs=" + transcriptsXrefs
                + ", transcriptsId=" + transcriptsId
                + ", transcriptsName=" + transcriptsName
                + ", transcriptsAnnotationFlags=" + transcriptsAnnotationFlags
                + ", transcriptsTfbsName=" + transcriptsTfbsName
                + ", annotationDiseasesId=" + annotationDiseasesId
                + ", annotationDiseasesName=" + annotationDiseasesName
                + ", annotationExpressionGene=" + annotationExpressionGene
                + ", annotationExpressionTissue=" + annotationExpressionTissue
                + ", annotationExpressionValue=" + annotationExpressionValue
                + ", annotationDrugsName=" + annotationDrugsName
                + ", annotationDrugsGene=" + annotationDrugsGene
                + '}';
    }

    public static class Builder {
        private List<String> ids;
        private List<String> names;
        private List<String> biotypes;
        private List<Region> regions;
        private List<String> transcriptsBiotype;
        private List<String> transcriptsXrefs;
        private List<String> transcriptsId;
        private List<String> transcriptsName;
        private List<String> transcriptsAnnotationFlags;
        private List<String> transcriptsTfbsName;
        private List<String> annotationDiseasesId;
        private List<String> annotationDiseasesName;
        private List<String> annotationExpressionGene;
        private List<String> annotationExpressionTissue;
        private List<String> annotationExpressionValue;
        private List<String> annotationDrugsName;
        private List<String> annotationDrugsGene;

        public Builder() {
        }

        public Builder withIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public Builder withNames(List<String> names) {
            this.names = names;
            return this;
        }

        public Builder withBiotypes(List<String> biotypes) {
            this.biotypes = biotypes;
            return this;
        }

        public Builder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public Builder withTranscriptsBiotype(List<String> transcriptsBiotype) {
            this.transcriptsBiotype = transcriptsBiotype;
            return this;
        }

        public Builder withTranscriptsXrefs(List<String> transcriptsXrefs) {
            this.transcriptsXrefs = transcriptsXrefs;
            return this;
        }

        public Builder withTranscriptsId(List<String> transcriptsId) {
            this.transcriptsId = transcriptsId;
            return this;
        }

        public Builder withTranscriptsName(List<String> transcriptsName) {
            this.transcriptsName = transcriptsName;
            return this;
        }

        public Builder withTranscriptsAnnotationFlags(List<String> transcriptsAnnotationFlags) {
            this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
            return this;
        }

        public Builder withTranscriptsTfbsName(List<String> transcriptsTfbsName) {
            this.transcriptsTfbsName = transcriptsTfbsName;
            return this;
        }

        public Builder withAnnotationDiseasesId(List<String> annotationDiseasesId) {
            this.annotationDiseasesId = annotationDiseasesId;
            return this;
        }

        public Builder withAnnotationDiseasesName(List<String> annotationDiseasesName) {
            this.annotationDiseasesName = annotationDiseasesName;
            return this;
        }

        public Builder withAnnotationExpressionGene(List<String> annotationExpressionGene) {
            this.annotationExpressionGene = annotationExpressionGene;
            return this;
        }

        public Builder withAnnotationExpressionTissue(List<String> annotationExpressionTissue) {
            this.annotationExpressionTissue = annotationExpressionTissue;
            return this;
        }

        public Builder withAnnotationExpressionValue(List<String> annotationExpressionValue) {
            this.annotationExpressionValue = annotationExpressionValue;
            return this;
        }

        public Builder withAnnotationDrugsName(List<String> annotationDrugsName) {
            this.annotationDrugsName = annotationDrugsName;
            return this;
        }

        public Builder withAnnotationDrugsGene(List<String> annotationDrugsGene) {
            this.annotationDrugsGene = annotationDrugsGene;
            return this;
        }

        public GeneQuery build() {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.ids = this.ids;
            geneQuery.names = this.names;
            geneQuery.biotypes = this.biotypes;
            geneQuery.regions = this.regions;
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
            geneQuery.annotationExpressionValue = this.annotationExpressionValue;
            geneQuery.annotationDrugsName = this.annotationDrugsName;
            geneQuery.annotationDrugsGene = this.annotationDrugsGene;
            return geneQuery;
        }
    }
}
