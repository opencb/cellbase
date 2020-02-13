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

package org.opencb.cellbase.lib.loader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonSerializationException;
import org.bson.Document;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.cellbase.core.api.core.CellBaseDBAdaptor;
import org.opencb.cellbase.core.api.core.DBAdaptorFactory;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DatabaseCredentials;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
@Deprecated
public class MongoDBCellBaseLoader extends CellBaseLoader {

    private static final String CLINICAL_VARIANTS_COLLECTION = "clinical_variants";
    private static final String GENOMIC_FEATURES = "genomicFeatures";
    private static final String XREFS = "xrefs";
    private static final String TRAIT_ASSOCIATION = "traitAssociation";
    private static final String SOMATIC_INFORMATION = "somaticInformation";
    private static final String PRIMARY_SITE = "primarySite";
    private static final String SITE_SUBTYPE = "siteSubtype";
    private static final String PRIMARY_HISTOLOGY = "primaryHistology";
    private static final String HISTOLOGY_SUBTYPE = "histologySubtype";
    private static final String TUMOUR_ORIGIN = "tumourOrigin";
    private static final String SAMPLE_SOURCE = "sampleSource";
    private static final String HERITABLE_TRAITS = "heritableTraits";
    private static final String TRAIT = "trait";
    private static final String PRIVATE_FEATURE_XREF_FIELD = "_featureXrefs";
    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final Set<String> SKIP_WORKDS = new HashSet<>(Arrays.asList("or", "and", "the", "of", "at",
            "in", "on"));
    private MongoDataStoreManager mongoDataStoreManager;
    private MongoDataStore mongoDataStore;
    private MongoDBCollection mongoDBCollection;

    private DBAdaptorFactory dbAdaptorFactory;
    @Deprecated
    private CellBaseDBAdaptor dbAdaptor;

    private Path indexScriptFolder;
    private int[] chunkSizes;

    @Deprecated
    private String clinicalVariantSource;

    @Deprecated
    private static final String CLINVARVARIANTSOURCE = "clinvar";
    @Deprecated
    private static final String COSMICVARIANTSOURCE = "cosmic";
    @Deprecated
    private static final String GWASVARIANTSOURCE = "gwas";
    private String collectionName;

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database) {
        this(queue, data, database, null, null, null);
    }

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, String database, String field,
                                 String[] innerFields, CellBaseConfiguration cellBaseConfiguration) {
        super(queue, data, database, field, innerFields, cellBaseConfiguration);
        if (cellBaseConfiguration.getDatabases().getMongodb().getOptions().get("mongodb-index-folder") != null) {
            indexScriptFolder = Paths.get(cellBaseConfiguration.getDatabases().getMongodb().getOptions().get("mongodb-index-folder"));
        }
    }


    @Override
    public void init() throws LoaderException {
        /*
         * OpenCB 'datastore' project is used to load data into MongoDB. The following code:
         * 1. creates a Manager to connect to a physical server
         * 2. a 'datastore' object connects to a specific database
         * 3. finally a connection to the collection is stored in 'mongoDBCollection'
         */

//        DatabaseProperties mongodbCredentials = cellBaseConfiguration.getDatabases().get("mongodb");
        DatabaseCredentials mongodbCredentials = cellBaseConfiguration.getDatabases().getMongodb();

        String[] hosts = mongodbCredentials.getHost().split(",");
        List<DataStoreServerAddress> dataStoreServerAddressList = new ArrayList<>(hosts.length);
        for (String host : hosts) {
            String[] hostAndPort = host.split(":");
            dataStoreServerAddressList.add(new DataStoreServerAddress(hostAndPort[0], (hostAndPort.length == 2)
                    ? Integer.parseInt(hostAndPort[1]) : 27017));
        }
        mongoDataStoreManager = new MongoDataStoreManager(dataStoreServerAddressList);

        MongoDBConfiguration mongoDBConfiguration;
        if (cellBaseConfiguration != null
                && mongodbCredentials.getOptions().get("authenticationDatabase") != null
                && !mongodbCredentials.getOptions().get("authenticationDatabase").isEmpty()) {
            mongoDBConfiguration = MongoDBConfiguration.builder()
                    .add("username", mongodbCredentials.getUser())
                    .add("password", mongodbCredentials.getPassword())
                    .add("authenticationDatabase", mongodbCredentials.getOptions().get("authenticationDatabase")).build();
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    mongodbCredentials.getOptions().get("authenticationDatabase"));
        } else {
            mongoDBConfiguration = MongoDBConfiguration.builder()
                    .add("username", mongodbCredentials.getUser())
                    .add("password", mongodbCredentials.getPassword()).build();
        }
        logger.debug("MongoDB credentials are user: '{}', password: '{}'",
                mongodbCredentials.getUser(), mongodbCredentials.getPassword());

        mongoDataStore = mongoDataStoreManager.get(database, mongoDBConfiguration);

        collectionName = getCollectionName(data);
        mongoDBCollection = mongoDataStore.getCollection(collectionName);
        logger.debug("Connection to MongoDB datastore '{}' created, collection '{}' is used",
                mongoDataStore.getDatabaseName(), collectionName);

        // Some collections need to add an extra _chunkIds field to speed up some queries
        getChunkSizes();
        logger.debug("Chunk sizes '{}' used for collection '{}'", Arrays.toString(chunkSizes), collectionName);

        dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        // This is not currently used and should no longer be used. To be soon removed
