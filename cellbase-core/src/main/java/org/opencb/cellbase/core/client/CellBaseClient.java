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

package org.opencb.cellbase.core.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.glassfish.jersey.client.ClientConfig;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.MeasureTraitType;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variation.*;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jacobo on 17/11/14.
 */
public class CellBaseClient {

    private String species;
    private String version;
    private UriBuilder uriBuilder;
    private Client client;
    private Map<String, ObjectReader> readers = new HashMap<>();
    private ObjectMapper mapper;
    private URI lastQuery = null;

    protected static Logger logger = LoggerFactory.getLogger(CellBaseClient.class);

    public enum Category {
        genomic, feature, regulatory, network
    }

    public enum SubCategory {
        //genomic
        region, variant, position, chromosome,
        //feature
        clinvar, exon, gene, id, karyotype, mutation, protein, snp, transcript, xref,
        //regulatory
        tfbs, mirna,
        //network
        pathway
    }

    public enum Resource {
        all, list, info, help,

        //genomic/region
        gene, transcript, exon, snp, mutation, structuralVariation, sequence, tfbs, mimaTarget, cpgIsland,
        conserved_region, regulatory, reverse,
        //genomic/variant
        effect, consequenceType, phenotype, snp_phenotype, mutation_phenotype, annotation,
        //genomic/position
        //gene, snp, transcript, consequence_type, functional
        //genomic/chromosome
            /*all, list, info, */ size, cytoband,

        //feature/clinvar
            /*info, */ /*TODO: listAcc??*/
        //feature/exon
        bysnp, aminos, /*sequence, */region, /*transcript,*/
        //feature/gene
            /*list, info, transcript, snp, mutation, tfbs, mima_target, */ protein,
        //feature/id,
        xref, /*gene, snp, */ starts_with,
        //feature/karyotype
        chromosome,
        //feature/mutation
        diseases,
        //feature/snp
            /*info, consequence_type, regulatory, xref, phenotype,*/ population_frequency,
        //feature/transcript
            /*info, all, gene, exon, sequence, mutation, protein_feature*/
        //feature/protein
            /*info, all, xref*/ feature, interaction,
        //feature/exon
            /*info, sequence, transcript*/
        //regulatory
        //network
    }

    //StringMap. Only needed if the enum.name() doesn't match with the rest
    private static final Map<Category, String> CATEGORY_STRING_MAP;
    private static final Map<SubCategory, String> SUB_CATEGORY_STRING_MAP;
    private static final Map<SubCategory, Class<?>> SUB_CATEGORY_BEAN_MAP;

    private static final Map<Resource, Class<?>> RESOURCE_BEAN_MAP;
    private static final Map<Resource, String> RESOURCE_STRING_MAP;
    private static final int DEFAULT_PORT = 80;

    static {
        CATEGORY_STRING_MAP = new HashMap<>();
        SUB_CATEGORY_STRING_MAP = new HashMap<>();
        RESOURCE_STRING_MAP = new HashMap<>();
        RESOURCE_STRING_MAP.put(Resource.structuralVariation, "structural_variation");
        RESOURCE_STRING_MAP.put(Resource.cpgIsland, "cpg_island");
        RESOURCE_STRING_MAP.put(Resource.mimaTarget, "mima_target");
        RESOURCE_STRING_MAP.put(Resource.consequenceType, "consequence_type");
        RESOURCE_STRING_MAP.put(Resource.annotation, "annotation");


        SUB_CATEGORY_BEAN_MAP = new HashMap<>();
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.protein, Entry.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.chromosome, InfoStats.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.exon, Exon.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.transcript, Transcript.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.gene, Gene.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.clinvar, MeasureTraitType.ClinVarAccession.class);
        SUB_CATEGORY_BEAN_MAP.put(SubCategory.mutation, Mutation.class);


