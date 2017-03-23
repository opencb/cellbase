package org.opencb.cellbase.core.variant.annotation.hgvs;

/**
 * Created by fjlopez on 26/01/17.
 */

import org.apache.commons.lang.NotImplementedException;

/**
 * Builder to get the hgvs identifier properly formatted.
 */
public class HgvsStringBuilder {

    // HGVS string parts
    private String prefix;
    private String chromosome;
    private String transcriptId;
    private String geneId;
    private String kind;
    private String mutationType;
    private int start;
    private int end;
    private String reference; // reference allele
    private String alternate; // alternate allele

    // cDNA-specific fields
    private CdnaCoord cdnaStart;
    private CdnaCoord cdnaEnd;

    public HgvsStringBuilder() {

    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public CdnaCoord getCdnaStart() {
        return cdnaStart;
    }

    public void setCdnaStart(CdnaCoord cdnaStart) {
        this.cdnaStart = cdnaStart;
    }

    public CdnaCoord getCdnaEnd() {
        return cdnaEnd;
    }

    public void setCdnaEnd(CdnaCoord cdnaEnd) {
        this.cdnaEnd = cdnaEnd;
    }

    /**
     * Generate a HGVS string.
     * @return String containing an HGVS formatted variant representation
     */
    public String format() {
//

        StringBuilder allele = new StringBuilder();
        allele.append(formatPrefix());  // if use_prefix else ''
        allele.append(":");

        if (kind.equals("c")) {
            allele.append("c.").append(formatCdna());
//    elif self.kind == 'p':
//    allele = 'p.' + self.format_protein()
//    elif self.kind == 'g':
//    allele = 'g.' + self.format_genome()
        } else {
            throw new NotImplementedException("HGVS calculation not implemented for variant " + chromosome + ":"
                    + start + ":" + reference + ":" + alternate + "; kind: " + kind);
        }
//            if prefix:
        return allele.toString();
//        else:
//                return allele
    }

    /**
     * Generate HGVS trancript/geneId prefix.
     * Some examples of full hgvs names with transcriptId include:
     * NM_007294.3:c.2207A>C
     * NM_007294.3(BRCA1):c.2207A>C
     */
    private String formatPrefix() {
//        if use_gene and self.geneId:
        StringBuilder stringBuilder = new StringBuilder(transcriptId);
        stringBuilder.append("(").append(geneId).append(")");

        return stringBuilder.toString();
//        return self.transcriptId
    }

    /**
     * Generate HGVS cDNA allele.
     * Some examples include:
     * Substitution: 101A>C,
     * Indel: 3428delCinsTA, 1000_1003delATG, 1000_1001insATG
     */
    private String formatCdna() {
        return formatCdnaCoords() + formatDnaAllele();
    }

    /**
     * Generate HGVS DNA allele.
     * @return
     */
    private String formatDnaAllele() {

//        We are not considering this case since would mean there's no variant
//        if self.mutation_type == '=':
//            #No change.
//            #example:
//        101 A =
//        return self.ref_allele + '='

        if (">".equals(mutationType)) {
            // SNP.
            // example:
            // 101 A > C
            return reference + '>' + alternate;
        } else if ("delins".equals(mutationType)) {
            // Indel.
            // example:
            // 112_117d elAGGTCAinsTG, 112_117d elinsTG
            return "del" + reference + "ins" + alternate;
        } else if ("del".equals(mutationType)) {
            // Delete, duplication.
            // example:
            // 1000_1003d elATG, 1000_1003d upATG
            return mutationType + reference;
        } else if ("dup".equals(mutationType)) {
            // Insertion normalized as duplication
            // example:
            // "ENST00000382869.3:c.1735+32dupA"
            return mutationType + alternate;
        } else if ("ins".equals(mutationType)) {
            // Insert.
            // example:
            // 1000_1001 insATG
            return mutationType + alternate;
        } else {
            throw new AssertionError("unknown mutation type: '" + mutationType + "'");
        }
    }

    /**
     * Generate HGVS cDNA coordinates string.
     */
    private String formatCdnaCoords() {
        // Format coordinates.
//        if (cdnaStart.equals(cdnaEnd)
//                || cdnaEnd.getCdsPosition() < cdnaStart.getCdsPosition()  // Happens when insertion
//                || (cdnaEnd.getCdsPosition() == cdnaStart.getCdsPosition()
//                    && cdnaEnd.getStartStopCodonOffset() < cdnaStart.getStartStopCodonOffset())) {
        if (cdnaStart.equals(cdnaEnd)
            || "dup".equals(mutationType)) {
//                || cdnaEnd.getCdsPosition() < cdnaStart.getCdsPosition()  // Happens when insertion
//                || (cdnaEnd.getCdsPosition() == cdnaStart.getCdsPosition()
//                    && cdnaEnd.getStartStopCodonOffset() < cdnaStart.getStartStopCodonOffset())) {
            return cdnaStart.toString();
        } else {
            return cdnaStart.toString() + "_" + cdnaEnd.toString();
        }
    }



//
//    def format_protein(self):
//            """
//    Generate HGVS protein name.
//    Some examples include:
//    No change: Glu1161=
//    Change: Glu1161Ser
//    Frameshift: Glu1161_Ser1164?fs
//        """
//                if (self.start == self.end and
//    self.ref_allele == self.ref2_allele == self.alt_allele):
//            # Match.
//            # Example: Glu1161=
//    pep_extra = self.pep_extra if self.pep_extra else '='
//            return self.ref_allele + str(self.start) + pep_extra
//
//    elif (self.start == self.end and
//                    self.ref_allele == self.ref2_allele and
//                    self.ref_allele != self.alt_allele):
//            # Change.
//            # Example: Glu1161Ser
//            return (self.ref_allele + str(self.start) +
//    self.alt_allele + self.pep_extra)
//
//    elif self.start != self.end:
//            # Range change.
//            # Example: Glu1161_Ser1164?fs
//            return (self.ref_allele + str(self.start) + '_' +
//    self.ref2_allele + str(self.end) +
//    self.pep_extra)
//
//            else:
//    raise NotImplementedError('protein name formatting.')
//
//    def format_coords(self):
//            """
//    Generate HGVS cDNA coordinates string.
//        """
//                # Format coordinates.
//            if self.start == self.end:
//            return str(self.start)
//        else:
//                return "%s_%s" % (self.start, self.end)
//
//    def format_genome(self):
//            """
//    Generate HGVS genomic allele.
//    Som examples include:
//    Substitution: 1000100A>T
//    Indel: 1000100_1000102delATG
//        """
//                return self.format_coords() + self.format_dna_allele()
//
//    def get_coords(self, transcriptId=None):
//            """Return genomic coordinates of reference allele."""
//            if self.kind == 'c':
//    chrom = transcriptId.tx_position.chrom
//            start = cdna_to_genomic_coord(transcriptId, self.cdna_start)
//    end = cdna_to_genomic_coord(transcriptId, self.cdna_end)
//
//            if not transcriptId.tx_position.is_forward_strand:
//            if end > start:
//    raise AssertionError(
//                        "cdna_start cannot be greater than cdna_end")
//    start, end = end, start
//            else:
//                    if start > end:
//    raise AssertionError(
//                        "cdna_start cannot be greater than cdna_end")
//
//            if self.mutation_type == "ins":
//            # Inserts have empty interval.
//            if start < end:
//    start += 1
//    end -= 1
//            else:
//    end = start - 1
//
//    elif self.mutation_type == "dup":
//    end = start - 1
//
//    elif self.kind == 'g':
//    chrom = self.chrom
//            start = self.start
//    end = self.end
//
//        else:
//    raise NotImplementedError(
//                'Coordinates are not available for this kind of HGVS name "%s"'
//                        % self.kind)
//
//        return chrom, start, end
//
//    def get_vcf_coords(self, transcriptId=None):
//            """Return genomic coordinates of reference allele in VCF-style."""
//    chrom, start, end = self.get_coords(transcriptId)
//
//            # Inserts and deletes require left-padding by 1 base
//        if self.mutation_type in ("=", ">"):
//    pass
//    elif self.mutation_type in ("del", "ins", "dup", "delins"):
//            # Indels have left-padding.
//            start -= 1
//            else:
//    raise NotImplementedError("Unknown mutation_type '%s'" %
//                              self.mutation_type)
//        return chrom, start, end
//
//    def get_ref_alt(self, is_forward_strand=True):
//            """Return reference and alternate alleles."""
//            if self.kind == 'p':
//    raise NotImplementedError(
//                'get_ref_alt is not implemented for protein HGVS names')
//    alleles = [self.ref_allele, self.alt_allele]
//
//            # Represent duplications are inserts.
//            if self.mutation_type == "dup":
//    alleles[0] = ""
//    alleles[1] = alleles[1][:len(alleles[1]) / 2]
//
//            if is_forward_strand:
//            return alleles
//        else:
//                return tuple(map(revcomp, alleles))

}
