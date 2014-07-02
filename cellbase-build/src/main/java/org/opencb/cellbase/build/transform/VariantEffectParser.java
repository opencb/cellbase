package org.opencb.cellbase.build.transform;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.effect.ConsequenceType;
import org.opencb.biodata.models.variant.effect.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.effect.ProteinSubstitutionScores;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;


/**
 * This class parses the Ensembl VEP output which contains the following columns:
 *
 * #Uploaded_variation, Location, Allele, Gene, Feature, Feature_type, Consequence, cDNA_position, CDS_position, Protein_position, Amino_acids, Codons, Existing_variation, Extra
 * Example:
 * 1_10001_T/-     1:10001 -       ENSG00000223972 ENST00000456328 Transcript      upstream_gene_variant   -       -       -       -       -       -       STRAND=1;SYMBOL=DDX11L1;BIOTYPE=processed_transcript;DISTANCE=1868;CANONICAL=YES;SYMBOL_SOURCE=HGNC
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 * @author Ignacio Medina <imedina@ebi.ac.uk>
 */
public class VariantEffectParser {

    //    private DataWriter serializer;
    private CellBaseSerializer serializer;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public VariantEffectParser(CellBaseSerializer serializer) {
        this.serializer = serializer;
    }

//    public VariantEffectParser(DataWriter serializer) {
//        this.serializer = serializer;
//    }

//    718787

    public int parse(Path file) throws IOException {
        BufferedReader reader = FileUtils.newBufferedReader(file);

        VariantEffect currentEffect = null;
        String currentAlternativeAllele = null;
        int numEffectsWritten = 0;
        String[] vepLinefields, variantIdFields, variantLocationFields;
        String variantChromosome;
        int variantStart, variantEnd;

        String line;
        while((line = reader.readLine()) != null) {
            // We must ignore lines starting with '#'
            if (!line.startsWith("#")) {
                vepLinefields = line.split("\t", -1);

                // Some VEP output examples:
                // 1_718787_-/T    1:718786-718787 T    ...
                // 1_718787_T/-    1:718787        -    ...
                // 1_718788_T/A    1:718788        A    ...
                variantIdFields = vepLinefields[0].split("[\\_\\/]");
                variantLocationFields = vepLinefields[1].split("[:-]");
                variantChromosome = variantLocationFields[0];
                variantStart = Integer.parseInt(variantLocationFields[1]);
                variantEnd = (variantLocationFields.length > 2) ? Integer.parseInt(variantLocationFields[2]) : Integer.parseInt(variantLocationFields[1]);

                if (variantIdFields.length < 4) {
                    // Only entries chr_pos_ref/alt will be parsed, ie, 1_909238_G/C or 3_361464_A/-
                    // Entries like 5_121187650_duplication will be ignored
                    continue;
                }

                if (isNewVariant(variantChromosome, variantStart, variantEnd, variantIdFields[2], variantIdFields[3], currentEffect, currentAlternativeAllele)) {
                    // We have to ignore the first "NewVariant" as is the first line of the file
                    if (currentEffect != null && serializer != null) {
//                        if (serializer.write(currentEffect)) {
//                            numEffectsWritten++;
//                        }
                        serializer.serialize(currentEffect);
                        numEffectsWritten++;
                    }

                    currentEffect = new VariantEffect(variantChromosome, variantStart, variantEnd, variantIdFields[2]);
                    currentAlternativeAllele = variantIdFields[3];
                } else if (isNewAllele(variantChromosome, variantStart, variantEnd, variantIdFields[2], variantIdFields[3], currentEffect, currentAlternativeAllele)) {
                    currentAlternativeAllele = variantIdFields[3];
                }

                parseLine(vepLinefields, currentEffect, currentAlternativeAllele);
            }
        }

        // Don't forget to serialize the last effect read!
        if (currentEffect != null && serializer != null) {
//                        if (serializer.write(currentEffect)) {
//                            numEffectsWritten++;
//                        }
            serializer.serialize(currentEffect);
            numEffectsWritten++;
        }

        reader.close();
        return numEffectsWritten;
    }

