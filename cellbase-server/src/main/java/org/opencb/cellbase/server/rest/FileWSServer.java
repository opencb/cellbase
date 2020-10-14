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

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.FileQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.FileManager;
import org.opencb.cellbase.server.exception.LimitException;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

//@Path("/{apiVersion}/file")
//@Produces("application/json")
//@Api(value = "File", description = "File RESTful Web Services API")
public class FileWSServer extends GenericRestWSServer {

    private FileManager fileManager;

    public FileWSServer(@PathParam("apiVersion")
                        @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, LimitException, IOException, CellbaseException {
        super(apiVersion, uriInfo, hsr);
        fileManager = cellBaseManagerFactory.getFileManager();
    }

    @GET
    @Path("/query")
    @ApiOperation(httpMethod = "GET", value = "Returns the values based on coordinates", response = String.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "filePath", value = "Full path to the file",
                    required = true, dataType = "java.lang.String", paramType = "query"),
            @ApiImplicitParam(name = "fileType", value = "Fasta files MUST have tabix file. Should be the same file name with suffix 'fai'",
                    required = true, dataType = "java.lang.String", paramType = "query", allowableValues = "fasta"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = true, dataType = "java.util.List", paramType = "query")
    })
    public Response getQuery() {
        try {
            FileQuery query = new FileQuery(uriParams);
            List<CellBaseDataResult<String>> queryResults = fileManager.search(query);
            return createOkResponse(queryResults);
        } catch (QueryException | IOException e) {
            return createErrorResponse(e);
        }
    }
}