//        dbAdaptor = getDBAdaptor(data);
    }

    @Deprecated
    private CellBaseDBAdaptor getDBAdaptor(String data) throws LoaderException {
        String[] databaseParts = database.split("_");
        String species = databaseParts[1];
//        String assembly = databaseParts[2];
        CellBaseDBAdaptor dbAdaptor;
        switch (data) {
            case "genome_info":
//                dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "genome_sequence":
//                dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "gene":
//                dbAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "variation":
                // Default assembly will be selected - it is a bad idea to get the assembly from the database name since
                // '-', '_', '.' symbols are removed from the assembly before building the database name. This getAdaptor
                // method will soon be remove
                dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species);
                dbAdaptor = null;
                break;
            case "svs":
                // Default assembly will be selected - it is a bad idea to get the assembly from the database name since
                // '-', '_', '.' symbols are removed from the assembly before building the database name. This getAdaptor
                // method will soon be remove
                dbAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species);
                break;
            case "cadd":
////                dbAdaptor = dbAdaptorFactory.getVariantFunctionalScoreDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "regulatory_region":
//                dbAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "protein":
//                dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "protein_protein_interaction":
//                dbAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
//            // TODO: implement an adaptor for protein_functional_prediction - current queries are issued from the
//            // TODO: ProteinDBAdaptors, that's why there isn't one yet
            case "protein_functional_prediction":
                dbAdaptor = null;
//                collectionName = "protein_functional_prediction";
                break;
            case "conservation":
//                dbAdaptor = dbAdaptorFactory.getConservationDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "cosmic":
                clinicalVariantSource = "cosmic";
//                dbAdaptor = dbAdaptorFactory.getClinicalLegacyDBAdaptor(species, assembly);
                dbAdaptor = null;
                break;
            case "clinical_variants":
                dbAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species);
                break;
            case "repeats":
                dbAdaptor = null;
//                collectionName = "protein_functional_prediction";
                break;
            case "metadata":
                dbAdaptor = null;