    private boolean isNewVariant(String chromosome, int start, int end, String referenceAllele, String alternateAllele,
                                 VariantEffect current, String currentAllele) {
        if (current == null) {
            return true;
        }

        return !chromosome.equals(current.getChromosome())
                || start != current.getStart()
                || end != current.getEnd()
                || !referenceAllele.equals(current.getReferenceAllele())
//                || !alternateAllele.equals(currentAllele)
                ;
    }

    private boolean isNewAllele(String chromosome, int start, int end, String referenceAllele, String alternateAllele,
                                VariantEffect current, String currentAllele) {
        if (current == null) {
            return true;
        }

        return chromosome.equals(current.getChromosome())
                && start == current.getStart()
                && end == current.getEnd()
                && referenceAllele.equals(current.getReferenceAllele())
                && !alternateAllele.equals(currentAllele);
    }

    private void parseLine(String[] fields, VariantEffect effect, String alternateAllele) {
        ConsequenceType ct = new ConsequenceType(alternateAllele);
        effect.addConsequenceType(alternateAllele, ct);

        // Gene and feature fields can be empty (marked with "-")
        if (!"-".equals(fields[3])) {
            ct.setGeneId(fields[3]);
        }
        if (!"-".equals(fields[4])) {
            ct.setFeatureId(fields[4]);
        }
        if (!"-".equals(fields[5])) {
            ct.setFeatureType(fields[5]);
        }

        // List of consequence types as SO codes
        String[] consequencesName = fields[6].split(",");
        int[] consequencesSo = new int[consequencesName.length];
        for (int i = 0; i < consequencesName.length; i++) {
            Integer so = ConsequenceTypeMappings.termToAccession.get(consequencesName[i]);
            if (so != null) {
                consequencesSo[i] = so;
            } else {
                logger.warn("{0} is not a valid consequence type", consequencesName[i]);
            }
        }
        ct.setConsequenceTypes(consequencesSo);

        // Fields related to position can be empty (marked with "-")
        if (!"-".equals(fields[7]) && StringUtils.isNumeric(fields[7])) {
            ct.setcDnaPosition(Integer.parseInt(fields[7]));
        }
        if (!"-".equals(fields[8]) && StringUtils.isNumeric(fields[8])) {
            ct.setCdsPosition(Integer.parseInt(fields[8]));
        }
        if (!"-".equals(fields[9]) && StringUtils.isNumeric(fields[9])) {
            ct.setProteinPosition(Integer.parseInt(fields[9]));
        }

        // Fields related to AA and codon changes can also be empty (marked with "-")
        if (!"-".equals(fields[10])) {
            ct.setAminoacidChange(fields[10]);
        }
        if (!"-".equals(fields[11])) {
            ct.setCodonChange(fields[11]);
        }

        // Variant ID
        if (!"-".equals(fields[12])) {
            ct.setVariationId(fields[12]);
        }

        parseExtraFields(fields[13], effect, alternateAllele, ct);
    }

