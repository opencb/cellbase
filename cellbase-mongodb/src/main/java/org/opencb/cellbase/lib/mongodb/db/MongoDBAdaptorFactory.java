package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.*;

public class MongoDBAdaptorFactory extends DBAdaptorFactory {

    private static Map<String, DB> mongoDBFactory;
    // private static Config applicationProperties;
    private static ResourceBundle resourceBundle;
    protected static Properties applicationProperties;

    protected static Map<String, String> speciesAlias;

    protected static CellbaseConfiguration config;


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

    public MongoDBAdaptorFactory(CellbaseConfiguration config){
        super();
        this.config = config;
    }

    private DB createCellBaseMongoDB(String species, String assembly) {
        // logger.debug("HibernateDBAdaptorFactory in getGeneDBAdaptor(): creating Hibernate SessionFactory object for SPECIES.VERSION: '"+speciesVersionPrefix+"' ...");
        // long t1 = System.currentTimeMillis();
//        System.out.println(speciesVersionPrefix + "=>"
//                + applicationProperties.getProperty(speciesVersionPrefix + ".DATABASE"));
        // initial load and setup from hibernate.cfg.xml
        // Configuration cfg = new
        // Configuration().configure("cellbase-hibernate.cfg.xml");

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

                mc = new MongoClient(new ServerAddress(config.getHost(speciesId, assembly), config.getPort(speciesId, assembly)), mongoClientOptions);
//                mc.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//                mc.setReadPreference(ReadPreference.primary());
//                System.out.println("Replica Status: "+mc.getReplicaSetStatus());

                db = mc.getDB(config.getDatabase(speciesId, assembly));
//db.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//db.setReadPreference(ReadPreference.primary());
//                System.out.println("Debug String: "+mc.debugString());
                if(!config.getUsername(speciesId,assembly).equals("") || !config.getPass(speciesId, assembly).equals("")){
//                    System.out.println("User: "+config.getUsername(speciesId,assembly)+", Password: "+config.getPass(speciesId, assembly)+"");
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
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (GeneDBAdaptor) new GeneMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getCoreChunkSize());
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (TranscriptDBAdaptor) new TranscriptMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species) {
        return getChromosomeDBAdaptor(species, null);
    }

    @Override
    public ChromosomeDBAdaptor getChromosomeDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ChromosomeDBAdaptor) new ChromosomeMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species) {
        return getExonDBAdaptor(species, null);
    }

    @Override
    public ExonDBAdaptor getExonDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ExonDBAdaptor) new ExonMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species) {
        return getGenomicVariantEffectDBAdaptor(species, null);
    }

    @Override
    public VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (VariantEffectDBAdaptor) new VariantEffectMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }


    @Override
    public VariantAnnotationDBAdaptor getGenomicVariantAnnotationDBAdaptor(String species) {
        return getGenomicVariantAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariantAnnotationDBAdaptor getGenomicVariantAnnotationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (VariantAnnotationDBAdaptor) new VariantAnnotationMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getCoreChunkSize());
    }


    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return new ProteinMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
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
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species) {
        return getGenomeSequenceDBAdaptor(species, null);
    }

    @Override
    public GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (GenomeSequenceDBAdaptor) new GenomeSequenceMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getGenomeSequenceChunkSize());
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (XRefsDBAdaptor) new XRefsMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species) {
        return getRegulatoryRegionDBAdaptor(species, null);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (RegulatoryRegionDBAdaptor) new RegulatoryRegionMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
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

    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species) {
        return getMutationDBAdaptor(species, null);
    }

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
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species) {
        return getProteinProteinInteractionDBAdaptor(species, null);
    }

    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ProteinProteinInteractionDBAdaptor) new ProteinProteinInteractionMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }


//    public RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species) {
//        return getRegulationDBAdaptor(species, null);
//    }
//
//    public RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species, String assembly) {
//        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
//        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
//            DB db = createCellBaseMongoDB(species, assembly);
//            mongoDBFactory.put(speciesAssemblyPrefix, db);
//        }
//        return (RegulatoryRegionDBAdaptor) new RegulatoryRegionMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
//                config.getAlias(species), assembly);
//    }

    public VariationDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    public VariationDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (VariationDBAdaptor) new VariationMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getVariationChunkSize());
    }

    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species) {
        return getConservedRegionDBAdaptor(species, null);
    }


    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ConservedRegionDBAdaptor) new ConservedRegionMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly, config.getConservedRegionChunkSize());
    }

    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species) {
        return getProteinFunctionPredictorDBAdaptor(species, null);
    }

    @Override
    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (ProteinFunctionPredictorDBAdaptor) new ProteinFunctionPredictorMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
    }


    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
        return getTfbsDBAdaptor(species, null);
    }


    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly) {
        String speciesAssemblyPrefix = getSpeciesAssemblyPrefix(species, assembly);
        String speciesId = config.getAlias(species);
        if (!mongoDBFactory.containsKey(speciesAssemblyPrefix)) {
            DB db = createCellBaseMongoDB(speciesId, assembly);
            mongoDBFactory.put(speciesAssemblyPrefix, db);
        }
        return (TfbsDBAdaptor) new TfbsMongoDBAdaptor(mongoDBFactory.get(speciesAssemblyPrefix),
                speciesId, assembly);
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
}
