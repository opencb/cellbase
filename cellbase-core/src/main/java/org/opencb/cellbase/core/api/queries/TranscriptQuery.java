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
import org.opencb.cellbase.core.ParamConstants;

import java.util.List;
import java.util.Map;

public class TranscriptQuery extends AbstractQuery {

    @QueryParameter(id = "region")
    private List<Region> regions;

    @QueryParameter(id = "transcripts.biotype", alias = {"biotype"})
    private List<String> transcriptsBiotype;
    @QueryParameter(id = "transcripts.xrefs.id", alias = {"xref", "xrefs"})
    private List<String> transcriptsXrefs;
    @QueryParameter(id = "transcripts.id", alias = {"id"})
    private List<String> transcriptsId;
    @QueryParameter(id = "transcripts.name", alias = {"name"})
    private List<String> transcriptsName;
    @QueryParameter(id = "transcripts.supportLevel", alias = {"supportLevel"}, allowedValues = {"1", "2", "3", "4", "5", "NA"})
    private List<String> transcriptSupportLevels;

    @QueryParameter(id = "transcripts.annotationFlags", alias = {"annotationFlags"})
    private LogicalList<String> transcriptsAnnotationFlags;
    @QueryParameter(id = "transcripts.tfbs.id", alias = {ParamConstants.TRANSCRIPT_TFBS_IDS_PARAM, "tfbs.id", "transcriptsTfbsId",
            "tfbsId"})
    private LogicalList<String> transcriptsTfbsId;
    @QueryParameter(id = "transcripts.tfbs.pfmId", alias = {ParamConstants.TRANSCRIPT_TFBS_PFMIDS_PARAM, "transcriptsTfbsPfmId"})
    private LogicalList<String> transcriptsTfbsPfmId;
    @QueryParameter(id = "transcripts.tfbs.transcriptionFactors", alias = {ParamConstants.TRANSCRIPT_TRANSCRIPTION_FACTORS_PARAM,
            "transcriptsTfbsTranscriptionFactors"})
    private LogicalList<String> transcriptsTfbsTranscriptionFactors;
    @QueryParameter(id = "transcripts.annotation.ontologies.id", alias = {ParamConstants.ONTOLOGY_IDS_PARAM,
            "transcriptAnnotationOntologiesId"})
    private LogicalList<String> transcriptsAnnotationOntologiesId;


    public TranscriptQuery() {
    }

