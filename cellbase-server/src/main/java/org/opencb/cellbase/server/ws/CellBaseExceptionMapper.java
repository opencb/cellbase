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

package org.opencb.cellbase.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

/**
 * Created by imedina on 02/03/16.
 */
@Provider
public class CellBaseExceptionMapper implements ExceptionMapper<Exception> {

    private final UriInfo uriInfo;
//    private final HttpServletRequest hsr;

    public CellBaseExceptionMapper(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
//        this.hsr = hsr;
    }

    @Override
    public Response toResponse(Exception e) {
        // First we print the exception in Server logs
        e.printStackTrace();

        // Now we prepare the response to client
//            queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        QueryResponse queryResponse = new QueryResponse();
//            queryResponse.setApiVersion(version);
        QueryOptions queryOptions = new QueryOptions(uriInfo.getQueryParameters(), true);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setError(e.toString());

        QueryResult result = new QueryResult();
        result.setWarningMsg("Future errors will ONLY be shown in the QueryResponse body");
        result.setErrorMsg("DEPRECATED: " + e.toString());
        queryResponse.setResponse(Collections.singletonList(result));

        try {
            return Response.ok(GenericRestWSServer.jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE)
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
            throw new RuntimeException(e1.toString());
        }
    }
}
