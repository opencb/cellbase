package org.opencb.cellbase.app.loaders.mongodb;

//import com.mongodb.BasicDBList;
//import com.mongodb.BasicDBObject;
//import org.apache.commons.lang.StringUtils;
//import org.opencb.biodata.models.variant.Variant;
//import org.opencb.biodata.models.variant.annotation.*;
//import org.opencb.commons.io.DataWriter;
//import org.opencb.commons.utils.CryptoUtils;
//import org.opencb.datastore.core.QueryResult;
//import org.opencb.datastore.mongodb.MongoDBCollection;
//import org.opencb.datastore.mongodb.MongoDataStore;
//import org.opencb.datastore.mongodb.MongoDataStoreManager;
//
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@Deprecated
public class VariantEffectMongoDBLoader {   //implements DataWriter<VariantAnnotation>

    /*
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
    public boolean write(VariantAnnotation elem) {
        return write(Arrays.asList(elem));
    }

    @Override
    public boolean write(List<VariantAnnotation> batch) {
        boolean ok = true;
        
        for (VariantAnnotation effect : batch) {
            String rowkey = buildRowkey(effect);
            BasicDBObject mongoEffect = new BasicDBObject("_id", rowkey)
                    .append("chr", effect.getChromosome())
                    .append("start", effect.getStart()).append("end", effect.getEnd())
                    .append("ref", effect.getReferenceAllele())
                    .append("freqs", getFrequenciesDBObject(effect.getFrequencies()))
                    .append("scores", getProteinSubstitutionScores(effect.getProteinSubstitutionScores()))
                    .append("regulatory", getRegulatoryEffect(effect.getRegulatoryEffect()));

            BasicDBList alleles = new BasicDBList();

            for (Map.Entry<String, List<VariantEffect>> allelesConsequences : effect.getEffects().entrySet()) {
                BasicDBObject alleleRoot = new BasicDBObject("alt", allelesConsequences.getKey());
                BasicDBList cts = new BasicDBList();

                for (VariantEffect ct : allelesConsequences.getValue()) {
                    cts.add(getConsequenceTypeDBObject(ct));
                }

                alleleRoot.append("val", cts);
                alleles.add(alleleRoot);
            }

            mongoEffect.append("ct", alleles);

//            QueryResult result = collection.insert(mongoEffect);
            QueryResult result = collection.update(new BasicDBObject("_id", rowkey), mongoEffect, true, true);
            if (result.getErrorMsg() != null) {
                // TODO Do anything special when an error occurs?
                Logger.getLogger(VariantEffectMongoDBLoader.class.getName()).log(Level.SEVERE, null, result.getErrorMsg());
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

    
    private BasicDBObject getConsequenceTypeDBObject(VariantEffect variantEffect) {
        BasicDBObject object = new BasicDBObject("so", variantEffect.getConsequenceTypes());

        if (variantEffect.getGeneId() != null) {
            object.append("geneId", variantEffect.getGeneId());
        }
        if (variantEffect.getGeneName() != null) {
            object.append("geneName", variantEffect.getGeneName());
        }
        if (variantEffect.getGeneNameSource() != null) {
            object.append("geneNameSource", variantEffect.getGeneNameSource());
        }

        if (variantEffect.getFeatureId() != null) {
            object.append("featureId", variantEffect.getFeatureId());
        }
        if (variantEffect.getFeatureType() != null) {
            object.append("featureType", variantEffect.getFeatureType());
        }
        if (variantEffect.getFeatureStrand() != null) {
            object.append("featureStrand", variantEffect.getFeatureStrand());
        }
        if (variantEffect.getFeatureBiotype() != null) {
            object.append("featureBiotype", variantEffect.getFeatureBiotype());
        }

        if (variantEffect.getcDnaPosition() >= 0) {
            object.append("cdnaPos", variantEffect.getcDnaPosition());
        }
        if (variantEffect.getCcdsId() != null) {
            object.append("ccdsId", variantEffect.getCcdsId());
        }
        if (variantEffect.getCdsPosition() >= 0) {
            object.append("cdsPos", variantEffect.getCdsPosition());
        }

        if (variantEffect.getProteinId() != null) {
            object.append("proteinId", variantEffect.getProteinId());
        }
        if (variantEffect.getProteinPosition() >= 0) {
            object.append("proteinPos", variantEffect.getProteinPosition());
        }
        if (variantEffect.getProteinDomains() != null) {
            object.append("proteinDomains", StringUtils.join(variantEffect.getProteinDomains(), ","));
        }

        if (variantEffect.getAminoacidChange() != null) {
            object.append("aaChange", variantEffect.getAminoacidChange());
        }
        if (variantEffect.getCodonChange() != null) {
            object.append("codonChange", variantEffect.getCodonChange());
        }

        if (variantEffect.getVariationId() != null) {
            object.append("id", variantEffect.getVariationId());
        }
        if (variantEffect.getStructuralVariantsId() != null) {
            object.append("svIds", StringUtils.join(variantEffect.getStructuralVariantsId(), ","));
        }

        if (variantEffect.getHgvsc() != null) {
            object.append("hgvsc", variantEffect.getHgvsc());
        }
        if (variantEffect.getHgvsp() != null) {
            object.append("hgvsp", variantEffect.getHgvsp());
        }

        if (variantEffect.getIntronNumber() != null) {
            object.append("intron", variantEffect.getIntronNumber());
        }
        if (variantEffect.getExonNumber() != null) {
            object.append("exon", variantEffect.getExonNumber());
        }

        if (variantEffect.getVariantToTranscriptDistance() >= 0) {
            object.append("distance", variantEffect.getVariantToTranscriptDistance());
        }

        if (variantEffect.getClinicalSignificance() != null) {
            object.append("clinicSig", variantEffect.getClinicalSignificance());
        }

        if (variantEffect.getPubmed() != null) {
            object.append("pubmed", StringUtils.join(variantEffect.getPubmed(), ","));
        }
        object.append("canonical", variantEffect.isCanonical());

        return object;
    }

    private BasicDBObject getFrequenciesDBObject(Map<String, Set<Frequency>> frequencies) {
        BasicDBObject object = null;
//        new BasicDBObject("mafAllele", frequencies.getAllele1000g())
//                .append("gmaf", frequencies.getMaf1000G())
//                .append("afrMaf", frequencies.getMaf1000GAfrican())
//                .append("amrMaf", frequencies.getMaf1000GAmerican())
//                .append("asnMaf", frequencies.getMaf1000GAsian())
//                .append("eurMaf", frequencies.getMaf1000GEuropean())
//                .append("afrAmrMaf", frequencies.getMafNhlbiEspAfricanAmerican())
//                .append("eurAmrMaf", frequencies.getMafNhlbiEspEuropeanAmerican());
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

    private String buildRowkey(VariantAnnotation v) {
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
    */
}