        RESOURCE_BEAN_MAP = new HashMap<>();
        //genomic/region
        RESOURCE_BEAN_MAP.put(Resource.gene, Gene.class);
        RESOURCE_BEAN_MAP.put(Resource.transcript, Transcript.class);
        RESOURCE_BEAN_MAP.put(Resource.exon, Exon.class);
        RESOURCE_BEAN_MAP.put(Resource.snp, Variation.class);
        RESOURCE_BEAN_MAP.put(Resource.mutation, Mutation.class);
        RESOURCE_BEAN_MAP.put(Resource.structuralVariation, StructuralVariation.class);
        RESOURCE_BEAN_MAP.put(Resource.sequence, GenomeSequenceFeature.class);
        RESOURCE_BEAN_MAP.put(Resource.tfbs, TranscriptTfbs.class);
//        RESOURCE_BEAN_MAP.put(Resource.mima_target, .class);
        RESOURCE_BEAN_MAP.put(Resource.cpgIsland, CpGIsland.class);
        RESOURCE_BEAN_MAP.put(Resource.conserved_region, GenomicScoreRegion.class);
        RESOURCE_BEAN_MAP.put(Resource.regulatory, RegulatoryFeature.class);
        //genomic/variant
        RESOURCE_BEAN_MAP.put(Resource.effect, GenomicVariantEffect.class);
        RESOURCE_BEAN_MAP.put(Resource.consequenceType, ConsequenceType.class);
        RESOURCE_BEAN_MAP.put(Resource.phenotype, Phenotype.class);
        RESOURCE_BEAN_MAP.put(Resource.snp_phenotype, String.class);  //TODO
        RESOURCE_BEAN_MAP.put(Resource.mutation_phenotype, Mutation.class);
        RESOURCE_BEAN_MAP.put(Resource.annotation, VariantAnnotation.class);  //TODO
        //genomic/chromosome
        RESOURCE_BEAN_MAP.put(Resource.size, Chromosome.class);
        RESOURCE_BEAN_MAP.put(Resource.cytoband, Cytoband.class);

        //feature/clinvar
        //feature/exon
        RESOURCE_BEAN_MAP.put(Resource.bysnp, Exon.class);    //FIXME Won't work. CellBase returns "List<List<List<Exon>>>"
        RESOURCE_BEAN_MAP.put(Resource.aminos, String.class);
        RESOURCE_BEAN_MAP.put(Resource.region, Region.class);
        //feature/id
        RESOURCE_BEAN_MAP.put(Resource.xref, Xref.class);
        RESOURCE_BEAN_MAP.put(Resource.starts_with, Xref.class);
        //feature/karyotype //Deprecated
        //feature/mutation
//        RESOURCE_BEAN_MAP.put(Resource.diseases, .class); //Without Id
        //feature/gene


    }

    public CellBaseClient(String url, String version, String species) throws URISyntaxException {
        String hostAndPort =  url.split("/", 2)[0];
        String path = "/" + (url.endsWith("/") ? url.split("/", 2)[1] : url.split("/", 2)[1] + "/");
        String host = null;
        int port = DEFAULT_PORT;
        if (hostAndPort.contains(":")) {
            String[] parts = hostAndPort.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            host = hostAndPort;
        }
        logger.info("Remote point access details:");
        logger.info("   host: {}", host);
        logger.info("   port: {}", port);
        logger.info("   path: {}", path);

        init(UriBuilder.fromUri(new URI("http", null, host, port, path, null, null)), version, species);
    }

    public CellBaseClient(String host, int port, String path, String version, String species) throws URISyntaxException {
        this(new URI("http", null, host, port, path.endsWith("/") ? path : path + "/", null, null), version, species);
    }

    public CellBaseClient(URI uri, String version, String species) throws URISyntaxException {
        this(UriBuilder.fromUri(uri), version, species);
    }

    public CellBaseClient(UriBuilder uriBuilder, String version, String species) {
        init(uriBuilder, version, species);
    }

    private void init(UriBuilder uriBuilder, String version, String species) {
        this.species = species;
        ClientConfig clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);

        if (version == null) {
            this.version = "latest";
        } else {
            this.version = version;
        }

        this.uriBuilder = uriBuilder.path(version).path(species);

        JsonFactory jsonFactory = new JsonFactory();
        mapper = new ObjectMapper(jsonFactory);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    /////////////////////////
    // Common getters:
    //          all, list, info, help
    /////////////////////////
    public <T> QueryResponse<QueryResult<T>> getAll(Category category, SubCategory subCategory,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, "", Resource.all, queryOptions, (Class<T>) SUB_CATEGORY_BEAN_MAP.get(subCategory));
    }

    public <T> QueryResponse<QueryResult<T>> getList(Category category, SubCategory subCategory,
                                                     QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, "", Resource.list, queryOptions, (Class<T>) SUB_CATEGORY_BEAN_MAP.get(subCategory));
    }

    public <T> QueryResponse<QueryResult<T>> getInfo(Category category, SubCategory subCategory, String id,
                                                     QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, id, Resource.info, queryOptions, (Class<T>) SUB_CATEGORY_BEAN_MAP.get(subCategory));
    }

    public QueryResponse<QueryResult<String>> getHelp(Category category, SubCategory subCategory,
                                                      QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, "", Resource.help, queryOptions, String.class);
    }

    /////////////////////////
    // Generic getters
    //
    /////////////////////////
    public <T> QueryResponse<QueryResult<T>> get(
            Category category, SubCategory subCategory, List<?> ids, Resource resource, QueryOptions queryOptions)
            throws IOException {
        return get(category, subCategory, ids, resource, queryOptions, (Class<T>) RESOURCE_BEAN_MAP.get(resource));
    }

    public QueryResponse<QueryResult<ObjectMap>> getObjectMap(
            Category category, SubCategory subCategory, List<?> ids, Resource resource, QueryOptions queryOptions)
            throws IOException {
        return get(category, subCategory, ids, resource, queryOptions, ObjectMap.class);
    }

    public QueryResponse<QueryResult<ObjectMap>> nativeGet(
            String category, String subCategory, String ids, String resource, QueryOptions queryOptions)
            throws IOException {
        return nativeGet(category, subCategory, ids, resource, queryOptions, ObjectMap.class);
    }

    public <T> QueryResponse<QueryResult<T>> nativeGet(
            String category, String subCategory, String ids, String resource, QueryOptions queryOptions, Class<T> c)
            throws IOException {
        return restGetter(category, subCategory, ids, resource, false, queryOptions, getJsonReader(c));
    }

    /////////////////////////
    // Some specific getters by JavaBean
    //
    /////////////////////////
    public QueryResponse<QueryResult<Gene>> getGene(Category category, SubCategory subCategory, List<Region> ids,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.gene, queryOptions, Gene.class);
    }

    public QueryResponse<QueryResult<Transcript>> getTranscript(Category category, SubCategory subCategory, List<Region> ids,
                                                                QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.transcript, queryOptions, Transcript.class);
    }

    public QueryResponse<QueryResult<Exon>> getExon(Category category, SubCategory subCategory, List<Region> ids,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.exon, queryOptions, (Exon.class));
    }

    public QueryResponse<QueryResult<Variation>> getSnp(Category category, SubCategory subCategory, List<Region> ids,
                                                        QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.snp, queryOptions, (Variation.class));
    }

    public QueryResponse<QueryResult<Mutation>> getMutation(Category category, SubCategory subCategory, List<Region> ids,
                                                            QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.mutation, queryOptions, (Mutation.class));
    }

