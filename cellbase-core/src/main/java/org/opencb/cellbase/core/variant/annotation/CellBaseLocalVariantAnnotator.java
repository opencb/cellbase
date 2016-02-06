package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.VariantAnnotationDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by fjlopez on 22/09/15.
 */
public class CellBaseLocalVariantAnnotator implements VariantAnnotator {

    private VariantAnnotationDBAdaptor variantAnnotationDBAdaptor;

    private QueryOptions queryOptions;

    private Logger logger;

    public CellBaseLocalVariantAnnotator(VariantAnnotationDBAdaptor variantAnnotationDBAdaptor) {
        this(variantAnnotationDBAdaptor, new QueryOptions());
    }

    public CellBaseLocalVariantAnnotator(VariantAnnotationDBAdaptor variantAnnotationDBAdaptor, QueryOptions queryOptions) {
        this.variantAnnotationDBAdaptor = variantAnnotationDBAdaptor;
        this.queryOptions = queryOptions;

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public boolean open() {
        return true;
    }

    public boolean close() {
        return false;
    }

    public void run(List<Variant> variantList) {
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", variantList.size());
        List<QueryResult> queryResultList = variantAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);
        //TODO: assuming CellBase annotation will always be the first and therefore variantAnnotationList will be empty
//        variantAnnotationList = new ArrayList<>(variantList.size());
        for (int i = 0; i < queryResultList.size(); i++) {
            if (queryResultList.get(i).getResult().size() > 0) {
                variantList.get(i).setAnnotation((VariantAnnotation) queryResultList.get(i).getResult().get(0));
            } else {
                logger.warn("Emtpy result for '{}'", queryResultList.get(i).getId());
            }
        }
    }


    // TODO: use a external class for this (this method could be added to GenomicVariant class)
    private Variant getGenomicVariant(Variant variant) {
        if (variant.getAlternate().equals(".")) {  // reference positions are not variants
            return null;
        } else {
            String ref;
            if (variant.getAlternate().equals("<DEL>")) {  // large deletion
                // .get("_") because studyId and fileId are empty strings when VariantSource is initialized at readInputFile
                int end = Integer.valueOf(variant.getSourceEntries().get("_").getAttributes().get("END"));
                ref = StringUtils.repeat("N", end - variant.getStart());
                return new Variant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
                // TODO: structural variants are not yet properly handled. Implement and remove this patch asap
            } else if (variant.getAlternate().startsWith("<")
                    || (variant.getAlternate().length() > 1 && variant.getReference().length() > 1)) {
                return null;
            } else {
                ref = variant.getReference().equals("") ? "-" : variant.getReference();
                return new Variant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
            }
        }
    }

}
