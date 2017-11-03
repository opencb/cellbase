package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.variant.avro.AlleleOrigin;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.FeatureTypes;
import org.opencb.biodata.models.variant.avro.GenomicFeature;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 04/10/16.
 */
public abstract class ClinicalIndexer {

    protected static Logger logger
            = LoggerFactory.getLogger("org.opencb.cellbase.app.transform.clinical.variant.ClinicalIndexer");

    protected int numberNewVariants = 0;
    protected int numberVariantUpdates = 0;
    protected int totalNumberRecords = 0;
    protected int numberIndexedRecords = 0;
    protected RocksDB rdb;


    private static final String SYMBOL = "symbol";

    protected static ObjectMapper mapper;
    protected static ObjectWriter jsonObjectWriter;

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = mapper.writer();
    }

    public ClinicalIndexer() {
    }


    protected List<EvidenceEntry> getEvidenceEntryList(byte[] key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key);
        List<EvidenceEntry> evidenceEntryList;
        if (dbContent == null) {
            evidenceEntryList = new ArrayList<>();
            numberNewVariants++;
        } else {
            evidenceEntryList = mapper.readValue(dbContent, mapper.getTypeFactory().constructParametrizedType(List.class,
                    List.class, EvidenceEntry.class));
            numberVariantUpdates++;
        }
        return evidenceEntryList;
    }

    protected GenomicFeature createGeneGenomicFeature(String gene) {
        Map<String, String> map = new HashMap<>(1);
        map.put(SYMBOL, gene);

        return new GenomicFeature(FeatureTypes.gene, null, map);
    }

    protected List<AlleleOrigin> getAlleleOriginList(List<String> sourceOriginList) {
        List<AlleleOrigin> alleleOrigin;
        alleleOrigin = new ArrayList<>(sourceOriginList.size());
        for (String originString : sourceOriginList) {
            if (VariantAnnotationUtils.ORIGIN_STRING_TO_ALLELE_ORIGIN.containsKey(originString)) {
                alleleOrigin.add(VariantAnnotationUtils.ORIGIN_STRING_TO_ALLELE_ORIGIN.get(originString));
            } else {
                logger.debug("No SO term found for allele origin {}. Skipping.", originString);
            }
        }
        return alleleOrigin;
    }

    class SequenceLocation {
        private String chromosome;
        private int start;
        private int end;
        private String reference;
        private String alternate;
        private String strand;

        SequenceLocation() {
        }

        SequenceLocation(String chromosome, int start, int end, String reference, String alternate) {
            this(chromosome, start, end, reference, alternate, "+");
        }

        SequenceLocation(String chromosome, int start, int end, String reference, String alternate, String strand) {
            this.chromosome = chromosome;
            this.start = start;
            this.end = end;
            this.reference = reference;
            this.alternate = alternate;
            this.strand = strand;
        }

        public String getChromosome() {
            return chromosome;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getReference() {
            return reference;
        }

        public String getAlternate() {
            return alternate;
        }

        public String getStrand() {
            return strand;
        }

        public void setChromosome(String chromosome) {
            this.chromosome = chromosome;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public void setAlternate(String alternate) {
            this.alternate = alternate;
        }

        public void setStrand(String strand) {
            this.strand = strand;
        }
    }


}
