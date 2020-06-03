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
    private List<Region> regions;

    @QueryParameter(id = "chromosome")
    private String chromosome;
    @QueryParameter(id = "start")
    private String start;
    @QueryParameter(id = "end")
    private String end;

    @QueryParameter(id = "source")
    private List<String> sources;
    @QueryParameter(id = "so")
    private List<String> so;
    @QueryParameter(id = "feature")
    private List<String> features;
    @QueryParameter(id = "trait")
    private List<String> traits;

    @QueryParameter(id = "accession")
    private List<String> accessions;
    @QueryParameter(id = "id")
    private List<String> ids;

    @QueryParameter(id = "type")
    private List<String> types;
    @QueryParameter(id = "consistencyStatus")
    private List<String> consistencyStatuses;
    @QueryParameter(id = "clinicalSignificance")
    private List<String> clinicalSignificances;
    @QueryParameter(id = "modeInheritance")
    private List<String> modeInheritances;
    @QueryParameter(id = "alleleOrigin")
    private List<String> alleleOrigins;

    public ClinicalVariantQuery() {
    }

    public ClinicalVariantQuery(Map<String, String> params) throws QueryException {
        super(params);
    }


    @Override
    protected void validateQuery() throws QueryException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClinicalVariantQuery{");
        sb.append("regions=").append(regions);
        sb.append(", chromosome='").append(chromosome).append('\'');
        sb.append(", start='").append(start).append('\'');
        sb.append(", end='").append(end).append('\'');
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

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getSo() {
        return so;
    }

    public void setSo(List<String> so) {
        this.so = so;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getTraits() {
        return traits;
    }

    public void setTraits(List<String> traits) {
        this.traits = traits;
    }

    public List<String> getAccessions() {
        return accessions;
    }

    public void setAccessions(List<String> accessions) {
        this.accessions = accessions;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getConsistencyStatuses() {
        return consistencyStatuses;
    }

    public void setConsistencyStatuses(List<String> consistencyStatuses) {
        this.consistencyStatuses = consistencyStatuses;
    }

    public List<String> getClinicalSignificances() {
        return clinicalSignificances;
    }

    public void setClinicalSignificances(List<String> clinicalSignificances) {
        this.clinicalSignificances = clinicalSignificances;
    }

    public List<String> getModeInheritances() {
        return modeInheritances;
    }

    public void setModeInheritances(List<String> modeInheritances) {
        this.modeInheritances = modeInheritances;
    }

    public List<String> getAlleleOrigins() {
        return alleleOrigins;
    }

    public void setAlleleOrigins(List<String> alleleOrigins) {
        this.alleleOrigins = alleleOrigins;
    }


    public static final class ClinicalVariantQueryBuilder {
        protected List<Region> regions;
        protected String chromosome;
        protected String start;
        protected String end;
        protected List<String> sources;
        protected List<String> so;
        protected List<String> features;
        protected List<String> traits;
        protected List<String> accessions;
        protected List<String> ids;
        protected List<String> types;
        protected List<String> consistencyStatuses;
        protected List<String> clinicalSignificances;
        protected List<String> modeInheritances;
        protected List<String> alleleOrigins;
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;

        private ClinicalVariantQueryBuilder() {
        }

        public static ClinicalVariantQueryBuilder aClinicalVariantQuery() {
            return new ClinicalVariantQueryBuilder();
        }

        public ClinicalVariantQueryBuilder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public ClinicalVariantQueryBuilder withChromosome(String chromosome) {
            this.chromosome = chromosome;
            return this;
        }

        public ClinicalVariantQueryBuilder withStart(String start) {
            this.start = start;
            return this;
        }

        public ClinicalVariantQueryBuilder withEnd(String end) {
            this.end = end;
            return this;
        }

        public ClinicalVariantQueryBuilder withSources(List<String> sources) {
            this.sources = sources;
            return this;
        }

        public ClinicalVariantQueryBuilder withSo(List<String> so) {
            this.so = so;
            return this;
        }

        public ClinicalVariantQueryBuilder withFeatures(List<String> features) {
            this.features = features;
            return this;
        }

        public ClinicalVariantQueryBuilder withTraits(List<String> traits) {
            this.traits = traits;
            return this;
        }

        public ClinicalVariantQueryBuilder withAccessions(List<String> accessions) {
            this.accessions = accessions;
            return this;
        }

        public ClinicalVariantQueryBuilder withIds(List<String> ids) {
            this.ids = ids;
            return this;
        }

        public ClinicalVariantQueryBuilder withTypes(List<String> types) {
            this.types = types;
            return this;
        }

        public ClinicalVariantQueryBuilder withConsistencyStatuses(List<String> consistencyStatuses) {
            this.consistencyStatuses = consistencyStatuses;
            return this;
        }

        public ClinicalVariantQueryBuilder withClinicalSignificances(List<String> clinicalSignificances) {
            this.clinicalSignificances = clinicalSignificances;
            return this;
        }

        public ClinicalVariantQueryBuilder withModeInheritances(List<String> modeInheritances) {
            this.modeInheritances = modeInheritances;
            return this;
        }

        public ClinicalVariantQueryBuilder withAlleleOrigins(List<String> alleleOrigins) {
            this.alleleOrigins = alleleOrigins;
            return this;
        }

        public ClinicalVariantQueryBuilder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public ClinicalVariantQueryBuilder withSkip(Integer skip) {
            this.skip = skip;
            return this;
        }

        public ClinicalVariantQueryBuilder withCount(Boolean count) {
            this.count = count;
            return this;
        }

        public ClinicalVariantQueryBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public ClinicalVariantQueryBuilder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public ClinicalVariantQueryBuilder withFacet(String facet) {
            this.facet = facet;
            return this;
        }

        public ClinicalVariantQueryBuilder withIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public ClinicalVariantQueryBuilder withExcludes(List<String> excludes) {
            this.excludes = excludes;
            return this;
        }

        public ClinicalVariantQuery build() {
            ClinicalVariantQuery clinicalVariantQuery = new ClinicalVariantQuery();
            clinicalVariantQuery.setRegions(regions);
            clinicalVariantQuery.setChromosome(chromosome);
            clinicalVariantQuery.setStart(start);
            clinicalVariantQuery.setEnd(end);
            clinicalVariantQuery.setSources(sources);
            clinicalVariantQuery.setSo(so);
            clinicalVariantQuery.setFeatures(features);
            clinicalVariantQuery.setTraits(traits);
            clinicalVariantQuery.setAccessions(accessions);
            clinicalVariantQuery.setIds(ids);
            clinicalVariantQuery.setTypes(types);
            clinicalVariantQuery.setConsistencyStatuses(consistencyStatuses);
            clinicalVariantQuery.setClinicalSignificances(clinicalSignificances);
            clinicalVariantQuery.setModeInheritances(modeInheritances);
            clinicalVariantQuery.setAlleleOrigins(alleleOrigins);
            clinicalVariantQuery.setLimit(limit);
            clinicalVariantQuery.setSkip(skip);
            clinicalVariantQuery.setCount(count);
            clinicalVariantQuery.setSort(sort);
            clinicalVariantQuery.setOrder(order);
            clinicalVariantQuery.setFacet(facet);
            clinicalVariantQuery.setIncludes(includes);
            clinicalVariantQuery.setExcludes(excludes);
            return clinicalVariantQuery;
        }
    }
}
