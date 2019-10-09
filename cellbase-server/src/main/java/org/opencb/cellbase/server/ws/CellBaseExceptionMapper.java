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

package org.opencb.cellbase.server.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger;
    private static ObjectMapper jsonObjectMapper;
    private static ObjectWriter jsonObjectWriter;
    private static final String ERROR = "error";

//    private final HttpServletRequest hsr;

    static {
        logger = LoggerFactory.getLogger(CellBaseExceptionMapper.class);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();

    }

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
            logger.info("{}\t{}\t{}",
                    uriInfo.getAbsolutePath().toString(),
                    jsonObjectWriter.writeValueAsString(queryOptions),
                    ERROR);

            return Response.ok(GenericRestWSServer.jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE)
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
            throw new RuntimeException(e1.toString());
        }
    }
}
