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

public class GeneQuery extends AbstractQuery {

    public static final int DEFAULT_LIMIT = 20;

    @QueryParameter(id = "id")
    private List<String> ids;
    @QueryParameter(id = "name")
    private List<String> names;
    @QueryParameter(id = "biotype")
    private List<String> biotypes;
    @QueryParameter(id = "region")
    private List<Region> regions;
    @QueryParameter(id = "transcripts.biotype")
    private List<String> transcriptsBiotype;
    @QueryParameter(id = "transcripts.xrefs")
    private List<String> transcriptsXrefs;
    @QueryParameter(id = "transcripts.id")
    private List<String> transcriptsId;
    @QueryParameter(id = "transcripts.name")
    private List<String> transcriptsName;

    @QueryParameter(id = "transcripts.annotationFlags")
    private LogicalList<String> transcriptsAnnotationFlags;
    @QueryParameter(id = "transcripts.tfbs.name")
    private LogicalList<String> transcriptsTfbsName;
    @QueryParameter(id = "annotation.diseases.id")
    private LogicalList<String> annotationDiseasesId;
    @QueryParameter(id = "annotation.diseases.name")
    private LogicalList<String> annotationDiseasesName;
    @QueryParameter(id = "annotation.expression.gene")
    private LogicalList<String> annotationExpressionGene;
    @QueryParameter(id = "annotation.expression.tissue")
    private LogicalList<String> annotationExpressionTissue;
    @QueryParameter(id = "annotation.expression.value")
    private LogicalList<String> annotationExpressionValue;
    @QueryParameter(id = "annotation.drugs.name")
    private LogicalList<String> annotationDrugsName;
    @QueryParameter(id = "annotation.drugs.gene")
    private LogicalList<String> annotationDrugsGene;

    public GeneQuery() {
    }

    public GeneQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GeneQuery{");
        sb.append("id=").append(ids);
        sb.append(", names=").append(names);
        sb.append(", biotypes=").append(biotypes);
        sb.append(", regions=").append(regions);
        sb.append(", transcriptsBiotype=").append(transcriptsBiotype);
        sb.append(", transcriptsXrefs=").append(transcriptsXrefs);
        sb.append(", transcriptsId=").append(transcriptsId);
        sb.append(", transcriptsName=").append(transcriptsName);
        sb.append(", transcriptsAnnotationFlags=").append(transcriptsAnnotationFlags);
        sb.append(", transcriptsTfbsName=").append(transcriptsTfbsName);
        sb.append(", annotationDiseasesId=").append(annotationDiseasesId);
        sb.append(", annotationDiseasesName=").append(annotationDiseasesName);
        sb.append(", annotationExpressionGene=").append(annotationExpressionGene);
        sb.append(", annotationExpressionTissue=").append(annotationExpressionTissue);
        sb.append(", annotationExpressionValue=").append(annotationExpressionValue);
        sb.append(", annotationDrugsName=").append(annotationDrugsName);
        sb.append(", annotationDrugsGene=").append(annotationDrugsGene);
        sb.append(", objectMapper=").append(objectMapper);
        sb.append(", limit=").append(limit);
        sb.append(", skip=").append(skip);
        sb.append(", count=").append(count);
        sb.append(", sort='").append(sort).append('\'');
        sb.append(", order=").append(order);
        sb.append(", facet='").append(facet).append('\'');
        sb.append(", includes=").append(includes);
        sb.append(", excludes=").append(excludes);
        sb.append('}');
        return sb.toString();
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

    public LogicalList<String> getTranscriptsAnnotationFlags() {
        return transcriptsAnnotationFlags;
    }

    public GeneQuery setTranscriptsAnnotationFlags(LogicalList<String> transcriptsAnnotationFlags) {
        this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
        return this;
    }

    public LogicalList<String> getTranscriptsTfbsName() {
        return transcriptsTfbsName;
    }

    public GeneQuery setTranscriptsTfbsName(LogicalList<String> transcriptsTfbsName) {
        this.transcriptsTfbsName = transcriptsTfbsName;
        return this;
    }

    public LogicalList getAnnotationDiseasesId() {
        return annotationDiseasesId;
    }

    public GeneQuery setAnnotationDiseasesId(LogicalList<String> annotationDiseasesId) {
        this.annotationDiseasesId = annotationDiseasesId;
        return this;
    }

    public LogicalList<String> getAnnotationDiseasesName() {
        return annotationDiseasesName;
    }

    public GeneQuery setAnnotationDiseasesName(LogicalList<String> annotationDiseasesName) {
        this.annotationDiseasesName = annotationDiseasesName;
        return this;
    }

    public LogicalList<String> getAnnotationExpressionGene() {
        return annotationExpressionGene;
    }

    public GeneQuery setAnnotationExpressionGene(LogicalList<String> annotationExpressionGene) {
        this.annotationExpressionGene = annotationExpressionGene;
        return this;
    }

    public LogicalList<String> getAnnotationExpressionTissue() {
        return annotationExpressionTissue;
    }

    public GeneQuery setAnnotationExpressionTissue(LogicalList<String> annotationExpressionTissue) {
        this.annotationExpressionTissue = annotationExpressionTissue;
        return this;
    }

    public LogicalList<String> getAnnotationExpressionValue() {
        return annotationExpressionValue;
    }

    public GeneQuery setAnnotationExpressionValue(LogicalList<String> annotationExpressionValue) {
        this.annotationExpressionValue = annotationExpressionValue;
        return this;
    }

    public LogicalList<String> getAnnotationDrugsName() {
        return annotationDrugsName;
    }

    public GeneQuery setAnnotationDrugsName(LogicalList<String> annotationDrugsName) {
        this.annotationDrugsName = annotationDrugsName;
        return this;
    }

    public LogicalList<String> getAnnotationDrugsGene() {
        return annotationDrugsGene;
    }

    public GeneQuery setAnnotationDrugsGene(LogicalList<String> annotationDrugsGene) {
        this.annotationDrugsGene = annotationDrugsGene;
        return this;
    }

    public static class Builder {
        private List<String> id;
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
            this.id = ids;
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
            geneQuery.ids = this.id;
            geneQuery.names = this.names;
            geneQuery.biotypes = this.biotypes;
            geneQuery.regions = this.regions;
            geneQuery.transcriptsBiotype = this.transcriptsBiotype;
            geneQuery.transcriptsXrefs = this.transcriptsXrefs;
            geneQuery.transcriptsId = this.transcriptsId;
            geneQuery.transcriptsName = this.transcriptsName;
//            geneQuery.transcriptsAnnotationFlags = this.transcriptsAnnotationFlags;
//            geneQuery.transcriptsTfbsName = this.transcriptsTfbsName;
//            geneQuery.annotationDiseasesId = this.annotationDiseasesId;
//            geneQuery.annotationDiseasesName = this.annotationDiseasesName;
//            geneQuery.annotationExpressionGene = this.annotationExpressionGene;
//            geneQuery.annotationExpressionTissue = this.annotationExpressionTissue;
//            geneQuery.annotationExpressionValue = this.annotationExpressionValue;
//            geneQuery.annotationDrugsName = this.annotationDrugsName;
//            geneQuery.annotationDrugsGene = this.annotationDrugsGene;
            return geneQuery;
        }
    }
}
