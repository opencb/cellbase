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
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.*;

/**
 * Created by imedina on 12/05/16.
 */
public class ParentRestClient<T> {

    protected Client client;

    protected String category;
    protected String subcategory;

    protected ClientConfiguration configuration;

    protected static ObjectMapper jsonObjectMapper;
    protected static final int LIMIT = 1000;

    protected static Logger logger;


    protected Class<T> clazz;

    public ParentRestClient(ClientConfiguration configuration) {
        this.configuration = configuration;

        this.client = ClientBuilder.newClient();
        jsonObjectMapper = new ObjectMapper();

        logger = LoggerFactory.getLogger(this.getClass().toString());
    }


    public QueryResponse<Long> count(Query query) throws IOException {
        return execute("count", query, new QueryOptions(), Long.class);
    }

    public QueryResponse<T> first() throws IOException {
        return execute("first", new Query(), new QueryOptions(), clazz);
    }

    public QueryResponse<T> get(List<String> id, QueryOptions queryOptions) throws IOException {
        return execute(id, "info", queryOptions, clazz);
    }


    protected <T> QueryResponse<T> execute(String action, Query query, QueryOptions queryOptions, Class<T> clazz) throws IOException {
        queryOptions.putAll(query);
        return execute("", action, queryOptions, clazz);
    }

    protected <T> QueryResponse<T> execute(String ids, String resource, QueryOptions queryOptions, Class<T> clazz) throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, queryOptions, clazz);
    }

    protected <T> QueryResponse<T> execute(List<String> idList, String resource, QueryOptions queryOptions, Class<T> clazz)
            throws IOException {

        // Build the basic URL
        WebTarget path = client
                .target(configuration.getRest().getHosts().get(0))
                .path("webservices/rest/v4")
                .path("hsapiens")
                .path(category)
                .path(subcategory);

        if (queryOptions == null) {
            queryOptions = new QueryOptions();
        }
        queryOptions.putIfAbsent("limit", LIMIT);

        String ids = "";
        if (idList != null && !idList.isEmpty()) {
            ids = StringUtils.join(idList, ',');
        }

        Map<Integer, Integer> idMap = new HashMap<>();
        List<String> prevIdList = idList;
        List<String> newIdsList = null;
        boolean call = true;
        int skip = 0;
        QueryResponse<T> queryResponse = null;
        QueryResponse<T> finalQueryResponse = null;
        while (call) {
            queryResponse = (QueryResponse<T>) callRest(path, ids, resource, queryOptions, clazz);

            // First iteration we set the response object, no merge needed
            if (finalQueryResponse == null) {
                finalQueryResponse = queryResponse;
            } else {    // merge query responses
                if (newIdsList != null && newIdsList.size() > 0) {
                    for (int i = 0; i < newIdsList.size(); i++) {
                        finalQueryResponse.getResponse().get(idMap.get(i)).getResult()
                                .addAll(queryResponse.getResponse().get(i).getResult());
                    }
                }
            }

            // check if we need to call again
            if (newIdsList != null) {
                prevIdList = newIdsList;
            }
            newIdsList = new ArrayList<>();
            idMap = new HashMap<>();
            for (int i = 0; i < queryResponse.getResponse().size(); i++) {
                if (queryResponse.getResponse().get(i).getNumResults() == LIMIT) {
                    idMap.put(newIdsList.size(), i);
                    newIdsList.add(prevIdList.get(i));
                }
            }

            if (newIdsList.isEmpty()) {
                // this breaks the while condition
                call = false;
            } else {
                ids = StringUtils.join(newIdsList, ',');
                skip += LIMIT;
                queryOptions.put("skip", skip);
            }
        }

        logger.debug("queryResponse = " + queryResponse);
        return finalQueryResponse;
    }

    private QueryResponse<T> callRest(WebTarget path, String ids, String resource, QueryOptions options, Class clazz)
            throws IOException {
        WebTarget callUrl = path;
        if (ids != null && !ids.isEmpty()) {
            callUrl = path.path(ids);
        }

        // Add the last URL part, the 'action'
        callUrl = callUrl.path(resource);

        if (options != null) {
            for (String s : options.keySet()) {
                callUrl = callUrl.queryParam(s, options.get(s));
            }
        }

        System.out.println("REST URL: " + callUrl.getUri().toURL());
        String jsonString = callUrl.request().get(String.class);
        logger.debug("jsonString = " + jsonString);
        return parseResult(jsonString, clazz);
    }

    public static <T> QueryResponse<T> parseResult(String json, Class<T> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

    protected Map<String, Object> createParamsMap(String key, Object value) {
        Map<String, Object> params = new HashMap<>(10);
        params.put(key, value);
        return params;
    }
}
