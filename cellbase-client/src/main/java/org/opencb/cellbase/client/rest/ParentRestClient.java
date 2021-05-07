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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.avro.DrugResponseClassification;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.mixin.DrugResponseClassificationMixIn;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by imedina on 12/05/16.
 */
public class ParentRestClient<T> {

    protected final String species;
    protected final String assembly;
    protected final Client client;

    // TODO: Should this be final?
    protected String category;
    protected String subcategory;

    protected Class<T> clazz;

    protected final ClientConfiguration configuration;

    protected static ObjectMapper jsonObjectMapper;
    protected final Logger logger;

    public static final int LIMIT = 1000;
    public static final int REST_CALL_BATCH_SIZE = 200;
    public static final int DEFAULT_NUM_THREADS = 4;

    protected static final String EMPTY_STRING = "";
    protected static final String META = "meta";
    protected static final String WEBSERVICES = "webservices";
    protected static final String REST = "rest";


    @Deprecated
    public ParentRestClient(ClientConfiguration configuration) {
        this(configuration.getDefaultSpecies(), null, configuration);
    }

    public ParentRestClient(String species, String assembly, ClientConfiguration configuration) {
        this.species = species;
        this.assembly = assembly;
        this.configuration = configuration;

        this.client = ClientBuilder.newClient();
        logger = LoggerFactory.getLogger(this.getClass().toString());
    }

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonObjectMapper.addMixIn(DrugResponseClassification.class, DrugResponseClassificationMixIn.class);
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


    protected <U> QueryResponse<U> execute(String action, Query query, QueryOptions queryOptions, Class<U> clazz) throws IOException {
        return  execute(action, query, queryOptions, clazz, false);
    }

    protected <U> QueryResponse<U> execute(String action, Query query, QueryOptions queryOptions, Class<U> clazz,
                                           boolean post) throws IOException {
        if (query != null && queryOptions != null) {
            queryOptions.putAll(query);
        }
        return execute("", action, queryOptions, clazz, post);
    }