//    public QueryResponse<QueryResult<Clinvar>> getClinvar(Category category, SubCategory subCategory, List<Region> ids,
//                                                    QueryOptions queryOptions) throws IOException {
//        return get(category, subCategory, ids, "clinvar", queryOptions, (Mutation.class));
//    }

    public QueryResponse<QueryResult<Phenotype>> getPhenotype(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.phenotype, queryOptions, (Phenotype.class));
    }

    public QueryResponse<QueryResult<StructuralVariation>> getStructuralVariation(Category category, SubCategory subCategory,
                                                                                  List<Region> ids, QueryOptions queryOptions)
            throws IOException {
        return get(category, subCategory, ids, Resource.structuralVariation, queryOptions, (StructuralVariation.class));
    }

    public QueryResponse<QueryResult<Cytoband>> getCytoband(Category category, SubCategory subCategory, List<String> ids,
                                                            QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.cytoband, queryOptions, (Cytoband.class));
    }

    public QueryResponse<QueryResult<GenomeSequenceFeature>> getSequence(Category category, SubCategory subCategory, List<Region> ids,
                                                                         QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.sequence, queryOptions, (GenomeSequenceFeature.class));
    }

    public QueryResponse<QueryResult<GenomeSequenceFeature>> getReverseSequence(Category category, SubCategory subCategory,
                                                                                List<Region> ids,
                                                                                QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.reverse, queryOptions, (GenomeSequenceFeature.class));
    }

    public QueryResponse<QueryResult<TranscriptTfbs>> getTfbs(Category category, SubCategory subCategory, List<Region> ids,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.tfbs, queryOptions, (TranscriptTfbs.class));
    }

    public QueryResponse<QueryResult<RegulatoryFeature>> getRegulatoryRegion(Category category, SubCategory subCategory, List<Region> ids,
                                                                            QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.regulatory, queryOptions, (RegulatoryFeature.class));
    }

    public QueryResponse<QueryResult<CpGIsland>> getCpgIsland(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.cpgIsland, queryOptions, (CpGIsland.class));
    }

    public QueryResponse<QueryResult<GenomicScoreRegion>> getConservedRegion(Category category, SubCategory subCategory, List<Region> ids,
                                                                          QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.conserved_region, queryOptions, (GenomicScoreRegion.class));
    }

