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
import org.bson.Document;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.core.XRefsDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/id")
@Produces("application/json")
@Api(value = "Xref", description = "External References RESTful Web Services API")
public class IdWSServer extends GenericRestWSServer {

    public IdWSServer(@PathParam("version") String version, @PathParam("species") String species,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Xref.class);
    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the external reference info for the ID")
    public Response getByFeatureIdInfo(@PathParam("id") String query) {
        try {
            parseQueryParams();
            XRefsDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            List<String> list = Splitter.on(",").splitToList(query);
            List<QueryResult> dbNameList = xRefDBAdaptor.getAllByDBNameList(Splitter.on(",").splitToList(query), queryOptions);
            for (int i = 0; i < dbNameList.size(); i++) {
                for (Object o : dbNameList.get(i).getResult()) {
                    if (((Document) o).get("id").equals(list.get(i))) {
                        List<Object> objectList = new ArrayList<>(1);
                        objectList.add(o);
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
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the external references for the ID")
    public Response getAllXrefsByFeatureId(@PathParam("id") String query, @DefaultValue("") @QueryParam("dbname") String dbname) {
        try {
            parseQueryParams();
            XRefsDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            if (dbname != null && !dbname.isEmpty()) {
                queryOptions.put("dbname", Splitter.on(",").splitToList(dbname));
            }
            return createOkResponse(xRefDBAdaptor.getAllByDBNameList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/starts_with")
    @ApiOperation(httpMethod = "GET", value = "Get the genes that match the beginning of the given string")
    public Response getByLikeQuery(@PathParam("id") String query) {
        try {
            parseQueryParams();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            return createOkResponse(x.getByStartsWithQueryList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/contains")
    @ApiOperation(httpMethod = "GET", value = "Get the IDs that contain the given string")
    public Response getByContainsQuery(@PathParam("id") String query) {
        try {
            parseQueryParams();
            XRefsDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.assembly);
            List<QueryResult> xrefs = xRefDBAdaptor.getByContainsQueryList(Splitter.on(",").splitToList(query), queryOptions);
            return createOkResponse(xrefs);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{id}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene for the given ID")
    public Response getGeneByEnsemblId(@PathParam("id") String query) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Deprecated
    @GET
    @Path("/{id}/snp")
    @ApiOperation(httpMethod = "GET", value = "Get the SNP for the given ID")
    public Response getSnpByFeatureId(@PathParam("id") String query) {
        try {
            parseQueryParams();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
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
