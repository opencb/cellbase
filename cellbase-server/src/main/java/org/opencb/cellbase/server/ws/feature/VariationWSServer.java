package org.opencb.cellbase.server.ws.feature;

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fjlopez on 29/04/16.
 */
@Path("/{version}/{species}/feature/variation")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Variation", description = "Known genomic variation RESTful Web Services API")
public class VariationWSServer extends GenericRestWSServer {

    protected static final HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<>();

    public VariationWSServer(@PathParam("version")
                           @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                   defaultValue = "latest") String version,
                           @PathParam("species")
                           @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                   + "of potentially available species ids, please refer to: "
                                   + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                           @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get JSON specification of Variant data model", response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Variant.class);
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response first() {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
        return createOkResponse(variationDBAdaptor.first());
    }

    @GET
    @Path("/count")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects for the regions.",
            response = Integer.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list ENSEMBL gene ids, e.g.: ENSG00000161905. Exact text matches will be "
                            + "returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "chromosome",
                    value = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reference",
                    value = "Comma separated list of possible reference to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "alternate",
                    value = "Comma separated list of possible alternate to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response count() {
//    public Response count(@DefaultValue("")
//                          @QueryParam("region")
//                          @ApiParam(name = "region",
//                                  value = "Comma separated list of genomic regions to be queried, "
//                                          + "e.g.: 1:6635137-6635325", required = true) String region) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//        query.append(VariantDBAdaptor.QueryParams.REGION.key(), region);
        return createOkResponse(variationDBAdaptor.count(query));
    }

    @GET
    @Path("/stats")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Not implemented yet.",
            response = Integer.class, responseContainer = "QueryResponse", hidden = true)
    public Response stats() {
        return super.stats();
    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get information about a (list of) SNPs", notes = "An independent"
            + " database query will be issued for each region in id, meaning that results for each region will be"
            + " returned in independent QueryResult objects within the QueryResponse object.",
            response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list ENSEMBL gene ids, e.g.: ENSG00000161905. Exact text matches will be "
                            + "returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "chromosome",
                    value = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reference",
                    value = "Comma separated list of possible reference to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "alternate",
                    value = "Comma separated list of possible alternate to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getByEnsemblId(@PathParam("id")
                                   @ApiParam(name = "id",
                                           value = "Comma separated list of rs ids, e.g.: rs6025",
                                           required = true) String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            String[] ids = id.split(",");
            List<Query> queries = new ArrayList<>(ids.length);
            for (String s : ids) {
                queries.add(new Query(VariantDBAdaptor.QueryParams.ID.key(), s));
            }
            return createOkResponse(variationDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/next")
    @ApiOperation(httpMethod = "GET", value = "Get information about the next SNP")
    public Response getNextById(@PathParam("id") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            query.put(VariantDBAdaptor.QueryParams.ID.key(), id.split(",")[0]);
            return createOkResponse(variationDBAdaptor.next(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/consequence_types")
    @ApiOperation(httpMethod = "GET", value = "Get all sequence ontology terms describing consequence types",
            response = String.class, responseContainer = "QueryResponse")
    public Response getAllConsequenceTypes() {
        try {
            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//            query.put(VariantDBAdaptor.QueryParams.REGION.key(), "22:1-50000000");
//            return createOkResponse(variationDBAdaptor.distinct(query, "displayConsequenceType"));

            List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            QueryResult<String> queryResult = new QueryResult<>("consequence_types");
            queryResult.setNumResults(consequenceTypes.size());
            queryResult.setResult(consequenceTypes);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 29/04/16 GET and POST web services to be fixed
    @GET
    @Path("/{snpId}/consequence_type")
    @ApiOperation(httpMethod = "GET", value = "Get the biological impact of the SNP(s)", hidden = true)
    public Response getConsequenceTypeByGetMethod(@PathParam("snpId") String snpId) {
        return getConsequenceType(snpId);
    }

    @POST
    @Path("/consequence_type")
    @ApiOperation(httpMethod = "POST", value = "Get the biological impact of the SNP(s)", hidden = true)
    public Response getConsequenceTypeByPostMethod(@QueryParam("id") String snpId) {
        return getConsequenceType(snpId);
    }

    private Response getConsequenceType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return generateResponse(snpId, "SNP_CONSEQUENCE_TYPE", Arrays.asList(""));
        } catch (Exception e) {
            return createErrorResponse("getConsequenceTypeByPostMethod", e.toString());
        }
    }

    // FIXME: 29/04/16 GET and POST methods to be fixed
    @GET
    @Path("/{snpId}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Get the regulatory impact of the SNP(s)", hidden = true)
    public Response getRegulatoryByGetMethod(@PathParam("snpId") String snpId) {
        return getRegulatoryType(snpId);
    }

    @POST
    @Path("/regulatory")
    @ApiOperation(httpMethod = "POST", value = "Get the regulatory impact of the SNP(s)", hidden = true)
    public Response getRegulatoryTypeByPostMethod(@QueryParam("id") String snpId) {
        return getRegulatoryType(snpId);
    }

    private Response getRegulatoryType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the adjacent sequence to the SNP(s) - Not yet implemented",
            hidden = true)
    public Response getSequence(@PathParam("snpId") String query) {
        try {
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 29/04/16 update implementation
    @GET
    @Path("/{snpId}/population_frequency")
    @ApiOperation(httpMethod = "GET", value = "Get the frequencies in the population for the SNP(s)", hidden = true)
    public Response getPopulationFrequency(@PathParam("snpId") String snpId) {
        try {
            parseQueryParams();
//            SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(species, version);
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return generateResponse(snpId, "SNP_POPULATION_FREQUENCY", Arrays.asList(""));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 29/04/16 finish implementation
    @GET
    @Path("/{snpId}/xref")
    @ApiOperation(httpMethod = "GET", value = "Retrieve all external references for the SNP(s)", hidden = true)
    public Response getXrefs(@PathParam("snpId") String query) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


}
