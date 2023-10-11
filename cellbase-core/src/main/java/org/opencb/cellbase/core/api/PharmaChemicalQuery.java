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

    @QueryParameter(id = "source", allowedValues = {"PharmGKB"})
    private List<String> sources;

    @QueryParameter(id = "types", alias = {"type"})
    private List<String> types;

    @QueryParameter(id = "variants.variantId", alias = {"variant"})
    private List<String> variants;

    @QueryParameter(id = "variants.location", alias = {"location"})
    private List<String> locations;

    @QueryParameter(id = "variants.chromosome", alias = {"chromosome"})
    private List<String> chromosomes;

    @QueryParameter(id = "variants.haplotypes", alias = {"haplotype"})
    private List<String> hapolotypes;

    @QueryParameter(id = "geneName")
    private List<String> geneNames;

    @QueryParameter(id = "variants.phenotypes", alias = {"phenotype"})
    private List<String> phenotypes;

    @QueryParameter(id = "variants.phenotypeTypes", alias = {"phenotypeType"})
    private List<String> phenotypeTypes;

    @QueryParameter(id = "variants.confidence", alias = {"confidence"})
    private List<String> confidences;

    @QueryParameter(id = "variants.evidences.pubmed", alias = {"pubmedId"})
    private List<String> pubmedIds;

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
        sb.append(", sources=").append(sources);
        sb.append(", types=").append(types);
        sb.append(", variants=").append(variants);
        sb.append(", locations=").append(locations);
        sb.append(", chromosomes=").append(chromosomes);
        sb.append(", hapolotypes=").append(hapolotypes);
        sb.append(", geneNames=").append(geneNames);
        sb.append(", phenotypes=").append(phenotypes);
        sb.append(", phenotypeTypes=").append(phenotypeTypes);
        sb.append(", confidences=").append(confidences);
        sb.append(", pubmedIds=").append(pubmedIds);
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

    public List<String> getSources() {
        return sources;
    }

    public PharmaChemicalQuery setSources(List<String> sources) {
        this.sources = sources;
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

    public List<String> getLocations() {
        return locations;
    }

    public PharmaChemicalQuery setLocations(List<String> locations) {
        this.locations = locations;
        return this;
    }

    public List<String> getChromosomes() {
        return chromosomes;
    }

    public PharmaChemicalQuery setChromosomes(List<String> chromosomes) {
        this.chromosomes = chromosomes;
        return this;
    }

    public List<String> getHapolotypes() {
        return hapolotypes;
    }

    public PharmaChemicalQuery setHapolotypes(List<String> hapolotypes) {
        this.hapolotypes = hapolotypes;
        return this;
    }

    public List<String> getGeneNames() {
        return geneNames;
    }

    public PharmaChemicalQuery setGeneNames(List<String> geneNames) {
        this.geneNames = geneNames;
        return this;
    }

    public List<String> getPhenotypes() {
        return phenotypes;
    }

    public PharmaChemicalQuery setPhenotypes(List<String> phenotypes) {
        this.phenotypes = phenotypes;
        return this;
    }

    public List<String> getPhenotypeTypes() {
        return phenotypeTypes;
    }

    public PharmaChemicalQuery setPhenotypeTypes(List<String> phenotypeTypes) {
        this.phenotypeTypes = phenotypeTypes;
        return this;
    }

    public List<String> getConfidences() {
        return confidences;
    }

    public PharmaChemicalQuery setConfidences(List<String> confidences) {
        this.confidences = confidences;
        return this;
    }

    public List<String> getPubmedIds() {
        return pubmedIds;
    }

    public PharmaChemicalQuery setPubmedIds(List<String> pubmedIds) {
        this.pubmedIds = pubmedIds;
        return this;
    }
}
