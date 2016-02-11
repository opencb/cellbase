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

package org.opencb.cellbase.mongodb.db;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.core.db.api.CpGIslandDBAdaptor;
import org.opencb.cellbase.core.db.api.CytobandDBAdaptor;
import org.opencb.cellbase.core.db.api.core.*;
import org.opencb.cellbase.core.db.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.db.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.core.db.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.*;
import org.opencb.cellbase.mongodb.db.core.*;
import org.opencb.cellbase.mongodb.db.regulatory.RegulatoryRegionMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.regulatory.TfbsMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.systems.PathwayMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.systems.ProteinProteinInteractionMongoDBAdaptor;
import org.opencb.cellbase.mongodb.db.variation.*;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;

import java.util.ArrayList;
import java.util.List;

public class MongoDBAdaptorFactory extends DBAdaptorFactory {

    /**
     * MongoDataStoreManager acts as singleton by keeping a reference to all databases connections created.
     */
    private MongoDataStoreManager mongoDataStoreManager;
//    private static Map<String, MongoDataStore> mongoDatastoreFactory;

    public MongoDBAdaptorFactory(CellBaseConfiguration cellBaseConfiguration) {
        super(cellBaseConfiguration);

        init();
    }

    private void init() {
        if (mongoDataStoreManager == null) {
            String[] hosts = cellBaseConfiguration.getDatabase().getHost().split(",");
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
        // We need to look for the species object in the configuration
        CellBaseConfiguration.SpeciesProperties.Species speciesObject = getSpecies(species);
        if (speciesObject != null) {
            species = speciesObject.getId();
            assembly = getAssembly(speciesObject, assembly).toLowerCase();

            if (species != null && !species.isEmpty() && assembly != null && !assembly.isEmpty()) {

                // Database name is built following the above pattern
                String database = "cellbase" + "_" + species + "_" + assembly.replaceAll("\\.", "").replaceAll("-", "")
                        .replaceAll("_", "") + "_" + cellBaseConfiguration.getVersion();
                logger.debug("Database for the species is '{}'", database);

                MongoDBConfiguration mongoDBConfiguration;
                MongoDBConfiguration.Builder builder = MongoDBConfiguration.builder().init();
                if (cellBaseConfiguration.getDatabase().getOptions().get("readPreference") != null
                        && !cellBaseConfiguration.getDatabase().getOptions().get("readPreference").isEmpty()) {
                    builder = builder.add("readPreference", cellBaseConfiguration.getDatabase().getOptions().get("readPreference"));
                }

                if (cellBaseConfiguration.getDatabase().getOptions().get("replicaSet") != null
                        && !cellBaseConfiguration.getDatabase().getOptions().get("replicaSet").isEmpty()) {
                    builder = builder.add("replicaSet", cellBaseConfiguration.getDatabase().getOptions().get("replicaSet"));
                }

                // For authenticated databases
                if (!cellBaseConfiguration.getDatabase().getUser().isEmpty()
                        && !cellBaseConfiguration.getDatabase().getPassword().isEmpty()) {
                    // MongoDB could authenticate against different databases
                    if (cellBaseConfiguration.getDatabase().getOptions().containsKey("authenticationDatabase")) {
                        builder = builder
                                .add("username", cellBaseConfiguration.getDatabase().getUser())
                                .add("password", cellBaseConfiguration.getDatabase().getPassword())
                                .add("authenticationDatabase", cellBaseConfiguration.getDatabase().getOptions()
                                        .get("authenticationDatabase"));
                    } else {
                        builder = builder
                                .add("username", cellBaseConfiguration.getDatabase().getUser())
                                .add("password", cellBaseConfiguration.getDatabase().getPassword());
                    }
                }

                mongoDBConfiguration = builder.build();
//                else {
//                    mongoDBConfiguration = MongoDBConfiguration.builder().init().build();
//                }

                // A MongoDataStore to this host and database is returned
                MongoDataStore mongoDatastore = mongoDataStoreManager.get(database, mongoDBConfiguration);

                // we return the MongoDataStore object
                return mongoDatastore;
            } else {
                logger.error("Species name or assembly are not valid, species '{}', assembly '{}'", species, assembly);
                return null;
            }
        } else {
            logger.error("Species name is not valid: '{}'", species);
            return null;
        }
    }


    @Override
    public void setConfiguration(CellBaseConfiguration cellBaseConfiguration) {
        if (cellBaseConfiguration != null) {
            this.cellBaseConfiguration = cellBaseConfiguration;
        }
    }

    @Override
    public void open(String species, String assembly) {

    }

    @Override
    public void close() {

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
    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species) {
        return getConservedRegionDBAdaptor(species, null);
    }

    @Override
    public ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ConservationMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        GeneMongoDBAdaptor geneMongoDBAdaptor = new GeneMongoDBAdaptor(species, assembly, mongoDatastore);
        geneMongoDBAdaptor.setClinicalDBAdaptor(getClinicalDBAdaptor(species, assembly));
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
    @Deprecated
    public ExonDBAdaptor getExonDBAdaptor(String species) {
        return getExonDBAdaptor(species, null);
    }

    @Override
    @Deprecated
    public ExonDBAdaptor getExonDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new ExonMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefsDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new XRefsMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public VariationDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    @Override
    public VariationDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new VariationMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species) {
        return getVariantFunctionalScoreDBAdaptor(species, null);
    }

    @Override
    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new VariantFunctionalScoreMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
        return getVariantAnnotationDBAdaptor(species, null);
    }

    @Override
    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = new VariantAnnotationMongoDBAdaptor(species, assembly,
                mongoDatastore);
        variantAnnotationDBAdaptor.setGeneDBAdaptor(getGeneDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setRegulatoryRegionDBAdaptor(getRegulatoryRegionDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setVariationDBAdaptor(getVariationDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setVariantClinicalDBAdaptor(getClinicalDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setProteinDBAdaptor(getProteinDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setConservedRegionDBAdaptor(getConservedRegionDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setVariantFunctionalScoreDBAdaptor(getVariantFunctionalScoreDBAdaptor(species, assembly));
        variantAnnotationDBAdaptor.setGenomeDBAdaptor(getGenomeDBAdaptor(species, assembly));

        return variantAnnotationDBAdaptor;
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
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species) {
        return getRegulatoryRegionDBAdaptor(species, null);
    }

    @Override
    public RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new RegulatoryRegionMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
        return getTfbsDBAdaptor(species, null);
    }

    @Override
    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new TfbsMongoDBAdaptor(species, assembly, mongoDatastore);
    }


    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species) {
        return getPathwayDBAdaptor(species, null);
    }

    @Override
    public PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return new PathwayMongoDBAdaptor(species, assembly, mongoDatastore);
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


    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species) {
        return getMutationDBAdaptor(species, null);
    }

    @Deprecated
    @Override
    public MutationDBAdaptor getMutationDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return (MutationDBAdaptor) new MutationMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public ClinVarDBAdaptor getClinVarDBAdaptor(String species) {
        return getClinVarDBAdaptor(species, null);
    }

    @Override
    public ClinVarDBAdaptor getClinVarDBAdaptor(String species, String assembly) {
        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
        return (ClinVarDBAdaptor) new ClinVarMongoDBAdaptor(species, assembly, mongoDatastore);
    }

    @Override
    public CytobandDBAdaptor getCytobandDBAdaptor(String species, String assembly) {
        // TODO Auto-generated method stub
        return null;
    }
}
