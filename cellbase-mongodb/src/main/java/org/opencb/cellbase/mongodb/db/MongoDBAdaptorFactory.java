package org.opencb.cellbase.mongodb.db;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.core.*;
import org.opencb.cellbase.core.lib.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.core.lib.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.*;
import org.opencb.cellbase.mongodb.db.network.PathwayMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.network.ProteinProteinInteractionMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.regulatory.RegulatoryRegionMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.regulatory.TfbsMongoDBAdaptor;
import org.opencb.datastore.mongodb.MongoDBConfiguration;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.*;

public class MongoDBAdaptorFactory extends DBAdaptorFactory {

    @Deprecated
    private static Map<String, DB> mongoDBFactory;
    private static Map<String, MongoDataStore> mongoDatastoreFactory;


    // private static Config applicationProperties;
    private static ResourceBundle resourceBundle;
    protected static Properties applicationProperties;

//    protected static Map<String, String> speciesAlias;

    protected static CellbaseConfiguration config;


    static {
        // mongoDBFactory = new HashMap<String, HibernateDBAdaptor>(20);
//        speciesAlias = new HashMap<>();
        mongoDBFactory = new HashMap<>(10);

        mongoDatastoreFactory = new HashMap<>(10);

        // reading application.properties file
//        resourceBundle = ResourceBundle.getBundle("mongodb");
////			applicationProperties = new Config(resourceBundle);
//        applicationProperties = new Properties();
//        if (resourceBundle != null) {
//            Set<String> keys = resourceBundle.keySet();
//            Iterator<String> iterator = keys.iterator();
//            String nextKey;
//            while (iterator.hasNext()) {
//                nextKey = iterator.next();
//                applicationProperties.put(nextKey, resourceBundle.getString(nextKey));
//            }
//        }
//
//
//        String[] speciesArray = applicationProperties.getProperty("SPECIES").split(",");
//        String[] alias = null;
//        String version;
//        for (String species : speciesArray) {
//            species = species.toUpperCase();
//            version = applicationProperties.getProperty(species + ".DEFAULT.VERSION").toUpperCase();
//            alias = applicationProperties.getProperty(species + "." + version + ".ALIAS").split(",");
//
////                System.out.println("");
////                System.out.println(species);
//            for (String al : alias) {
////                System.out.print(al+' ');
//                speciesAlias.put(al, species);
//            }
////                System.out.println("");
//            // For to recognize the species code
//            speciesAlias.put(species, species);
//        }
    }

    public MongoDBAdaptorFactory(CellbaseConfiguration config){
        super();
        this.config = config;
    }

    private MongoDataStore createCellBaseMongoDatastore(String species, String assembly) {
        String speciesId = config.getAlias(species);
        MongoDataStoreManager mongoDataStoreManager = new MongoDataStoreManager(config.getHost(speciesId, assembly),
                config.getPort(speciesId, assembly));

        MongoDBConfiguration mongoDBConfiguration;
        if(!config.getUsername(speciesId,assembly).equals("") || !config.getPass(speciesId, assembly).equals("")) {
            mongoDBConfiguration = MongoDBConfiguration.builder().add("username", config.getUsername(speciesId, assembly)).
                    add("password", config.getPass(speciesId, assembly)).init().build();
        } else {
            mongoDBConfiguration = MongoDBConfiguration.builder().init().build();
        }
        return mongoDataStoreManager.get(config.getDatabase(speciesId, assembly), mongoDBConfiguration);
    }

