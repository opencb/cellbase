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

package org.opencb.cellbase.server.rest.feature;

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.XrefQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.XrefManager;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.core.ParamConstants.*;

/**
 * @author imedina
 */
@Path("/{apiVersion}/{species}/feature/id")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Xref", description = "External References RESTful Web Services API")
public class IdWSServer extends GenericRestWSServer {

    private XrefManager xrefManager;
    private GeneManager geneManager;

    public IdWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION, defaultValue = DEFAULT_VERSION)
                              String apiVersion,
                      @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                      @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly") String assembly,
                      @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                              int dataRelease,
                      @ApiParam(name = "token", value = API_KEY_DESCRIPTION, hidden = true) @DefaultValue("") @QueryParam("token")
                              String token,
                      @ApiParam(name = "apiKey", value = API_KEY_DESCRIPTION) @DefaultValue("") @QueryParam("apiKey") String apiKey,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws QueryException, IOException, CellBaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }

        xrefManager = cellBaseManagerFactory.getXrefManager(species, assembly);
        geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Xref.class);
    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the external reference(s) info for the ID(s)",
            notes = "An independent database query will be issued for each id, meaning that results for each id will be"
                    + " returned in independent CellBaseDataResult objects within the QueryResponse object.", response = Xref.class,
            responseContainer = "QueryResponse")
    public Response getInfo(@PathParam("id") @ApiParam(name = "id", value = FEATURE_IDS_DESCRIPTION, required = true)
                                    String id) {
        try {
            XrefQuery query = new XrefQuery(uriParams);
            List<CellBaseDataResult<Xref>> queryResults = xrefManager.info(Arrays.asList(id.split(",")), query, getDataRelease(),
                    getApiKey());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/xref")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the external references related with given ID(s)",
            response = Xref.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = FEATURE_IDS_DESCRIPTION,
                    required = true, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "dbname", value = XREF_DBNAMES,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAllXrefs() {
        try {
            XrefQuery query = new XrefQuery(uriParams);
            query.setDataRelease(getDataRelease());
            CellBaseDataResult<Xref> queryResults = xrefManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{id}/startsWith")
//    @ApiOperation(httpMethod = "GET", value = "Get the gene HGNC symbols of genes for which there is an Xref id that "
//            + "matches the beginning of the given string", response = Map.class, responseContainer = "QueryResponse")
//    public Response getByLikeQuery(@PathParam("id")
//                                   @ApiParam(name = "id", value = "One single string to be matched at the beginning of"
//                                           + " the Xref id", required = true) String id) {
//        try {
//            parseQueryParams();
//            XRefDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
//            CellBaseDataResult queryResult = x.startsWith(id, queryOptions);
//            queryResult.setId(id);
//            return createOkResponse(queryResult);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{id}/contains")
//    @ApiOperation(httpMethod = "GET", value = "Get gene HGNC symbols for which there is an Xref id containing the given "
//            + "string", response = Map.class, responseContainer = "QueryResponse")
//    public Response getByContainsQuery(@PathParam("id")
//                                       @ApiParam(name = "id", value = "Comma separated list of strings to "
//                                               + "be contained within the xref id, e.g.: BRCA2", required = true) String id) {
//        try {
//            parseQueryParams();
//            XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
//            CellBaseDataResult xrefs = xRefDBAdaptor.contains(id, queryOptions);
//            xrefs.setId(id);
//            return createOkResponse(xrefs);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{id}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene(s) for the given ID(s)", notes = "An independent"
            + " database query will be issued for each id, meaning that results for each id will be"
            + " returned in independent CellBaseDataResult objects within the QueryResponse object.",
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getGeneByEnsemblId(@PathParam("id")
                                       @ApiParam(name = "id", value = "Comma separated list of ids to look"
                                               + " for within gene xrefs, e.g.: BRCA2", required = true) String id) {
        try {
            GeneQuery query = new GeneQuery(uriParams);
            List<CellBaseDataResult<Gene>> queryResults = geneManager.info(Arrays.asList(id.split(",")), query, getDataRelease(),
                    getApiKey());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/dbnames")
//    @ApiOperation(httpMethod = "GET", value = "Get list of distinct source DB names from which xref ids were collected ",
//        response = String.class, responseContainer = "QueryResponse")
//    public Response getDBNames() {
//        try {
//            parseQueryParams();
//            CellBaseDataResult queryResults = xrefManager.getDBNames(query);
//            return createOkResponse(queryResults);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }

}