//                collectionName = "protein_functional_prediction";
                break;
            default:
                throw new LoaderException("Unknown data to load: '" + data + "'");
        }

        return dbAdaptor;
    }

    // TODO: use adaptors within MongoDBCellBaseLoader, avoid using mongoDBCollection and remove this method
    private String getCollectionName(String data) throws LoaderException {
        String collection;
        switch (data) {
            case "genome_info":
                collection = "genome_info";
                break;
            case "genome_sequence":
                collection = "genome_sequence";
                break;
            case "gene":
                collection = "gene";
                break;
            case "variation":
                collection = "variation";
                break;
            case "svs":
                collection = "variation";
                break;
            case "cadd":
                collection = "variation_functional_score";
                break;
            case "regulatory_region":
                collection = "regulatory_region";
                break;
            case "protein":
                collection = "protein";
                break;
            case "protein_protein_interaction":
                collection = "protein_protein_interaction";
                break;
            case "protein_functional_prediction":
                collection = "protein_functional_prediction";
                break;
            case "conservation":
                collection = "conservation";
                break;
            case "cosmic":
                clinicalVariantSource = "cosmic";
                collection = "clinical";
                break;
            case "clinvar":
                clinicalVariantSource = "clinvar";
                collection = "clinical";
                break;
            case "gwas":
                clinicalVariantSource = "gwas";
                collection = "clinical";
                break;
            case "clinical":
                collection = "clinical";
                break;
            case "clinical_variants":
                collection = CLINICAL_VARIANTS_COLLECTION;
                break;
            case "metadata":
                collection = "metadata";
                break;
            case "repeats":
                collection = "repeats";
                break;
            default:
                throw new LoaderException("Unknown data to load: '" + data + "'");
        }

        return collection;
    }

    private void getChunkSizes() {
        if (collectionName != null) {
            switch (collectionName) {
                case "genome_sequence":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE};
                    break;
                case "gene":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.GENE_CHUNK_SIZE};
                    break;
                case "variation":  // TODO: why are we using different chunk sizes??
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE,
                            10 * MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, };
                    break;
                case "variation_functional_score":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE};
                    break;
                case "regulatory_region":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE};
                    break;
                case "repeats":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.REPEATS_CHUNK_SIZE};
                    break;
                case "conservation":
                    chunkSizes = new int[]{MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE};
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Integer call() {
        if (field != null) {
            return prepareBatchAndUpdate();
        } else {
            return prepareBatchAndLoad();
        }
    }

    private int prepareBatchAndUpdate() {
        int numLoadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = blockingQueue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<Document> dbObjectsBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        Document dbObject = Document.parse(jsonLine);
                        dbObjectsBatch.add(dbObject);
                    }

                    Long numUpdates = (Long) dbAdaptor.update(dbObjectsBatch, field, innerFields).first();
                    numLoadedObjects += numUpdates;
                }
            } catch (InterruptedException e) {
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + numLoadedObjects + " records loaded");
        return numLoadedObjects;
    }

    private int prepareBatchAndLoad() {
        int numLoadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = blockingQueue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<Document> documentBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        Document document = Document.parse(jsonLine);
                        addChunkId(document);
                        addClinicalPrivateFields(document);
//                        addVariationPrivateFields(document);
                        documentBatch.add(document);
                    }
                    numLoadedObjects += load(documentBatch);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + numLoadedObjects + " records loaded");
        return numLoadedObjects;
    }

