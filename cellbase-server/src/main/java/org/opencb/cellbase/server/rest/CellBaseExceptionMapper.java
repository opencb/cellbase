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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Event;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;


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

    static {
        logger = LoggerFactory.getLogger(CellBaseExceptionMapper.class);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();

    }

    public CellBaseExceptionMapper(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(Exception e) {
        // First we print the exception in Server logs
        e.printStackTrace();

        // Now we prepare the response to client
        CellBaseDataResponse queryResponse = new CellBaseDataResponse();
        QueryOptions queryOptions = new QueryOptions(uriInfo.getQueryParameters(), true);
        queryResponse.setParams(new ObjectMap(queryOptions));
        queryResponse.addEvent(new Event(Event.Type.ERROR, e.toString()));

        CellBaseDataResponse result = new CellBaseDataResponse();
        result.addEvent(new Event(Event.Type.WARNING, "Future errors will ONLY be shown in the QueryResponse body"));
        result.addEvent(new Event(Event.Type.ERROR, "DEPRECATED: " + e.toString()));

        queryResponse.setResponses(Collections.singletonList(result));

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
