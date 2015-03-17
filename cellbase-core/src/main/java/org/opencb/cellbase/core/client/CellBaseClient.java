package org.opencb.cellbase.core.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.glassfish.jersey.client.ClientConfig;


import org.opencb.biodata.formats.protein.uniprot.v140jaxb.Protein;
import org.opencb.biodata.formats.variant.clinvar.v19jaxb.MeasureTraitType;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.GenomeSequenceFeature;
import org.opencb.cellbase.core.common.core.*;
import org.opencb.cellbase.core.common.core.Xref;
import org.opencb.cellbase.core.common.regulatory.ConservedRegion;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.opencb.cellbase.core.common.regulatory.Tfbs;
import org.opencb.cellbase.core.common.variation.*;
import org.opencb.datastore.core.ObjectMap;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
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

    private final String species;
    private final String version;
    private final UriBuilder uriBuilder;
    private final Client client;
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
            effect, consequenceType, phenotype, snp_phenotype, mutation_phenotype, fullAnnotation, annotation,
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
    private static final Map<Category, String> categoryStringMap;
    private static final Map<SubCategory, String> subCategoryStringMap;
    private static final Map<SubCategory, Class<?>> subCategoryBeanMap;

    private static final Map<Resource, Class<?>> resourceBeanMap;
    private static final Map<Resource, String> resourceStringMap;

    static {
        categoryStringMap = new HashMap<>();
        subCategoryStringMap = new HashMap<>();
        resourceStringMap = new HashMap<>();
        resourceStringMap.put(Resource.structuralVariation, "structural_variation");
        resourceStringMap.put(Resource.cpgIsland, "cpg_island");
        resourceStringMap.put(Resource.mimaTarget, "mima_target");
        resourceStringMap.put(Resource.consequenceType, "consequence_type");
        resourceStringMap.put(Resource.fullAnnotation, "full_annotation");


        subCategoryBeanMap = new HashMap<>();
        subCategoryBeanMap.put(SubCategory.protein, Protein.class);
        subCategoryBeanMap.put(SubCategory.chromosome, Chromosome.class);
        subCategoryBeanMap.put(SubCategory.exon, Exon.class);
        subCategoryBeanMap.put(SubCategory.transcript, Transcript.class);
        subCategoryBeanMap.put(SubCategory.gene, Gene.class);
        subCategoryBeanMap.put(SubCategory.clinvar, MeasureTraitType.ClinVarAccession.class);
        subCategoryBeanMap.put(SubCategory.mutation, Mutation.class);


        resourceBeanMap = new HashMap<>();
        //genomic/region
        resourceBeanMap.put(Resource.gene, Gene.class);
        resourceBeanMap.put(Resource.transcript, Transcript.class);
        resourceBeanMap.put(Resource.exon, Exon.class);
        resourceBeanMap.put(Resource.snp, Variation.class);
        resourceBeanMap.put(Resource.mutation, Mutation.class);
        resourceBeanMap.put(Resource.structuralVariation, StructuralVariation.class);
        resourceBeanMap.put(Resource.sequence, GenomeSequenceFeature.class);
        resourceBeanMap.put(Resource.tfbs, Tfbs.class);
//        resourceBeanMap.put(Resource.mima_target, .class);
        resourceBeanMap.put(Resource.cpgIsland, CpGIsland.class);
        resourceBeanMap.put(Resource.conserved_region, ConservedRegion.class);
        resourceBeanMap.put(Resource.regulatory, RegulatoryRegion.class);
        //genomic/variant
        resourceBeanMap.put(Resource.effect, GenomicVariantEffect.class);
        resourceBeanMap.put(Resource.consequenceType, ConsequenceType.class);
        resourceBeanMap.put(Resource.phenotype, Phenotype.class);
        resourceBeanMap.put(Resource.snp_phenotype, String.class);  //TODO
        resourceBeanMap.put(Resource.mutation_phenotype, Mutation.class);
        resourceBeanMap.put(Resource.annotation, VariantAnnotation.class);  //TODO
        resourceBeanMap.put(Resource.fullAnnotation, VariantAnnotation.class);  //TODO
        //genomic/chromosome
        resourceBeanMap.put(Resource.size, Chromosome.class);
        resourceBeanMap.put(Resource.cytoband, Cytoband.class);

        //feature/clinvar
        //feature/exon
        resourceBeanMap.put(Resource.bysnp, Exon.class);    //FIXME Won't work. CellBase returns "List<List<List<Exon>>>"
        resourceBeanMap.put(Resource.aminos, String.class);
        resourceBeanMap.put(Resource.region, Region.class);
        //feature/id
        resourceBeanMap.put(Resource.xref, Xref.class);
        resourceBeanMap.put(Resource.starts_with, Xref.class);
        //feature/karyotype //Deprecated
        //feature/mutation
//        resourceBeanMap.put(Resource.diseases, .class); //Without Id
        //feature/gene



    }

    public CellBaseClient(String host, int port, String path, String version, String species) throws URISyntaxException {
        this(new URI("http", null, host, port, path.endsWith("/") ? path : path + "/", null, null), version, species);
    }

    public CellBaseClient(URI uri, String version, String species) throws URISyntaxException {
        this(UriBuilder.fromUri(uri), version, species);
    }

    public CellBaseClient(UriBuilder uriBuilder, String version, String species) {
        this.species = species;
        ClientConfig clientConfig = new ClientConfig();
//        clientConfig.register()
        client = ClientBuilder.newClient(clientConfig);
//        client.register()

        if(version == null) {
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
        return get(category, subCategory, "", Resource.all, queryOptions, (Class<T>)subCategoryBeanMap.get(subCategory));
    }

    public <T> QueryResponse<QueryResult<T>> getList(Category category, SubCategory subCategory,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, "", Resource.list, queryOptions, (Class<T>)subCategoryBeanMap.get(subCategory));
    }

    public <T> QueryResponse<QueryResult<T>> getInfo(Category category, SubCategory subCategory, String id,
                                                    QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, id, Resource.info, queryOptions, (Class<T>)subCategoryBeanMap.get(subCategory));
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
        return get(category, subCategory, ids, resource, queryOptions, (Class<T>)resourceBeanMap.get(resource));
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

    public QueryResponse<QueryResult<StructuralVariation>> getStructuralVariation(Category category, SubCategory subCategory, List<Region> ids,
                                                                                  QueryOptions queryOptions) throws IOException {
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

    public QueryResponse<QueryResult<GenomeSequenceFeature>> getReverseSequence(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.reverse, queryOptions, (GenomeSequenceFeature.class));
    }

    public QueryResponse<QueryResult<Tfbs>> getTfbs(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.tfbs, queryOptions, (Tfbs.class));
    }

    public QueryResponse<QueryResult<RegulatoryRegion>> getRegulatoryRegion(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.regulatory, queryOptions, (RegulatoryRegion.class));
    }

    public QueryResponse<QueryResult<CpGIsland>> getCpgIsland(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.cpgIsland, queryOptions, (CpGIsland.class));
    }

    public QueryResponse<QueryResult<ConservedRegion>> getConservedRegion(Category category, SubCategory subCategory, List<Region> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.conserved_region, queryOptions, (ConservedRegion.class));
    }

