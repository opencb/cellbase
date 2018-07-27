package org.opencb.cellbase.lib.impl;

import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.tools.sequence.SequenceAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CellBaseNormalizerSequenceAdaptor implements SequenceAdaptor {
    private static final String EMPTY_STRING = "";
    private final GenomeMongoDBAdaptor genomeMongoDBAdaptor;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CellBaseNormalizerSequenceAdaptor(GenomeMongoDBAdaptor genomeMongoDBAdaptor) {
        this.genomeMongoDBAdaptor = genomeMongoDBAdaptor;
    }

    @Override
    public String query(String contig, int start, int end) throws Exception {
         QueryResult<GenomeSequenceFeature> queryResult
                 = genomeMongoDBAdaptor.getSequence(new Region(contig, start, end), QueryOptions.empty());

         if (queryResult.getNumResults() > 0) {
             return queryResult.getResult().get(0).getSequence();
         } else {
             logger.warn("No sequence found for {}:{}-{}", contig, start, end);
             return EMPTY_STRING;
         }
    }
}
