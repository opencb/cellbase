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

package org.opencb.cellbase.app.cli.main.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.variant.PopulationFrequencyPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by fjlopez on 18/07/16.
 */
public class PopulationFrequenciesAnnotator implements VariantAnnotator {

    private String fileName;
    private RocksDB dbIndex;
    private RandomAccessFile reader;
    private final QueryOptions queryOptions;


    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter jsonObjectWriter;
    private static PopulationFrequencyPhasedQueryManager phasedQueryManager
            = new PopulationFrequencyPhasedQueryManager();

    static {
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = mapper.writer();
    }

    public PopulationFrequenciesAnnotator(String fileName, RocksDB dbIndex, QueryOptions queryOptions) {
        this.fileName = fileName;
        this.dbIndex = dbIndex;
        this.queryOptions = queryOptions;
    }

    public boolean open() {
        try {
            reader = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Updates VariantAnnotation objects in variantAnnotationList.
     *
     * @param variantList List of Variant objects. variantList and variantAnnotationList must contain variants in the
     *                    SAME order: variantAnnotation at position i must correspond to variant i
     */
    public void run(List<Variant> variantList) {

        List<QueryResult<Variant>> variantQueryResult = new ArrayList<>(variantList.size());
        for (Variant variant: variantList) {
            variantQueryResult.add(getPopulationFrequencies(variant));
        }

        if (queryOptions.get(IGNORE_PHASE) != null && !queryOptions.getBoolean(IGNORE_PHASE)) {
            variantQueryResult = phasedQueryManager.run(variantList, variantQueryResult);
        }

        for (int i = 0; i < variantList.size(); i++) {
            if (!variantQueryResult.get(i).getResult().isEmpty()) {
                // Assuming if it gets to this point the variant has VariantAnnotation
                // Only one variant  can be returned per query to RocksDB
                List<PopulationFrequency> populationFrequencies
                        = variantQueryResult.get(i).getResult().get(0).getAnnotation().getPopulationFrequencies();
                // Update only if there are annotations for this variant
                if (populationFrequencies != null && populationFrequencies.size() > 0) {
                    VariantAnnotation variantAnnotation = variantList.get(i).getAnnotation();
                    if (variantAnnotation != null) {
                        // variantList and populationFrequencyList must contain variants in the SAME order: population
                        // frequencies at position i must correspond to variant i
                        variantAnnotation.setPopulationFrequencies(populationFrequencies);
                    } else {
                        variantAnnotation = new VariantAnnotation();
                        variantAnnotation.setPopulationFrequencies(populationFrequencies);
                        variantList.get(i).setAnnotation(variantAnnotation);
                    }
                }
            }
        }
    }

    private QueryResult<Variant> getPopulationFrequencies(Variant variant) {
        QueryResult<Variant> populationFrequencyQueryResult = new QueryResult<>();
        populationFrequencyQueryResult.setId(variant.toString());
        long start = System.currentTimeMillis();
        try {
            byte[] variantKey = variant.toString().getBytes();
            byte[] dbContent = dbIndex.get(variantKey);
            if (dbContent != null) {
                Variant variant1 = mapper.readValue(dbContent, Variant.class);
                flagVisitedVariant(variantKey, variant1);

                populationFrequencyQueryResult.setResult(Collections.singletonList(variant1));
                populationFrequencyQueryResult.setNumTotalResults(1);
                populationFrequencyQueryResult.setNumResults(1);
            }
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        populationFrequencyQueryResult.setDbTime((int) (end - start));

        return populationFrequencyQueryResult;
    }

    private void flagVisitedVariant(byte[] key, Variant variant) {
        // The annotation.additionalAttributes field is initialized with an empty map to flag this variant as
        // already visited
        variant.getAnnotation().setAdditionalAttributes(Collections.emptyMap());
        try {
            dbIndex.put(key, jsonObjectWriter.writeValueAsBytes(variant));
        } catch (RocksDBException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        try {
            reader.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
