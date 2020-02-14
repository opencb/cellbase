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

package org.opencb.cellbase.lib.managers;

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class VariantManager extends AbstractManager {

    private static final String PHASE_DATA_URL_SEPARATOR = "\\+";
    private static final String VARIANT_STRING_FORMAT = "(chr)"
            + ":[(cipos_left)<](start)[<(cipos_right)]" + "[-[(ciend_left)<](end)[<(ciend_right)]]"
            + "[:(ref)]"
            + ":[(alt)|(left_ins_seq)...(right_ins_seq)]";

    public VariantManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String id) {
        logger.debug("Querying for variant info");
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        List<Query> queries = createQueries(query, id, VariantDBAdaptor.QueryParams.ID.key());
        List<CellBaseDataResult> queryResults = variationDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(VariantDBAdaptor.QueryParams.ID.key()));
        }
        return queryResults;
    }

    public CellBaseDataResult search(Query query, QueryOptions queryOptions, String species, String assembly) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        CellBaseDataResult queryResults = variationDBAdaptor.nativeGet(query, queryOptions);
        return queryResults;
    }

    public List<CellBaseDataResult<VariantAnnotation>> getAnnotationByVariant(QueryOptions queryOptions, String species, String assembly,
                                                                              String variants,
                                                                              Boolean normalize,
                                                                              Boolean skipDecompose,
                                                                              Boolean ignorePhase,
                                                                              @Deprecated Boolean phased,
                                                                              Boolean imprecise,
                                                                              Integer svExtraPadding,
                                                                              Integer cnvExtraPadding)
            throws ExecutionException, InterruptedException {
        List<Variant> variantList = parseVariants(variants);
        logger.debug("queryOptions: " + queryOptions);

        // If ignorePhase (new parameter) is present, then overrides presence of "phased"
        if (ignorePhase != null) {
            queryOptions.put("ignorePhase", ignorePhase);
            // If the new parameter (ignorePhase) is not present but old one ("phased") is, then follow old one - probably
            // someone who has not moved to the new parameter yet
        } else if (phased != null) {
            queryOptions.put("ignorePhase", !phased);
            // Default behavior is to perform phased annotation
        } else {
            queryOptions.put("ignorePhase", false);
        }

        if (normalize != null) {
            queryOptions.put("normalize", normalize);
        }
        if (skipDecompose != null) {
            queryOptions.put("skipDecompose", skipDecompose);
        }
        if (imprecise != null) {
            queryOptions.put("imprecise", imprecise);
        }
        if (svExtraPadding != null) {
            queryOptions.put("svExtraPadding", svExtraPadding);
        }
        if (cnvExtraPadding != null) {
            queryOptions.put("cnvExtraPadding", cnvExtraPadding);
        }
        VariantAnnotationCalculator variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly, dbAdaptorFactory);
        List<CellBaseDataResult<VariantAnnotation>> queryResults =
                variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);
        return queryResults;
    }

    private List<Variant> parseVariants(String variantsString) {
        List<Variant> variants = null;
        if (variantsString != null && !variantsString.isEmpty()) {
            String[] variantItems = variantsString.split(",");
            variants = new ArrayList<>(variantItems.length);

            for (String variantString: variantItems) {
                variants.add(parseVariant(variantString));
            }
        }
        return variants;
    }

    private Variant parseVariant(String variantString) {
        String[] variantStringPartArray = variantString.split(PHASE_DATA_URL_SEPARATOR);

        VariantBuilder variantBuilder;
        if (variantStringPartArray.length > 0) {
            variantBuilder = new VariantBuilder(variantStringPartArray[0]);
            // Either 1 or 3 parts expected variant+GT+PS
            if (variantStringPartArray.length == 3) {
                List<String> formatList = new ArrayList<>(2);
                // If phase set tag is not provided not phase data is added at all to the Variant object
                if (!variantStringPartArray[2].isEmpty()) {
                    formatList.add(AnnotationBasedPhasedQueryManager.PHASE_SET_TAG);
                    List<String> sampleData = new ArrayList<>(2);
                    sampleData.add(variantStringPartArray[2]);
                    // Genotype field might be empty - just PS would be added to Variant object in that case
                    if (!variantStringPartArray[1].isEmpty()) {
                        formatList.add(AnnotationBasedPhasedQueryManager.GENOTYPE_TAG);
                        sampleData.add(variantStringPartArray[1]);
                    }
                    variantBuilder.setFormat(formatList);
                    variantBuilder.setSamplesData(Collections.singletonList(sampleData));
                }
            } else if (variantStringPartArray.length > 3) {
                throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                        + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                        + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
            }
        } else {
            throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                    + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                    + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
        }

        return variantBuilder.build();
    }

    public CellBaseDataResult<String> getConsequenceTypes() {
        List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        CellBaseDataResult<String> queryResult = new CellBaseDataResult<>("consequence_types");
        queryResult.setNumResults(consequenceTypes.size());
        queryResult.setResults(consequenceTypes);
        return queryResult;
    }

    public boolean validateRegionInput(String regions) {
        List<Region> regionList = Region.parseRegions(regions);
        // check for regions bigger than 10Mb
        if (regionList != null) {
            for (Region r : regionList) {
                if ((r.getEnd() - r.getStart()) > 10000000) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String species, String assembly, String regions) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        if (hasHistogramQueryParam(queryOptions)) {
            List<Query> queries = createQueries(query, regions, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = variationDBAdaptor.getIntervalFrequencies(queries,
                    getHistogramIntervalSize(queryOptions), queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId(queries.get(i).getString(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        } else {
            query.put(VariantDBAdaptor.QueryParams.REGION.key(), regions);
            logger.debug("query = " + query.toJson());
            logger.debug("queryOptions = " + queryOptions.toJson());
            List<Query> queries = createQueries(query, regions, VariantDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = variationDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(VariantDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        }
    }
}
