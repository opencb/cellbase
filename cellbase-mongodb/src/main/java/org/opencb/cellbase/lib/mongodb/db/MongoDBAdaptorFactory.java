package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.network.PathwayDBAdaptor;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.*;
import org.opencb.cellbase.lib.mongodb.db.network.PathwayMongoDBAdaptor;
import org.opencb.cellbase.lib.mongodb.db.network.ProteinProteinInteractionMongoDBAdaptor;
import org.opencb.cellbase.lib.mongodb.db.regulatory.RegulatoryRegionMongoDBAdaptor;
import org.opencb.cellbase.lib.mongodb.db.regulatory.TfbsMongoDBAdaptor;

import java.net.UnknownHostException;
import java.util.*;

public class MongoDBAdaptorFactory extends DBAdaptorFactory {

    private static Map<String, DB> mongoDBFactory;
    // private static Config applicationProperties;
    private static ResourceBundle resourceBundle;
    protected static Properties applicationProperties;

    protected static Map<String, String> speciesAlias;

    protected static Config config;


    static {
        // mongoDBFactory = new HashMap<String, HibernateDBAdaptor>(20);
        speciesAlias = new HashMap<>();
        mongoDBFactory = new HashMap<>(10);

        // reading application.properties file
        resourceBundle = ResourceBundle.getBundle("mongodb");
//			applicationProperties = new Config(resourceBundle);
        applicationProperties = new Properties();
        if (resourceBundle != null) {
            Set<String> keys = resourceBundle.keySet();
            Iterator<String> iterator = keys.iterator();
            String nextKey;
            while (iterator.hasNext()) {
                nextKey = iterator.next();
                applicationProperties.put(nextKey, resourceBundle.getString(nextKey));
            }
        }


        String[] speciesArray = applicationProperties.getProperty("SPECIES").split(",");
        String[] alias = null;
        String version;
        for (String species : speciesArray) {
            species = species.toUpperCase();
            version = applicationProperties.getProperty(species + ".DEFAULT.VERSION").toUpperCase();
            alias = applicationProperties.getProperty(species + "." + version + ".ALIAS").split(",");

//                System.out.println("");
//                System.out.println(species);
            for (String al : alias) {
//                System.out.print(al+' ');
                speciesAlias.put(al, species);
            }
//                System.out.println("");
            // For to recognize the species code
            speciesAlias.put(species, species);
        }
    }

    public MongoDBAdaptorFactory(Config config){
        super();
        this.config = config;
    }

