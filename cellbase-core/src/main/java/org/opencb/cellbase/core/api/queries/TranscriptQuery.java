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

import java.util.List;
import java.util.Map;

public class TranscriptQuery extends AbstractQuery {

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

    public TranscriptQuery() {
    }

    public TranscriptQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private TranscriptQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setRegions(builder.regions);
        setTranscriptsBiotype(builder.transcriptsBiotype);
        setTranscriptsXrefs(builder.transcriptsXrefs);
        setTranscriptsId(builder.transcriptsId);
        setTranscriptsName(builder.transcriptsName);
        setTranscriptsAnnotationFlags(builder.transcriptsAnnotationFlags);
        setTranscriptsTfbsName(builder.transcriptsTfbsName);
    }

    @Override
    protected void validateQuery() throws QueryException {

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

    public LogicalList<String> getTranscriptsAnnotationFlags() {
        return transcriptsAnnotationFlags;
    }

    public TranscriptQuery setTranscriptsAnnotationFlags(LogicalList<String> transcriptsAnnotationFlags) {
        this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
        return this;
    }

    public LogicalList<String> getTranscriptsTfbsName() {
        return transcriptsTfbsName;
    }

    public TranscriptQuery setTranscriptsTfbsName(LogicalList<String> transcriptsTfbsName) {
        this.transcriptsTfbsName = transcriptsTfbsName;
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
        private List<Region> regions;
        private List<String> transcriptsBiotype;
        private List<String> transcriptsXrefs;
        private List<String> transcriptsId;
        private List<String> transcriptsName;
        private LogicalList<String> transcriptsAnnotationFlags;
        private LogicalList<String> transcriptsTfbsName;

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

        public TranscriptQuery build() {
            return new TranscriptQuery(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TranscriptQuery{");
        sb.append("regions=").append(regions);
        sb.append(", transcriptsBiotype=").append(transcriptsBiotype);
        sb.append(", transcriptsXrefs=").append(transcriptsXrefs);
        sb.append(", transcriptsId=").append(transcriptsId);
        sb.append(", transcriptsName=").append(transcriptsName);
        sb.append(", transcriptsAnnotationFlags=").append(transcriptsAnnotationFlags);
        sb.append(", transcriptsTfbsName=").append(transcriptsTfbsName);
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
}