    public TranscriptQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TranscriptQuery{");
        sb.append("regions=").append(regions);
        sb.append(", transcriptsBiotype=").append(transcriptsBiotype);
        sb.append(", transcriptsXrefs=").append(transcriptsXrefs);
        sb.append(", transcriptsId=").append(transcriptsId);
        sb.append(", transcriptsName=").append(transcriptsName);
        sb.append(", transcriptSupportLevels=").append(transcriptSupportLevels);
        sb.append(", transcriptsAnnotationFlags=").append(transcriptsAnnotationFlags);
        sb.append(", transcriptsTfbsId=").append(transcriptsTfbsId);
        sb.append(", transcriptsTfbsPfmId=").append(transcriptsTfbsPfmId);
        sb.append(", transcriptsTfbsTranscriptionFactors=").append(transcriptsTfbsTranscriptionFactors);
        sb.append(", transcriptsAnnotationOntologiesId=").append(transcriptsAnnotationOntologiesId);
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

    public List<Region> getRegions() {
        return regions;
    }

    public TranscriptQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public List<String> getTranscriptsBiotype() {
        return transcriptsBiotype;
    }

    public TranscriptQuery setTranscriptsBiotype(List<String> transcriptsBiotype) {
        this.transcriptsBiotype = transcriptsBiotype;
        return this;
    }

    public List<String> getTranscriptsXrefs() {
        return transcriptsXrefs;
    }

    public TranscriptQuery setTranscriptsXrefs(List<String> transcriptsXrefs) {
        this.transcriptsXrefs = transcriptsXrefs;
        return this;
    }

    public List<String> getTranscriptsId() {
        return transcriptsId;
    }

    public TranscriptQuery setTranscriptsId(List<String> transcriptsId) {
        this.transcriptsId = transcriptsId;
        return this;
    }

    public List<String> getTranscriptsName() {
        return transcriptsName;
    }

    public TranscriptQuery setTranscriptsName(List<String> transcriptsName) {
        this.transcriptsName = transcriptsName;
        return this;
    }

    public List<String> getTranscriptSupportLevels() {
        return transcriptSupportLevels;
    }

    public TranscriptQuery setTranscriptSupportLevels(List<String> transcriptSupportLevels) {
        this.transcriptSupportLevels = transcriptSupportLevels;
        return this;
    }

    public LogicalList<String> getTranscriptsAnnotationFlags() {
        return transcriptsAnnotationFlags;
    }

    public TranscriptQuery setTranscriptsAnnotationFlags(LogicalList<String> transcriptsAnnotationFlags) {
        this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
        return this;
    }

    public LogicalList<String> getTranscriptsTfbsId() {
        return transcriptsTfbsId;
    }

    public TranscriptQuery setTranscriptsTfbsId(LogicalList<String> transcriptsTfbsId) {
        this.transcriptsTfbsId = transcriptsTfbsId;
        return this;
    }

    public LogicalList<String> getTranscriptsTfbsPfmId() {
        return transcriptsTfbsPfmId;
    }

    public TranscriptQuery setTranscriptsTfbsPfmId(LogicalList<String> transcriptsTfbsPfmId) {
        this.transcriptsTfbsPfmId = transcriptsTfbsPfmId;
        return this;
    }

    public LogicalList<String> getTranscriptsTfbsTranscriptionFactors() {
        return transcriptsTfbsTranscriptionFactors;
    }

    public TranscriptQuery setTranscriptsTfbsTranscriptionFactors(LogicalList<String> transcriptsTfbsTranscriptionFactors) {
        this.transcriptsTfbsTranscriptionFactors = transcriptsTfbsTranscriptionFactors;
        return this;
    }

    public LogicalList<String> getTranscriptsAnnotationOntologiesId() {
        return transcriptsAnnotationOntologiesId;
    }

    public TranscriptQuery setTranscriptsAnnotationOntologiesId(LogicalList<String> transcriptsAnnotationOntologiesId) {
        this.transcriptsAnnotationOntologiesId = transcriptsAnnotationOntologiesId;
        return this;
    }

    public static final class TranscriptQueryBuilder {
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<Region> regions;
        private List<String> transcriptsBiotype;
        private List<String> transcriptsXrefs;
        private List<String> transcriptsId;
        private List<String> transcriptsName;
        private List<String> transcriptSupportLevels;
        private LogicalList<String> transcriptsAnnotationFlags;
        private LogicalList<String> transcriptsTfbsId;
        private LogicalList<String> transcriptsTfbsPfmId;
        private LogicalList<String> transcriptsTfbsTranscriptionFactors;
        private LogicalList<String> transcriptsAnnotationOntologiesId;

        private TranscriptQueryBuilder() {
        }

        public static TranscriptQueryBuilder aTranscriptQuery() {
            return new TranscriptQueryBuilder();
        }

        public TranscriptQueryBuilder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsBiotype(List<String> transcriptsBiotype) {
            this.transcriptsBiotype = transcriptsBiotype;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsXrefs(List<String> transcriptsXrefs) {
            this.transcriptsXrefs = transcriptsXrefs;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsId(List<String> transcriptsId) {
            this.transcriptsId = transcriptsId;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsName(List<String> transcriptsName) {
            this.transcriptsName = transcriptsName;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptSupportLevels(List<String> transcriptSupportLevels) {
            this.transcriptSupportLevels = transcriptSupportLevels;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsAnnotationFlags(LogicalList<String> transcriptsAnnotationFlags) {
            this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsTfbsId(LogicalList<String> transcriptsTfbsId) {
            this.transcriptsTfbsId = transcriptsTfbsId;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsTfbsPfmId(LogicalList<String> transcriptsTfbsPfmId) {
            this.transcriptsTfbsPfmId = transcriptsTfbsPfmId;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsTfbsTranscriptionFactors(LogicalList<String> transcriptsTfbsTranscriptionFactors) {
            this.transcriptsTfbsTranscriptionFactors = transcriptsTfbsTranscriptionFactors;
            return this;
        }

        public TranscriptQueryBuilder withTranscriptsAnnotationOntologiesId(LogicalList<String> transcriptsAnnotationOntologiesId) {
            this.transcriptsAnnotationOntologiesId = transcriptsAnnotationOntologiesId;
            return this;
        }

        public TranscriptQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public TranscriptQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public TranscriptQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public TranscriptQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public TranscriptQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public TranscriptQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public TranscriptQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public TranscriptQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public TranscriptQuery build() {
            TranscriptQuery transcriptQuery = new TranscriptQuery();
            transcriptQuery.setRegions(regions);
            transcriptQuery.setTranscriptsBiotype(transcriptsBiotype);
            transcriptQuery.setTranscriptsXrefs(transcriptsXrefs);
            transcriptQuery.setTranscriptsId(transcriptsId);
            transcriptQuery.setTranscriptsName(transcriptsName);
            transcriptQuery.setTranscriptSupportLevels(transcriptSupportLevels);
            transcriptQuery.setTranscriptsAnnotationFlags(transcriptsAnnotationFlags);
            transcriptQuery.setTranscriptsTfbsId(transcriptsTfbsId);
            transcriptQuery.setTranscriptsTfbsPfmId(transcriptsTfbsPfmId);
            transcriptQuery.setTranscriptsTfbsTranscriptionFactors(transcriptsTfbsTranscriptionFactors);
            transcriptQuery.setTranscriptsAnnotationOntologiesId(transcriptsAnnotationOntologiesId);
            transcriptQuery.setLimit(limit);
            transcriptQuery.setSkip(skip);
            transcriptQuery.setCount(count);
            transcriptQuery.setSort(sort);
            transcriptQuery.setOrder(order);
            transcriptQuery.setFacet(facet);
            transcriptQuery.setIncludes(includes);
            transcriptQuery.setExcludes(excludes);
            return transcriptQuery;
        }
    }
}
