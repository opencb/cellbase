package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.io.DataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int nReadVariants = 0;

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
            Variant variant = iterator.next();
            // New variants in the variation collection created during the update of the frequencies may not have
            // the variant type set and this might cause NPE
            if (variant.getType() == null) {
                variant.setType(variant.inferType(variant.getReference(), variant.getAlternate(),
                        variant.getLength()));
            }
            return Collections.singletonList(variant);
        } else {
            return null;
        }
    }

    public List<Variant> read(int batchSize) {
        List<Variant> listRecords = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            List<Variant> variants = this.read();
            if (variants != null) {
                listRecords.addAll(variants);
                nReadVariants += variants.size();
            } else {
                logger.info("{} variants read", nReadVariants);
                return listRecords;
            }
        }

        logger.info("{} variants read", nReadVariants);
        return listRecords;
    }

    public boolean post() {
        return true;
    }

    public boolean close() {
        return true;
    }

}
