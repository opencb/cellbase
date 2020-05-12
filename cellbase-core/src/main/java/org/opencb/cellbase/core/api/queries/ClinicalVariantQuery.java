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

public class ClinicalVariantQuery extends AbstractQuery {

    @QueryParameter(id = "region")
    protected List<Region> regions;

    @QueryParameter(id = "source")
    protected List<String> sources;
    @QueryParameter(id = "so")
    protected List<String> so;
    @QueryParameter(id = "feature")
    protected List<String> features;
    @QueryParameter(id = "trait")
    protected List<String> traits;

    @QueryParameter(id = "accession")
    protected List<String> accessions;
    @QueryParameter(id = "id")
    protected List<String> ids;

    @QueryParameter(id = "type")
    protected List<String> types;
    @QueryParameter(id = "consistencyStatus")
    protected List<String> consistencyStatuses;
    @QueryParameter(id = "clinicalSignificance")
    protected List<String> clinicalSignificances;
    @QueryParameter(id = "modeInheritance")
    protected List<String> modeInheritances;
    @QueryParameter(id = "alleleOrigin")
    protected List<String> alleleOrigins;

    public ClinicalVariantQuery() {
    }

    public ClinicalVariantQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    private ClinicalVariantQuery(Builder builder) {
        setIncludes(builder.includes);
        setExcludes(builder.excludes);
        setLimit(builder.limit);
        setSkip(builder.skip);
        setCount(builder.count);
        setSort(builder.sort);
        setOrder(builder.order);
        setFacet(builder.facet);
        setRegions(builder.regions);
        setSources(builder.sources);
        setSo(builder.so);
        setFeatures(builder.features);
        setTraits(builder.traits);
        setAccessions(builder.accessions);
        setIds(builder.ids);
        setTypes(builder.types);
        setConsistencyStatuses(builder.consistencyStatuses);
        setClinicalSignificances(builder.clinicalSignificances);
        setModeInheritances(builder.modeInheritances);
        setAlleleOrigins(builder.alleleOrigins);
    }

    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClinicalVariantQuery{");
        sb.append("regions=").append(regions);
        sb.append(", sources=").append(sources);
        sb.append(", so=").append(so);
        sb.append(", features=").append(features);
        sb.append(", traits=").append(traits);
        sb.append(", accessions=").append(accessions);
        sb.append(", ids=").append(ids);
        sb.append(", types=").append(types);
        sb.append(", consistencyStatuses=").append(consistencyStatuses);
        sb.append(", clinicalSignificances=").append(clinicalSignificances);
        sb.append(", modeInheritances=").append(modeInheritances);
        sb.append(", alleleOrigins=").append(alleleOrigins);
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

    public ClinicalVariantQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public List<String> getSources() {
        return sources;
    }

    public ClinicalVariantQuery setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }

    public List<String> getSo() {
        return so;
    }

    public ClinicalVariantQuery setSo(List<String> so) {
        this.so = so;
        return this;
    }

    public List<String> getFeatures() {
        return features;
    }

    public ClinicalVariantQuery setFeatures(List<String> features) {
        this.features = features;
        return this;
    }

    public List<String> getTraits() {
        return traits;
    }

    public ClinicalVariantQuery setTraits(List<String> traits) {
        this.traits = traits;
        return this;
    }

    public List<String> getAccessions() {
        return accessions;
    }

    public ClinicalVariantQuery setAccessions(List<String> accessions) {
        this.accessions = accessions;
        return this;
    }

    public List<String> getIds() {
        return ids;
    }

    public ClinicalVariantQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getTypes() {
        return types;
    }

    public ClinicalVariantQuery setTypes(List<String> types) {
        this.types = types;
        return this;
    }

    public List<String> getConsistencyStatuses() {
        return consistencyStatuses;
    }

    public ClinicalVariantQuery setConsistencyStatuses(List<String> consistencyStatuses) {
        this.consistencyStatuses = consistencyStatuses;
        return this;
    }

    public List<String> getClinicalSignificances() {
        return clinicalSignificances;
    }

    public ClinicalVariantQuery setClinicalSignificances(List<String> clinicalSignificances) {
        this.clinicalSignificances = clinicalSignificances;
        return this;
    }

    public List<String> getModeInheritances() {
        return modeInheritances;
    }

    public ClinicalVariantQuery setModeInheritances(List<String> modeInheritances) {
        this.modeInheritances = modeInheritances;
        return this;
    }

    public List<String> getAlleleOrigins() {
        return alleleOrigins;
    }

    public ClinicalVariantQuery setAlleleOrigins(List<String> alleleOrigins) {
        this.alleleOrigins = alleleOrigins;
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
        private List<String> sources;
        private List<String> so;
        private List<String> features;
        private List<String> traits;
        private List<String> accessions;
        private List<String> ids;
        private List<String> types;
        private List<String> consistencyStatuses;
        private List<String> clinicalSignificances;
        private List<String> modeInheritances;
        private List<String> alleleOrigins;

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

        public Builder withSources(List<String> val) {
            sources = val;
            return this;
        }

        public Builder withSo(List<String> val) {
            so = val;
            return this;
        }

        public Builder withFeatures(List<String> val) {
            features = val;
            return this;
        }

        public Builder withTraits(List<String> val) {
            traits = val;
            return this;
        }

        public Builder withAccessions(List<String> val) {
            accessions = val;
            return this;
        }

        public Builder withIds(List<String> val) {
            ids = val;
            return this;
        }

        public Builder withTypes(List<String> val) {
            types = val;
            return this;
        }

        public Builder withConsistencyStatuses(List<String> val) {
            consistencyStatuses = val;
            return this;
        }

        public Builder withClinicalSignificances(List<String> val) {
            clinicalSignificances = val;
            return this;
        }

        public Builder withModeInheritances(List<String> val) {
            modeInheritances = val;
            return this;
        }

        public Builder withAlleleOrigins(List<String> val) {
            alleleOrigins = val;
            return this;
        }

        public ClinicalVariantQuery build() {
            return new ClinicalVariantQuery(this);
        }
    }
}
