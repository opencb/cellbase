package org.opencb.cellbase.build.loaders.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.effect.ConsequenceType;
import org.opencb.biodata.models.variant.effect.Frequencies;
import org.opencb.biodata.models.variant.effect.ProteinSubstitutionScores;
import org.opencb.biodata.models.variant.effect.RegulatoryEffect;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.utils.CryptoUtils;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDBConfiguration;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectMongoDBLoader implements DataWriter<VariantEffect> {

    private String host;
    private int port;
    private String dbName;
    private String collectionName;
    private String user;
    private String pass;

    private MongoDataStoreManager manager;
    private MongoDataStore datastore;
    private MongoDBCollection collection;

    public VariantEffectMongoDBLoader(Properties applicationProperties) {
        host = applicationProperties.getProperty("MONGO.HOST", "localhost");
        port = Integer.parseInt(applicationProperties.getProperty("MONGO.PORT", "27017"));
        user = applicationProperties.getProperty("MONGO.USERNAME", null);
        pass = applicationProperties.getProperty("MONGO.PASSWORD", null);
        dbName = applicationProperties.getProperty("MONGO.DB", "");
        collectionName = applicationProperties.getProperty("MONGO.COLLECTIONS.VARIANT_EFFECT", "");
    }
    
    public VariantEffectMongoDBLoader(String host, int port, String dbName, String collectionName) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    public boolean open() {
        manager = new MongoDataStoreManager(host, port);
        
        datastore = manager.get(dbName);
        if(user != null && pass != null && (!"".equals(user) || !"".equals(pass))) {
            datastore.getDb().authenticate(user,pass.toCharArray());
        }
        
        collection = datastore.getCollection(collectionName);

        return true;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public boolean write(VariantEffect elem) {
        return write(Arrays.asList(elem));
    }

    @Override
    public boolean write(List<VariantEffect> batch) {
        boolean ok = true;
        
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

//            QueryResult result = collection.insert(mongoEffect);
            QueryResult result = collection.update(new BasicDBObject("_id", rowkey), mongoEffect, true, true);
            if (result.getError() != null) { 
                // TODO Do anything special when an error occurs?
                Logger.getLogger(VariantEffectMongoDBLoader.class.getName()).log(Level.SEVERE, null, result.getError());
                ok = false;
            }
        }

        return ok;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean close() {
        manager.close(datastore.getDatabaseName());
        return true;
    }

    
    private BasicDBObject getConsequenceTypeDBObject(ConsequenceType ct) {
        BasicDBObject object = new BasicDBObject("so", ct.getConsequenceTypes());

        if (ct.getGeneId() != null) {
            object.append("geneId", ct.getGeneId());
        }
        if (ct.getGeneName() != null) {
            object.append("geneName", ct.getGeneName());
        }
        if (ct.getGeneNameSource() != null) {
            object.append("geneNameSource", ct.getGeneNameSource());
        }

        if (ct.getFeatureId() != null) {
            object.append("featureId", ct.getFeatureId());
        }
        if (ct.getFeatureType() != null) {
            object.append("featureType", ct.getFeatureType());
        }
        if (ct.getFeatureStrand() != null) {
            object.append("featureStrand", ct.getFeatureStrand());
        }
        if (ct.getFeatureBiotype() != null) {
            object.append("featureBiotype", ct.getFeatureBiotype());
        }

        if (ct.getcDnaPosition() >= 0) {
            object.append("cdnaPos", ct.getcDnaPosition());
        }
        if (ct.getCcdsId() != null) {
            object.append("ccdsId", ct.getCcdsId());
        }
        if (ct.getCdsPosition() >= 0) {
            object.append("cdsPos", ct.getCdsPosition());
        }

        if (ct.getProteinId() != null) {
            object.append("proteinId", ct.getProteinId());
        }
        if (ct.getProteinPosition() >= 0) {
            object.append("proteinPos", ct.getProteinPosition());
        }
        if (ct.getProteinDomains() != null) {
            object.append("proteinDomains", StringUtils.join(ct.getProteinDomains(), ","));
        }

        if (ct.getAminoacidChange() != null) {
            object.append("aaChange", ct.getAminoacidChange());
        }
        if (ct.getCodonChange() != null) {
            object.append("codonChange", ct.getCodonChange());
        }

        if (ct.getVariationId() != null) {
            object.append("id", ct.getVariationId());
        }
        if (ct.getStructuralVariantsId() != null) {
            object.append("svIds", StringUtils.join(ct.getStructuralVariantsId(), ","));
        }

        if (ct.getHgvsc() != null) {
            object.append("hgvsc", ct.getHgvsc());
        }
        if (ct.getHgvsp() != null) {
            object.append("hgvsp", ct.getHgvsp());
        }

        if (ct.getIntronNumber() != null) {
            object.append("intron", ct.getIntronNumber());
        }
        if (ct.getExonNumber() != null) {
            object.append("exon", ct.getExonNumber());
        }

        if (ct.getVariantToTranscriptDistance() >= 0) {
            object.append("distance", ct.getVariantToTranscriptDistance());
        }

        if (ct.getClinicalSignificance() != null) {
            object.append("clinicSig", ct.getClinicalSignificance());
        }

        if (ct.getPubmed() != null) {
            object.append("pubmed", StringUtils.join(ct.getPubmed(), ","));
        }
        object.append("canonical", ct.isCanonical());

        return object;
    }

    private BasicDBObject getFrequenciesDBObject(Frequencies frequencies) {
        BasicDBObject object = new BasicDBObject("mafAllele", frequencies.getAllele1000g())
                .append("gmaf", frequencies.getMaf1000G())
                .append("afrMaf", frequencies.getMaf1000GAfrican())
                .append("amrMaf", frequencies.getMaf1000GAmerican())
                .append("asnMaf", frequencies.getMaf1000GAsian())
                .append("eurMaf", frequencies.getMaf1000GEuropean())
                .append("afrAmrMaf", frequencies.getMafNhlbiEspAfricanAmerican())
                .append("eurAmrMaf", frequencies.getMafNhlbiEspEuropeanAmerican());
        return object;
    }

    private BasicDBObject getProteinSubstitutionScores(ProteinSubstitutionScores scores) {
        BasicDBObject object = new BasicDBObject("polyScore", scores.getPolyphenScore())
                .append("siftScore", scores.getSiftScore());
        
        if (scores.getPolyphenEffect() != null) {
            object.append("polyEff", scores.getPolyphenEffect().name());
        }
        if (scores.getSiftEffect() != null) {
            object.append("siftEff", scores.getSiftEffect().name());
        }
        
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
