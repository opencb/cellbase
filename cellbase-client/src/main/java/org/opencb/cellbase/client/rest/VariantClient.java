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

package org.opencb.cellbase.client.rest;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager.GENOTYPE_TAG;
import static org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager.PHASE_SET_TAG;


public final class VariantClient extends FeatureClient<Variant> {

    private static final String IGNORE_PHASE = "ignorePhase";
    private static final char PHASE_DATA_URL_SEPARATOR = '+';
    private static final char ABSENT_ALLELE = '0';
    private static final String REFERENCE_HOMOZYGOUS_GENOTYPE = "0|0";

    public VariantClient(String species, String assembly, ClientConfiguration configuration) {
        super(species, assembly, configuration);
        this.clazz = Variant.class;

        this.category = "genomic";
        this.subcategory = "variant";
    }

    @Deprecated
    public CellBaseDataResponse<VariantAnnotation> getAnnotationByVariantIds(String id, QueryOptions options) throws IOException {
        return this.getAnnotationByVariantIds(Arrays.asList(id.split(",")), options, false);
    }

    @Deprecated
    public CellBaseDataResponse<VariantAnnotation> getAnnotationByVariantIds(String id, QueryOptions options,
                                                                             boolean post) throws IOException {
        return this.getAnnotationByVariantIds(Arrays.asList(id.split(",")), options, post);
    }

    public CellBaseDataResponse<VariantAnnotation> getAnnotationByVariantIds(List<String> ids, QueryOptions options) throws IOException {
        return this.getAnnotationByVariantIds(ids, options, false);
    }

    public CellBaseDataResponse<VariantAnnotation> getAnnotationByVariantIds(List<String> ids, QueryOptions options, boolean post)
            throws IOException {
        CellBaseDataResponse<VariantAnnotation> result = execute(ids, "annotation", options, VariantAnnotation.class, post);
        return initRequiredAnnotation(result);
    }


    public CellBaseDataResponse<Variant> annotate(List<Variant> variants, QueryOptions options) throws IOException {
        return annotate(variants, options, false);
    }

    public CellBaseDataResponse<Variant> annotate(List<Variant> variants, QueryOptions options, boolean post) throws IOException {
        List<String> variantIds = getVariantAnnotationIds(variants, options.getBoolean(IGNORE_PHASE));
        CellBaseDataResponse<VariantAnnotation> annotations = this.getAnnotationByVariantIds(variantIds, options, post);

        int timePerId = annotations.getTime() / variants.size();
        List<CellBaseDataResult<Variant>> annotatedVariants = new ArrayList<>(variants.size());
        for (int i = 0; i < variants.size(); i++) {
            variants.get(i).setAnnotation(annotations.getResponses().get(i).first());
            annotatedVariants.add(new CellBaseDataResult<>(variantIds.get(i), timePerId, null, 1,
                    Collections.singletonList(variants.get(i)), 1));
        }

        return new CellBaseDataResponse<>(configuration.getVersion(), annotations.getTime(), null,
                new ObjectMap(options), annotatedVariants);
    }

    public CellBaseDataResponse<VariantAnnotation> getAnnotation(List<Variant> variants, QueryOptions options) throws IOException {
        return getAnnotation(variants, options, false);
    }

    public CellBaseDataResponse<VariantAnnotation> getAnnotation(List<Variant> variants, QueryOptions options, boolean post)
            throws IOException {
        CellBaseDataResponse<VariantAnnotation> result = execute(getVariantAnnotationIds(variants,
                options.getBoolean(IGNORE_PHASE)),
                "annotation",
                options,
                VariantAnnotation.class, post);
        return initRequiredAnnotation(result);
    }


