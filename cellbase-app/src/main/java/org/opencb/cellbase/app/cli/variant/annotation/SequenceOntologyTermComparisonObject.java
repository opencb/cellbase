package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

/**
 * Created by fjlopez on 08/04/16.
 */
public class SequenceOntologyTermComparisonObject {

    private static final String SO_0001631 = "SO:0001631"; // upstream_variant
    private static final String SO_0001636 = "SO:0001636"; // 2KB_upstream_variant
    private static final String SO_0001632 = "SO:0001632"; // downstream_variant
    private static final String SO_0002083 = "SO:0002083"; // 2KB_downstream_variant
    private String transcriptId;
    private String name;
    private String accession;

    public SequenceOntologyTermComparisonObject(String transcriptId, SequenceOntologyTerm sequenceOntologyTerm) {
        this.transcriptId = transcriptId;
        this.name = sequenceOntologyTerm.getName();
        this.accession = sequenceOntologyTerm.getAccession();
    }
//
//    public boolean equals(SequenceOntologyTermComparisonObject o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null) {
//            return false;
//        }
//
//        if (getTranscriptId() != null ? !getTranscriptId().equals(o.getTranscriptId()) : o.getTranscriptId() != null) {
//            return false;
//        }
//        if (getAccession() != null ? !getAccession().equals(o.getAccession()) : o.getAccession() != null) {
//            return false;
//        }
//        if (getName() != null ? !equivalentAccession(getName(), o.getName()) : o.getName() != null) {
//            return false;
//        }
//
//        return true;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SequenceOntologyTermComparisonObject that = (SequenceOntologyTermComparisonObject) o;

        if (getTranscriptId() != null ? !getTranscriptId().equals(that.getTranscriptId()) : that.getTranscriptId() != null) {
            return false;
        }
        return getEquivalentAccession() != null ? getEquivalentAccession().equals(that.getEquivalentAccession())
                : that.getEquivalentAccession() == null;

    }

    public String getEquivalentAccession() {
        // VEP does not use 2KB_upstream/downstream_variant. This class is exclusively used for CellBase - VEP
        // comparison and in this comparison these two terms must be considered  equivalent to upstream/downstream_variant
        if (SO_0001636.equals(getAccession())) {
            return SO_0001631;
        } else if (SO_0002083.equals(getAccession())) {
            return SO_0001632;
        } else {
            return getAccession();
        }
    }

    @Override
    public int hashCode() {
        int result = getTranscriptId() != null ? getTranscriptId().hashCode() : 0;
        result = 31 * result + (getEquivalentAccession() != null ? getEquivalentAccession().hashCode() : 0);
        return result;
    }

    private boolean equalsName(String name1, String name2) {
        if (name1.equals(name2)) {
            return true;
        } else if (((name1.equals(VariantAnnotationUtils.TWOKB_UPSTREAM_VARIANT)
                      || name1.equals(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT))
                    && (name2.equals(VariantAnnotationUtils.TWOKB_UPSTREAM_VARIANT)
                      || name2.equals(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT)))
                || ((name1.equals(VariantAnnotationUtils.TWOKB_DOWNSTREAM_VARIANT)
                      || name1.equals(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT))
                    && (name2.equals(VariantAnnotationUtils.TWOKB_DOWNSTREAM_VARIANT)
                      || name2.equals(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT)))
                || ((name1.equals("nc_transcript_variant")
                      || name1.equals(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT))
                    && (name2.equals("nc_transcript_variant")
                      || name2.equals(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT)))
                ) {
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }
}