    @Deprecated
    private DB createCellBaseMongoDB(String species, String assembly) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        MongoClient mc = null;
        DB db = null;
        if (species != null && !species.trim().equals("")) {
            String speciesId = config.getAlias(species);
            try {
                MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                        .connectionsPerHost(config.getMaxPoolSize(speciesId, assembly))
                        .connectTimeout(config.getTimeout(speciesId, assembly))
                        .build();

                logger.info(config.getHost(speciesId, assembly));
                logger.info(Integer.toString(config.getPort(speciesId, assembly)));
                mc = new MongoClient(new ServerAddress(config.getHost(speciesId, assembly), config.getPort(speciesId, assembly)), mongoClientOptions);

                logger.info(config.getDatabase(speciesId, assembly));
                db = mc.getDB(config.getDatabase(speciesId, assembly));

                logger.info(config.getUsername(speciesId,assembly));
                logger.info(config.getPass(speciesId, assembly));
                if(!config.getUsername(speciesId,assembly).equals("") || !config.getPass(speciesId, assembly).equals("")){
                    db.authenticate(config.getUsername(speciesId,assembly), config.getPass(speciesId, assembly).toCharArray());
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("MongoDBAdaptorFactory in createCellBaseMongoDB(): 'species' parameter is null or empty");
        }
        return db;
    }

    @Deprecated
    protected String getSpeciesVersionPrefix(String species, String version) {
        String speciesPrefix = null;
        if (species != null && !species.equals("")) {
            // coding an alias to application code species
            species = config.getAlias(species);
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

    protected String getSpeciesAssemblyPrefix(String species, String assembly) {
        String speciesPrefix = null;
        if (species != null && !species.equals("")) {
            // coding an alias to application code species
            species = config.getAlias(species);
            // if 'version' parameter has not been provided the default version is selected
            if (assembly == null || assembly.trim().equals("")) {
                assembly = "default";
//				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
            }

            // setting database configuration for the 'species.version'
            speciesPrefix = species.toUpperCase() + "." + assembly.toUpperCase();
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
    public void open(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);

        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(species, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
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
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species) {
        return getGenomeSequenceDBAdaptor(species, null);
    }

    @Override
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new GenomeSequenceMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species) {
        return getChromosomeDBAdaptor(species, null);
    }

    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ChromosomeMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species) {
        return getConservedRegionDBAdaptor(species, null);
    }

    @Override
    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ConservedRegionMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species) {
        return getExonDBAdaptor(species, null);
    }

    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ExonMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new TranscriptMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new GeneMongoDBAdaptor(speciesId, assembly, config.getCoreChunkSize(),
                mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new XRefsMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public VariationDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    @Override
    public VariationDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new VariationMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
        return getVariantAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = new VariantAnnotationMongoDBAdaptor(speciesId, assembly,
                mongoDatastoreFactory.get(speciesAssemblyPrefix));
        variantAnnotationDBAdaptor.setGeneDBAdaptor(getGeneDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setRegulatoryRegionDBAdaptor(getRegulatoryRegionDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setVariationDBAdaptor(getVariationDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setVariantClinicalDBAdaptor(getClinicalDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setProteinFunctionPredictorDBAdaptor(getProteinFunctionPredictorDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setConservedRegionDBAdaptor(getConservedRegionDBAdaptor(species, assembly));

        return variantAnnotationDBAdaptor;
    }


    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species) {
        return getClinicalDBAdaptor(species, null);
    }

    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ClinicalMongoDBAdaptor(speciesId, assembly,
                mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ProteinMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species) {
        return getProteinFunctionPredictorDBAdaptor(species, null);
    }

    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ProteinFunctionPredictorMongoDBAdaptor(speciesId, assembly,
                mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species) {
        return getProteinProteinInteractionDBAdaptor(species, null);
    }

    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new ProteinProteinInteractionMongoDBAdaptor(speciesId, assembly,
                mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species) {
        return getRegulatoryRegionDBAdaptor(species, null);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new RegulatoryRegionMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
        return getTfbsDBAdaptor(species, null);
    }

    @Override
    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if(!mongoDatastoreFactory.containsKey(speciesAssemblyPrefix)) {
            MongoDataStore mongoDataStore = createCellBaseMongoDatastore(speciesId, assembly);
            mongoDatastoreFactory.put(speciesAssemblyPrefix, mongoDataStore);
        }
        return new TfbsMongoDBAdaptor(speciesId, assembly, mongoDatastoreFactory.get(speciesAssemblyPrefix));
    }


    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species) {
        return getPathwayDBAdaptor(species, null);
    }

    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (PathwayDBAdaptor) new PathwayMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getGenomeSequenceChunkSize());
    }





    @Override
    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species) {
        return getVariationPhenotypeAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (VariationPhenotypeAnnotationDBAdaptor) new VariationPhenotypeAnnotationMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SnpDBAdaptor getSnpDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SnpDBAdaptor getSnpDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MirnaDBAdaptor getMirnaDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MirnaDBAdaptor getMirnaDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Deprecated
    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species) {
        return getGenomicVariantEffectDBAdaptor(species, null);
    }

    @Deprecated
    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return new VariantEffectMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix), speciesId, assembly);
    }

    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species) {
        return getMutationDBAdaptor(species, null);
    }

    @Deprecated
    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (MutationDBAdaptor) new MutationMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public ClinVarDBAdaptor getClinVarDBAdaptor(String species) {
        return getClinVarDBAdaptor(species, null);
    }

    @Override
    public ClinVarDBAdaptor getClinVarDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ClinVarDBAdaptor) new ClinVarMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }
}