    // FIXME Next two methods should be moved near the Variant Annotation tool
    public String getVariantAnnotationId(Variant variant, Boolean ignorePhase) {
        StringBuilder stringBuilder = new StringBuilder(variant.toString());

        if (!ignorePhase) {
            String phaseSet = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, PHASE_SET_TAG);
            if (StringUtils.isNotBlank(phaseSet)) {
                stringBuilder.append(PHASE_DATA_URL_SEPARATOR)
                        .append(getGenotypeString(variant))
                        .append(PHASE_DATA_URL_SEPARATOR)
                        .append(phaseSet);
            }
        }
        return stringBuilder.toString();
    }

    private String getGenotypeString(Variant variant) {
        String genotypeString = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant, GENOTYPE_TAG);
        if (genotypeString == null) {
            return EMPTY_STRING;
        }

        // Unphased genotype - we don't really care about it then since here we're just interested in the genotype
        // for being able to determine wheter two variants are potentially in cis in the server
        if (StringUtils.countMatches(genotypeString, AnnotationBasedPhasedQueryManager.UNPHASED_GENOTYPE_SEPARATOR) > 0) {
            // However, if genotype is homozygous for the reference, must make this explicit so that server knows
            // this variant is not present when deciding to return or not MNVs
            if (StringUtils.countMatches(genotypeString, ABSENT_ALLELE) == 2) {
                return REFERENCE_HOMOZYGOUS_GENOTYPE;
            } else {
                return EMPTY_STRING;
            }
        }

        return genotypeString;

    }

    public List<String> getVariantAnnotationIds(List<Variant> variants, Boolean ignorePhase) {
        if (variants == null) {
            return null;
        }

        List<String> variantIds = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            variantIds.add(getVariantAnnotationId(variant, ignorePhase));
        }
        return variantIds;
    }

    private CellBaseDataResponse<VariantAnnotation> initRequiredAnnotation(CellBaseDataResponse<VariantAnnotation> queryResponse) {
        for (int i = 0; i < queryResponse.getResponses().size(); i++) {
            VariantAnnotation annotation = queryResponse.getResponses().get(i).first();
            // It can happen that no annotation is returned for variants that could not be parsed and raised problems
            // e.g. 1:645710:A:<INS:ME:ALU>
            if (annotation != null) {
                // Patch to remove by updating the Evidence avdl model
                if (annotation.getTraitAssociation() != null) {
                    for (EvidenceEntry evidenceEntry : annotation.getTraitAssociation()) {
                        if (evidenceEntry.getSubmissions() == null) {
                            evidenceEntry.setSubmissions(Collections.emptyList());
                        }
                        if (evidenceEntry.getHeritableTraits() == null) {
                            evidenceEntry.setHeritableTraits(Collections.emptyList());
                        } else {
                            for (HeritableTrait heritableTrait : evidenceEntry.getHeritableTraits()) {
                                if (heritableTrait.getInheritanceMode() == null) {
                                    heritableTrait.setInheritanceMode(ModeOfInheritance.unknown);
                                }
                            }
                        }
                        if (evidenceEntry.getGenomicFeatures() == null) {
                            evidenceEntry.setGenomicFeatures(Collections.emptyList());
                        }
                        if (evidenceEntry.getAdditionalProperties() == null) {
                            evidenceEntry.setAdditionalProperties(Collections.emptyList());
                        }
                        if (evidenceEntry.getEthnicity() == null) {
                            evidenceEntry.setEthnicity(EthnicCategory.Z);
                        }
                        if (evidenceEntry.getBibliography() == null) {
                            evidenceEntry.setBibliography(Collections.emptyList());
                        }
                        if (evidenceEntry.getSomaticInformation() != null) {
                            if (evidenceEntry.getSomaticInformation().getSampleSource() == null) {
                                evidenceEntry.getSomaticInformation().setSampleSource("");
                            }
                            if (evidenceEntry.getSomaticInformation().getTumourOrigin() == null) {
                                evidenceEntry.getSomaticInformation().setTumourOrigin("");
                            }
                        }
                    }
                }
                // TODO This data model is obsolete, this code must be removed
                if (annotation.getVariantTraitAssociation() != null) {
                    if (annotation.getVariantTraitAssociation().getCosmic() != null) {
                        for (Cosmic cosmic : annotation.getVariantTraitAssociation().getCosmic()) {
                            if (cosmic.getSiteSubtype() == null) {
                                cosmic.setSiteSubtype("");
                            }
                            if (cosmic.getSampleSource() == null) {
                                cosmic.setSampleSource("");
                            }
                            if (cosmic.getTumourOrigin() == null) {
                                cosmic.setTumourOrigin("");
                            }
                            if (cosmic.getHistologySubtype() == null) {
                                cosmic.setHistologySubtype("");
                            }
                            if (cosmic.getPrimarySite() == null) {
                                cosmic.setPrimarySite("");
                            }
                            if (cosmic.getPrimaryHistology() == null) {
                                cosmic.setPrimaryHistology("");
                            }
                        }
                    }
                }
            }
        }
        return queryResponse;
    }
}
