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

package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;
import org.opencb.cellbase.core.api.query.LogicalList;

import java.util.List;
import java.util.Map;

public class VariantQuery extends AbstractQuery {

    @QueryParameter(id = "chromosome")
    private String chromosome;
    @QueryParameter(id = "start")
    private String start;
    @QueryParameter(id = "end")
    private String end;
    @QueryParameter(id = "region")
    private List<Region> regions;

    @QueryParameter(id = "reference")
    private String reference;
    @QueryParameter(id = "alternate")
    private String alternate;
    @QueryParameter(id = "id")
    private String id;

    @QueryParameter(id = "sv.ciStartLeft")
    private Integer ciStartLeft;
    @QueryParameter(id = "sv.ciStartRight")
    private Integer ciStartRight;
    @QueryParameter(id = "sv.ciEndLeft")
    private Integer ciEndLeft;
    @QueryParameter(id = "sv.ciEndRight")
    private Integer ciEndRight;

    @QueryParameter(id = "sv.type")
    private String svType;
    @QueryParameter(id = "type")
    private String type;
    @QueryParameter(id = "consequenceType")
    private String consequenceType;

    @QueryParameter(id = "gene")
    private LogicalList<String> genes;

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
        setGenes(builder.genes);
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
        sb.append(", ciStartLeft=").append(ciStartLeft);
        sb.append(", ciStartRight=").append(ciStartRight);
        sb.append(", ciEndLeft=").append(ciEndLeft);
        sb.append(", ciEndRight=").append(ciEndRight);
        sb.append(", svType='").append(svType).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", consequenceType='").append(consequenceType).append('\'');
        sb.append(", genes=").append(genes);
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

    public Integer getCiStartLeft() {
        return ciStartLeft;
    }

    public VariantQuery setCiStartLeft(Integer ciStartLeft) {
        this.ciStartLeft = ciStartLeft;
        return this;
    }

    public Integer getCiStartRight() {
        return ciStartRight;
    }

    public VariantQuery setCiStartRight(Integer ciStartRight) {
        this.ciStartRight = ciStartRight;
        return this;
    }

    public Integer getCiEndLeft() {
        return ciEndLeft;
    }

    public VariantQuery setCiEndLeft(Integer ciEndLeft) {
        this.ciEndLeft = ciEndLeft;
        return this;
    }

    public Integer getCiEndRight() {
        return ciEndRight;
    }

    public VariantQuery setCiEndRight(Integer ciEndRight) {
        this.ciEndRight = ciEndRight;
        return this;
    }

    public LogicalList<String> getGenes() {
        return genes;
    }

    public VariantQuery setGenes(LogicalList<String> genes) {
        this.genes = genes;
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
        private int ciStartLeft;
        private int ciStartRight;
        private int ciEndLeft;
        private int ciEndRight;
        private String svType;
        private String type;
        private String consequenceType;
        private LogicalList<String> genes;

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

        public Builder withCiStartLeft(int val) {
            ciStartLeft = val;
            return this;
        }

        public Builder withCiStartRight(int val) {
            ciStartRight = val;
            return this;
        }

        public Builder withCiEndLeft(int val) {
            ciEndLeft = val;
            return this;
        }

        public Builder withCiEndRight(int val) {
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

        public Builder withGenes(LogicalList<String> val) {
            genes = val;
            return this;
        }

        public VariantQuery build() {
            return new VariantQuery(this);
        }
    }
}
