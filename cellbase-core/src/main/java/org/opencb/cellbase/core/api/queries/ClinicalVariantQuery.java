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

    @QueryParameter(id = "id")
    private String id;

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
        sb.append(", id='").append(id).append('\'');
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

    public ClinicalVariantQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }

    public String getId() {
        return id;
    }

    public ClinicalVariantQuery setId(String id) {
        this.id = id;
        return this;
    }

    public String getChromosome() {
        return chromosome;
    }

    public ClinicalVariantQuery setChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

    public String getStart() {
        return start;
    }

    public ClinicalVariantQuery setStart(String start) {
        this.start = start;
        return this;
    }

    public String getEnd() {
        return end;
    }

    public ClinicalVariantQuery setEnd(String end) {
        this.end = end;
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


    public static final class ClinicalVariantQueryBuilder {
        protected Integer limit;
        protected Integer skip;
        protected Boolean count = false;
        protected String sort;
        protected Order order;
        protected String facet;
        protected List<String> includes;
        protected List<String> excludes;
        private List<Region> regions;
        private String id;
        private String chromosome;
        private String start;
        private String end;
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

        private ClinicalVariantQueryBuilder() {
        }

        public static ClinicalVariantQueryBuilder aClinicalVariantQuery() {
            return new ClinicalVariantQueryBuilder();
        }

        public ClinicalVariantQueryBuilder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public ClinicalVariantQueryBuilder withId(String id) {
            this.id = id;
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
            clinicalVariantQuery.setId(id);
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
