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

import org.apache.commons.collections4.CollectionUtils;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;

import java.util.List;
import java.util.Map;

public class PharmaChemicalQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;
    @QueryParameter(id = "name")
    private List<String> names;
//    @QueryParameter(id = "source", allowedValues = {"PharmGKB"})
//    private List<String> source;

    @QueryParameter(id = "type")
    private List<String> types;

    @QueryParameter(id = "variants.variantId")
    private List<String> variants;

    @QueryParameter(id = "variants.gene")
    private List<String> genes;
    @QueryParameter(id = "variants.location")
    private List<String> locations;

    public PharmaChemicalQuery() {
    }

    public PharmaChemicalQuery(Map<String, String> params) throws QueryException {
        super(params);

        objectMapper.readerForUpdating(this);
        objectMapper.readerFor(PharmaChemicalQuery.class);
        objectWriter = objectMapper.writerFor(PharmaChemicalQuery.class);
    }

    @Override
    protected void validateQuery() throws QueryException {
        if (CollectionUtils.isNotEmpty(variants)) {
            for (String variant : variants) {
                if (!variant.startsWith("rs")) {
                    throw new QueryException("Invalid variant ID: '" + variant + "'; it has to start with rs");
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PharmaChemicalQuery{");
        sb.append("ids=").append(ids);
        sb.append(", names=").append(names);
        sb.append(", types=").append(types);
        sb.append(", variants=").append(variants);
        sb.append(", genes=").append(genes);
        sb.append(", locations=").append(locations);
        sb.append('}');
        return sb.toString();
    }

    public List<String> getIds() {
        return ids;
    }

    public PharmaChemicalQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public PharmaChemicalQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getTypes() {
        return types;
    }

    public PharmaChemicalQuery setTypes(List<String> types) {
        this.types = types;
        return this;
    }

    public List<String> getVariants() {
        return variants;
    }

    public PharmaChemicalQuery setVariants(List<String> variants) {
        this.variants = variants;
        return this;
    }

    public List<String> getGenes() {
        return genes;
    }

    public PharmaChemicalQuery setGenes(List<String> genes) {
        this.genes = genes;
        return this;
    }

    public List<String> getLocations() {
        return locations;
    }

    public PharmaChemicalQuery setLocations(List<String> locations) {
        this.locations = locations;
        return this;
    }

//    public static final class QueryBuilder {
//        protected Integer limit;
//        protected Integer skip;
//        protected Boolean count = false;
//        protected String sort;
//        protected Order order;
//        protected String facet;
//        protected List<String> includes;
//        protected List<String> excludes;
//        private List<String> ids;
//        private List<String> names;
//        private List<String> biotypes;
//        private List<String> source;
//        private List<Region> regions;
//        private List<String> transcriptsBiotype;
//        private List<String> transcriptsXrefs;
//        private List<String> transcriptsId;
//        private List<String> transcriptsName;
//        private LogicalList<String> transcriptsAnnotationFlags;
//        private LogicalList<String> transcriptsTfbsId;
//        private LogicalList<String> transcriptsTfbsPfmId;
//        private LogicalList<String> transcriptsTfbsTranscriptionFactors;
//        private LogicalList<String> transcriptAnnotationOntologiesId;
//        private LogicalList<String> annotationDiseases;
//        private LogicalList<String> annotationExpressionTissue;
//        private LogicalList<String> annotationExpressionValue;
//        private LogicalList<String> annotationDrugsName;
//        private LogicalList<String> annotationConstraints;
//        private LogicalList<String> annotationTargets;
//        private LogicalList<String> mirnas;
//
//        private GeneQueryBuilder() {
//        }
//
//        public static GeneQueryBuilder aGeneQuery() {
//            return new GeneQueryBuilder();
//        }
//
//        public GeneQueryBuilder withIds(List<String> ids) {
//            this.ids = ids;
//            return this;
//        }
//
//        public GeneQueryBuilder withNames(List<String> names) {
//            this.names = names;
//            return this;
//        }
//
//        public GeneQueryBuilder withBiotypes(List<String> biotypes) {
//            this.biotypes = biotypes;
//            return this;
//        }
//
//        public GeneQueryBuilder withSource(List<String> source) {
//            this.source = source;
//            return this;
//        }
//
//        public GeneQueryBuilder withRegions(List<Region> regions) {
//            this.regions = regions;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsBiotype(List<String> transcriptsBiotype) {
//            this.transcriptsBiotype = transcriptsBiotype;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsXrefs(List<String> transcriptsXrefs) {
//            this.transcriptsXrefs = transcriptsXrefs;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsId(List<String> transcriptsId) {
//            this.transcriptsId = transcriptsId;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsName(List<String> transcriptsName) {
//            this.transcriptsName = transcriptsName;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsAnnotationFlags(LogicalList<String> transcriptsAnnotationFlags) {
//            this.transcriptsAnnotationFlags = transcriptsAnnotationFlags;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsTfbsId(LogicalList<String> transcriptsTfbsId) {
//            this.transcriptsTfbsId = transcriptsTfbsId;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsTfbsPfmId(LogicalList<String> transcriptsTfbsPfmId) {
//            this.transcriptsTfbsPfmId = transcriptsTfbsPfmId;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptsTfbsTranscriptionFactors(LogicalList<String> transcriptsTfbsTranscriptionFactors) {
//            this.transcriptsTfbsTranscriptionFactors = transcriptsTfbsTranscriptionFactors;
//            return this;
//        }
//
//        public GeneQueryBuilder withTranscriptAnnotationOntologiesId(LogicalList<String> transcriptAnnotationOntologiesId) {
//            this.transcriptAnnotationOntologiesId = transcriptAnnotationOntologiesId;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationDiseases(LogicalList<String> annotationDiseases) {
//            this.annotationDiseases = annotationDiseases;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationExpressionTissue(LogicalList<String> annotationExpressionTissue) {
//            this.annotationExpressionTissue = annotationExpressionTissue;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationExpressionValue(LogicalList<String> annotationExpressionValue) {
//            this.annotationExpressionValue = annotationExpressionValue;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationDrugsName(LogicalList<String> annotationDrugsName) {
//            this.annotationDrugsName = annotationDrugsName;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationConstraints(LogicalList<String> annotationConstraints) {
//            this.annotationConstraints = annotationConstraints;
//            return this;
//        }
//
//        public GeneQueryBuilder withAnnotationTargets(LogicalList<String> annotationTargets) {
//            this.annotationTargets = annotationTargets;
//            return this;
//        }
//
//        public GeneQueryBuilder withMirnas(LogicalList<String> mirnas) {
//            this.mirnas = mirnas;
//            return this;
//        }
//
//        public GeneQueryBuilder withLimit(Integer limit) {
//            this.limit = limit;
//            return this;
//        }
//
//        public GeneQueryBuilder withSkip(Integer skip) {
//            this.skip = skip;
//            return this;
//        }
//
//        public GeneQueryBuilder withCount(Boolean count) {
//            this.count = count;
//            return this;
//        }
//
//        public GeneQueryBuilder withSort(String sort) {
//            this.sort = sort;
//            return this;
//        }
//
//        public GeneQueryBuilder withOrder(Order order) {
//            this.order = order;
//            return this;
//        }
//
//        public GeneQueryBuilder withFacet(String facet) {
//            this.facet = facet;
//            return this;
//        }
//
//        public GeneQueryBuilder withIncludes(List<String> includes) {
//            this.includes = includes;
//            return this;
//        }
//
//        public GeneQueryBuilder withExcludes(List<String> excludes) {
//            this.excludes = excludes;
//            return this;
//        }
//
//        public PharmaChemicalQuery build() {
//            PharmaChemicalQuery geneQuery = new PharmaChemicalQuery();
//            geneQuery.setIds(ids);
//            geneQuery.setNames(names);
//            geneQuery.setBiotypes(biotypes);
//            geneQuery.setSource(source);
//            geneQuery.setRegions(regions);
//            geneQuery.setTranscriptsBiotype(transcriptsBiotype);
//            geneQuery.setTranscriptsXrefs(transcriptsXrefs);
//            geneQuery.setTranscriptsId(transcriptsId);
//            geneQuery.setTranscriptsName(transcriptsName);
//            geneQuery.setTranscriptsAnnotationFlags(transcriptsAnnotationFlags);
//            geneQuery.setTranscriptsTfbsId(transcriptsTfbsId);
//            geneQuery.setTranscriptsTfbsPfmId(transcriptsTfbsPfmId);
//            geneQuery.setTranscriptsTfbsTranscriptionFactors(transcriptsTfbsTranscriptionFactors);
//            geneQuery.setTranscriptAnnotationOntologiesId(transcriptAnnotationOntologiesId);
//            geneQuery.setAnnotationDiseases(annotationDiseases);
//            geneQuery.setAnnotationExpressionTissue(annotationExpressionTissue);
//            geneQuery.setAnnotationExpressionValue(annotationExpressionValue);
//            geneQuery.setAnnotationDrugsName(annotationDrugsName);
//            geneQuery.setAnnotationConstraints(annotationConstraints);
//            geneQuery.setAnnotationTargets(annotationTargets);
//            geneQuery.setMirnas(mirnas);
//            geneQuery.setLimit(limit);
//            geneQuery.setSkip(skip);
//            geneQuery.setCount(count);
//            geneQuery.setSort(sort);
//            geneQuery.setOrder(order);
//            geneQuery.setFacet(facet);
//            geneQuery.setIncludes(includes);
//            geneQuery.setExcludes(excludes);
//            return geneQuery;
//        }
//    }
}
