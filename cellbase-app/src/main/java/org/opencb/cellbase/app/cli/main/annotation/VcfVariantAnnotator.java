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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.AdditionalAttribute;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.variant.CustomAnnotationPhasedQueryManager;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by fjlopez on 28/04/15.
 */
public class VcfVariantAnnotator implements VariantAnnotator {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
    }

    private final QueryOptions queryOptions;
    private String fileName;
    private RocksDB dbIndex;
    private String fileId;
    private RandomAccessFile reader;
    private static CustomAnnotationPhasedQueryManager phasedQueryManager = new CustomAnnotationPhasedQueryManager();

    public VcfVariantAnnotator(String fileName, RocksDB dbIndex, String fileId, QueryOptions queryOptions) {
        this.fileName = fileName;
        this.dbIndex = dbIndex;
        this.fileId = fileId;
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

        List<CellBaseDataResult<Variant>> variantCellBaseDataResult = new ArrayList<>(variantList.size());
        for (Variant variant: variantList) {
            variantCellBaseDataResult.add(getCustomAnnotation(variant));
        }

        if (queryOptions.get(IGNORE_PHASE) != null && !queryOptions.getBoolean(IGNORE_PHASE)) {
            variantCellBaseDataResult = phasedQueryManager.run(variantList, variantCellBaseDataResult);
        }

        for (int i = 0; i < variantList.size(); i++) {
            if (!variantCellBaseDataResult.get(i).getResults().isEmpty()) {
                // Assuming if it gets to this point the variant has VariantAnnotation
                // Only one variant  can be returned per query to RocksDB
                Map<String, AdditionalAttribute> customAnnotation
                        = parseCustomAnnotation(variantCellBaseDataResult.get(i).getResults().get(0));
                // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
                // exists in the vcf but the info field does not contain any of the required attributes
                if (customAnnotation != null && !customAnnotation.isEmpty()) {
                    VariantAnnotation variantAnnotation = variantList.get(i).getAnnotation();
                    if (variantAnnotation != null) {
                        // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                        // at position i must correspond to variant i
                        if (variantAnnotation.getAdditionalAttributes() == null) {
                            variantAnnotation.setAdditionalAttributes(customAnnotation);
                        } else {
                            variantAnnotation.getAdditionalAttributes().putAll(customAnnotation);
                        }
                    } else {
                        variantAnnotation = new VariantAnnotation();
                        variantAnnotation.setAdditionalAttributes(customAnnotation);
                        variantList.get(i).setAnnotation(variantAnnotation);
                    }
                }
            }
        }
    }

    private Map<String, AdditionalAttribute> parseCustomAnnotation(Variant variant) {
        List<StudyEntry> studyEntryList = variant.getStudies();
        if (studyEntryList != null && !studyEntryList.isEmpty()) {
            StudyEntry studyEntry = studyEntryList.get(0);
            if (studyEntry.getFiles() != null && !studyEntry.getFiles().isEmpty()) {
                Map<String, String> customAnnotationMap = studyEntry.getFiles().get(0).getAttributes();
                if (customAnnotationMap != null && !customAnnotationMap.isEmpty()) {
                    AdditionalAttribute infoAttribute = new AdditionalAttribute();
                    infoAttribute.setAttribute(customAnnotationMap);
                    Map<String, AdditionalAttribute> customAnnotation = new HashMap<>(1);
                    customAnnotation.put(fileId, infoAttribute);

                    return customAnnotation;
                }
            }
        }
        return null;
    }

    private CellBaseDataResult<Variant> getCustomAnnotation(Variant variant) {
        CellBaseDataResult<Variant> customAnnotationCellBaseDataResult = new CellBaseDataResult<>();
        customAnnotationCellBaseDataResult.setId(variant.toString());
        long start = System.currentTimeMillis();
        try {
            byte[] variantKey = variant.toString().getBytes();
            byte[] dbContent = dbIndex.get(variantKey);
            if (dbContent != null) {
                Variant variant1 = mapper.readValue(dbContent, Variant.class);

                customAnnotationCellBaseDataResult.setResults(Collections.singletonList(variant1));
                customAnnotationCellBaseDataResult.setNumTotalResults(1);
                customAnnotationCellBaseDataResult.setNumResults(1);
            }
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        customAnnotationCellBaseDataResult.setTime((int) (end - start));

        return customAnnotationCellBaseDataResult;
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
