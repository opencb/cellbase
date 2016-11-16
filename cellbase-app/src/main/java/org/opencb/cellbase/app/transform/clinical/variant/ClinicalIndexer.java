package org.opencb.cellbase.app.transform.clinical.variant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ClinicalIndexer() {
    }

    class SequenceLocation {
        private String chromosome;
        private int start;
        private int end;
        private String reference;
        private String alternate;
        private String strand;

        public SequenceLocation() {
        }

        public SequenceLocation(String chromosome, int start, int end, String reference, String alternate) {
            this(chromosome, start, end, reference, alternate, "+");
        }

        public SequenceLocation(String chromosome, int start, int end, String reference, String alternate, String strand) {
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
