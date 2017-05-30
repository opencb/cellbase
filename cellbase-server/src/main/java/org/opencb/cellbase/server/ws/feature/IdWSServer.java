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

package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.bson.Document;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.XRefDBAdaptor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/id")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Xref", description = "External References RESTful Web Services API")
public class IdWSServer extends GenericRestWSServer {

    public IdWSServer(@PathParam("version")
                      @ApiParam(name = "version", value = "Possible values: v3, v4",
                              defaultValue = "v4") String version,
                      @PathParam("species")
                      @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                              + "of potentially available species ids, please refer to: "
                              + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get JSON specification of Xref data model", response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Xref.class);
    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the external reference(s) info for the ID(s)",
            notes = "An independent database query will be issued for each id, meaning that results for each id will be"
            + " returned in independent QueryResult objects within the QueryResponse object.", response = Xref.class,
            responseContainer = "QueryResponse")
    public Response getByFeatureIdInfo(@PathParam("id")
                                       @ApiParam(name = "id", value = "Comma separated list of ids, e.g.: BRCA2. Exact "
                                               + "text matches will be returned.", required = true) String id) {
        try {
            parseQueryParams();
            XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory2.getXRefDBAdaptor(this.species, this.assembly);

            List<String> list = Splitter.on(",").splitToList(id);
//            String[] ids = id.split(",");
//            List<Query> queries = new ArrayList<>(ids.length);
//            for (String s : ids) {
//                queries.add(new Query(XRefDBAdaptor.QueryParams.ID.key(), s));
//            }
            List<Query> queries = createQueries(id, XRefDBAdaptor.QueryParams.ID.key());

            List<QueryResult<Document>> dbNameList = xRefDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < dbNameList.size(); i++) {
                dbNameList.get(i).setId(list.get(i));
                for (Document document : dbNameList.get(i).getResult()) {
                    if (document.get("id").equals(list.get(i))) {
                        List<Document> objectList = new ArrayList<>(1);
                        objectList.add(document);
                        dbNameList.get(i).setResult(objectList);
                        break;
                    }
                }
            }
            return createOkResponse(dbNameList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/xref")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the external references related with given ID(s)",
        response = Xref.class, responseContainer = "QueryResponse")
    public Response getAllXrefsByFeatureId(@PathParam("id")
                                           @ApiParam(name = "id", value = "Comma separated list of ids, e.g.: BRCA2."
                                                   + "Exact text matches will be searched.", required = true) String ids,
                                           @DefaultValue("")
                                           @QueryParam("dbname")
                                           @ApiParam(name = "dbname", value = "Comma separated list of source DB names"
                                                   + " to include in the search, e.g.: ensembl_gene,vega_gene,havana_gene."
                                                   + " Available db names are shown by this web service: "
                                                   + " http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/#!/Xref/"
                                                   + "getDBNames", required = false) String dbname) {
        try {
            parseQueryParams();
            XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory2.getXRefDBAdaptor(this.species, this.assembly);

            Query query = new Query();
            query.put(XRefDBAdaptor.QueryParams.ID.key(), ids);
            if (dbname != null && !dbname.isEmpty()) {
                query.put(XRefDBAdaptor.QueryParams.DBNAME.key(), dbname);
            }
//            return createOkResponse(xRefDBAdaptor.nativeGet(Splitter.on(",").splitToList(ids), queryOptions));
            QueryResult queryResult = xRefDBAdaptor.nativeGet(query, queryOptions);
            queryResult.setId(ids);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/starts_with")
    @ApiOperation(httpMethod = "GET", value = "Get the gene HGNC symbols of genes for which there is an Xref id that "
            + "matches the beginning of the given string", response = Map.class, responseContainer = "QueryResponse")
    public Response getByLikeQuery(@PathParam("id")
                                   @ApiParam(name = "id", value = "One single string to be matched at the beginning of"
                                           + " the Xref id", required = true) String id) {
        try {
            parseQueryParams();
            XRefDBAdaptor x = dbAdaptorFactory2.getXRefDBAdaptor(this.species, this.assembly);
            QueryResult queryResult = x.startsWith(id, queryOptions);
            queryResult.setId(id);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/contains")
    @ApiOperation(httpMethod = "GET", value = "Get gene HGNC symbols for which there is an Xref id containing the given "
            + "string", response = Map.class, responseContainer = "QueryResponse")
    public Response getByContainsQuery(@PathParam("id")
                                       @ApiParam(name = "id", value = "Comma separated list of strings to "
                                               + "be contained within the xref id, e.g.: BRCA2", required = true) String id) {
        try {
            parseQueryParams();
            XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory2.getXRefDBAdaptor(this.species, this.assembly);
            QueryResult xrefs = xRefDBAdaptor.contains(id, queryOptions);
            xrefs.setId(id);
            return createOkResponse(xrefs);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{id}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene(s) for the given ID(s)", notes = "An independent"
            + " database query will be issued for each id, meaning that results for each id will be"
            + " returned in independent QueryResult objects within the QueryResponse object.",
            responseContainer = "QueryResponse")
    public Response getGeneByEnsemblId(@PathParam("id")
                                       @ApiParam(name = "id", value = "Comma separated list of ids to look"
                                               + " for within gene xrefs, e.g.: BRCA2", required = true) String id) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);

            String[] ids = id.split(",");
            List<Query> queries = new ArrayList<>(ids.length);
            for (String s : ids) {
                queries.add(new Query(GeneDBAdaptor.QueryParams.XREFS.key(), s));
            }
            List<QueryResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < ids.length; i++) {
                queryResults.get(i).setId(ids[i]);
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/dbnames")
    @ApiOperation(httpMethod = "GET", value = "Get list of distinct source DB names from which xref ids were collected ",
        response = String.class, responseContainer = "QueryResponse")
    public Response getDBNames() {
        try {
            parseQueryParams();
            XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory2.getXRefDBAdaptor(this.species, this.assembly);
            QueryResult xrefs = xRefDBAdaptor.distinct(query, "transcripts.xrefs.dbName");
            return createOkResponse(xrefs);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }

}
