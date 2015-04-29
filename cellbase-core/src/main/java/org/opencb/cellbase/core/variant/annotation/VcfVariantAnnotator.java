package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

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

    /**
     * Gets the custom annotation in this VCF file for a list of Variants. variantAnnotationList may already contain
     * annotations in which case would be updated.
     *
     * @param variantList List of Variant objects. IF variantAnnotationList already contains annotations, it will be
     *                    updated. In order for variantAnnotationList to be properly updated, variantList and
     *                    variantAnnotationList must contain variants in the SAME order: variantAnnotation at position
     *                    i must correspond to variant i.
     */
    @Override
    public List<VariantAnnotation> run(List<Variant> variantList) {
        if(variantAnnotationList==null) {
            variantAnnotationList = new ArrayList<>(variantList.size());
        }
        if(!variantAnnotationList.isEmpty()) {
            udpateVariantAnnotationList(variantList);
        } else {
            fillVariantAnnotationList(variantList);
        }
        return variantAnnotationList;
    }

    private void fillVariantAnnotationList(List<Variant> variantList) {
        for(int i=0; i<variantList.size(); i++) {
            VariantAnnotation variantAnnotation = new VariantAnnotation();
            Map<String, Object> customAnnotation = getInfoAnnotation(variantList.get(i));
            // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
            // exists in the vcf but the info field does not contain any of the required attributes
            if(customAnnotation!=null && ((Map) customAnnotation.get(fileId)).size()>0) {
                variantAnnotation.setAdditionalAttributes(customAnnotation);
            }
            variantAnnotationList.add(variantAnnotation);
        }
    }

    /**
     * Updates VariantAnnotation objects in variantAnnotationList.
     *
     * @param variantList List of Variant objects. variantList and variantAnnotationList must contain variants in the
     *                    SAME order: variantAnnotation at position i must correspond to variant i
     */
    private void udpateVariantAnnotationList(List<Variant> variantList) {
        for(int i=0; i<variantList.size(); i++) {
            Map<String, Object> customAnnotation = getInfoAnnotation(variantList.get(i));
            // Update only if there are annotations for this variant. customAnnotation may be empty if the variant
            // exists in the vcf but the info field does not contain any of the required attributes
            if(customAnnotation!=null && ((Map) customAnnotation.get(fileId)).size()>0) {
                Map<String,Object> additionalAttributes;
                if((additionalAttributes = variantAnnotationList.get(i).getAdditionalAttributes()) == null) {
                    // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                    // at position i must correspond to variant i
                    variantAnnotationList.get(i).setAdditionalAttributes(customAnnotation);
                } else {
                    additionalAttributes.putAll(customAnnotation);
                    // variantList and variantAnnotationList must contain variants in the SAME order: variantAnnotation
                    // at position i must correspond to variant i
                    variantAnnotationList.get(i).setAdditionalAttributes(additionalAttributes);
                }
            }
        }
    }

    private Map<String,Object> getInfoAnnotation(Variant variant) {
        try {
            byte[] dbContent = dbIndex.get((variant.getChromosome() + "_" + variant.getStart() + "_"
                    + variant.getReference() + "_" + variant.getAlternate()).getBytes());
            if(dbContent==null) {
                return null;
            } else {
                reader.seek(ByteBuffer.wrap(dbContent).getLong());
                String[] fields = reader.readLine().split("\t");

                return parseInfoAttributes(fields[7], getAlleleNumber(variant.getAlternate(), fields[4]));
            }
        } catch (RocksDBException | IOException e) {
            return null;
        }
    }

    protected int getAlleleNumber(String alternate, String altField) {
        return Arrays.asList(altField.split(",")).indexOf(alternate);
    }

    protected Map<String, Object> parseInfoAttributes(String info, int numAllele) {
        Map<String, Object> infoAttributes = new HashMap<>();
        for (String var : info.split(";")) {
            String[] splits = var.split("=");
            if (splits.length == 2 && infoFields.contains(splits[0])) {
                // Managing values for the allele
                String[] values = splits[1].split(",");
                if(NumberUtils.isNumber(values[numAllele])) {
                    try {
                        infoAttributes.put(splits[0], Integer.parseInt(values[numAllele]));
                    } catch (NumberFormatException e) {
                        try {
                            infoAttributes.put(splits[0], Float.parseFloat(values[numAllele]));
                        } catch (NumberFormatException e1) {
                            infoAttributes.put(splits[0], Double.parseDouble(values[numAllele]));
                        }
                    }
                } else {
                    infoAttributes.put(splits[0], values[numAllele]);
                }
            }
        }
        Map<String,Object> customAnnotations = new HashMap<>(1);
        customAnnotations.put(fileId, infoAttributes);

        return customAnnotations;
    }

    public boolean close() {
        try {
            reader.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void setVariantAnnotationList(List<VariantAnnotation> variantAnnotationList) {
        this.variantAnnotationList = variantAnnotationList;
    }
}
