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

import org.apache.commons.collections.CollectionUtils;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.BioUtils;

import java.util.List;
import java.util.Map;

public class GeneQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;
    @QueryParameter(id = "name")
    private List<String> names;
    @QueryParameter(id = "biotype")
    private List<String> biotypes;

    @QueryParameter(id = "region")
    protected List<Region> regions;

    @QueryParameter(id = "transcripts.biotype")
    protected List<String> transcriptsBiotype;
    @QueryParameter(id = "transcripts.xrefs")
    protected List<String> transcriptsXrefs;
    @QueryParameter(id = "transcripts.id")
    protected List<String> transcriptsId;
    @QueryParameter(id = "transcripts.name")
    protected List<String> transcriptsName;

    @QueryParameter(id = "transcripts.annotationFlags")
    protected LogicalList<String> transcriptsAnnotationFlags;
    @QueryParameter(id = "transcripts.tfbs.id")
    protected LogicalList<String> transcriptsTfbsId;

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

    private GeneQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setIds(builder.ids);
        setNames(builder.names);
        setBiotypes(builder.biotypes);
        setRegions(builder.regions);
        setTranscriptsBiotype(builder.transcriptsBiotype);
        setTranscriptsXrefs(builder.transcriptsXrefs);
        setTranscriptsId(builder.transcriptsId);
        setTranscriptsName(builder.transcriptsName);
        setTranscriptsAnnotationFlags(builder.transcriptsAnnotationFlags);
        setTranscriptsTfbsId(builder.transcriptsTfbsName);
        setAnnotationDiseasesId(builder.annotationDiseasesId);
        setAnnotationDiseasesName(builder.annotationDiseasesName);
        setAnnotationExpressionGene(builder.annotationExpressionGene);
        setAnnotationExpressionTissue(builder.annotationExpressionTissue);
        setAnnotationExpressionValue(builder.annotationExpressionValue);
        setAnnotationDrugsName(builder.annotationDrugsName);
        setAnnotationDrugsGene(builder.annotationDrugsGene);
    }

    @Override
    protected void validateQuery() throws QueryException {
        if (CollectionUtils.isNotEmpty(biotypes)) {
            for (String biotype : biotypes) {
                if (!BioUtils.isValidBiotype(biotype)) {
                    throw new QueryException("Invalid biotype: '" + biotype + "'");
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GeneQuery{");
        sb.append("ids=").append(ids);
        sb.append(", names=").append(names);
        sb.append(", biotypes=").append(biotypes);
        sb.append(", regions=").append(regions);
        sb.append(", transcriptsBiotype=").append(transcriptsBiotype);
        sb.append(", transcriptsXrefs=").append(transcriptsXrefs);
        sb.append(", transcriptsId=").append(transcriptsId);
        sb.append(", transcriptsName=").append(transcriptsName);
        sb.append(", transcriptsAnnotationFlags=").append(transcriptsAnnotationFlags);
        sb.append(", transcriptsTfbsName=").append(transcriptsTfbsId);
        sb.append(", annotationDiseasesId=").append(annotationDiseasesId);
        sb.append(", annotationDiseasesName=").append(annotationDiseasesName);
        sb.append(", annotationExpressionGene=").append(annotationExpressionGene);
        sb.append(", annotationExpressionTissue=").append(annotationExpressionTissue);
        sb.append(", annotationExpressionValue=").append(annotationExpressionValue);
        sb.append(", annotationDrugsName=").append(annotationDrugsName);
        sb.append(", annotationDrugsGene=").append(annotationDrugsGene);
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

    public LogicalList<String> getTranscriptsTfbsId() {
        return transcriptsTfbsId;
    }

    public GeneQuery setTranscriptsTfbsId(LogicalList<String> transcriptsTfbsId) {
        this.transcriptsTfbsId = transcriptsTfbsId;
        return this;
    }

    public LogicalList<String> getAnnotationDiseasesId() {
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


    public static final class Builder {
        private List<String> includes;
        private List<String> excludes;
        private Integer limit;
        private Integer skip;
        private Boolean count;
        private String sort;
        private Order order;
        private String facet;
        private List<String> ids;
        private List<String> names;
        private List<String> biotypes;
        private List<Region> regions;
        private List<String> transcriptsBiotype;
        private List<String> transcriptsXrefs;
        private List<String> transcriptsId;
        private List<String> transcriptsName;
        private LogicalList<String> transcriptsAnnotationFlags;
        private LogicalList<String> transcriptsTfbsName;
        private LogicalList<String> annotationDiseasesId;
        private LogicalList<String> annotationDiseasesName;
        private LogicalList<String> annotationExpressionGene;
        private LogicalList<String> annotationExpressionTissue;
        private LogicalList<String> annotationExpressionValue;
        private LogicalList<String> annotationDrugsName;
        private LogicalList<String> annotationDrugsGene;

        public Builder() {
        }

        public Builder withIncludes(List<String> val) {
            includes = val;
            return this;
        }

        public Builder withExcludes(List<String> val) {
            excludes = val;
            return this;
        }

        public Builder withLimit(Integer val) {
            limit = val;
            return this;
        }

        public Builder withSkip(Integer val) {
            skip = val;
            return this;
        }

        public Builder withCount(Boolean val) {
            count = val;
            return this;
        }

        public Builder withSort(String val) {
            sort = val;
            return this;
        }

        public Builder withOrder(Order val) {
            order = val;
            return this;
        }

        public Builder withFacet(String val) {
            facet = val;
            return this;
        }

        public Builder withIds(List<String> val) {
            ids = val;
            return this;
        }

        public Builder withNames(List<String> val) {
            names = val;
            return this;
        }

        public Builder withBiotypes(List<String> val) {
            biotypes = val;
            return this;
        }

        public Builder withRegions(List<Region> val) {
            regions = val;
            return this;
        }

        public Builder withTranscriptsBiotype(List<String> val) {
            transcriptsBiotype = val;
            return this;
        }

        public Builder withTranscriptsXrefs(List<String> val) {
            transcriptsXrefs = val;
            return this;
        }

        public Builder withTranscriptsId(List<String> val) {
            transcriptsId = val;
            return this;
        }

        public Builder withTranscriptsName(List<String> val) {
            transcriptsName = val;
            return this;
        }

        public Builder withTranscriptsAnnotationFlags(LogicalList<String> val) {
            transcriptsAnnotationFlags = val;
            return this;
        }

        public Builder withTranscriptsTfbsName(LogicalList<String> val) {
            transcriptsTfbsName = val;
            return this;
        }

        public Builder withAnnotationDiseasesId(LogicalList<String> val) {
            annotationDiseasesId = val;
            return this;
        }

        public Builder withAnnotationDiseasesName(LogicalList<String> val) {
            annotationDiseasesName = val;
            return this;
        }

        public Builder withAnnotationExpressionGene(LogicalList<String> val) {
            annotationExpressionGene = val;
            return this;
        }

        public Builder withAnnotationExpressionTissue(LogicalList<String> val) {
            annotationExpressionTissue = val;
            return this;
        }

        public Builder withAnnotationExpressionValue(LogicalList<String> val) {
            annotationExpressionValue = val;
            return this;
        }

        public Builder withAnnotationDrugsName(LogicalList<String> val) {
            annotationDrugsName = val;
            return this;
        }

        public Builder withAnnotationDrugsGene(LogicalList<String> val) {
            annotationDrugsGene = val;
            return this;
        }

        public GeneQuery build() {
            return new GeneQuery(this);
        }
    }
}
