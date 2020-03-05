/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core;

public class BioUtils {

    public enum Biotype {
        IG_C_GENE("IG_C_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        IG_D_GENE("IG_D_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        IG_J_GENE("IG_J_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        IG_LV_GENE("IG_LV_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or "
                + "annotated according to the IMGT."),
        IG_V_GENE("IG_V_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        TR_C_GENE("TR_C_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        TR_J_GENE("TR_J_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        TR_V_GENE("TR_V_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        TR_D_GENE("TR_D_gene", "Immunoglobulin (Ig) variable chain and T-cell receptor (TcR) genes imported or annotated "
                + "according to the IMGT."),
        IG_PSEUDOGENE("IG_pseudogene", "Inactivated immunoglobulin gene."),
        IG_C_PSEUDOGENE("IG_C_pseudogene", "Inactivated immunoglobulin gene."),
        IG_J_PSEUDOGENE("IG_J_pseudogene", "Inactivated immunoglobulin gene."),
        IG_V_PSEUDOGENE("IG_V_pseudogene", "Inactivated immunoglobulin gene."),
        TR_V_PSEUDOGENE("TR_V_pseudogene", "Inactivated immunoglobulin gene."),
        TR_J_PSEUDOGENE("TR_J_pseudogene", "Inactivated immunoglobulin gene."),
        MT_RRNA("Mt_rRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        MT_TRNA("Mt_tRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        MIRNA("miRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        MISC_RNA("misc_RNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        RRNA("rRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        SCRNA("scRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        SNRNA("snRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        SNORNA("snoRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        RIBOZYME("ribozyme", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        SRNA("sRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        SCARNA("scaRNA", "Non-coding RNA predicted using sequences from Rfam and miRBase"),
        LNCRNA("lncRNA", "Generic long non-coding RNA biotype that replaced the following biotypes: "
                + "3prime_overlapping_ncRNA, antisense, bidirectional_promoter_lncRNA, lincRNA, macro_lncRNA, non_coding, "
                + "processed_transcript, sense_intronic and sense_overlapping."),
        MT_TRNA_PSEUDOGENE("Mt_tRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        TRNA_PSEUDOGENE("tRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        SNORNA_PSEUDOGENE("snoRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        SNRNA_PSEUDOGENE("snRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        SCRNA_PSEUDOGENE("scRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        RRNA_PSEUDOGENE("rRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        MISC_RNA_PSEUDOGENE("misc_RNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        MIRNA_PSEUDOGENE("miRNA_pseudogene", "Non-coding RNA predicted to be pseudogene by the Ensembl pipeline"),
        TEC("TEC", "To be Experimentally Confirmed. This is used for non-spliced EST clusters that have polyA features. "
                + "This category has been specifically created for the ENCODE project to highlight regions that could indicate the presence"
                + " of protein coding genes that require experimental validation, either by 5' RACE or RT-PCR to extend the transcripts, or"
                + " by confirming expression of the putatively-encoded peptide with specific antibodies."),
        NONSENSE_MEDIATED_DECAY("nonsense_mediated_decay", "If the coding sequence (following the appropriate reference) "
                + "of a transcript finishes >50bp from a downstream splice site then it is tagged as NMD. If the variant does not cover the"
                + " full reference coding sequence then it is annotated as NMD if NMD is unavoidable i.e. no matter what the exon structure"
                + " of the missing portion is the transcript will be subject to NMD."),
        NON_STOP_DECAY("non_stop_decay", "Transcript that has polyA features (including signal) without a prior stop "
                + "codon in the CDS, i.e. a non-genomic polyA tail attached directly to the CDS without 3' UTR. These transcripts are "
                + "subject to degradation."),
        RETAINED_INTRON("retained_intron", "Alternatively spliced transcript believed to contain intronic sequence "
                + "relative to other, coding, variants."),
        PROTEIN_CODING("protein_coding", "Contains an open reading frame (ORF)."),
        PROCESSED_TRANSCRIPT("processed_transcript", "Doesn't contain an ORF."),
        NON_CODING("non_coding", "Transcript which is known from the literature to not be protein coding."),
        AMBIGUOUS_ORF("ambiguous_orf", "Transcript believed to be protein coding, but with more than one possible open "
                + "reading frame."),
        SENSE_INTRONIC("sense_intronic", "Long non-coding transcript in introns of a coding gene that does not overlap "
                + "any exons."),
        SENSE_OVERLAPPING("sense_overlapping", "Long non-coding transcript that contains a coding gene in its intron on "
                + "the same strand."),
        ANTISENSE_ANTISENSE_RNA("antisense/antisense_RNA", "Has transcripts that overlap the genomic span (i.e. exon or "
                + "introns) of a protein-coding locus on the opposite strand."),
        KNOWN_NCRNA("known_ncrna", ""),
        PSEUDOGENE("pseudogene", "Have homology to proteins but generally suffer from a disrupted coding sequence and an "
                + "active homologous gene can be found at another locus. Sometimes these entries have an intact coding sequence or an open "
                + "but truncated ORF, in which case there is other evidence used (for example genomic polyA stretches at the 3' end) to "
                + "classify them as a pseudogene. Can be further classified as one of the following."),
        PROCESSED_PSEUDOGENE("processed_pseudogene", "Pseudogene that lack introns and is thought to arise from reverse "
                + "transcription of mRNA followed by reinsertion of DNA into the genome."),
        POLYMORPHIC_PSEUDOGENE("polymorphic_pseudogene", "Pseudogene owing to a SNP/DIP but in other "
                + "individuals/haplotypes/strains the gene is translated."),
        RETROTRANSPOSED("retrotransposed", "Pseudogene owing to a reverse transcribed and re-inserted sequence."),
        TRANSCRIBED_PROCESSED_PSEUDOGENE("transcribed_processed_pseudogene", ""),
        TRANSCRIBED_UNPROCESSED_PSEUDOGENE("transcribed_unprocessed_pseudogene", ""),
        TRANSCRIBED_UNITARY_PSEUDOGENE("transcribed_unitary_pseudogene", "Pseudogene where protein homology or genomic "
                + "structure indicates a pseudogene, but the presence of locus-specific transcripts indicates expression."),
        TRANSLATED_PROCESSED_PSEUDOGENE("translated_processed_pseudogene", ""),
        TRANSLATED_UNPROCESSED_PSEUDOGENE("translated_unprocessed_pseudogene", "Pseudogene that has mass spec data "
                + "suggesting that it is also translated."),
        UNITARY_PSEUDOGENE("unitary_pseudogene", "A species-specific unprocessed pseudogene without a parent gene, as it"
                + " has an active orthologue in another species."),
        UNPROCESSED_PSEUDOGENE("unprocessed_pseudogene", "Pseudogene that can contain introns since produced by gene "
                + "duplication."),
        ARTIFACT("artifact", "Used to tag mistakes in the public databases (Ensembl/SwissProt/Trembl)"),
        LINCRNA("lincRNA", "Long, intervening noncoding (linc) RNA that can be found in evolutionarily conserved, "
                + "intergenic regions."),
        MACRO_LNCRNA("macro_lncRNA", "Unspliced lncRNA that is several kb in size."),
        THREE_PRIME_OVERLAPPING_NCRNA("3prime_overlapping_ncRNA", "Transcript where ditag and/or published experimental "
                + "data strongly supports the existence of short non-coding transcripts transcribed from the 3'UTR."),
        DISRUPTED_DOMAIN("disrupted_domain", "Otherwise viable coding region omitted from this alternatively spliced "
                + "transcript because the splice variation affects a region coding for a protein domain."),
        VAULTRNA("vaultRNA", "Short non coding RNA gene that forms part of the vault ribonucleoprotein complex."),
        BIDIRECTIONAL_PROMOTER_LNCRNA("bidirectional_promoter_lncRNA", "A non-coding locus that originates from within "
                + "the promoter region of a protein-coding gene, with transcription proceeding in the opposite direction on the other "
                + "strand.");

        private final String id;
        private final String description;

        Biotype(String id, String description) {
            this.id = id;
            this.description = description;
        }
        private String id() {
            return id;
        }
        private String description() {
            return description;
        }
    }

    public static boolean isValidBiotype(String text) {
        for (Biotype b : Biotype.values()) {
            if (b.id.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }
}
