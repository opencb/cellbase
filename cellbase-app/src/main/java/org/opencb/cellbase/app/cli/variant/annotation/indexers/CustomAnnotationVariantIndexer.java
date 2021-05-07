package org.opencb.cellbase.app.cli.variant.annotation.indexers;

import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.*;

public class CustomAnnotationVariantIndexer extends VariantIndexer {
    private final Set<String> fieldSet;

    public CustomAnnotationVariantIndexer(VariantReader variantReader, int maxOpenFiles, List<String> fields) {
        this(variantReader, maxOpenFiles, false, fields);
    }

    public CustomAnnotationVariantIndexer(VariantReader variantReader, int maxOpenFiles, boolean forceCreate,
                                          List<String> fieldList) {
        super(variantReader, maxOpenFiles, forceCreate);

        this.fieldSet = new HashSet<>(fieldList);
    }

    @Override
    protected void updateIndex(List<Variant> variantList) throws IOException, RocksDBException {
        for (Variant variant : variantList) {
            if (variant.getType() != VariantType.NO_VARIATION) {
                dbIndex.put(variant.toString().getBytes(),
                        jsonObjectWriter.writeValueAsBytes(getVariantToIndex(variant)));
            }

        }

    }

    private Variant getVariantToIndex(Variant variant) {
        // Only essential variant data will be indexed
        VariantBuilder variantBuilder = new VariantBuilder(variant.getChromosome(),
                variant.getStart(),
                variant.getEnd(),
                variant.getReference(),
                variant.getAlternate());

        variantBuilder.setAttributes(parseInfoAttributes(variant));

        // Samples data contains the phase
        if (variant.getStudies() != null && !variant.getStudies().isEmpty()) {
            variantBuilder.setSamplesData(variant.getStudies().get(0).getSamplesData());
            variantBuilder.setFormat(variant.getStudies().get(0).getFormat());
        }

         return variantBuilder.build();
    }

    private Map<String, String> parseInfoAttributes(Variant variant) {
        Map<String, String> infoMap = variant.getStudies().get(0).getFiles().get(0).getAttributes();
        Map<String, String> parsedInfo = new HashMap<>();
        for (String attribute : infoMap.keySet()) {
            if (fieldSet.contains(attribute)) {
                parsedInfo.put(attribute, infoMap.get(attribute));
            }
        }

        return parsedInfo;
    }


}