    protected <U> QueryResponse<U> execute(String ids, String resource, QueryOptions queryOptions, Class<U> clazz)
            throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, queryOptions, clazz, false);
    }

    protected <U> QueryResponse<U> execute(String ids, String resource, QueryOptions queryOptions, Class<U> clazz,
                                           boolean post) throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, queryOptions, clazz, post);
    }

    protected <U> QueryResponse<U> execute(List<String> idList, String resource, QueryOptions options, Class<U> clazz) throws IOException {
        return execute(idList, resource, options, clazz, false);
    }

    protected <U> QueryResponse<U> execute(List<String> idList, String resource, QueryOptions options, Class<U> clazz,
                                           boolean post) throws IOException {

        if (idList == null || idList.isEmpty()) {
            return new QueryResponse<>();
        }

        // If the list contain less than REST_CALL_BATCH_SIZE variants then we can make a normal REST call.
        if (idList.size() <= REST_CALL_BATCH_SIZE) {
            return fetchData(idList, resource, options, clazz, post);
        }

        // But if there are more than REST_CALL_BATCH_SIZE variants then we launch several threads to increase performance.
        int numThreads = (options != null)
                ? options.getInt("numThreads", DEFAULT_NUM_THREADS)
                : DEFAULT_NUM_THREADS;

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<QueryResponse<U>>> futureList = new ArrayList<>((idList.size() / REST_CALL_BATCH_SIZE) + 1);
        for (int i = 0; i < idList.size(); i += REST_CALL_BATCH_SIZE) {
            final int from = i;
            final int to = (from + REST_CALL_BATCH_SIZE > idList.size())
                    ? idList.size()
                    : from + REST_CALL_BATCH_SIZE;
            futureList.add(executorService.submit(() ->
                    fetchData(idList.subList(from, to), resource, options, clazz, post)
            ));
        }

        List<QueryResult<U>> queryResults = new ArrayList<>(idList.size());
        for (Future<QueryResponse<U>> responseFuture : futureList) {
            try {
                while (!responseFuture.isDone()) {
                    Thread.sleep(5);
                }
                queryResults.addAll(responseFuture.get().getResponse());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        QueryResponse<U> finalResponse = new QueryResponse<>();
        finalResponse.setResponse(queryResults);
        executorService.shutdown();

        return finalResponse;
    }

    private <U> QueryResponse<U> fetchData(List<String> idList, String resource, QueryOptions options, Class<U> clazz,
                                           boolean post) throws IOException {

        if (options == null) {
            options = new QueryOptions();
        }
        options.putIfAbsent("limit", LIMIT);

        Map<String, Integer> idMap = new HashMap<>();
        List<String> prevIdList = idList;
        List<String> newIdsList = null;
        boolean call = true;
        int skip = 0;
        QueryResponse<U> queryResponse = null;
        QueryResponse<U> finalQueryResponse = null;
        while (call) {
            queryResponse = robustRestCall(idList, resource, options, clazz, post);

            // First iteration we set the response object, no merge needed
            // Create id -> finalQueryResponse-position map, so that we can know in forthcoming iterations where to
            // save corresponding lists of query results
            if (finalQueryResponse == null) {
                finalQueryResponse = queryResponse;
                idMap = new HashMap<>();
                // WARN: assuming the order of QueryResults in queryResponse corresponds to the order of ids in idList
                // i.e. queryResponse[0] contains queryResult for idList[0], queryResponse[1] for idList[1], etc.
                for (int i = 0; i < idList.size(); i++) {
                    idMap.put(idList.get(i), i);
                }
            } else {    // merge query responses
//                if (newIdsList != null && newIdsList.size() > 0) {
                if (newIdsList.size() > 0) {
                    for (int i = 0; i < newIdsList.size(); i++) {
                        finalQueryResponse.getResponse().get(idMap.get(newIdsList.get(i))).getResult()
                                .addAll(queryResponse.getResponse().get(i).getResult());
                    }
                }
            }

            // check if we need to call again
            if (newIdsList != null) {
                prevIdList = newIdsList;
            }
            newIdsList = new ArrayList<>();
            for (int i = 0; i < queryResponse.getResponse().size(); i++) {
                if (queryResponse.getResponse().get(i).getNumResults() == LIMIT) {
                    newIdsList.add(prevIdList.get(i));
                }
            }

            if (newIdsList.isEmpty()) {
                // this breaks the while condition
                call = false;
            } else {
                idList = newIdsList;
                skip += LIMIT;
                options.put("skip", skip);
            }
        }

        logger.debug("queryResponse = " + queryResponse);
        return finalQueryResponse;
    }

    private <U> QueryResponse<U> robustRestCall(List<String> idList, String resource, QueryOptions queryOptions,
                                                Class<U> clazz, boolean post)
            throws IOException {

        String ids = "";
        if (idList == null) {
            idList = Collections.emptyList();
        }
        if (!idList.isEmpty()) {
            ids = StringUtils.join(idList, ',');
        }

        boolean queryError = false;
        QueryResponse<U> queryResponse;
        try {
            queryResponse = restCall(configuration.getRest().getHosts(), configuration.getVersion(),
                    ids, resource, queryOptions, clazz, post);
            if (queryResponse == null) {
                logger.warn("CellBase REST fail. Returned null for ids {}. hosts: {}, version: {}, "
                                + "category: {}, subcategory: {}, resource: {}, queryOptions: {}",
                        ids, StringUtils.join(configuration.getRest().getHosts(), ","), configuration.getVersion(),
                        category, subcategory, resource, queryOptions.toJson());
                queryError = true;
            }
        } catch (JsonProcessingException | javax.ws.rs.ProcessingException | WebApplicationException e) {
            logger.warn("CellBase REST fail. Error parsing query result for ids {}. hosts: {}, version: {}, "
                            + "category: {}, subcategory: {}, resource: {}, queryOptions: {}. Exception message: {}",
                    ids, StringUtils.join(configuration.getRest().getHosts(), ","), configuration.getVersion(),
                    category, subcategory, resource, queryOptions.toJson(), e.getMessage());
            logger.debug("CellBase REST exception.", e);
            queryError = true;
            queryResponse = null;
            if (e instanceof WebApplicationException) {
                Response.Status status = Response.Status.fromStatusCode(((WebApplicationException) e).getResponse().getStatus());
                switch (status) {
                    case GATEWAY_TIMEOUT:
                    case INTERNAL_SERVER_ERROR:
                        // Do not propagate this error
                        // TODO: Add a counter?
                        break;
                    default:
                        throw e;
                }
            }
        }

        if (queryResponse != null && queryResponse.getResponse().size() != idList.size()) {
            logger.warn("QueryResponse size (" + queryResponse.getResponse().size() + ") != id list size ("
                    + idList.size() + ").");
        }

        if (queryError) {
            if (idList.size() == 1) {
                logger.warn("CellBase REST warning. Skipping id. {}", idList.get(0));
                return new QueryResponse<U>(configuration.getVersion(), -1, null,
                        "CellBase REST error. Skipping id " + idList.get(0), queryOptions,
                        Collections.singletonList(new QueryResult<U>(idList.get(0), -1, 0, 0, null, null,
                                Collections.emptyList())));
            }

            List<QueryResult<U>> queryResultList = new LinkedList<>();
            queryResponse = new QueryResponse<U>(configuration.getVersion(), -1, null, null, queryOptions,
                    queryResultList);
            logger.info("Re-attempting to solve the query - trying to identify any problematic id to skip it");
            List<String> idList1 = idList.subList(0, idList.size() / 2);
            if (!idList1.isEmpty()) {
                queryResultList.addAll(robustRestCall(idList1, resource, queryOptions, clazz, post).getResponse());
            }
            List<String> idList2 = idList.subList(idList.size() / 2, idList.size());
            if (!idList2.isEmpty()) {
                queryResultList.addAll(robustRestCall(idList2, resource, queryOptions, clazz, post).getResponse());
            }
        }
        return queryResponse;
    }

    private <U> QueryResponse<U> restCall(List<String> hosts, String version, String ids, String resource, QueryOptions queryOptions,
                                          Class<U> clazz, boolean post) throws IOException {

        WebTarget path = getBaseUrl(hosts, version);

        WebTarget callUrl = path;
        if (ids != null && !ids.isEmpty() && !post) {
            callUrl = path.path(ids);
        }

        // Add the last URL part, the 'action' or 'resource'
        callUrl = callUrl.path(resource);

        if (queryOptions != null) {
            for (String s : queryOptions.keySet()) {
                callUrl = callUrl.queryParam(s, queryOptions.get(s));
            }
            if (assembly != null && StringUtils.isEmpty(queryOptions.getString("assembly"))) {
                callUrl = callUrl.queryParam("assembly", assembly);
            }
        } else {
            if (assembly != null) {
                callUrl = callUrl.queryParam("assembly", assembly);
            }
        }

        String jsonString;
        if (post) {
            logger.debug("Making POST call to REST URL: {}", callUrl.getUri().toURL());
            jsonString = callUrl.request().post(Entity.text(ids), String.class);
        } else {
            logger.debug("Making GET call to REST URL: {}", callUrl.getUri().toURL());
            jsonString = callUrl.request().get(String.class);
        }

        return parseResult(jsonString, clazz);
    }

    protected WebTarget getBaseUrl(List<String> hosts, String version) {
        return client
                    .target(URI.create(hosts.get(0)))
                    .path(WEBSERVICES)
                    .path(REST)
                    .path(version)
                    .path(species)
                    .path(category)
                    .path(subcategory);
    }

    private static <U> QueryResponse<U> parseResult(String json, Class<U> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(QueryResponse.class, QueryResult.class, clazz));
        return reader.readValue(json);
    }

}
