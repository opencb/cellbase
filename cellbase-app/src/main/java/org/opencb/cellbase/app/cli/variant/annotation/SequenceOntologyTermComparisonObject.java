package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

/**
 * Created by fjlopez on 08/04/16.
 */
public class SequenceOntologyTermComparisonObject {

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
//        if (getName() != null ? !equalsName(getName(), o.getName()) : o.getName() != null) {
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
        return getAccession() != null ? getAccession().equals(that.getAccession()) : that.getAccession() == null;

    }

    @Override
    public int hashCode() {
        int result = getTranscriptId() != null ? getTranscriptId().hashCode() : 0;
        result = 31 * result + (getAccession() != null ? getAccession().hashCode() : 0);
        return result;
    }

    private boolean equalsName(String name1, String name2) {
        if (name1.equals(name2)) {
            return true;
        } else if (((name1.equals("2KB_upstream_gene_variant") || name1.equals(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT))
                    && (name2.equals("2KB_upstream_gene_variant") || name2.equals("upstream_gene_variant")))
                || ((name1.equals("2KB_downstream_gene_variant") || name1.equals("downstream_gene_variant"))
                    && (name2.equals("2KB_downstream_gene_variant") || name2.equals("downstream_gene_variant")))
                || ((name1.equals("nc_transcript_variant") || name1.equals("non_coding_transcript_variant"))
                    && (name2.equals("nc_transcript_variant") || name2.equals("non_coding_transcript_variant")))
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
