/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.app.cli.main.annotation;

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
    private static final String VARIANT_STRING_PATTERN = "[ACGT]*";

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

    /**
     * Performs one read of from a CellBase variation collection.
     *
     * @return  List of variants. It can be expected to contain only one variant.
     */
    public List<Variant> read() {
        Variant variant = null;
        boolean valid = false;
        while (iterator.hasNext() && !valid) {
            variant = iterator.next();
            valid = isValid(variant);
        }
        if (valid) {
            // New variants in the variation collection created during the update of the frequencies may not have
            // the variant type set and this might cause NPE
            if (variant.getType() == null) {
                variant.setType(variant.inferType(variant.getReference(), variant.getAlternate()));
            }
            return Collections.singletonList(variant);
        } else {
            return null;
        }
    }

    /**
     * Checks whether a variant is valid.
     *
     * @param variant Variant object to be checked.
     * @return   true/false depending on whether 'variant' does contain valid values. Currently just a simple check of
     * reference/alternate attributes being strings of [A,C,G,T] of length >= 0 is performed to detect cases such as
     * 19:13318673:(CAG)4:(CAG)5 which are not currently supported by CellBase. Ref and alt alleles must be different
     * as well for the variant to be valid. Functionality of the method may be improved in the future.
     */
    private boolean isValid(Variant variant) {
        return (variant.getReference().matches(VARIANT_STRING_PATTERN)
                && variant.getAlternate().matches(VARIANT_STRING_PATTERN)
                && !variant.getAlternate().equals(variant.getReference()));
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