    private DB createCellBaseMongoDB(String dbName) {
        // logger.debug("HibernateDBAdaptorFactory in getGeneDBAdaptor(): creating Hibernate SessionFactory object for SPECIES.VERSION: '"+speciesVersionPrefix+"' ...");
        // long t1 = System.currentTimeMillis();
//        System.out.println(speciesVersionPrefix + "=>"
//                + applicationProperties.getProperty(speciesVersionPrefix + ".DATABASE"));
        // initial load and setup from hibernate.cfg.xml
        // Configuration cfg = new
        // Configuration().configure("cellbase-hibernate.cfg.xml");
        MongoClient mc = null;
        DB db = null;
        if (dbName != null && !dbName.trim().equals("")) {
            try {
                MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                        .connectionsPerHost(config.maxPoolSize)
                        .connectTimeout(config.timeout)
                        .build();

                mc = new MongoClient(new ServerAddress(config.host, config.port), mongoClientOptions);
//                mc.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//                mc.setReadPreference(ReadPreference.primary());
//                System.out.println("Replica Status: "+mc.getReplicaSetStatus());
                db = mc.getDB(dbName);
//db.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//db.setReadPreference(ReadPreference.primary());
                System.out.println("Debug String: "+mc.debugString());
                if(!config.user.equals("") || !config.pass.equals("")){
                    db.authenticate(config.user,config.pass.toCharArray());
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'species' parameter is null or empty");
        }

        return db;
    }

    protected String getSpeciesVersionPrefix(String species, String version) {
        String speciesPrefix = null;
        if (species != null && !species.equals("")) {
            // coding an alias to application code species
            species = config.getSpeciesAlias().get(species);
            // if 'version' parameter has not been provided the default version is selected
            if (version == null || version.trim().equals("")) {
                version = applicationProperties.getProperty(species + ".DEFAULT.VERSION").toUpperCase();
//				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
            }

            // setting database configuration for the 'species.version'
            speciesPrefix = species.toUpperCase() + "." + version.toUpperCase();
        }
        return speciesPrefix;
    }

    @Override
    public void setConfiguration(Properties properties) {
        if (properties != null) {
            if (applicationProperties == null) {
//                applicationProperties = new Config();
                applicationProperties = properties;
            } else {
                for (Object key : properties.keySet()) {
                    applicationProperties.setProperty((String) key, properties.getProperty((String) key));
                }
            }
        }
    }

    @Override
    public void open(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);

        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
    }

    @Override
    public void close() {
        for (DB sessionFactory : mongoDBFactory.values()) {
            if (sessionFactory != null) {
                sessionFactory.cleanCursors(true);
            }
        }
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (GeneDBAdaptor) new GeneMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version, config.getCoreChunksize());
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (TranscriptDBAdaptor) new TranscriptMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species) {
        return getChromosomeDBAdaptor(species, null);
    }

    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (ChromosomeDBAdaptor) new ChromosomeMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species) {
        return getExonDBAdaptor(species, null);
    }

    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (ExonDBAdaptor) new ExonMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species) {
        return getGenomicVariantEffectDBAdaptor(species, null);
    }

    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (VariantEffectDBAdaptor) new VariantEffectMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }


    @Override
    public VariantAnnotationDBAdaptor getGenomicVariantAnnotationDBAdaptor(String species) {
        return getGenomicVariantAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariantAnnotationDBAdaptor getGenomicVariantAnnotationDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (VariantAnnotationDBAdaptor) new VariantAnnotationMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }


    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return new ProteinMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public SnpDBAdaptor getSnpDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SnpDBAdaptor getSnpDBAdaptor(String species, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species) {
        return getGenomeSequenceDBAdaptor(species, null);
    }

    @Override
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (GenomeSequenceDBAdaptor) new GenomeSequenceMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (XRefsDBAdaptor) new XRefsMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species) {
        return getRegulatoryRegionDBAdaptor(species, null);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (RegulatoryRegionDBAdaptor) new RegulatoryRegionMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public MirnaDBAdaptor getMirnaDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MirnaDBAdaptor getMirnaDBAdaptor(String species, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species) {
        return getMutationDBAdaptor(species, null);
    }

    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (MutationDBAdaptor) new MutationMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species) {
        return getPathwayDBAdaptor(species, null);
    }

    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (PathwayDBAdaptor) new PathwayMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version, config.getGenomeSequenceChunkSize());
    }


    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species) {
        return getProteinProteinInteractionDBAdaptor(species, null);
    }

    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (ProteinProteinInteractionDBAdaptor) new ProteinProteinInteractionMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }


//    public RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species) {
//        return getRegulationDBAdaptor(species, null);
//    }
//
//    public RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species, String version) {
//        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
//        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
//            DB db = createCellBaseMongoDB(speciesVersionPrefix);
//            mongoDBFactory.put(speciesVersionPrefix, db);
//        }
//        return (RegulatoryRegionDBAdaptor) new RegulatoryRegionMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
//                config.getSpeciesAlias().get(species), version);
//    }

    public VariationDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    public VariationDBAdaptor getVariationDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (VariationDBAdaptor) new VariationMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version, config.getVariationChunksize());
    }

    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species) {
        return getConservedRegionDBAdaptor(species, null);
    }


    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (ConservedRegionDBAdaptor) new ConservedRegionMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }

    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species) {
        return getProteinFunctionPredictorDBAdaptor(species, null);
    }

    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (ProteinFunctionPredictorDBAdaptor) new ProteinFunctionPredictorMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }


    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
        return getTfbsDBAdaptor(species, null);
    }


    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (TfbsDBAdaptor) new TfbsMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }


    @Override
    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species) {
        return getVariationPhenotypeAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species, String version) {
        String speciesVersionPrefix = getSpeciesVersionPrefix(species, version);
        if (!mongoDBFactory.containsKey(speciesVersionPrefix)) {
            DB db = createCellBaseMongoDB(speciesVersionPrefix);
            mongoDBFactory.put(speciesVersionPrefix, db);
        }
        return (VariationPhenotypeAnnotationDBAdaptor) new VariationPhenotypeAnnotationMongoDBAdaptor(mongoDBFactory.get(speciesVersionPrefix),
                config.getSpeciesAlias().get(species), version);
    }
}
