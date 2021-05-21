package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.FileEntry;
import org.opencb.biodata.models.variant.avro.StudyEntry;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.run.ParallelTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariantAnnotatorTask implements
        ParallelTaskRunner.TaskWithException<Variant, Variant, Exception> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<VariantAnnotator> variantAnnotatorList;
    private QueryOptions serverQueryOptions;
    private static final String FILTER_PARAM = "filter";

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList) {
        this(variantAnnotatorList, new QueryOptions());
    }

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList, QueryOptions serverQueryOptions) {
        this.variantAnnotatorList = variantAnnotatorList;
        this.serverQueryOptions = serverQueryOptions;
    }

    public void pre() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.open();
        }
    }

    public List<Variant> apply(List<Variant> batch) throws Exception {
        List<Variant> variantListToAnnotate = filter(batch);
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.run(variantListToAnnotate);
        }
        return variantListToAnnotate;
    }

    private List<Variant> filter(List<Variant> variantList) {
        List<Variant> filteredVariantList = new ArrayList<>(variantList.size());
        String queryOptionsFilterValue = null;
        if (serverQueryOptions != null && serverQueryOptions.containsKey(FILTER_PARAM)) {
            queryOptionsFilterValue = (String) serverQueryOptions.get(FILTER_PARAM);
        }
        for (Variant variant : variantList) {
            // true when we find a FILTER match. to prevent variant being added twice.
            boolean variantFound = false;
            // filter out reference blocks
            if (!VariantType.NO_VARIATION.equals(variant.getType())) {
                // if FILTER param set, VCF line must match or it's skipped
                if (queryOptionsFilterValue != null) {
                    Iterator<StudyEntry> studyIterator = variant.getImpl().getStudies().listIterator();
                    while (studyIterator.hasNext() && !variantFound) {
                        for (FileEntry fileEntry : studyIterator.next().getFiles()) {
                            Map<String, String> attributes = fileEntry.getAttributes();
                            String vcfFilterValue = attributes.get("FILTER");
                            if (vcfFilterValue != null && vcfFilterValue.equalsIgnoreCase(queryOptionsFilterValue)) {
                                // matched, variant added. we are done here.
                                filteredVariantList.add(variant);
                                variantFound = true;
                                break;
                            }
                        }
                    }
                } else {
                    filteredVariantList.add(variant);
                }
            }
        }

        return filteredVariantList;
    }

    public void post() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.close();
        }
    }

}
