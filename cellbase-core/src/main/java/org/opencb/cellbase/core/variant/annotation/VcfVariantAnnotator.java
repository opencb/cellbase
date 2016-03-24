package org.opencb.cellbase.core.variant.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 28/04/15.
 */
public class VcfVariantAnnotator implements VariantAnnotator {

    private String fileName;
    private RocksDB dbIndex;
    private String fileId;
    private List<String> infoFields;
    private RandomAccessFile reader;
    private List<VariantAnnotation> variantAnnotationList;

    public VcfVariantAnnotator(String fileName, RocksDB dbIndex, String fileId, List<String> infoFields) {
        this.fileName = fileName;
        this.dbIndex = dbIndex;
        this.fileId = fileId;
        this.infoFields = infoFields;
    }

    public boolean open() {
        try {
            reader = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

//    /**
//     * Gets the custom annotation in this VCF file for a list of Variants. variantAnnotationList may already contain
//     * annotations in which case would be updated.
//     *
//     * @param variantList List of Variant objects. IF variantAnnotationList already contains annotations, it will be
//     *                    updated. In order for variantAnnotationList to be properly updated, variantList and
//     *                    variantAnnotationList must contain variants in the SAME order: variantAnnotation at position
//     *                    i must correspond to variant i.
//     */
//    @Override
//    public void run(List<Variant> variantList) {
//        if (variantAnnotationList == null) {
//            variantAnnotationList = new ArrayList<>(variantList.size());
//        }
//        if (!variantAnnotationList.isEmpty()) {
//            udpateVariantAnnotationList(variantList);
//        } else {
//            fillVariantAnnotationList(variantList);
//        }
//    }
//
//    private void fillVariantAnnotationList(List<Variant> variantList) {
//        for (int i = 0; i < variantList.size(); i++) {
//            VariantAnnotation variantAnnotation = new VariantAnnotation();
//            Map<String, Object> customAnnotation = getCustomAnnotation(variantList.get(i));
//            // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
//            // exists in the vcf but the info field does not contain any of the required attributes
////            Map<String, String> auxMap = new HashMap<>();
////            customAnnotation.forEach((k, v) -> auxMap.put(k, v.toString()));
//            if (customAnnotation != null && ((Map) customAnnotation.get(fileId)).size() > 0) {
//                variantAnnotation.setAdditionalAttributes(customAnnotation);
//                //variantAnnotation.setAdditionalAttributes(auxMap);
//            }
//            variantAnnotationList.add(variantAnnotation);
//        }
//    }

    /**
     * Updates VariantAnnotation objects in variantAnnotationList.
     *
     * @param variantList List of Variant objects. variantList and variantAnnotationList must contain variants in the
     *                    SAME order: variantAnnotation at position i must correspond to variant i
     */
    public void run(List<Variant> variantList) {
        for (int i = 0; i < variantList.size(); i++) {
            Map<String, Object> customAnnotation = getCustomAnnotation(variantList.get(i));
            // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
            // exists in the vcf but the info field does not contain any of the required attributes
            if (customAnnotation != null && ((Map) customAnnotation.get(fileId)).size() > 0) {
                VariantAnnotation variantAnnotation = variantList.get(i).getAnnotation();
                if (variantAnnotation != null) {
                    Map<String, Object> additionalAttributes = variantAnnotation.getAdditionalAttributes();
                    if (additionalAttributes == null) {
                        // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                        // at position i must correspond to variant i
                        variantAnnotation.setAdditionalAttributes(customAnnotation);
                    } else {
                        additionalAttributes.putAll(customAnnotation);
                        // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                        // at position i must correspond to variant i
//                        variantAnnotation.setAdditionalAttributes(additionalAttributes);
                    }
                }
            }
        }
    }

    private Map<String, Object> getCustomAnnotation(Variant variant) {
        try {
            byte[] dbContent = dbIndex.get((variant.getChromosome() + "_" + variant.getStart() + "_"
                    + variant.getReference() + "_" + variant.getAlternate()).getBytes());
            if (dbContent == null) {
                return null;
            } else {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> infoAttributes = mapper.readValue(dbContent, Map.class);
                Map<String, Object> customAnnotation = new HashMap<>(1);
                customAnnotation.put(fileId, infoAttributes);

                return customAnnotation;
            }
        } catch (RocksDBException | IOException e) {
            return null;
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
