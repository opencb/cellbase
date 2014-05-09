package org.opencb.cellbase.build.transform.serializers.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.effect.ConsequenceType;
import org.opencb.biodata.models.variant.effect.Frequencies;
import org.opencb.biodata.models.variant.effect.ProteinSubstitutionScores;
import org.opencb.biodata.models.variant.effect.RegulatoryEffect;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.utils.CryptoUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectMongoDBSerializer implements DataWriter<VariantEffect> {

    @Override
    public boolean open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean pre() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean post() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean write(VariantEffect elem) {
        return write(Arrays.asList(elem));
    }

    @Override
    public boolean write(List<VariantEffect> batch) {
        for (VariantEffect effect : batch) {
            String rowkey = buildRowkey(effect);
            BasicDBObject mongoEffect = new BasicDBObject("_id", rowkey)
                    .append("chr", effect.getChromosome())
                    .append("start", effect.getStart()).append("end", effect.getEnd())
                    .append("ref", effect.getReferenceAllele())
                    .append("freqs", getFrequenciesDBObject(effect.getFrequencies()))
                    .append("scores", getProteinSubstitutionScores(effect.getProteinSubstitutionScores()))
                    .append("regulatory", getRegulatoryEffect(effect.getRegulatoryEffect()));

            BasicDBList alleles = new BasicDBList();

            for (Map.Entry<String, List<ConsequenceType>> allelesConsequences : effect.getConsequenceTypes().entrySet()) {
                BasicDBObject alleleRoot = new BasicDBObject("alt", allelesConsequences.getKey());
                BasicDBList cts = new BasicDBList();

                for (ConsequenceType ct : allelesConsequences.getValue()) {
                    cts.add(getConsequenceTypeDBObject(ct));
                }

                alleleRoot.append("val", cts);
                alleles.add(alleleRoot);
            }

            mongoEffect.append("ct", alleles);

        }

        return true;
    }

    private BasicDBObject getConsequenceTypeDBObject(ConsequenceType ct) {
        BasicDBObject object = new BasicDBObject("so", ct.getConsequenceTypes());
        
        if (ct.getGeneId() != null) { object.append("geneId", ct.getGeneId()); }
        if (ct.getGeneName() != null) { object.append("geneName", ct.getGeneName()); }
        if (ct.getGeneNameSource() != null) { object.append("geneNameSource", ct.getGeneNameSource()); }

        if (ct.getFeatureId() != null) { object.append("featureId", ct.getFeatureId()); }
        if (ct.getFeatureType() != null) { object.append("featureType", ct.getFeatureType()); }
        if (ct.getFeatureStrand() != null) { object.append("featureStrand", ct.getFeatureStrand()); }
        if (ct.getFeatureBiotype() != null) { object.append("featureBiotype", ct.getFeatureBiotype()); }

        if (ct.getcDnaPosition() >= 0) { object.append("cdnaPos", ct.getcDnaPosition()); }
        if (ct.getCcdsId() != null) { object.append("ccdsId", ct.getCcdsId()); }
        if (ct.getCdsPosition() >= 0) { object.append("cdsPos", ct.getCdsPosition()); }
        
        if (ct.getProteinId() != null) { object.append("proteinId", ct.getProteinId()); }
        if (ct.getProteinPosition() >= 0) { object.append("proteinPos", ct.getProteinPosition()); }
        if (ct.getProteinDomains() != null) { object.append("proteinDomains", StringUtils.join(ct.getProteinDomains(), ",")); }
                
        if (ct.getAminoacidChange() != null) { object.append("aaChange", ct.getAminoacidChange()); }
        if (ct.getCodonChange() != null) { object.append("codonChange", ct.getCodonChange()); }
        
        if (ct.getVariationId() != null) { object.append("id", ct.getVariationId()); }
        if (ct.getStructuralVariantsId() != null) { object.append("svIds", StringUtils.join(ct.getStructuralVariantsId(), ",")); }
                
        if (ct.getHgvsc() != null) { object.append("hgvsc", ct.getHgvsc()); }
        if (ct.getHgvsp() != null) { object.append("hgvsp", ct.getHgvsp()); }
        
        if (ct.getIntronNumber() != null) { object.append("intron", ct.getIntronNumber()); }
        if (ct.getExonNumber() != null) { object.append("exon", ct.getExonNumber()); }
        
        if (ct.getVariantToTranscriptDistance() >= 0) { object.append("distance", ct.getVariantToTranscriptDistance()); } 
        
        if (ct.getClinicalSignificance() != null) { object.append("clinicSig", ct.getClinicalSignificance()); }
        
        if (ct.getPubmed() != null) { object.append("pubmed", StringUtils.join(ct.getPubmed(), ",")); }
        object.append("canonical", ct.isCanonical());
        
        return object;
    }

    private BasicDBObject getFrequenciesDBObject(Frequencies frequencies) {
        BasicDBObject object = new BasicDBObject("mafAllele", frequencies.getAllele1000g())
                .append("gmaf", frequencies.getMaf1000G())
                .append("african_maf", frequencies.getMaf1000GAfrican())
                .append("american_maf", frequencies.getMaf1000GAmerican())
                .append("asian_maf", frequencies.getMaf1000GAsian())
                .append("european_maf", frequencies.getMaf1000GEuropean())
                .append("aa_maf", frequencies.getMafNhlbiEspAfricanAmerican())
                .append("ea_maf", frequencies.getMafNhlbiEspEuropeanAmerican());
        return object;
    }

    private BasicDBObject getProteinSubstitutionScores(ProteinSubstitutionScores scores) {
        BasicDBObject object = new BasicDBObject("polyScore", scores.getPolyphenScore())
                .append("polyEff", scores.getPolyphenEffect().name())
                .append("siftScore", scores.getSiftScore())
                .append("siftEff", scores.getSiftEffect().name());
        return object;
    }

    private BasicDBObject getRegulatoryEffect(RegulatoryEffect regulatory) {
        BasicDBObject object = new BasicDBObject("motifName", regulatory.getMotifName())
                .append("motifPos", regulatory.getMotifPosition())
                .append("motifScoreChange", regulatory.getMotifScoreChange())
                .append("highInfoPos", regulatory.isHighInformationPosition())
                .append("cellType", regulatory.getCellType());
        return object;
    }

    private String buildRowkey(VariantEffect v) {
        StringBuilder builder = new StringBuilder(v.getChromosome());
        builder.append("_");
        builder.append(v.getStart());
        builder.append("_");
        if (v.getReferenceAllele().length() < Variant.SV_THRESHOLD) {
            builder.append(v.getReferenceAllele());
        } else {
            builder.append(new String(CryptoUtils.encryptSha1(v.getReferenceAllele())));
        }

        return builder.toString();
    }

}
