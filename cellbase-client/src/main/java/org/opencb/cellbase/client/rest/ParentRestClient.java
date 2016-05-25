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

    protected QueryResponse<Long> count(Query query) throws IOException {
        return execute("count", query, Long.class);
    }

    protected QueryResponse<T> first() throws IOException {
        return execute("first", null, clazz);
    }

    protected QueryResponse<T> get(List<String> id, Map<String, Object> params) throws IOException {
        return execute(id, "info", params, clazz);
    }

    protected <T> QueryResponse<T> execute(String action, Map<String, Object> params, Class<T> clazz) throws IOException {
        return execute("", action, params, clazz);
    }

    protected <T> QueryResponse<T> execute(String ids, String resource, Map<String, Object> params, Class<T> clazz) throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, params, clazz);
    }

    protected <T> QueryResponse<T> execute(List<String> idList, String resource, Map<String, Object> params, Class<T> clazz)
            throws IOException {

        // Build the basic URL
        WebTarget path = client
                .target(configuration.getRest().getHosts().get(0))
                .path("webservices/rest/v4")
                .path("hsapiens")
                .path(category)
                .path(subcategory);

//        // TODO we still have to check if there are multiple IDs, the lmit is 200 pero query, this can be parallelized
//        // Some WS do not have IDs such as 'create'
//        if (idList != null && !idList.isEmpty()) {
//            String ids = StringUtils.join(idList, ',');
//            path = path.path(ids);
//        }
//
//        // Add the last URL part, the 'action'
//        path = path.path(resource);
//
//        // TODO we still have to check the limit of the query, and keep querying while there are more results
//        if (params != null) {
//            for (String s : params.keySet()) {
//                path = path.queryParam(s, params.get(s));
//            }
//        }

        params.put("limit", LIMIT);

        String ids = "";
        if (idList != null && !idList.isEmpty()) {
            ids = StringUtils.join(idList, ',');
        }

        Map<Integer, Integer> idMap = new HashMap<>();
        List<String> newIdsList = null;
        boolean call = true;
        int skip = 0;
        QueryResponse<T> queryResponse = null;
        QueryResponse<T> finalQueryResponse = null; // = (QueryResponse<T>) callRest(path, ids, resource, params, clazz);
        while (call) {

//            System.out.println("REST URL: " + path.getUri().toURL());
//            String jsonString = path.request().get(String.class);
//            logger.debug("jsonString = " + jsonString);
//            queryResponse = parseResult(jsonString, clazz);
            queryResponse = (QueryResponse<T>) callRest(path, ids, resource, params, clazz);
            if (finalQueryResponse == null) {
                finalQueryResponse = queryResponse;
            } else {
                // merge query responses
                if (newIdsList != null && newIdsList.size() > 0) {
                    for (int i = 0; i < newIdsList.size(); i++) {
                        finalQueryResponse.getResponse().get(idMap.get(i)).getResult()
                                .addAll(queryResponse.getResponse().get(i).getResult());
                    }
                } else {
                    System.out.println("really???");
                }
            }

            // check if we need to call again
            newIdsList = new ArrayList<>();
            idMap = new HashMap<>();

            int numTotal;
            for (int i = 0; i < queryResponse.getResponse().size(); i++) {
                System.out.println("aaaaaaaaaaaa");
                numTotal = queryResponse.getResponse().get(i).getNumResults();
                if (numTotal == LIMIT) {
                    idMap.put(newIdsList.size(), i);
                    newIdsList.add(idList.get(i));
                }
            }

            if (newIdsList.isEmpty()) {
                // this breaks the while condition
                call = false;
                break;
            } else {
//                int skip = 0;
                ids = StringUtils.join(newIdsList, ',');
                skip += LIMIT;
                params.put("skip", skip);
//                params.put("limit", LIMIT);
//                QueryResponse<T> queryResponse1 = (QueryResponse<T>) callRest(path, ids, resource, params, clazz);
//                for (Map.Entry<Integer, Integer> entry : idMap.entrySet()) {
//                    finalQueryResponse.getResponse().get(entry.getValue())
// .addAllResults(queryResponse1.getResponse().get(entry.getKey()).getResult());
//                }
                break;
            }
        }

        logger.debug("queryResponse = " + queryResponse);
        return finalQueryResponse;
    }

    private QueryResponse<T> callRest(WebTarget path, String ids, String resource, Map<String, Object> params, Class clazz)
            throws IOException {
        WebTarget callUrl = path;
        if (ids != null && !ids.isEmpty()) {
            callUrl = path.path(ids);
        }

        // Add the last URL part, the 'action'
        callUrl = callUrl.path(resource);

        // TODO we still have to check the limit of the query, and keep querying while there are more results
        if (params != null) {
            for (String s : params.keySet()) {
                callUrl = callUrl.queryParam(s, params.get(s));
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