    private void parseExtraFields(String extra, VariantEffect effect, String alternateAllele, ConsequenceType ct) {
        for (String field : extra.split(";")) {
            String[] keyValue = field.split("=");

            switch (keyValue[0].toLowerCase()) {
                case "aa_maf":
                    effect.getFrequencies().setMafNhlbiEspAfricanAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "afr_maf":
                    effect.getFrequencies().setMaf1000GAfrican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "amr_maf":
                    effect.getFrequencies().setMaf1000GAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "asn_maf":
                    effect.getFrequencies().setMaf1000GAsian((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "biotype":
                    ct.setFeatureBiotype(keyValue[1]);
                    break;
                case "canonical":
                    ct.setCanonical(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "ccds":
                    ct.setCcdsId(keyValue[1]);
                    break;
                case "cell_type":
                    effect.getRegulatoryEffect().setCellType(keyValue[1]);
                    break;
                case "clin_sig":
                    ct.setClinicalSignificance(keyValue[1]);
                    break;
                case "distance":
                    ct.setVariantToTranscriptDistance(Integer.parseInt(keyValue[1]));
                    break;
                case "domains":
                    ct.setProteinDomains(keyValue[1].split(","));
                    break;
                case "ea_maf":
                    effect.getFrequencies().setMafNhlbiEspEuropeanAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "ensp":
                    ct.setProteinId(keyValue[1]);
                    break;
                case "eur_maf":
                    effect.getFrequencies().setMaf1000GEuropean((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "exon":
                    ct.setExonNumber(keyValue[1]);
                    break;
                case "gmaf": // Format is GMAF=G:0.2640  or  GMAF=T:0.1221,-:0.0905
                    String[] freqs = keyValue[1].split(",");
                    for(String freq: freqs) {
                        String[] gmafFields = freq.split(":");
                        if(gmafFields[0].equals(alternateAllele)) {
                            effect.getFrequencies().setAllele1000g(gmafFields[0]);
                            effect.getFrequencies().setMaf1000G(Float.parseFloat(gmafFields[1]));
                            break;
                        }
                    }

//                    if(freqs.length == 1) {
//                        String[] gmafFields = keyValue[1].split(":");
//                        effect.getFrequencies().setAllele1000g(gmafFields[0]);
//                        effect.getFrequencies().setMaf1000G(Float.parseFloat(gmafFields[1]));
//                    }else {
//                        for(String freq: freqs) {
//                            String[] gmafFields = freq.split(":");
//                            if(gmafFields[0].equals(alternateAllele)) {
//                                effect.getFrequencies().setAllele1000g(gmafFields[0]);
//                                effect.getFrequencies().setMaf1000G(Float.parseFloat(gmafFields[1]));
//                            }
//                        }
//                    }
                    break;
                case "hgvsc":
                    ct.setHgvsc(keyValue[1]);
                    break;
                case "hgvsp":
                    ct.setHgvsp(keyValue[1]);
                    break;
                case "high_inf_pos":
                    effect.getRegulatoryEffect().setHighInformationPosition(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "intron":
                    ct.setIntronNumber(keyValue[1]);
                    break;
                case "motif_name":
                    effect.getRegulatoryEffect().setMotifName(keyValue[1]);
                    break;
                case "motif_pos":
                    effect.getRegulatoryEffect().setMotifPosition(Integer.parseInt(keyValue[1]));
                    break;
                case "motif_score_change":
                    effect.getRegulatoryEffect().setMotifScoreChange(Float.parseFloat(keyValue[1]));
                    break;
                case "polyphen": // Format is PolyPhen=possibly_damaging(0.859)
                    String[] polyphenFields = keyValue[1].split("[\\(\\)]");
                    effect.getProteinSubstitutionScores().setPolyphenEffect(ProteinSubstitutionScores.PolyphenEffect.valueOf(polyphenFields[0].toUpperCase()));
                    effect.getProteinSubstitutionScores().setPolyphenScore(Float.parseFloat(polyphenFields[1]));
                    break;
                case "pubmed":
                    ct.setPubmed(keyValue[1].split(","));
                    break;
                case "sift": // Format is SIFT=tolerated(0.07)
                    String[] siftFields = keyValue[1].split("[\\(\\)]");
                    effect.getProteinSubstitutionScores().setSiftEffect(ProteinSubstitutionScores.SiftEffect.valueOf(siftFields[0].toUpperCase()));
                    effect.getProteinSubstitutionScores().setSiftScore(Float.parseFloat(siftFields[1]));
                    break;
                case "strand":
                    ct.setFeatureStrand(keyValue[1]);
                    break;
                case "sv":
                    ct.setStructuralVariantsId(keyValue[1].split(","));
                    break;
                case "symbol":
                    ct.setGeneName(keyValue[1]);
                    break;
                case "symbol_source":
                    ct.setGeneNameSource(keyValue[1]);
                    break;
                default:
                    // ALLELE_NUM, FREQS, IND, ZYG
                    break;
            }
        }

    }
}