//    public QueryResponse<QueryResult<Peptide>> getPeptide(Category category, SubCategory subCategory, List<Region> ids,
//                                                              QueryOptions queryOptions) throws IOException {
//        return get(category, subCategory, ids, "peptide", queryOptions, (.class));
//    }

    public QueryResponse<QueryResult<GenomicVariantEffect>> getEffect(Category category, SubCategory subCategory, List<Variant> ids,
                                                                      QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.effect, queryOptions, (GenomicVariantEffect.class));
    }

    public QueryResponse<QueryResult<ConsequenceType>> getConsequenceType(Category category, SubCategory subCategory, List<Variant> ids,
                                                                          QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.consequenceType, queryOptions, (ConsequenceType.class));
    }

    public QueryResponse<QueryResult<VariantAnnotation>> getAnnotation(Category category, SubCategory subCategory, List<Variant> ids,
                                                                       QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.annotation, queryOptions, (VariantAnnotation.class));
    }

    public QueryResponse<QueryResult<Phenotype>> getPhenotype(Category category, SubCategory subCategory, String phenotype,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, phenotype, Resource.phenotype, queryOptions, (Phenotype.class));
    }

    public QueryResponse<QueryResult<Xref>> getXref(Category category, SubCategory subCategory, List<String> ids,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.xref, queryOptions, (Xref.class));
    }


    /////////////////////////
    // Private getters
    //
    /////////////////////////

    private <T> QueryResponse<QueryResult<T>> get(
            Category category, SubCategory subCategory, List<?> ids, Resource resource, QueryOptions queryOptions, Class<T> c)
            throws IOException {
        String idsCvs = null;
        if (ids != null && !ids.isEmpty()) {
            Iterator<?> iterator = ids.iterator();
            idsCvs = iterator.next().toString();
            while (iterator.hasNext()) {
                idsCvs += "," + iterator.next().toString();
            }
        }
        return get(category, subCategory, idsCvs, resource, queryOptions, c);
    }

    private <T> QueryResponse<QueryResult<T>> get(Category category, SubCategory subCategory, String idsCvs, Resource resource,
                                                  QueryOptions queryOptions, Class<T> c)
            throws IOException {

        if (queryOptions == null) {
            queryOptions = new QueryOptions();
        }
        ObjectReader responseReader;
        switch (queryOptions.getString("of", "NONE")) {
            case "NONE":
                queryOptions.put("of", "json");
            case "json":
                responseReader = getJsonReader(c);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported dataType");
        }

        boolean post = queryOptions.getBoolean("post", false);

        String categoryStr = CATEGORY_STRING_MAP.containsKey(category) ? CATEGORY_STRING_MAP.get(category) : category.name();
        String subCategoryStr = SUB_CATEGORY_STRING_MAP.containsKey(subCategory)
                ? SUB_CATEGORY_STRING_MAP.get(subCategory) : subCategory.name();
        String resourceStr = RESOURCE_STRING_MAP.containsKey(resource) ? RESOURCE_STRING_MAP.get(resource) : resource.name();

        return restGetter(categoryStr, subCategoryStr, idsCvs, resourceStr, post, queryOptions, responseReader);
    }

    private <T> QueryResponse<QueryResult<T>> restGetter(
            String categoryStr, String subCategoryStr, String idsCvs, String resourceStr, boolean post, QueryOptions queryOptions,
            ObjectReader responseReader) throws IOException {

        UriBuilder clone = uriBuilder.clone()
                .path(categoryStr)
                .path(subCategoryStr);
        if (idsCvs != null && !idsCvs.isEmpty() && !post) {
            clone = clone.path(idsCvs);
        }

        clone = clone.path(resourceStr);
        for (Map.Entry<String, Object> entry : queryOptions.entrySet()) {
            if (!entry.getKey().equals("post")) {
                clone.queryParam(entry.getKey(), entry.getValue());
            }
        }

        lastQuery = clone.build();
//        System.out.println(clone.build().toString());
        Invocation.Builder request = client.target(clone).request();
        Response response;
        if (post) {
            response = request.post(Entity.text(idsCvs));
        } else {
            response = request.get();
        }
        String responseStr = response.readEntity(String.class);

        try {
            return responseReader.readValue(responseStr);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing response to : {}", lastQuery);
            throw e;
        }
    }

    private ObjectReader getJsonReader(Class<?> c) {
        if (c == null) {
            c = ObjectMap.class;
        }
        if (!readers.containsKey(c.getName())) {
            readers.put(c.getName(), mapper.reader(mapper.getTypeFactory().constructParametricType(
                    QueryResponse.class, mapper.getTypeFactory().constructParametricType(QueryResult.class, c))));
        }
        return readers.get(c.getName());
    }

    public URI getLastQuery() {
        return lastQuery;
    }


}
