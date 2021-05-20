package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.FileEntry;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.run.ParallelTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        String filterValue = null;
        if (serverQueryOptions != null && serverQueryOptions.containsKey(FILTER_PARAM)) {
            filterValue = (String) serverQueryOptions.get(FILTER_PARAM);
        }
        for (Variant variant : variantList) {
            // if filter set, VCF line must match or skip
            if (filterValue != null) {
                boolean filterMatch = true;
                List<org.opencb.biodata.models.variant.avro.StudyEntry> studyEntryList = variant.getImpl().getStudies();
                for (org.opencb.biodata.models.variant.avro.StudyEntry studyEntry : studyEntryList) {
                    FileEntry fileEntry = studyEntry.getFiles().get(0);
                    Map<String, String> attributes = fileEntry.getAttributes();
                    String filterVcfValue = attributes.get("FILTER");
                    if (!filterVcfValue.equalsIgnoreCase(filterValue)) {
                        // filter is set but there is no match. skip.
                        filterMatch = false;
                    }
                }
                if (!filterMatch) {
                    // filter is set but there is no match. skip.
                    continue;
                }
            }
            // filter out reference blocks
            if (!VariantType.NO_VARIATION.equals(variant.getType())) {
                filteredVariantList.add(variant);
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
