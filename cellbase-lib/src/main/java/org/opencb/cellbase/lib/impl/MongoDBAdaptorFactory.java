/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.core.monitor.HealthStatus;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDBAdaptorFactory extends DBAdaptorFactory {

    private static final String CELLBASE_DB_MONGODB_REPLICASET = "CELLBASE.DB.MONGODB.REPLICASET";
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String MEMBERS = "members";
    private static final String SET = "set";
    private static final String STATE_STR = "stateStr";
    private static final String NAME = "name";
    /**
     * MongoDataStoreManager acts as singleton by keeping a reference to all databases connections created.
     */
    private MongoDataStoreManager mongoDataStoreManager;
    private static Map<String, MongoDataStoreManager> memberDataStoreManagerMap = new HashMap<>();
//    private static Map<String, MongoDataStore> mongoDatastoreFactory;

    public MongoDBAdaptorFactory(CellBaseConfiguration cellBaseConfiguration) {
        super(cellBaseConfiguration);

        init();
    }

    private void init() {
        if (mongoDataStoreManager == null) {
//            String[] hosts = cellBaseConfiguration.getDatabases().get("mongodb").getHost().split(",");
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

//        logger = LoggerFactory.getLogger(this.getClass());
    }

    private MongoDataStore createMongoDBDatastore(String species, String assembly) {
        /**
         Database name has the following pattern in lower case and with no '.' in the name:
         cellbase_speciesId_assembly_cellbaseVersion
         Example:
         cellbase_hsapiens_grch37_v3
         **/

        DatabaseCredentials mongodbCredentials = cellBaseConfiguration.getDatabases().getMongodb();

        // We need to look for the species object in the configuration
        Species speciesObject = getSpecies(species);
        if (speciesObject != null) {
            species = speciesObject.getId();
            String cellbaseAssembly = getAssembly(speciesObject, assembly);

            if (species != null && !species.isEmpty() && cellbaseAssembly != null && !cellbaseAssembly.isEmpty()) {
                cellbaseAssembly = cellbaseAssembly.toLowerCase();
                // Database name is built following the above pattern
                String database = "cellbase" + "_" + species + "_" + cellbaseAssembly.replaceAll("\\.", "").replaceAll("-", "")
                        .replaceAll("_", "") + "_" + cellBaseConfiguration.getVersion();
                logger.debug("Database for the species is '{}'", database);

                MongoDBConfiguration mongoDBConfiguration;
                MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder();

                // For authenticated databases
                if (!mongodbCredentials.getUser().isEmpty() && !mongodbCredentials.getPassword().isEmpty()) {
                    // MongoDB could authenticate against different databases
                    builder.setUserPassword(mongodbCredentials.getUser(), mongodbCredentials.getPassword());
                    if (mongodbCredentials.getOptions().containsKey(MongoDBConfiguration.AUTHENTICATION_DATABASE)) {
                        builder.setAuthenticationDatabase(mongodbCredentials.getOptions()
                                .get(MongoDBConfiguration.AUTHENTICATION_DATABASE));
                    }
                }

                if (mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE) != null
                        && !mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE).isEmpty()) {
                    builder.add(MongoDBConfiguration.READ_PREFERENCE,
                            mongodbCredentials.getOptions().get(MongoDBConfiguration.READ_PREFERENCE));
                }

                String replicaSet = mongodbCredentials.getOptions().get(MongoDBConfiguration.REPLICA_SET);
                if (replicaSet != null && !replicaSet.isEmpty() && !replicaSet.contains(CELLBASE_DB_MONGODB_REPLICASET)) {
                    builder.setReplicaSet(mongodbCredentials.getOptions().get(MongoDBConfiguration.REPLICA_SET));
                }

                String connectionsPerHost = mongodbCredentials.getOptions().get(MongoDBConfiguration.CONNECTIONS_PER_HOST);
                if (connectionsPerHost != null && !connectionsPerHost.isEmpty()) {
                    builder.setConnectionsPerHost(Integer.valueOf(mongodbCredentials.getOptions()
                            .get(MongoDBConfiguration.CONNECTIONS_PER_HOST)));
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
//                } else {
//                    mongoDBConfiguration = MongoDBConfiguration.builder().init().build();
//                }

                // A MongoDataStore to this host and database is returned
                MongoDataStore mongoDatastore = mongoDataStoreManager.get(database, mongoDBConfiguration);

                // we return the MongoDataStore object
                return mongoDatastore;
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


    @Override
    public void open(String species, String assembly) {

    }

    @Override
    public void close() {

    }

    @Override
    public Map<String, HealthStatus.DependenciesStatus.DatastoreDependenciesStatus.DatastoreStatus> getDatabaseStatus(
            String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        Document statusDocument = mongoDatastore.getReplSetStatus();

        for (Map memberStatus : (List<Map>) statusDocument.get(MEMBERS)) {
            HealthStatus.DependenciesStatus.DatastoreDependenciesStatus.DatastoreStatus datastoreStatus
                    = new HealthStatus.DependenciesStatus.DatastoreDependenciesStatus.DatastoreStatus();
            datastoreStatus.setRepset((String) statusDocument.get(SET));
            datastoreStatus.setRole(((String) memberStatus.get(STATE_STR)).toLowerCase());
            datastoreStatus.setResponseTime(getResponseTime((String) memberStatus.get(NAME)));

        }
    }

    private String getResponseTime(String memberName) {
        MongoDataStoreManager dataStoreManager = getMemberDataStoreManager(memberName);
        MongoDatastore mongoDatastore = dataStoreManager.get(database, mongoDBConfiguration);

        return mongoDatastore.getResponseTime();
    }

    private MongoDataStoreManager getMemberDataStoreManager(String memberName) {

        if (!memberDataStoreManagerMap.containsKey(memberName)) {
            CREATE NEW DATASTOREMANAGER
            memberDataStoreManagerMap.put(memberName, dataStoreManager);
        }
        return memberDataStoreManagerMap.get(memberName);
    }


    @Override
    public GenomeDBAdaptor getGenomeDBAdaptor(String species) {
        return getGenomeDBAdaptor(species, null);
    }

    @Override
    public GenomeDBAdaptor getGenomeDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new GenomeMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public CellBaseDBAdaptor<Document> getMetaDBAdaptor(String species) {
        return getMetaDBAdaptor(species, null);
    }

    @Override
    public CellBaseDBAdaptor<Document> getMetaDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new MetaMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        GeneMongoDBAdaptor geneMongoDBAdaptor = new GeneMongoDBAdaptor(species, assembly, mongoDatastore);
//        geneMongoDBAdaptor.setClinicalDBAdaptor(getClinicalLegacyDBAdaptor(species, assembly));
        return geneMongoDBAdaptor;
    }


    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new TranscriptMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public ConservationDBAdaptor getConservationDBAdaptor(String species) {
        return getConservationDBAdaptor(species, null);
    }

    @Override
    public ConservationDBAdaptor getConservationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ConservationMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public XRefDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new XRefMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public VariantDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    @Override
    public VariantDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new VariantMongoDBAdaptor(species, assembly, mongoDatastore);
    }

//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
//        return getVariantAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new VariantAnnotationCalculator(species, assembly, mongoDatastore, this);
//    }


//    @Override
//    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species) {
//        return getVariantFunctionalScoreDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new VariantFunctionalScoreMongoDBAdaptor(species, assembly, mongoDatastore);
//    }


    @Override
    public ClinicalDBAdaptor getClinicalLegacyDBAdaptor(String species) {
        return getClinicalLegacyDBAdaptor(species, null);
    }

    @Override
    public ClinicalDBAdaptor getClinicalLegacyDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ClinicalLegacyMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species) {
        return getClinicalDBAdaptor(species, null);
    }

    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ClinicalMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public RepeatsDBAdaptor getRepeatsDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new RepeatsMongoDBAdaptor(species, assembly, mongoDatastore);
    }


