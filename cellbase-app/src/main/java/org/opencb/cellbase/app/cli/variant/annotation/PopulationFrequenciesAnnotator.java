package org.opencb.cellbase.app.cli.variant.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 18/07/16.
 */
public class PopulationFrequenciesAnnotator implements VariantAnnotator {

    private String fileName;
    private RocksDB dbIndex;
    private RandomAccessFile reader;
    private List<VariantAnnotation> variantAnnotationList;

    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter jsonObjectWriter;

    static {
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = mapper.writer();
    }

    public PopulationFrequenciesAnnotator(String fileName, RocksDB dbIndex) {
        this.fileName = fileName;
        this.dbIndex = dbIndex;
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
        for (int i = 0; i < variantList.size(); i++) {
            List<PopulationFrequency> populationFrequencies = getPopulationFrequencies(variantList.get(i));
            // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
            // exists in the vcf but the info field does not contain any of the required attributes
            if (populationFrequencies != null && populationFrequencies.size() > 0) {
                VariantAnnotation variantAnnotation = variantList.get(i).getAnnotation();
                if (variantAnnotation != null) {
                    // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                    // at position i must correspond to variant i
                    variantAnnotation.setPopulationFrequencies(populationFrequencies);
                }
            }
        }
    }

    private List<PopulationFrequency> getPopulationFrequencies(Variant variant) {
        try {
            byte[] variantKey = VariantAnnotationUtils.buildVariantId(variant.getChromosome(),
                    variant.getStart(), variant.getReference(), variant.getAlternate()).getBytes();
            byte[] dbContent = dbIndex.get(variantKey);
            if (dbContent == null) {
                return null;
            } else {
//                dbIndex.remove(variantKey);
                VariantAvro variantAvro = mapper.readValue(dbContent, VariantAvro.class);
                flagVisitedVariant(variantKey, variantAvro);

                return variantAvro.getAnnotation().getPopulationFrequencies();
            }
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void flagVisitedVariant(byte[] key, VariantAvro variantAvro) {
        // The annotation.additionalAttributes field is initialized with an empty map to flag this variant as
        // already visited
        variantAvro.getAnnotation().setAdditionalAttributes(Collections.emptyMap());
        try {
            dbIndex.put(key, jsonObjectWriter.writeValueAsBytes(variantAvro));
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
