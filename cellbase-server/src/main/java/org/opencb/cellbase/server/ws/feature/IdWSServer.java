package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.lib.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.core.XRefsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/id")
@Produces("application/json")
@Api(value = "Xref", description = "External References RESTful Web Services API")
public class IdWSServer extends GenericRestWSServer {

    public IdWSServer(@PathParam("version") String version, @PathParam("species") String species,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{id}/xref")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the external references for the ID")
    public Response getByFeatureId(@PathParam("id") String query, @DefaultValue("") @QueryParam("dbname") String dbname) {
        try {
            checkParams();
            XRefsDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            if (!dbname.equals("")) {
                queryOptions.put("dbname", Splitter.on(",").splitToList(dbname));
            }
            return createOkResponse(xRefDBAdaptor.getAllByDBNameList(Splitter.on(",").splitToList(query), queryOptions));
//            if (dbName.equals("")) {
//                return generateResponse(query, "XREF", x.getAllByDBNameList(Splitter.on(",").splitToList(query), null));
//            }
//            else {
//                return generateResponse(query, "XREF", x.getAllByDBNameList(Splitter.on(",").splitToList(query), Splitter.on(",").splitToList(dbName)));
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @GET
    @Path("/{id}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene for the given ID")
    public Response getGeneByEnsemblId(@PathParam("id") String query) {
        try {
            checkParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);

            QueryOptions queryOptions = new QueryOptions("exclude", exclude);

            return createJsonResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));

//			return generateResponse(query, "GENE",  x.getAllByNameList(Splitter.on(",").splitToList(query),exclude));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @Deprecated
    @GET
    @Path("/{id}/snp")
    @ApiOperation(httpMethod = "GET", value = "Get the SNP for the given ID")
    public Response getSnpByFeatureId(@PathParam("id") String query) {
        try {
            checkParams();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @GET
    @Path("/{id}/starts_with")
    @ApiOperation(httpMethod = "GET", value = "Get the genes that match the beginning of the given string")
    public Response getByLikeQuery(@PathParam("id") String query) {
        try {
            checkParams();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
//            if (query.startsWith("rs") || query.startsWith("AFFY_") || query.startsWith("SNP_") || query.startsWith("VAR_") || query.startsWith("CRTAP_") || query.startsWith("FKBP10_") || query.startsWith("LEPRE1_") || query.startsWith("PPIB_")) {
//                List<List<Xref>> snpXrefs = x.getByStartsWithSnpQueryList(Splitter.on(",").splitToList(query));
//                for (List<Xref> xrefList : snpXrefs) {
//                    xrefs.get(0).addAll(xrefList);
//                }
//            }
//            return generateResponse(query, "XREF", xrefs);
            return createOkResponse(x.getByStartsWithQueryList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @GET
    @Path("/{id}/contains")
/*
    @ApiOperation(httpMethod = "GET", value = "Get the genes that contain the given string")
*/
    public Response getByContainsQuery(@PathParam("id") String query) {
        try {
            checkParams();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            List<List<Xref>> xrefs = x.getByContainsQueryList(Splitter.on(",").splitToList(query));
            if (query.startsWith("rs") || query.startsWith("AFFY_") || query.startsWith("SNP_") || query.startsWith("VAR_") || query.startsWith("CRTAP_") || query.startsWith("FKBP10_") || query.startsWith("LEPRE1_") || query.startsWith("PPIB_")) {
                List<QueryResult> snpXrefs = x.getByStartsWithSnpQueryList(Splitter.on(",").splitToList(query),queryOptions);
//                for (List<Xref> xrefList : snpXrefs) {
//                    xrefs.get(0).addAll(xrefList);
//                }
            }
            return generateResponse(query, xrefs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @GET
    public Response getHelp() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }

}
