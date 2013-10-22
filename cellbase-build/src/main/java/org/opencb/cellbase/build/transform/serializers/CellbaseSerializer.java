package org.opencb.cellbase.build.transform.serializers;

import org.opencb.cellbase.build.transform.MutationParser;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.core.GenomeSequenceChunk;
import org.opencb.cellbase.core.common.variation.Mutation;
import org.opencb.cellbase.core.common.variation.Variation;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CellbaseSerializer {

    public void serialize(Gene gene);

    public void serialize(Variation variation);

    public void serialize(GenomeSequenceChunk genomeSequenceChunk);

    public void serialize(Mutation mutation);

    public void close();

}
