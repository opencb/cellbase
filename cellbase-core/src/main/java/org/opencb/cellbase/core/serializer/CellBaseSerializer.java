package org.opencb.cellbase.core.serializer;

import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.biodata.models.protein.Interaction;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.Mutation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.opencb.cellbase.core.common.GenericFeature;

import java.nio.file.Path;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CellBaseSerializer implements AutoCloseable {

    protected Path outdirPath;
    protected Path outputFileName;
    protected boolean serializeEmptyValues;

    public CellBaseSerializer() {

    }

    public CellBaseSerializer(Path outdirPath) {
        this.outdirPath = outdirPath;
    }

    public void setOutputFileName(Path outputFileName) {
        this.outputFileName = outputFileName;
    }

    public abstract void serialize(Gene gene);

    public abstract void serialize(Entry protein);

    public abstract void serialize(Variation variation);

    public abstract void serialize(VariantAnnotation variantAnnotation);

    public abstract void serialize(GenericFeature genericFeature);

    public abstract void serialize(GenomeSequenceChunk genomeSequenceChunk);

    public abstract void serialize(VariationPhenotypeAnnotation variationPhenotypeAnnotation);

    public abstract void serialize(Mutation mutation);

    public abstract void serialize(Interaction interaction);

    public abstract void serialize(ConservedRegionChunk conservedRegionChunk);

    public abstract void serialize(Object object);

    public abstract void close();

    public void setSerializeEmptyValues(boolean serializeEmptyValues) {
        this.serializeEmptyValues = serializeEmptyValues;
    }
}