//    public QueryResponse<QueryResult<Peptide>> getPeptide(Category category, SubCategory subCategory, List<Region> ids,
//                                                              QueryOptions queryOptions) throws IOException {
//        return get(category, subCategory, ids, "peptide", queryOptions, (.class));
//    }

    public QueryResponse<QueryResult<GenomicVariantEffect>> getEffect(Category category, SubCategory subCategory, List<GenomicVariant> ids,
                                                              QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.effect, queryOptions, (GenomicVariantEffect.class));
    }

    public QueryResponse<QueryResult<ConsequenceType>> getConsequenceType(Category category, SubCategory subCategory, List<GenomicVariant> ids,
                                                                          QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.consequenceType, queryOptions, (ConsequenceType.class));
    }

    public QueryResponse<QueryResult<VariantAnnotation>> getFullAnnotation(Category category, SubCategory subCategory, List<GenomicVariant> ids,
                                                                          QueryOptions queryOptions) throws IOException {
        return get(category, subCategory, ids, Resource.fullAnnotation, queryOptions, (VariantAnnotation.class));
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

        if(queryOptions == null) {
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
        queryOptions.remove("post");

        String categoryStr = categoryStringMap.containsKey(category) ? categoryStringMap.get(category) : category.name();
        String subCategoryStr = subCategoryStringMap.containsKey(subCategory) ? subCategoryStringMap.get(subCategory) : subCategory.name();
        String resourceStr = resourceStringMap.containsKey(resource) ? resourceStringMap.get(resource) : resource.name();

        QueryResponse<QueryResult<T>> qr = restGetter(categoryStr, subCategoryStr, idsCvs, resourceStr, post, queryOptions, responseReader);
        return qr;
    }

    private <T> QueryResponse<QueryResult<T>> restGetter(
            String categoryStr, String subCategoryStr, String idsCvs, String resourceStr, boolean post, QueryOptions queryOptions, ObjectReader responseReader)
            throws IOException {

        UriBuilder clone = uriBuilder.clone()
                .path(categoryStr)
                .path(subCategoryStr);
        if(idsCvs != null && !idsCvs.isEmpty() && !post) {
            clone = clone.path(idsCvs);
        }

        clone = clone.path(resourceStr);
        for (Map.Entry<String, Object> entry : queryOptions.entrySet()) {
            clone.queryParam(entry.getKey(), entry.getValue());
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
        if(c == null) {
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
