package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 22/09/15.
 */
public class CellBaseLocalVariantAnnotator implements VariantAnnotator {

    private VariantAnnotationCalculator variantAnnotationCalculator;
    private QueryOptions queryOptions;

    private Logger logger;

    public CellBaseLocalVariantAnnotator(VariantAnnotationCalculator variantAnnotationCalculator, QueryOptions queryOptions) {
        this.variantAnnotationCalculator = variantAnnotationCalculator;
        this.queryOptions = queryOptions;
        logger = LoggerFactory.getLogger(this.getClass());
    }


    public boolean open() {
        return true;
    }

    public void run(List<Variant> variantList) throws InterruptedException, ExecutionException {
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", variantList.size());

        // getAnnotationByVariantList will not create new Variant objects but modify the ones passed as parameters - no
        // need to go through the queryResultList afterwards
        List<QueryResult<VariantAnnotation>> queryResultList =
                variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);

//        //TODO: assuming CellBase annotation will always be the first and therefore variantAnnotationList will be empty
//        for (int i = 0; i < queryResultList.size(); i++) {
//            if (queryResultList.get(i).getResult().size() > 0) {
//                if (variantList.get(i).getAnnotation() == null) {
//                    variantList.get(i).setAnnotation(queryResultList.get(i).getResult().get(0));
//                } else {
//                    mergeAnnotation(variantList.get(i).getAnnotation(), queryResultList.get(i).getResult().get(0));
//                }
//            } else {
//                logger.warn("Emtpy result for '{}'", queryResultList.get(i).getId());
//            }
//        }
    }

    public boolean close() {
        return false;
    }

}
