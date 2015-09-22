package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.cellbase.core.db.DBAdaptor;
import org.opencb.cellbase.core.db.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 22/09/15.
 */
public class CellBaseLocalVariantAnnotator implements VariantAnnotator {
    private VariantAnnotationDBAdaptor variantAnnotationDBAdaptor;
    private List<VariantAnnotation> variantAnnotationList;

    private Logger logger;

    public CellBaseLocalVariantAnnotator(VariantAnnotationDBAdaptor variantAnnotationDBAdaptor) {
        this.variantAnnotationDBAdaptor = variantAnnotationDBAdaptor;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public boolean open() {
        return true;
    }

    public boolean close() {
        return false;
    }

    public List<VariantAnnotation> run(List<Variant> variantList) {

        List<GenomicVariant> batch = convertVariantsToGenomicVariants(variantList);
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", batch.size());
        List<QueryResult> queryResultList =
                variantAnnotationDBAdaptor.getAnnotationByVariantList(batch, new QueryOptions());

        //TODO: assuming CellBase annotation will always be the first and therefore variantAnnotationList will be empty
//        variantAnnotationList = new ArrayList<>(variantList.size());
        for (QueryResult<VariantAnnotation> queryResult : queryResultList) {
            if (queryResult.getResult().size() > 0) {
                variantAnnotationList.add(queryResult.getResult().get(0));
            } else {
                logger.warn("Emtpy result for '{}'", queryResult.getId());
            }
        }
        return variantAnnotationList;
    }


    private List<GenomicVariant> convertVariantsToGenomicVariants(List<Variant> vcfBatch) {
        List<GenomicVariant> genomicVariants = new ArrayList<>(vcfBatch.size());
        for (Variant variant : vcfBatch) {
            GenomicVariant genomicVariant;
            if((genomicVariant = getGenomicVariant(variant))!=null) {
                genomicVariants.add(genomicVariant);
            }
        }
        return genomicVariants;
    }

    // TODO: use a external class for this (this method could be added to GenomicVariant class)
    private GenomicVariant getGenomicVariant(Variant variant) {
        if(variant.getAlternate().equals(".")) {  // reference positions are not variants
            return null;
        } else {
            String ref;
            if (variant.getAlternate().equals("<DEL>")) {  // large deletion
                int end = Integer.valueOf(variant.getSourceEntries().get("_").getAttributes().get("END"));  // .get("_") because studyId and fileId are empty strings when VariantSource is initialized at readInputFile
                ref = StringUtils.repeat("N", end - variant.getStart());
                return new GenomicVariant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
                // TODO: structural variants are not yet properly handled. Implement and remove this patch asap
            } else if(variant.getAlternate().startsWith("<") || (variant.getAlternate().length()>1 && variant.getReference().length()>1)) {
                return null;
            } else {
                ref = variant.getReference().equals("") ? "-" : variant.getReference();
                return new GenomicVariant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
            }
//            return new GenomicVariant(variant.getChromosome(), variant.getStart(),
//                    ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
        }
    }

    public void setVariantAnnotationList(List<VariantAnnotation> variantAnnotationList) {
        this.variantAnnotationList = variantAnnotationList;
    }

}
