package org.opencb.cellbase.app.cli.variant.annotation.indexers;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.cellbase.core.variant.PhasedQueryManager;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.List;

public class PopulationFrequencyVariantIndexer extends VariantIndexer {
    public PopulationFrequencyVariantIndexer(VariantReader variantReader, int maxOpenFiles, boolean forceCreate) {
        super(variantReader, maxOpenFiles, forceCreate);
    }

    @Override
    protected void updateIndex(List<Variant> variantList) throws IOException, RocksDBException {
        for (Variant variant : variantList) {
            // If MNV then edit alternate allele to include a string tha represents all variants forming the MNV
            String haplotypeString = PhasedQueryManager.getSampleAttribute(variant, PhasedQueryManager.PHASE_SET_TAG);
            if (StringUtils.isNotBlank(haplotypeString)) {
                for (PopulationFrequency populationFrequency : variant.getAnnotation().getPopulationFrequencies()) {
                    populationFrequency.setAltAllele(haplotypeString);
                }
            }

            byte[] dbContent = dbIndex.get(variant.toString().getBytes());
            Variant variantToIndex;

            if (dbContent == null) {
                variantToIndex = variant;
            } else {
                variantToIndex = jsonObjectMapper.readValue(dbContent, Variant.class);

                // Add all pop frequencies from current variant
                variantToIndex
                        .getAnnotation()
                        .getPopulationFrequencies().addAll(variant.getAnnotation().getPopulationFrequencies());
            }

            dbIndex.put(variantToIndex.toString().getBytes(), jsonObjectWriter.writeValueAsBytes(variantToIndex));
        }
    }
}
