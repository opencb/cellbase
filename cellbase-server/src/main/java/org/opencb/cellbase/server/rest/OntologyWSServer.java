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

package org.opencb.cellbase.server.rest;

import io.swagger.annotations.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.OntologyQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.SpeciesUtils;
import org.opencb.cellbase.lib.managers.OntologyManager;

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

@Path("/{apiVersion}/{species}/feature/ontology")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Ontology", description = "Ontology RESTful Web Services API")
public class OntologyWSServer extends GenericRestWSServer {

    private OntologyManager ontologyManager;

    public OntologyWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion",
                                value = ParamConstants.VERSION_DESCRIPTION,
                                defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                            @PathParam("species") @ApiParam(name = "species",
                                    value = ParamConstants.SPECIES_DESCRIPTION) String species,
                            @ApiParam(name = "assembly", value = ParamConstants.ASSEMBLY_DESCRIPTION)
                            @QueryParam("assembly") String assembly,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        List<String> assemblies = uriInfo.getQueryParameters().get("assembly");
        if (CollectionUtils.isNotEmpty(assemblies)) {
            assembly = assemblies.get(0);
        }
        if (StringUtils.isEmpty(assembly)) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }
        ontologyManager = cellBaseManagerFactory.getOntologyManager(species, assembly);
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all ontology objects", response = OntologyTerm.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "id", value = ParamConstants.ONTOLOGY_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.ONTOLOGY_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "namespace",  value = ParamConstants.ONTOLOGY_NAMESPACES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "source",  value = ParamConstants.ONTOLOGY_SOURCES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "synonyms", value = ParamConstants.ONTOLOGY_SYNONYMS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "xrefs", value = ParamConstants.ONTOLOGY_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "parents", value = ParamConstants.ONTOLOGY_PARENTS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "children", value = ParamConstants.ONTOLOGY_CHILDREN,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = ParamConstants.DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = ParamConstants.DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getAll() {
        try {
            OntologyQuery query = new OntologyQuery(uriParams);
            logger.info("/search OntologyQuery: " + query.toString());
            CellBaseDataResult<OntologyTerm> queryResults = ontologyManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{ids}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified ontology terms(s)", response = OntologyTerm.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getInfo(@PathParam("ids") @ApiParam(name = "ids", value = ParamConstants.ONTOLOGY_DESCRIPTION, required = true)
                                        String ids) {
        try {
            OntologyQuery query = new OntologyQuery(uriParams);
            List<CellBaseDataResult<OntologyTerm>> queryResults = ontologyManager.info(Arrays.asList(ids.split(",")), query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/distinct")
    @ApiOperation(httpMethod = "GET", notes = "Gets a unique list of values, e.g. namespace",
            value = "Get a unique list of values for a given field.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = ParamConstants.ONTOLOGY_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.ONTOLOGY_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "namespace",  value = ParamConstants.ONTOLOGY_NAMESPACES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "synonyms", value = ParamConstants.ONTOLOGY_SYNONYMS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "xrefs", value = ParamConstants.ONTOLOGY_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "parents", value = ParamConstants.ONTOLOGY_PARENTS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "children", value = ParamConstants.ONTOLOGY_CHILDREN,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getUniqueValues(@QueryParam("field") @ApiParam(name = "field", required = true,
            value = "Name of column to return, e.g. namespace") String field) {
        try {
            copyToFacet("field", field);
            OntologyQuery query = new OntologyQuery(uriParams);
            CellBaseDataResult<String> queryResults = ontologyManager.distinct(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = ParamConstants.DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(OntologyTerm.class);
    }

}
