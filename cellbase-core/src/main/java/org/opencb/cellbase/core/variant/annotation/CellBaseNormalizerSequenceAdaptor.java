package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.tools.sequence.SequenceAdaptor;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CellBaseNormalizerSequenceAdaptor implements SequenceAdaptor {
    private final GenomeDBAdaptor genomeDBAdaptor;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CellBaseNormalizerSequenceAdaptor(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    /**
     * Returns sequence from contig "contig" in the range [start, end] (1-based, both inclusive).
     * For corner cases mimics the behaviour of the org.opencb.biodata.tools.sequence.SamtoolsFastaIndex with one
     * difference. If chromosome does not exist, start is under the left bound, start AND end are out of the right
     * bound, then a RunTime exception will be thrown. HOWEVER: if start is within the bounds BUT end is out of the
     * right bound, then THIS implementaiton will return available nucleotides while SamtoolsFastaIndex will keep
     * returning the exception.
     * @param contig
     * @param start
     * @param end
     * @return String containing the sequence of contig "contig" in the range [start, end] (1-based, both inclusive).
     * Throws RunTimeException if contig does not exist, or start is under the left bound, or start AND end are out of the right
     * bound. If start is within the bounds BUT end is out of the
     * right bound, then THIS implementaiton will return available nucleotides while SamtoolsFastaIndex will keep
     * returning the exception.
     * @throws Exception
     * @throws RuntimeException
     */
    @Override
    public String query(String contig, int start, int end) throws Exception {
        Region region = new Region(contig, start, end);
        QueryResult<GenomeSequenceFeature> queryResult
                 = genomeDBAdaptor.getSequence(region, QueryOptions.empty());

        // This behaviour mimics the behaviour of the org.opencb.biodata.tools.sequence.SamtoolsFastaIndex with one
        // difference. If contig does not exist, start is under the left bound, start AND end are out of the right
        // bound, then a RunTime exception will be thrown. HOWEVER: if start is within the bounds BUT end is out of the
        // right bound, then THIS implementaiton will return available nucleotides while SamtoolsFastaIndex will keep
        // returning the exception.
        if (queryResult.getResult().size() > 0 && StringUtils.isNotBlank(queryResult.getResult().get(0).getSequence())) {
            if (queryResult.getResult().get(0).getSequence().length() < (end - start + 1)) {
                logger.warn("End coordinate out of the right bound. Returning available nucleotides.");
            }
            return queryResult.getResult().get(0).getSequence();
        } else {
            throw new RuntimeException("Unable to find entry for " + region.toString());
        }
    }
}
