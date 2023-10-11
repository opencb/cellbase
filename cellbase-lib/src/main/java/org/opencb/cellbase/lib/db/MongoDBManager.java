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

package org.opencb.cellbase.lib.db;

import com.mongodb.MongoTimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.impl.core.ReleaseMongoDBAdaptor;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.monitor.DatastoreStatus;
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

public class MongoDBManager {

    public static final String DBNAME_SEPARATOR = "_";

    private MongoDataStoreManager mongoDataStoreManager;
    private CellBaseConfiguration cellBaseConfiguration;

    private Logger logger;

    private static final String CELLBASE_DB_MONGODB_REPLICASET = "CELLBASE.DB.MONGODB.REPLICASET";
    private static final String MEMBERS = "members";
    private static final String SET = "set";
    private static final String STATE_STR = "stateStr";
    private static final String NAME = "name";
    private static final String COLON = ":";
    private static final String REPLICA_SET = "replica_set";
    private static final String HOST = "host";
    private static final String ADMIN_DATABASE = "admin";

    public MongoDBManager(CellBaseConfiguration cellBaseConfiguration) {
        this.cellBaseConfiguration = cellBaseConfiguration;

        init();
    }

    private void init() {
        logger = LoggerFactory.getLogger(this.getClass());

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

    public MongoDataStore createMongoDBDatastore(String speciesStr, String assemblyStr) {
        try {
            // We need to look for the species object in the configuration
            Species species = SpeciesUtils.getSpecies(cellBaseConfiguration, speciesStr, assemblyStr);
            // Database name has the following pattern in lower case and with no '.' in the name:
            //  cellbase_speciesId_assembly_cellbaseVersion
            // Example:
            //  cellbase_hsapiens_grch37_v3
            String database = getDatabaseName(species.getId(), species.getAssembly(), cellBaseConfiguration.getVersion());
            logger.debug("Database for the species is '{}'", database);
            return createMongoDBDatastore(database);
        } catch (CellBaseException e) {
            e.printStackTrace();
            logger.error("Species name is not valid: '{}'. Valid species: {}", speciesStr,
                    String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                            -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                            .collect(Collectors.toList())));
            throw new InvalidParameterException("Species name is not valid: '" + speciesStr + "'. Please provide one"
                    + " of supported species: {"
                    + String.join(",", cellBaseConfiguration.getAllSpecies().stream().map((tmpSpeciesObject)
                    -> (tmpSpeciesObject.getCommonName() + "|" + tmpSpeciesObject.getScientificName()))
                    .collect(Collectors.toList())) + "}");
        }
    }

    public MongoDataStore createMongoDBDatastore(String database) {
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
        logger.debug("{} = {}", MongoDBConfiguration.AUTHENTICATION_MECHANISM,
                mongoDBConfiguration.get(MongoDBConfiguration.AUTHENTICATION_MECHANISM));
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

    public static String getDatabaseName(String species, String assembly, String version) {
        if (StringUtils.isEmpty(species) || StringUtils.isEmpty(assembly)) {
            throw new InvalidParameterException("Species and assembly are required");
        }

        String cleanAssembly = assembly
                .replaceAll("\\.", "")
                .replaceAll("-", "")
                .replaceAll("_", "");

        // Process version from the configuration file, in order to suffix the database name
        //  - Production environment, e.g.: if version is "v5", the suffix added wil be "_v5"
        //  - Test environment, e.g.: if version is "v5.6" or "v5.6.0-SNAPSHOT", the suffix added will be "_v5_6"
        String auxVersion = version.replace(".", DBNAME_SEPARATOR).replace("-", DBNAME_SEPARATOR);
        String[] split = auxVersion.split(DBNAME_SEPARATOR);
        String dbName = "cellbase" + DBNAME_SEPARATOR + species.toLowerCase() + DBNAME_SEPARATOR + cleanAssembly.toLowerCase()
                + DBNAME_SEPARATOR + split[0];
        if (split.length > 1) {
            dbName += (DBNAME_SEPARATOR + split[1]);
        }
        return dbName;
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
            DatastoreStatus datastoreStatus = new DatastoreStatus();
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
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        // TODO: check and get the default data release
        int dataRelease = 0;
        ReleaseMongoDBAdaptor releaseMongoDBAdaptor = new ReleaseMongoDBAdaptor(mongoDatastore);
//        GeneMongoDBAdaptor geneMongoDBAdaptor = new GeneMongoDBAdaptor(mongoDatastore);
        try {
            CellBaseDataResult<DataRelease> releases = releaseMongoDBAdaptor.getAll();
            // Query must return at least one data release. Otherwise there's a problem
            if (releases.getNumResults() >= 1) {
                return releases.getTime() + "ms";
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

    public void close() {
        mongoDataStoreManager.close();
    }

}
