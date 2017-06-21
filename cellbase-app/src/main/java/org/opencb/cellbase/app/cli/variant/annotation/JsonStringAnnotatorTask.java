/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.cli.variant.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.run.ParallelTaskRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 02/03/15.
 */
public class JsonStringAnnotatorTask implements ParallelTaskRunner.TaskWithException<String, Variant, Exception> {

    private List<VariantAnnotator> variantAnnotatorList;
    private boolean normalize;
    private static ObjectMapper jsonObjectMapper;
    private static VariantNormalizer normalizer;

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        normalizer = new VariantNormalizer(true, false, true);
    }

    private static final String VARIANT_STRING_PATTERN = "([ACGTN]*)|(<CNV[0-9]+>)";

    public JsonStringAnnotatorTask(List<VariantAnnotator> variantAnnotatorList) {
        this(variantAnnotatorList, true);
    }

    public JsonStringAnnotatorTask(List<VariantAnnotator> variantAnnotatorList, boolean normalize) {
        this.variantAnnotatorList = variantAnnotatorList;
        this.normalize = normalize;
    }

    public void pre() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.open();
        }
    }

    public List<Variant> apply(List<String> batch) throws Exception {
        List<Variant> variantList = parseVariantList(batch);
        List<Variant> normalizedVariantList;
        if (normalize) {
            normalizedVariantList = normalizer.apply(variantList);
        } else {
            normalizedVariantList = variantList;
        }
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.run(normalizedVariantList);
        }
        return normalizedVariantList;
    }

    private List<Variant> parseVariantList(List<String> batch) {
        List<Variant> variantList = new ArrayList<>(batch.size());
        for (String line : batch) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            Variant variant = new Variant(jsonObjectMapper.convertValue(JSON.parse(line), VariantAvro.class));
            if (isValid(variant)) {
                // Read variants may not have the variant type set and this might cause NPE
                if (variant.getType() == null) {
                    variant.setType(variant.inferType(variant.getReference(), variant.getAlternate()));
                    variant.resetLength();
                }
                variantList.add(variant);
            } else {
                continue;
            }
        }

        return variantList;
    }

    /**
     * Checks whether a variant is valid.
     *
     * @param variant Variant object to be checked.
     * @return   true/false depending on whether 'variant' does contain valid values. Currently just a simple check of
     * reference/alternate attributes being strings of [A,C,G,T] of length >= 0 is performed to detect cases such as
     * 19:13318673:(CAG)4:(CAG)5 which are not currently supported by CellBase. Ref and alt alleles must be different
     * as well for the variant to be valid. Functionality of the method may be improved in the future.
     */
    private boolean isValid(Variant variant) {
        return (variant.getReference().matches(VARIANT_STRING_PATTERN)
                && variant.getAlternate().matches(VARIANT_STRING_PATTERN)
                && !variant.getAlternate().equals(variant.getReference()));
    }

    public void post() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.close();
        }
    }

}
