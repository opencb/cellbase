package org.opencb.cellbase.build.transform.serializers;

import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.common.GenericFeatureChunk;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.core.GenomeSequenceChunk;
import org.opencb.cellbase.core.common.protein.Interaction;
import org.opencb.cellbase.core.common.variation.Mutation;
import org.opencb.cellbase.core.common.variation.Variation;
import org.opencb.cellbase.core.common.variation.VariationPhenotypeAnnotation;
import org.opencb.commons.bioformats.protein.uniprot.v201311jaxb.Entry;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CellBaseSerializer {

    public void serialize(Gene gene);

    public void serialize(Entry protein);

    public void serialize(Variation variation);

    public void serialize(GenericFeature genericFeature);

    public void serialize(GenomeSequenceChunk genomeSequenceChunk);

    public void serialize(VariationPhenotypeAnnotation variationPhenotypeAnnotation);

    public void serialize(Mutation mutation);

    public void serialize(Interaction interaction);

    public void close();

}
