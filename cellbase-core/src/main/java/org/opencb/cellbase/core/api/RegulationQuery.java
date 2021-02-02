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

import java.util.List;
import java.util.Map;

public class RegulationQuery extends AbstractQuery {

    @QueryParameter(id = "name")
    private List<String> names;
    @QueryParameter(id = "featureType")
    private List<String> featureTypes;
    @QueryParameter(id = "cellType")
    private List<String> cellTypes;
    @QueryParameter(id = "region")
    private List<Region> regions;

    public RegulationQuery() {
    }

    public RegulationQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private RegulationQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setNames(builder.names);
        setFeatureTypes(builder.featureTypes);
        setCellTypes(builder.cellTypes);
        setRegions(builder.regions);
    }


    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegulationQuery{");
        sb.append("names=").append(names);
        sb.append(", featureTypes=").append(featureTypes);
        sb.append(", cellTypes=").append(cellTypes);
        sb.append(", regions=").append(regions);
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

    public List<String> getNames() {
        return names;
    }

    public RegulationQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getFeatureTypes() {
        return featureTypes;
    }

    public RegulationQuery setFeatureTypes(List<String> featureTypes) {
        this.featureTypes = featureTypes;
        return this;
    }

    public List<String> getCellTypes() {
        return cellTypes;
    }

    public RegulationQuery setCellTypes(List<String> cellTypes) {
        this.cellTypes = cellTypes;
        return this;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public RegulationQuery setRegions(List<Region> regions) {
        this.regions = regions;
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
        private List<String> names;
        private List<String> featureTypes;
        private List<String> cellTypes;
        private List<Region> regions;

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

        public Builder withNames(List<String> val) {
            names = val;
            return this;
        }

        public Builder withFeatureTypes(List<String> val) {
            featureTypes = val;
            return this;
        }

        public Builder withCellTypes(List<String> val) {
            cellTypes = val;
            return this;
        }

        public Builder withRegions(List<Region> val) {
            regions = val;
            return this;
        }

        public RegulationQuery build() {
            return new RegulationQuery(this);
        }
    }
}
