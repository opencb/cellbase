package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;

import java.util.List;

/**
 * Created by fjlopez on 19/06/15.
 */
public interface ConsequenceTypeCalculator {

    public List<ConsequenceType> run(GenomicVariant variant, List<Gene> geneList, List<Region> regulatoryRegionList);

}
