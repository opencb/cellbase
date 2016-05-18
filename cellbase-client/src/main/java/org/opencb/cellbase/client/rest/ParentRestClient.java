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

package org.opencb.cellbase.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by imedina on 12/05/16.
 */
public class ParentRestClient {

    protected Client client;

    protected String category;
    protected String subcategory;

    protected ClientConfiguration configuration;

    protected static ObjectMapper jsonObjectMapper;

    public ParentRestClient(ClientConfiguration configuration) {
        this.configuration = configuration;

        this.client = ClientBuilder.newClient();
        jsonObjectMapper = new ObjectMapper();
    }

    protected QueryResponse<Long> count(Query query) throws IOException {
        return execute("count", query, Long.class);
    }

    protected <T> QueryResponse<T> execute(String action, Map<String, Object> params, Class<T> clazz)
            throws IOException {
        return execute(null, action, params, clazz);
    }

    protected <T> QueryResponse<T> execute(String id, String resource, Map<String, Object> params, Class<T> clazz)
            throws IOException {

        // Build the basic URL
        WebTarget path = client
                .target(configuration.getRest().getHosts().get(0))
                .path("webservices/rest/v4")
                .path("hsapiens")
                .path(category)
                .path(subcategory);

        // TODO we still have to check if there are multiple IDs, the lmit is 200 pero query, this can be parallelized
        // Some WS do not have IDs such as 'create'
        if (id != null && !id.isEmpty()) {
            path = path.path(id);
        }

        // Add the last URL part, the 'action'
        path = path.path(resource);

        // TODO we still have to check the limit of the query, and keep querying while there are more results
        if (params != null) {
            for (String s : params.keySet()) {
                path = path.queryParam(s, params.get(s));
            }
        }

        System.out.println("REST URL: " + path.getUri().toURL());
        String jsonString = path.request().get(String.class);
        System.out.println("jsonString = " + jsonString);
        QueryResponse<T> queryResponse = parseResult(jsonString, clazz);
        System.out.println("queryResponse = " + queryResponse);
        return queryResponse;
    }

    public static <T> QueryResponse<T> parseResult(String json, Class<T> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .reader(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

    protected Map<String, Object> createParamsMap(String key, Object value) {
        Map<String, Object> params= new HashMap<>(10);
        params.put(key, value);
        return params;
    }
}
