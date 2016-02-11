package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.io.DataReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariationDataReader implements DataReader<Variant> {

    private VariantDBAdaptor dbAdaptor;
    private Query query;
    private QueryOptions options;
    private Iterator<Variant> iterator;

    public VariationDataReader(VariantDBAdaptor dbAdaptor, Query query, QueryOptions options) {
        this.dbAdaptor = dbAdaptor;
        this.query = query;
        this.options = options;
    }

    public boolean open() {
        return true;
    }

    public boolean pre() {
        this.iterator = dbAdaptor.iterator(query, options);

        return this.iterator != null;
    }

    public List<Variant> read() {

        if (iterator.hasNext()) {
            return Collections.singletonList(iterator.next());
        } else {
            return null;
        }
    }

    public List<Variant> read(int batchSize) {
        List<Variant> listRecords = new ArrayList<>(batchSize);

        List<Variant> variants = this.read();
        int i = variants != null ? variants.size() : 0;
        while ((i < batchSize) && (variants != null)) {
            listRecords.addAll(variants);
            variants = this.read();
            i += variants.size();
        }

        return listRecords;
    }

    public boolean post() {
        return true;
    }

    public boolean close() {
        return true;
    }

}
