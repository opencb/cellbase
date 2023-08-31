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
import org.opencb.biodata.formats.pubmed.v233jaxb.PubmedArticle;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.PublicationQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.PublicationManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Map;

import static org.opencb.cellbase.core.ParamConstants.*;

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{apiVersion}/publication")
@Produces("application/json")
@Api(value = "Publication", description = "Publication RESTful Web Services API")
public class PublicationWSServer extends GenericRestWSServer {

    private PublicationManager publicationManager;

    private static final String PONG = "pong";

    public PublicationWSServer(@PathParam("apiVersion")
                        @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                               @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0")
                               @QueryParam("dataRelease") int dataRelease,
                               @ApiParam(name = "apiKey", value = API_KEY_DESCRIPTION) @DefaultValue("") @QueryParam("apiKey")
                                       String apiKey,
                               @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellBaseException {
        super(apiVersion, uriInfo, hsr);
        publicationManager = cellBaseManagerFactory.getPublicationManager();
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(PubmedArticle.class);
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all publication objects", response = Map.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "id", value = PUBLICATION_ID_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "author", value = PUBLICATION_AUTHOR_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "title", value = PUBLICATION_TITLE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = PUBLICATION_KEYWORD_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "abstract", value = PUBLICATION_ABSTRACT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
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
    public Response getAll() {
        try {
            PublicationQuery query = new PublicationQuery(uriParams);
            query.setDataRelease(getDataRelease());
            logger.info("/search PublicationQuery: " + query);
            CellBaseDataResult<PubmedArticle> queryResults = publicationManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