//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
//        return getVariantAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = new VariantAnnotationMongoDBAdaptor(species, assembly,
//                mongoDatastore);
//        variantAnnotationDBAdaptor.setGeneDBAdaptor(getGeneDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setRegulationDBAdaptor(getRegulatoryRegionDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantDBAdaptor(getVariationDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantClinicalDBAdaptor(getClinicalLegacyDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setProteinDBAdaptor(getProteinDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setConservationDBAdaptor(getConservedRegionDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantFunctionalScoreDBAdaptor(getVariantFunctionalScoreDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setGenomeDBAdaptor(getGenomeDBAdaptor(species, assembly));
//
//        return variantAnnotationDBAdaptor;
//    }
//
//


    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ProteinMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species) {
        return getProteinProteinInteractionDBAdaptor(species, null);
    }

    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ProteinProteinInteractionMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public RegulationDBAdaptor getRegulationDBAdaptor(String species) {
        return getRegulationDBAdaptor(species, null);
    }

    @Override
    public RegulationDBAdaptor getRegulationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new RegulationMongoDBAdaptor(species, assembly, mongoDatastore);
    }
//
//    @Override
//    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
//        return getTfbsDBAdaptor(species, null);
//    }
//
//    @Override
//    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new TfbsMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public PathwayDBAdaptor getPathwayDBAdaptor(String species) {
//        return getPathwayDBAdaptor(species, null);
//    }
//
//    @Override
//    public PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new PathwayMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species) {
//        return getVariationPhenotypeAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return (VariationPhenotypeAnnotationDBAdaptor) new VariationPhenotypeAnnotationMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String assembly) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public MirnaDBAdaptor getMirnaDBAdaptor(String species) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public MirnaDBAdaptor getMirnaDBAdaptor(String species, String assembly) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
}
