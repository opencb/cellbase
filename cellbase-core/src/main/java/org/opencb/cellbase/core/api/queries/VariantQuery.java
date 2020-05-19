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

public class VariantQuery extends AbstractQuery {

    @QueryParameter(id = "chromosome")
    protected String chromosome;
    @QueryParameter(id = "start")
    protected String start;
    @QueryParameter(id = "end")
    protected String end;
    @QueryParameter(id = "region")
    protected List<Region> regions;

    @QueryParameter(id = "reference")
    protected String reference;
    @QueryParameter(id = "alternate")
    protected String alternate;
    @QueryParameter(id = "id")
    protected String id;

    @QueryParameter(id = "sv.ciStartLeft")
    protected String ciStartLeft;
    @QueryParameter(id = "sv.ciStartRight")
    protected String ciStartRight;
    @QueryParameter(id = "sv.ciEndLeft")
    protected String ciEndLeft;
    @QueryParameter(id = "sv.ciEndRight")
    protected String ciEndRight;

    @QueryParameter(id = "sv.type")
    protected String svType;
    @QueryParameter(id = "type")
    protected String type;

    @QueryParameter(id = "annotation.consequenceTypes.sequenceOntologyTerms.name")
    protected String consequenceType;

    public VariantQuery() {
    }

    public VariantQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private VariantQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setChromosome(builder.chromosome);
        setStart(builder.start);
        setEnd(builder.end);
        setRegions(builder.regions);
        setReference(builder.reference);
        setAlternate(builder.alternate);
        setId(builder.id);
        setCiStartLeft(builder.ciStartLeft);
        setCiStartRight(builder.ciStartRight);
        setCiEndLeft(builder.ciEndLeft);
        setCiEndRight(builder.ciEndRight);
        setSvType(builder.svType);
        setType(builder.type);
        setConsequenceType(builder.consequenceType);
    }

    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VariantQuery{");
        sb.append("chromosome='").append(chromosome).append('\'');
        sb.append(", start='").append(start).append('\'');
        sb.append(", end='").append(end).append('\'');
        sb.append(", regions=").append(regions);
        sb.append(", reference='").append(reference).append('\'');
        sb.append(", alternate='").append(alternate).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", ciStartLeft='").append(ciStartLeft).append('\'');
        sb.append(", ciStartRight='").append(ciStartRight).append('\'');
        sb.append(", ciEndLeft='").append(ciEndLeft).append('\'');
        sb.append(", ciEndRight='").append(ciEndRight).append('\'');
        sb.append(", svType='").append(svType).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", consequenceType='").append(consequenceType).append('\'');
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

    public String getChromosome() {
        return chromosome;
    }

    public VariantQuery setChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

    public String getStart() {
        return start;
    }

    public VariantQuery setStart(String start) {
        this.start = start;
        return this;
    }

    public String getEnd() {
        return end;
    }

    public VariantQuery setEnd(String end) {
        this.end = end;
        return this;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public VariantQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public VariantQuery setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public String getAlternate() {
        return alternate;
    }

    public VariantQuery setAlternate(String alternate) {
        this.alternate = alternate;
        return this;
    }

    public String getId() {
        return id;
    }

    public VariantQuery setId(String id) {
        this.id = id;
        return this;
    }

    public String getCiStartLeft() {
        return ciStartLeft;
    }

    public VariantQuery setCiStartLeft(String ciStartLeft) {
        this.ciStartLeft = ciStartLeft;
        return this;
    }

    public String getCiStartRight() {
        return ciStartRight;
    }

    public VariantQuery setCiStartRight(String ciStartRight) {
        this.ciStartRight = ciStartRight;
        return this;
    }

    public String getCiEndLeft() {
        return ciEndLeft;
    }

    public VariantQuery setCiEndLeft(String ciEndLeft) {
        this.ciEndLeft = ciEndLeft;
        return this;
    }

    public String getCiEndRight() {
        return ciEndRight;
    }

    public VariantQuery setCiEndRight(String ciEndRight) {
        this.ciEndRight = ciEndRight;
        return this;
    }

    public String getSvType() {
        return svType;
    }

    public VariantQuery setSvType(String svType) {
        this.svType = svType;
        return this;
    }

    public String getType() {
        return type;
    }

    public VariantQuery setType(String type) {
        this.type = type;
        return this;
    }

    public String getConsequenceType() {
        return consequenceType;
    }

    public VariantQuery setConsequenceType(String consequenceType) {
        this.consequenceType = consequenceType;
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
        private String chromosome;
        private String start;
        private String end;
        private List<Region> regions;
        private String reference;
        private String alternate;
        private String id;
        private String ciStartLeft;
        private String ciStartRight;
        private String ciEndLeft;
        private String ciEndRight;
        private String svType;
        private String type;
        private String consequenceType;

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

        public Builder withChromosome(String val) {
            chromosome = val;
            return this;
        }

        public Builder withStart(String val) {
            start = val;
            return this;
        }

        public Builder withEnd(String val) {
            end = val;
            return this;
        }

        public Builder withRegions(List<Region> val) {
            regions = val;
            return this;
        }

        public Builder withReference(String val) {
            reference = val;
            return this;
        }

        public Builder withAlternate(String val) {
            alternate = val;
            return this;
        }

        public Builder withId(String val) {
            id = val;
            return this;
        }

        public Builder withCiStartLeft(String val) {
            ciStartLeft = val;
            return this;
        }

        public Builder withCiStartRight(String val) {
            ciStartRight = val;
            return this;
        }

        public Builder withCiEndLeft(String val) {
            ciEndLeft = val;
            return this;
        }

        public Builder withCiEndRight(String val) {
            ciEndRight = val;
            return this;
        }

        public Builder withSvType(String val) {
            svType = val;
            return this;
        }

        public Builder withType(String val) {
            type = val;
            return this;
        }

        public Builder withConsequenceType(String val) {
            consequenceType = val;
            return this;
        }

        public VariantQuery build() {
            return new VariantQuery(this);
        }
    }
}
