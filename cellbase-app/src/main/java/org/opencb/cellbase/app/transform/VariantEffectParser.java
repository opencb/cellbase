package org.opencb.cellbase.app.transform;

import org.opencb.biodata.models.variant.annotation.ProteinSubstitutionScores;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variant.annotation.VariantEffect;
import org.opencb.cellbase.app.serializers.CellBaseFileSerializer;
import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


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
public class VariantEffectParser extends CellBaseParser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Path inputFile;
    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;

    public VariantEffectParser(Path file, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.inputFile = file;
        outputFileNames = new HashMap<>();
    }

//    public VariantEffectParser(DataWriter serializer) {
//        this.serializer = serializer;
//    }

//    718787

    public void parse() throws IOException {
        BufferedReader reader = FileUtils.newBufferedReader(inputFile);

        VariantAnnotation currentAnnotation = null;
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

                if (isNewVariant(variantChromosome, variantStart, variantEnd, variantIdFields[2], variantIdFields[3], currentAnnotation, currentAlternativeAllele)) {
                    // We have to ignore the first "NewVariant" as is the first line of the file
                    if (currentAnnotation != null) {
//                        if (serializer.write(currentEffect)) {
//                            numEffectsWritten++;
//                        }
                        fileSerializer.serialize(currentAnnotation, getOutputFileName(currentAnnotation));
                        numEffectsWritten++;
                    }

                    currentAnnotation = new VariantAnnotation(variantChromosome, variantStart, variantEnd, variantIdFields[2]);
                    currentAlternativeAllele = variantIdFields[3];
                } else if (isNewAllele(variantChromosome, variantStart, variantEnd, variantIdFields[2], variantIdFields[3], currentAnnotation, currentAlternativeAllele)) {
                    currentAlternativeAllele = variantIdFields[3];
                }

                parseLine(vepLinefields, currentAnnotation, currentAlternativeAllele);
            }
        }

        // Don't forget to serialize the last effect read!
        if (currentAnnotation != null) {
//                        if (serializer.write(currentEffect)) {
//                            numEffectsWritten++;
//                        }
            fileSerializer.serialize(currentAnnotation, getOutputFileName(currentAnnotation));
            numEffectsWritten++;
        }

        reader.close();
    }

    private String getOutputFileName(VariantAnnotation annotation) {
        String outputFileName = outputFileNames.get(annotation.getChromosome());
        if (outputFileName == null) {
            outputFileName = "variant_effect_chr" + annotation.getChromosome();
            outputFileNames.put(annotation.getChromosome(), outputFileName);
        }
        return outputFileName;
    }

    private boolean isNewVariant(String chromosome, int start, int end, String referenceAllele, String alternateAllele,
                                 VariantAnnotation current, String currentAllele) {
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
                                VariantAnnotation current, String currentAllele) {
        if (current == null) {
            return true;
        }

        return chromosome.equals(current.getChromosome())
                && start == current.getStart()
                && end == current.getEnd()
                && referenceAllele.equals(current.getReferenceAllele())
                && !alternateAllele.equals(currentAllele);
    }

    private void parseLine(String[] fields, VariantAnnotation variantAnnotation, String alternateAllele) {
//        VariantEffect variantEffect = new VariantEffect(alternateAllele);
//        variantAnnotation.addEffect(alternateAllele, variantEffect);
//
//        // Gene and feature fields can be empty (marked with "-")
//        if (!"-".equals(fields[3])) {
//            variantEffect.setGeneId(fields[3]);
//        }
//        if (!"-".equals(fields[4])) {
//            variantEffect.setFeatureId(fields[4]);
//        }
//        if (!"-".equals(fields[5])) {
//            variantEffect.setFeatureType(fields[5]);
//        }
//
//        // List of consequence types as SO codes
//        String[] consequencesName = fields[6].split(",");
//        int[] consequencesSo = new int[consequencesName.length];
//        for (int i = 0; i < consequencesName.length; i++) {
//            Integer so = ConsequenceTypeMappings.termToAccession.get(consequencesName[i]);
//            if (so != null) {
//                consequencesSo[i] = so;
//            } else {
//                logger.warn("{0} is not a valid consequence type", consequencesName[i]);
//            }
//        }
//        variantEffect.setConsequenceTypes(consequencesSo);
//
//        // Fields related to position can be empty (marked with "-")
//        if (!"-".equals(fields[7]) && StringUtils.isNumeric(fields[7])) {
//            variantEffect.setcDnaPosition(Integer.parseInt(fields[7]));
//        }
//        if (!"-".equals(fields[8]) && StringUtils.isNumeric(fields[8])) {
//            variantEffect.setCdsPosition(Integer.parseInt(fields[8]));
//        }
//        if (!"-".equals(fields[9]) && StringUtils.isNumeric(fields[9])) {
//            variantEffect.setProteinPosition(Integer.parseInt(fields[9]));
//        }
//
//        // Fields related to AA and codon changes can also be empty (marked with "-")
//        if (!"-".equals(fields[10])) {
//            variantEffect.setAminoacidChange(fields[10]);
//        }
//        if (!"-".equals(fields[11])) {
//            variantEffect.setCodonChange(fields[11]);
//        }
//
//        // Variant ID
//        if (!"-".equals(fields[12])) {
//            variantEffect.setVariationId(fields[12]);
//        }
//
//        parseExtraFields(fields[13], variantAnnotation, alternateAllele, variantEffect);
    }

    private void parseExtraFields(String extra, VariantAnnotation variantAnnotation, String alternateAllele, VariantEffect variantEffect) {
        for (String field : extra.split(";")) {
            String[] keyValue = field.split("=");

            switch (keyValue[0].toLowerCase()) {
                case "aa_maf":
//                    variantAnnotation.getFrequencies().setMafNhlbiEspAfricanAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "afr_maf":
//                    variantAnnotation.getFrequencies().setMaf1000GAfrican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "amr_maf":
//                    variantAnnotation.getFrequencies().setMaf1000GAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "asn_maf":
//                    variantAnnotation.getFrequencies().setMaf1000GAsian((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "biotype":
                    variantEffect.setFeatureBiotype(keyValue[1]);
                    break;
                case "canonical":
                    variantEffect.setCanonical(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "ccds":
                    variantEffect.setCcdsId(keyValue[1]);
                    break;
                case "cell_type":
                    variantAnnotation.getRegulatoryEffect().setCellType(keyValue[1]);
                    break;
                case "clin_sig":
                    variantEffect.setClinicalSignificance(keyValue[1]);
                    break;
                case "distance":
                    variantEffect.setVariantToTranscriptDistance(Integer.parseInt(keyValue[1]));
                    break;
                case "domains":
                    variantEffect.setProteinDomains(keyValue[1].split(","));
                    break;
                case "ea_maf":
//                    variantAnnotation.getFrequencies().setMafNhlbiEspEuropeanAmerican((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "ensp":
                    variantEffect.setProteinId(keyValue[1]);
                    break;
                case "eur_maf":
//                    variantAnnotation.getFrequencies().setMaf1000GEuropean((keyValue.length == 2) ? Float.parseFloat(keyValue[1]) : -1f);
                    break;
                case "exon":
                    variantEffect.setExonNumber(keyValue[1]);
                    break;
                case "gmaf": // Format is GMAF=G:0.2640  or  GMAF=T:0.1221,-:0.0905
                    String[] freqs = keyValue[1].split(",");
                    for(String freq: freqs) {
                        String[] gmafFields = freq.split(":");
                        if(gmafFields[0].equals(alternateAllele)) {
//                            variantAnnotation.getFrequencies().setAllele1000g(gmafFields[0]);
//                            variantAnnotation.getFrequencies().setMaf1000G(Float.parseFloat(gmafFields[1]));
                            break;
                        }
                    }
                    break;
                case "hgvsc":
                    variantEffect.setHgvsc(keyValue[1]);
                    break;
                case "hgvsp":
                    variantEffect.setHgvsp(keyValue[1]);
                    break;
                case "high_inf_pos":
                    variantAnnotation.getRegulatoryEffect().setHighInformationPosition(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "intron":
                    variantEffect.setIntronNumber(keyValue[1]);
                    break;
                case "motif_name":
                    variantAnnotation.getRegulatoryEffect().setMotifName(keyValue[1]);
                    break;
                case "motif_pos":
                    variantAnnotation.getRegulatoryEffect().setMotifPosition(Integer.parseInt(keyValue[1]));
                    break;
                case "motif_score_change":
                    variantAnnotation.getRegulatoryEffect().setMotifScoreChange(Float.parseFloat(keyValue[1]));
                    break;
                case "polyphen": // Format is PolyPhen=possibly_damaging(0.859)
                    String[] polyphenFields = keyValue[1].split("[\\(\\)]");
                    variantAnnotation.getProteinSubstitutionScores().setPolyphenEffect(ProteinSubstitutionScores.PolyphenEffect.valueOf(polyphenFields[0].toUpperCase()));
                    variantAnnotation.getProteinSubstitutionScores().setPolyphenScore(Float.parseFloat(polyphenFields[1]));
                    break;
                case "pubmed":
                    variantEffect.setPubmed(keyValue[1].split(","));
                    break;
                case "sift": // Format is SIFT=tolerated(0.07)
                    String[] siftFields = keyValue[1].split("[\\(\\)]");
                    variantAnnotation.getProteinSubstitutionScores().setSiftEffect(ProteinSubstitutionScores.SiftEffect.valueOf(siftFields[0].toUpperCase()));
                    variantAnnotation.getProteinSubstitutionScores().setSiftScore(Float.parseFloat(siftFields[1]));
                    break;
                case "strand":
                    variantEffect.setFeatureStrand(keyValue[1]);
                    break;
                case "sv":
                    variantEffect.setStructuralVariantsId(keyValue[1].split(","));
                    break;
                case "symbol":
                    variantEffect.setGeneName(keyValue[1]);
                    break;
                case "symbol_source":
                    variantEffect.setGeneNameSource(keyValue[1]);
                    break;
                default:
                    // ALLELE_NUM, FREQS, IND, ZYG
                    break;
            }
        }

    }
}