//    private void addVariationPrivateFields(Document document) {
//        if (data.equals("variation")) {
//            document.put("_id", buildId((String) document.get("chromosome"), (int) document.get("start"),
//                    (String) document.get("reference"), (String) document.get("alternate")));
//        }
//    }

    private void addClinicalPrivateFields(Document document) throws JsonProcessingException, FileFormatException {
        if (collectionName.equals(CLINICAL_VARIANTS_COLLECTION)) {
            Document annotationDocument = (Document) document.get("annotation");
            List<String> featureXrefs = getFeatureXrefsFromClinicalVariants(annotationDocument);
            if (!featureXrefs.isEmpty()) {
                document.put(PRIVATE_FEATURE_XREF_FIELD, featureXrefs);
            }
            List<String> traitList = getTraitFromClinicalVariants(annotationDocument);
            if (!featureXrefs.isEmpty()) {
                document.put(PRIVATE_TRAIT_FIELD, traitList);
            }
        }
//
//
//
//
//        if (clinicalVariantSource != null) {
//            List<String> geneIdList = null;
//            List<String> phenotypeList = null;
//            switch (clinicalVariantSource) {
//                case CLINVARVARIANTSOURCE:
//                    geneIdList = getClinvarGeneIds(document);
//                    phenotypeList = getClinvarPhenotypes(document);
//                    break;
//                case COSMICVARIANTSOURCE:
//                    geneIdList = document.get("geneName") != null ? Collections.singletonList(document.getString("geneName")) : null;
//                    phenotypeList = getCosmicPhenotypes(document);
//                    break;
//                case GWASVARIANTSOURCE:
//                    geneIdList = document.get("reportedGenes") != null
//                            ? Collections.singletonList(document.getString("reportedGenes"))
//                            : null;
//                    phenotypeList = getGwasPhenotypes(document);
//                    break;
//                default:
//                    break;
//            }
//            if (geneIdList != null) {
//                document.put("_geneIds", geneIdList);
//            }
//            if (phenotypeList != null) {
//                document.put("_phenotypes", phenotypeList);
//            }
//        }
    }

    private List<String> getTraitFromClinicalVariants(Document document) throws JsonProcessingException, FileFormatException {
        Set<String> values = new HashSet<>();
        if (document.get(TRAIT_ASSOCIATION) != null) {
            for (Document evidenceEntryDocument : (List<Document>) document.get(TRAIT_ASSOCIATION)) {
                if (evidenceEntryDocument.get(SOMATIC_INFORMATION) != null) {
                    Document somaticInformationDocument = (Document) evidenceEntryDocument.get(SOMATIC_INFORMATION);
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(PRIMARY_SITE))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(PRIMARY_SITE)));
                    }
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(SITE_SUBTYPE))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(SITE_SUBTYPE)));
                    }
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(PRIMARY_HISTOLOGY))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(PRIMARY_HISTOLOGY)));
                    }
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(HISTOLOGY_SUBTYPE))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(HISTOLOGY_SUBTYPE)));
                    }
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(TUMOUR_ORIGIN))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(TUMOUR_ORIGIN)));
                    }
                    if (StringUtils.isNotBlank((String) somaticInformationDocument.get(SAMPLE_SOURCE))) {
                        values.addAll(splitKeywords(somaticInformationDocument.getString(SAMPLE_SOURCE)));
                    }
                }
                if (evidenceEntryDocument.get(HERITABLE_TRAITS) != null) {
                    for (Document traitDocument : (List<Document>) evidenceEntryDocument.get(HERITABLE_TRAITS)) {
                        if (StringUtils.isNotBlank((String) traitDocument.get(TRAIT))) {
                            values.addAll(splitKeywords(traitDocument.getString(TRAIT)));
                        }
                    }
                }
            }
        } else {
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            ObjectWriter jsonObjectWriter = jsonObjectMapper.writer();

            throw new FileFormatException("traitAssociation field missing in input objects. Please, ensure"
                    + " that input file contains variants with appropriate clinical annotation: "
                    + jsonObjectWriter.writeValueAsString(document));
        }

        return new ArrayList<>(values);

    }

    private List<String> splitKeywords(String string) {
        String[] stringArray = string.toLowerCase().split("\\W");
        List<String> stringList = new ArrayList<>(stringArray.length);
        for (String keyword : stringArray) {
            if (!keyword.isEmpty() && !SKIP_WORKDS.contains(keyword)) {
                stringList.add(keyword);
            }
        }

        return stringList;
    }

    private void getValuesFromClinicalObject(List clinicalObjectList, String field, Set<String> values) {
        if (clinicalObjectList != null && !clinicalObjectList.isEmpty()) {
            for (Object object : clinicalObjectList) {
                String value = (String) ((Document) object).get(field);
                if (value != null) {
                    values.add(value);
                }
            }
        }
    }

    private List<String> getFeatureXrefsFromClinicalVariants(Document document) throws JsonProcessingException, FileFormatException {
        Set<String> values = new HashSet<>();
        if (document.containsKey(TRAIT_ASSOCIATION)) {
            List evidenceEntryList = (List) document.get(TRAIT_ASSOCIATION);
            getFeatureXrefsFromClinicalObject(evidenceEntryList, values);
            getFeatureXrefsFromConsequenceTypes((List) document.get("consequenceTypes"), values);
        } else {
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            ObjectWriter jsonObjectWriter = jsonObjectMapper.writer();

            throw new FileFormatException("traitAssociation field missing in input objects. Please, ensure"
                    + " that input file contains variants with appropriate clinical annotation: "
                    + jsonObjectWriter.writeValueAsString(document));
        }

        return new ArrayList<>(values);

    }

    private void getFeatureXrefsFromConsequenceTypes(List consequenceTypeObjectList, Set<String> values) {
        if (consequenceTypeObjectList != null && !consequenceTypeObjectList.isEmpty()) {
            for (Object consequenceTypeObject : consequenceTypeObjectList) {
                String geneName = (String) ((Document) consequenceTypeObject).get("geneName");
                if (geneName != null) {
                    values.add(geneName);
                }
                String ensemblGeneId = (String) ((Document) consequenceTypeObject).get("ensemblGeneId");
                if (geneName != null) {
                    values.add(ensemblGeneId);
                }
                String ensemblTranscriptId = (String) ((Document) consequenceTypeObject).get("ensemblTranscriptId");
                if (geneName != null) {
                    values.add(ensemblTranscriptId);
                }
                Document proteinVariantAnnotationObject
                        = (Document) ((Document) consequenceTypeObject).get("proteinVariantAnnotation");
                if (proteinVariantAnnotationObject != null) {
                    String uniprotAccession = (String) proteinVariantAnnotationObject.get("uniprotAccession");
                    if (uniprotAccession != null) {
                        values.add(uniprotAccession);
                    }
                    String uniprotName = (String) proteinVariantAnnotationObject.get("uniprotName");
                    if (uniprotName != null) {
                        values.add(uniprotName);
                    }
                }
            }
        }
    }

    private void getFeatureXrefsFromClinicalObject(List evidenceEntryList, Set<String> values) {
        if (evidenceEntryList != null && !evidenceEntryList.isEmpty()) {
            for (Object object : evidenceEntryList) {
                List<Document> genomicFeatureList = (List<Document>) ((Document) object).get(GENOMIC_FEATURES);
                if (genomicFeatureList != null) {
                    for (Document genomicFeature : genomicFeatureList) {
                        if (genomicFeature.get(XREFS) != null && !((Document) genomicFeature.get(XREFS)).isEmpty()) {
                            for (String key : ((Document) genomicFeature.get(XREFS)).keySet()) {
                                values.add((String) ((Document) genomicFeature.get(XREFS)).get(key));
                            }
                        }
                    }
                }
            }
        }
    }

    private List<String> getGwasPhenotypes(Document document) {
        List<String> phenotypeList = new ArrayList<>();
        List studiesDBList = document.get("studies", List.class);
        for (Object studyObject : studiesDBList) {
            Document studyDBObject = (Document) studyObject;
            List traitsDBList = studyDBObject.get("traits", List.class);
            if (traitsDBList != null) {
                for (Object traitObject : traitsDBList) {
                    Document traitDBObject = (Document) traitObject;
                    if (traitDBObject.get("diseaseTrait") != null) {
                        phenotypeList.add(traitDBObject.getString("diseaseTrait"));
                    }
                }
            }
        }
        return phenotypeList;
    }

    private List<String> getCosmicPhenotypes(Document document) {
        List<String> phenotypeList = new ArrayList<>(4);
        addIfNotEmpty((String) document.get("primarySite"), phenotypeList);
        addIfNotEmpty((String) document.get("histologySubtype"), phenotypeList);
        addIfNotEmpty((String) document.get("primaryHistology"), phenotypeList);
        addIfNotEmpty((String) document.get("siteSubtype"), phenotypeList);

        return phenotypeList;

    }

    private void addIfNotEmpty(String element, List<String> stringList) {
        if (element != null && !element.isEmpty()) {
            stringList.add(element);
        }
    }

    private List<String> getClinvarPhenotypes(Document dbObject) {
        List<String> phenotypeList = new ArrayList<>();
        List basicDBList = ((Document) ((Document) ((Document) dbObject.get("clinvarSet")).get("referenceClinVarAssertion"))
                .get("traitSet")).get("trait", List.class);
        for (Object object : basicDBList) {
            Document document = (Document) object;
            List nameDBList = document.get("name", List.class);
            if (nameDBList != null) {
                for (Object nameObject : nameDBList) {
                    Document elementValueDBObject = (Document) ((Document) nameObject).get("elementValue");
                    if (elementValueDBObject != null) {
                        String phenotype = (String) elementValueDBObject.get("value");
                        if (phenotype != null) {
                            phenotypeList.add(phenotype);
                        }
                    }
                }

            }
        }
        if (phenotypeList.size() > 0) {
            return phenotypeList;
        } else {
            return null;
        }
    }

    private List<String> getClinvarGeneIds(Document dbObject) {
        List<String> geneIdList = new ArrayList<>();
        List basicDBList = ((Document) ((Document) ((Document) dbObject.get("clinvarSet")).get("referenceClinVarAssertion"))
                .get("measureSet")).get("measure", List.class);
        for (Object object : basicDBList) {
            Document document = (Document) object;
            List measureRelationshipDBList = document.get("measureRelationship", List.class);
            if (measureRelationshipDBList != null) {
                for (Object measureRelationShipObject : measureRelationshipDBList) {
                    List symbolDBList = ((Document) measureRelationShipObject).get("symbol", List.class);
                    if (symbolDBList != null) {
                        for (Object symbolObject : symbolDBList) {
                            Document elementValueDBObject = (Document) ((Document) symbolObject).get("elementValue");
                            if (elementValueDBObject != null) {
                                String geneId = (String) elementValueDBObject.get("value");
                                if (geneId != null) {
                                    geneIdList.add(geneId);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (geneIdList.size() > 0) {
            return geneIdList;
        } else {
            return null;
        }
    }

    @Override
    public void createIndex(String data) throws LoaderException {
        Path indexFilePath = getIndexFilePath(data);
        if (indexFilePath != null) {
            logger.info("Creating indexes...");
            try {
                runCreateIndexProcess(indexFilePath);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("No index found for '{}'", data);
        }
    }


    public int load(List<Document> batch) {
        // End recursive calls
        if (batch.size() > 0) {
            try {
                DataResult result = mongoDBCollection.insert(batch, new QueryOptions());
                return Math.toIntExact(result.getNumInserted());
            } catch (BsonSerializationException e) {
                // End recursive calls
                if (batch.size() == 1) {
                    logger.warn("Found document raising load problems: {}...", batch.get(0).toJson().substring(0, 1000));
                    if (data.equalsIgnoreCase("variation")) {
                        Document annotationDocument = (Document) batch.get(0).get("annotation");
                        if (annotationDocument.get("xrefs") != null && ((List) annotationDocument.get("xrefs")).size() > 3) {
                            logger.warn("Truncating xrefs array");
                            annotationDocument.put("xrefs", ((List) annotationDocument.get("xrefs")).subList(0, 3));
                            return load(batch);
                        } else if (annotationDocument.get("additionalAttributes") != null) {
                            logger.warn("Removing additionalAttributes field");
                            annotationDocument.remove("additionalAttributes");
                            return load(batch);
                        } else {
                            logger.warn("Skipping and continuing with the load");
                            return 0;
                        }
                    } else {
                        logger.warn("Skipping and continuing with the load");
                        return 0;
                    }
                }
                logger.warn("Found problems loading document batch, loading one by one...");
                int nInserted = 0;
                for (Document document : batch) {
                    // TODO: queryOptions?
                    nInserted += load(Collections.singletonList(document));
                }
                return nInserted;
            } catch (MongoBulkWriteException e) {
                for (BulkWriteError bulkWriteError : e.getWriteErrors()) {
                    // Duplicated key due to a batch which was partially inserted before, just skip the variant
                    if (ErrorCategory.fromErrorCode(bulkWriteError.getCode()).equals(ErrorCategory.DUPLICATE_KEY)) {
                        return 0;
                    }
                }
                // It is not a duplicated key error - propagate it
                throw e;
            }
        } else {
            return 0;
        }
    }

    private void addChunkId(Document document) {
        if (chunkSizes != null && chunkSizes.length > 0) {
            List<String> chunkIds = new ArrayList<>();
            for (int chunkSize : chunkSizes) {
                int chunkStart = (Integer) document.get("start") / chunkSize;
                int chunkEnd = (Integer) document.get("end") / chunkSize;
                String chunkIdSuffix = chunkSize / 1000 + "k";
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    if (document.containsKey("chromosome")) {
                        chunkIds.add(document.get("chromosome") + "_" + i + "_" + chunkIdSuffix);
                    } else {
                        chunkIds.add(document.get("sequenceName") + "_" + i + "_" + chunkIdSuffix);
                    }
                }
            }
            logger.debug("Setting chunkIds to {}", chunkIds.toString());
            document.put("_chunkIds", chunkIds);
        }
    }

    @Override
    public void close() {
        mongoDataStoreManager.close(database);
    }

    private Path getIndexFilePath(String data) throws LoaderException {
        if (indexScriptFolder == null || data == null) {
            logger.error("No path can be provided for index, check index folder '{}' and data '{}'",
                    indexScriptFolder, data);
            return null;
        }

        String indexFileName = null;
        switch (data) {
            case "genome_info":
                indexFileName = null;
                break;
            case "genome_sequence":
                indexFileName = "genome_sequence-indexes.js";
                break;
            case "gene":
                indexFileName = "gene-indexes.js";
                break;
            case "variation":
                indexFileName = "variation-indexes.js";
                break;
            case "variation_functional_score":
                indexFileName = "variation_functional_score-indexes.js";
                break;
            case "regulatory_region":
                indexFileName = "regulatory_region-indexes.js";
                break;
            case "protein":
                indexFileName = "protein-indexes.js";
                break;
            case "protein_protein_interaction":
                indexFileName = "protein_protein_interaction-indexes.js";
                break;
            case "protein_functional_prediction":
                indexFileName = "protein_functional_prediction-indexes.js";
                break;
            case "conservation":
                indexFileName = "conservation-indexes.js";
                break;
            case "cosmic":
            case "clinvar":
            case "gwas":
            case "clinical":
                indexFileName = "clinical-indexes.js";
                break;
            case "clinical_variants":
                indexFileName = "clinical-indexes.js";
                break;
            case "repeats":
                indexFileName = "repeat-indexes.js";
                break;
            default:
                break;
        }
        if (indexFileName == null) {
            return null;
        }
        return indexScriptFolder.resolve(indexFileName);
    }


    protected boolean runCreateIndexProcess(Path indexFilePath) throws IOException, InterruptedException {
//        DatabaseProperties mongodbCredentials = cellBaseConfiguration.getDatabases().get("mongodb");
        DatabaseCredentials mongodbCredentials = cellBaseConfiguration.getDatabases().getMongodb();
        List<String> args = new ArrayList<>();
        args.add("mongo");
        args.add("--host");
        args.add(mongodbCredentials.getHost());
        if (mongodbCredentials.getUser() != null && !mongodbCredentials.getUser().equals("")) {
            args.addAll(Arrays.asList(
                    "-u", mongodbCredentials.getUser(),
                    "-p", mongodbCredentials.getPassword()
            ));
        }
        if (cellBaseConfiguration != null && mongodbCredentials.getOptions().get("authenticationDatabase") != null) {
            args.add("--authenticationDatabase");
            args.add(mongodbCredentials.getOptions().get("authenticationDatabase"));
            logger.debug("MongoDB 'authenticationDatabase' database parameter set to '{}'",
                    mongodbCredentials.getOptions().get("authenticationDatabase"));
        }
        args.add(database);
        args.add(indexFilePath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        logger.debug("Executing command: '{}'", StringUtils.join(processBuilder.command(), " "));

//        processBuilder.redirectErrorStream(true);
//        if (logFilePath != null) {
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
//        }

        Process process = processBuilder.start();
        process.waitFor();

        // Check process output
        boolean executedWithoutErrors = true;
        int genomeInfoExitValue = process.exitValue();
        if (genomeInfoExitValue != 0) {
            logger.warn("Error executing {}, error code: {}", indexFilePath, genomeInfoExitValue);
            executedWithoutErrors = false;
        }
        return executedWithoutErrors;
    }

}
