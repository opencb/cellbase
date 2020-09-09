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

package org.opencb.cellbase.lib.impl.core;

import com.mongodb.MongoTimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.DatastoreStatus;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoDBAdaptorFactory {

    protected CellBaseConfiguration cellBaseConfiguration;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CELLBASE_DB_MONGODB_REPLICASET = "CELLBASE.DB.MONGODB.REPLICASET";
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String MEMBERS = "members";
    private static final String SET = "set";
    private static final String STATE_STR = "stateStr";
    private static final String NAME = "name";
    private static final String COLON = ":";
    private static final String REPLICA_SET = "replica_set";
    private static final String HOST = "host";
    private static final String ADMIN_DATABASE = "admin";
    /**
     * MongoDataStoreManager acts as singleton by keeping a reference to all databases connections created.
     */
    private static MongoDataStoreManager mongoDataStoreManager;
    private static Map<String, MongoDataStoreManager> memberDataStoreManagerMap = new HashMap<>();

    public MongoDBAdaptorFactory(CellBaseConfiguration cellBaseConfiguration) {
        this.cellBaseConfiguration = cellBaseConfiguration;
        init();
    }

    private void init() {
        if (mongoDataStoreManager == null) {
            String[] hosts = cellBaseConfiguration.getDatabases().getMongodb().getHost().split(",");
            List<DataStoreServerAddress> dataStoreServerAddresses = new ArrayList<>(hosts.length);
            for (String host : hosts) {
                String[] hostPort = host.split(":");
                if (hostPort.length == 1) {
                    dataStoreServerAddresses.add(new DataStoreServerAddress(hostPort[0], 27017));
                } else {
                    dataStoreServerAddresses.add(new DataStoreServerAddress(hostPort[0], Integer.parseInt(hostPort[1])));
                }
            }
            mongoDataStoreManager = new MongoDataStoreManager(dataStoreServerAddresses);
            logger.debug("MongoDBAdaptorFactory constructor, this should be only be printed once");
        }
    }

    /**
     * Get database based on species, assembly and version. Throws IllegalArgumentException if no database exists.
     *
     * @param species Species name
     * @param assembly Assembly version
     * @return the datastore associated with given species and assembly
     */
    public MongoDataStore getMongoDBDatastore(String species, String assembly) {
        String databaseName = getDatabaseName(species, assembly);
        MongoDataStore mongoDataStore = null;
        try {
            mongoDataStore = mongoDataStoreManager.get(databaseName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Database does not exist: '" + databaseName + "'");
        }
        return mongoDataStore;
    }

    /**
     * Get database based on database name. Throws IllegalArgumentException if no database exists.
     *
     * @param databaseName name of database
     * @return the datastore of the given name
     */
    public MongoDataStore getMongoDBDatastore(String databaseName) {
        MongoDataStore mongoDataStore = null;
        try {
            mongoDataStore = mongoDataStoreManager.get(databaseName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Database does not exist: '" + databaseName + "'");
        }
        return mongoDataStore;
    }

    // TODO replace with method from SpeciesUtils. We shouldn't have this logic in multiple places
    protected SpeciesConfiguration getSpecies(String speciesName) {
        SpeciesConfiguration species = null;
        for (SpeciesConfiguration sp : cellBaseConfiguration.getAllSpecies()) {
            if (speciesName.equalsIgnoreCase(sp.getId()) || speciesName.equalsIgnoreCase(sp.getScientificName())) {
                species = sp;
                break;
            }
        }
        return species;
    }

    // TODO replace with method from SpeciesUtils. We shouldn't have this logic in multiple places
    protected String getAssembly(SpeciesConfiguration speciesConfiguration, String assemblyName) {
        String assembly = null;
        if (assemblyName == null || assemblyName.isEmpty()) {
            assembly = speciesConfiguration.getAssemblies().get(0).getName();
        } else {
            for (SpeciesConfiguration.Assembly assembly1 : speciesConfiguration.getAssemblies()) {
                if (assemblyName.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1.getName();
                }
            }
        }
        return assembly;
    }


    private MongoDataStore createMongoDBDatastore(String species, String assembly) {
        /**
         Database name has the following pattern in lower case and with no '.' in the name:
         cellbase_speciesId_assembly_cellbaseVersion
         Example:
         cellbase_hsapiens_grch37_v3
         **/

        // We need to look for the species object in the configuration
        SpeciesConfiguration speciesObject = getSpecies(species);
        if (speciesObject != null) {
            species = speciesObject.getId();
            String cellbaseAssembly = getAssembly(speciesObject, assembly);

            if (species != null && !species.isEmpty() && cellbaseAssembly != null && !cellbaseAssembly.isEmpty()) {
                cellbaseAssembly = cellbaseAssembly.toLowerCase();
                // Database name is built following the above pattern
                String database = getDatabaseName(species, cellbaseAssembly);
                logger.debug("Database for the species is '{}'", database);
                return createMongoDBDatastore(database);
            } else {
                logger.error("Assembly is not valid, assembly '{}'. Valid assemblies: {}", assembly,
                        String.join(",", speciesObject.getAssemblies().stream().map((assemblyObject)
                                -> assemblyObject.getName()).collect(Collectors.toList())));
                throw new InvalidParameterException("Assembly is not valid, assembly '" + assembly
                        + "'. Please provide one of supported assemblies: {"
                        + String.join(",", speciesObject.getAssemblies().stream().map((assemblyObject)
                        -> assemblyObject.getName()).collect(Collectors.toList())) + "}");
            }
        } else {
            logger.error("Species name is not valid: '{}'. Valid species: {}", species,
                    String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                            -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                            .collect(Collectors.toList())));
            throw new InvalidParameterException("Species name is not valid: '" + species + "'. Please provide one"
                    + " of supported species: {"
                    + String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                    -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                    .collect(Collectors.toList())) + "}");
        }
    }

    protected String getDatabaseName(String species, String cellbaseAssembly) {
        if (species == null) {
            throw new InvalidParameterException("Species is required");
        }
        if (cellbaseAssembly == null) {
            throw new InvalidParameterException("Assembly is required");
        }
        String cleanAssembly = cellbaseAssembly
                .replaceAll("\\.", "")
                .replaceAll("-", "")
                .replaceAll("_", "");
        return "cellbase" + "_" + species.toLowerCase() + "_" + cleanAssembly.toLowerCase() + "_" + cellBaseConfiguration.getVersion();
    }

    private MongoDataStore createMongoDBDatastore(String database) {
        DatabaseCredentials mongodbCredentials = cellBaseConfiguration.getDatabases().getMongodb();
        MongoDBConfiguration mongoDBConfiguration;
        MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();

        // For authenticated databases
        if (!mongodbCredentials.getUser().isEmpty() && !mongodbCredentials.getPassword().isEmpty()) {
            // MongoDB could authenticate against different databases
            builder.setUserPassword(mongodbCredentials.getUser(), mongodbCredentials.getPassword());
        }

        for (Map.Entry<String, String> entry : mongodbCredentials.getOptions().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equalsIgnoreCase(MongoDBConfiguration.REPLICA_SET) && value.contains(CELLBASE_DB_MONGODB_REPLICASET)) {
                // Skip replica set
                continue;
            }
            if (StringUtils.isNotEmpty(value)) {
                builder.add(key, value);
            }
        }

        mongoDBConfiguration = builder.build();

        logger.debug("*************************************************************************************");
        logger.debug("MongoDataStore configuration parameters: ");
        logger.debug("{} = {}", MongoDBConfiguration.AUTHENTICATION_DATABASE,
                mongoDBConfiguration.get(MongoDBConfiguration.AUTHENTICATION_DATABASE));
        logger.debug("{} = {}", MongoDBConfiguration.READ_PREFERENCE,
                mongoDBConfiguration.get(MongoDBConfiguration.READ_PREFERENCE));
        logger.debug("{} = {}", MongoDBConfiguration.REPLICA_SET,
                mongoDBConfiguration.get(MongoDBConfiguration.REPLICA_SET));
        logger.debug("{} = {}", MongoDBConfiguration.CONNECTIONS_PER_HOST,
                mongoDBConfiguration.get(MongoDBConfiguration.CONNECTIONS_PER_HOST));
        logger.debug("*************************************************************************************");

        // A MongoDataStore to this host and database is returned
        MongoDataStore mongoDatastore = mongoDataStoreManager.get(database, mongoDBConfiguration);

        // we return the MongoDataStore object
        return mongoDatastore;
    }

    public void open(String species, String assembly) {

    }

    public void close() {
        mongoDataStoreManager.close();
    }

    public Map<String, DatastoreStatus> getDatabaseStatus(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        try {
            if (mongoDatastore.isReplSet()) {
                // This mongoDatastore object is not valid for a RS since will be used to run replSetGetStatus which
                // can only be run against the "admin" database
                return getReplSetStatus(species, assembly);
            } else {
                return getSingleMachineDBStatus(mongoDatastore, species, assembly);
            }
        // Can happen if cannot find host, for example
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, DatastoreStatus> getSingleMachineDBStatus(MongoDataStore mongoDatastore, String species, String assembly) {
        Document statusDocument = mongoDatastore.getServerStatus();
        Map<String, DatastoreStatus> statusMap = new HashMap<>(1);
        DatastoreStatus datastoreStatus = new DatastoreStatus();
        datastoreStatus.setResponseTime(getDataResponseTime(species, assembly));
        statusMap.put((String) statusDocument.get(HOST), datastoreStatus);
        return statusMap;
    }

    private Map<String, DatastoreStatus> getReplSetStatus(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(ADMIN_DATABASE);
        Document statusDocument = mongoDatastore.getReplSetStatus();
        Map<String, DatastoreStatus> statusMap = new HashMap<>(4);

        String repset = (String) statusDocument.get(SET);
        if (StringUtils.isNotBlank(repset)) {
            DatastoreStatus datastoreStatus = new DatastoreStatus();
            datastoreStatus.setRepset(repset);
            // Overall database response time is measured by raising a query to Gene collection
            datastoreStatus.setResponseTime(getDataResponseTime(species, assembly));
            datastoreStatus.setRole(REPLICA_SET);
            statusMap.put(repset, datastoreStatus);
        }

        for (Map memberStatus : (List<Map>) statusDocument.get(MEMBERS)) {
            DatastoreStatus datastoreStatus
                    = new DatastoreStatus();
            datastoreStatus.setRepset(repset);
            datastoreStatus.setRole(((String) memberStatus.get(STATE_STR)).toLowerCase());
            String memberName = ((String) memberStatus.get(NAME)).split(COLON)[0];
            // Per-machine response time is measured by doing ping to the machine. it's not possible to create a connection
            // to one single machine in the rep set
            datastoreStatus.setResponseTime(getPingResponseTime(memberName));
            statusMap.put(memberName, datastoreStatus);
        }
        return statusMap;
    }

    private String getDataResponseTime(String species, String assembly) {
        GeneMongoDBAdaptor geneDBAdaptor = getGeneDBAdaptor(species, assembly);
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.setLimit(1);
            CellBaseDataResult<Gene> cellBaseDataResult = geneDBAdaptor.query(geneQuery);
            // Query must return one gene. Otherwise there's a problem
            if (cellBaseDataResult.getNumResults() == 1) {
                return cellBaseDataResult.getTime() + "ms";
            } else {
                return null;
            }
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPingResponseTime(String memberName) {
        try {
            StopWatch uptime = new StopWatch();
            uptime.start();
            InetAddress address = InetAddress.getByName(memberName);
            boolean chkConnection = address.isReachable(1000);
            if (chkConnection) {
                return String.valueOf(TimeUnit.NANOSECONDS.toMillis(uptime.getNanoTime())) + "ms";
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public GenomeMongoDBAdaptor getGenomeDBAdaptor(String species) {
        return getGenomeDBAdaptor(species, null);
    }

    public GenomeMongoDBAdaptor getGenomeDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new GenomeMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public MetaMongoDBAdaptor getMetaDBAdaptor(String species) {
        return getMetaDBAdaptor(species, null);
    }

    public MetaMongoDBAdaptor getMetaDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new MetaMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public GeneMongoDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    public GeneMongoDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        GeneMongoDBAdaptor geneMongoDBAdaptor = new GeneMongoDBAdaptor(species, assembly, mongoDatastore);
        return geneMongoDBAdaptor;
    }

    public TranscriptMongoDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    public TranscriptMongoDBAdaptor getTranscriptDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new TranscriptMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public XRefMongoDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    public XRefMongoDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new XRefMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public VariantMongoDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    public VariantMongoDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new VariantMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public ClinicalMongoDBAdaptor getClinicalDBAdaptor(String species) throws CellbaseException {
        return getClinicalDBAdaptor(species, null);
    }

    public ClinicalMongoDBAdaptor getClinicalDBAdaptor(String species, String assembly) throws CellbaseException {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        // FIXME temporarily add config so we can get to the manager. this should be removed when we move all
        // methods to the manager.
        return new ClinicalMongoDBAdaptor(species, assembly, mongoDatastore, cellBaseConfiguration);
    }

    public RepeatsMongoDBAdaptor getRepeatsDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new RepeatsMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public ProteinMongoDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    public ProteinMongoDBAdaptor getProteinDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ProteinMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public OntologyMongoDBAdaptor getOntologyMongoDBAdaptor(String species) {
        return getOntologyMongoDBAdaptor(species, null);
    }

    public OntologyMongoDBAdaptor getOntologyMongoDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new OntologyMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public RegulationMongoDBAdaptor getRegulationDBAdaptor(String species) {
        return getRegulationDBAdaptor(species, null);
    }

    public RegulationMongoDBAdaptor getRegulationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new RegulationMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    public MissenseVariationFunctionalScoreMongoDBAdaptor getMissenseVariationFunctionalScoreMongoDBAdaptor(
            String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new MissenseVariationFunctionalScoreMongoDBAdaptor(species, assembly, mongoDatastore);
    }
}
