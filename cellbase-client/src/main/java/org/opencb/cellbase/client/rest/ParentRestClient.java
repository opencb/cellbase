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

package org.opencb.cellbase.client.rest;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.DrugResponseClassification;
import org.opencb.biodata.models.variant.avro.GeneCancerAssociation;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.models.mixin.DrugResponseClassificationMixIn;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Event;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.utils.VersionUtils;
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

import static org.opencb.cellbase.core.ParamConstants.*;

/**
 * Created by imedina on 12/05/16.
 */
public class ParentRestClient<T> {

    protected final String species;
    protected final String assembly;
    protected final String dataRelease;
    protected final String apiKey;
    protected final Client client;

    // TODO: Should this be final?
    protected String category;
    protected String subcategory;

    protected Class<T> clazz;

    protected final ClientConfiguration configuration;

    protected static ObjectMapper jsonObjectMapper;
    protected final Logger logger;

    public static final int LIMIT = 10;
    public static final int REST_CALL_BATCH_SIZE = 200;
    public static final int DEFAULT_NUM_THREADS = 4;

    protected static final String EMPTY_STRING = "";
    protected static final String META = "meta";
    protected static final String WEBSERVICES = "webservices";
    protected static final String REST = "rest";
    private String apiKeyParam;
    private String serverVersion;

    @Deprecated
    ParentRestClient(ClientConfiguration configuration) {
        this(configuration.getDefaultSpecies(), null, null, null, configuration);
    }

    @Deprecated
    ParentRestClient(String species, String assembly, ClientConfiguration configuration) {
        this(species, assembly, null, null, configuration);
    }

    ParentRestClient(String species, String assembly, String dataRelease, String apiKey, ClientConfiguration configuration) {
        this.species = species;
        this.assembly = assembly;
        this.dataRelease = dataRelease;
        this.apiKey = apiKey;
        this.configuration = configuration;
        logger = LoggerFactory.getLogger(this.getClass().toString());

        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        client.property(ClientProperties.READ_TIMEOUT, configuration.getRest().getTimeout());

        logger.debug("Configure read timeout : " + configuration.getRest().getTimeout() + "ms");
    }

    public String getSpecies() {
        return species;
    }

    public String getAssembly() {
        return assembly;
    }

    public String getDataRelease() {
        return dataRelease;
    }

    public String getApiKey() {
        return apiKey;
    }

    static {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonObjectMapper.addMixIn(DrugResponseClassification.class, DrugResponseClassificationMixIn.class);
        jsonObjectMapper.addMixIn(CellBaseDataResponse.class, CellBaseDataResponseMixIn.class);
        jsonObjectMapper.addMixIn(CellBaseDataResult.class, CellBaseDataResultMixIn.class);
        jsonObjectMapper.addMixIn(VariantAnnotation.class, VariantAnnotationMixin.class);
        jsonObjectMapper.addMixIn(ConsequenceType.class, ConsequenceTypeMixin.class);

    }

    // These methods keep backwark compatability with CellBase 4.x
    public interface CellBaseDataResponseMixIn<T> {
        @JsonAlias("response")
        List<CellBaseDataResult<T>> getResponses();
    }

    public interface CellBaseDataResultMixIn<T> {
        @JsonAlias("result")
        List<T> getResults();
    }

    public interface VariantAnnotationMixin {

        @JsonAlias("cancerGeneAssociations")
        List<GeneCancerAssociation> getGeneCancerAssociations();
    }

    @JsonDeserialize(converter = ConsequenceTypeMixin.ConsequenceTypeConverter.class)
    public interface ConsequenceTypeMixin {
        class ConsequenceTypeConverter extends StdConverter<ConsequenceType, ConsequenceType> {
            @Override
            public ConsequenceType convert(ConsequenceType consequenceType) {
                if (consequenceType != null) {
                    if (consequenceType.getGeneId() == null) {
                        consequenceType.setGeneId(consequenceType.getEnsemblGeneId());
                    }
                    if (consequenceType.getTranscriptId() == null) {
                        consequenceType.setTranscriptId(consequenceType.getEnsemblTranscriptId());
                    }
                }
                return consequenceType;
            }
        }

        @JsonAlias("transcriptAnnotationFlags")
        List<String> getTranscriptFlags();

        @JsonIgnore
        List<String> getTranscriptAnnotationFlags();
    }

// end points have been removed
//    public CellBaseDataResponse<Long> count(Query query) throws IOException {
//        QueryOptions queryOptions = new QueryOptions();
//        queryOptions.put("count", true);
//        return execute("count", query, queryOptions, Long.class);
//    }
//
//    public CellBaseDataResponse<T> first() throws IOException {
//        return execute("first", new Query(), new QueryOptions(), clazz);
//    }

    public CellBaseDataResponse<T> get(List<String> id, QueryOptions queryOptions) throws IOException {
        return execute(id, "info", queryOptions, clazz);
    }


    protected <U> CellBaseDataResponse<U> execute(String action, Query query, QueryOptions queryOptions,
                                                  Class<U> clazz) throws IOException {
        return  execute(action, query, queryOptions, clazz, false);
    }

    protected <U> CellBaseDataResponse<U> execute(String action, Query query, QueryOptions queryOptions, Class<U> clazz,
                                           boolean post) throws IOException {
        if (query != null && queryOptions != null) {
            queryOptions.putAll(query);
        }
        return execute("", action, queryOptions, clazz, post);
    }

    protected <U> CellBaseDataResponse<U> execute(String ids, String resource, QueryOptions queryOptions, Class<U> clazz)
            throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, queryOptions, clazz, false);
    }

    protected <U> CellBaseDataResponse<U> execute(String ids, String resource, QueryOptions queryOptions, Class<U> clazz,
                                           boolean post) throws IOException {
        return execute(Arrays.asList(ids.split(",")), resource, queryOptions, clazz, post);
    }

    protected <U> CellBaseDataResponse<U> execute(List<String> idList, String resource, QueryOptions options,
                                                  Class<U> clazz) throws IOException {
        return execute(idList, resource, options, clazz, false);
    }

    protected <U> CellBaseDataResponse<U> execute(List<String> idList, String resource, QueryOptions options, Class<U> clazz,
                                           boolean post) throws IOException {

        if (idList == null || idList.isEmpty()) {
            return new CellBaseDataResponse<>();
        }

        lazyInit();

        // If the list contain less than REST_CALL_BATCH_SIZE variants then we can make a normal REST call.
        if (idList.size() <= REST_CALL_BATCH_SIZE) {
            return fetchData(idList, resource, options, clazz, post);
        }

        // But if there are more than REST_CALL_BATCH_SIZE variants then we launch several threads to increase performance.
        int numThreads = (options != null)
                ? options.getInt("numThreads", DEFAULT_NUM_THREADS)
                : DEFAULT_NUM_THREADS;

        // TODO: Use cached thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<CellBaseDataResponse<U>>> futureList = new ArrayList<>((idList.size() / REST_CALL_BATCH_SIZE) + 1);
        for (int i = 0; i < idList.size(); i += REST_CALL_BATCH_SIZE) {
            final int from = i;
            final int to = (from + REST_CALL_BATCH_SIZE > idList.size())
                    ? idList.size()
                    : from + REST_CALL_BATCH_SIZE;
            futureList.add(executorService.submit(() ->
                    fetchData(idList.subList(from, to), resource, options, clazz, post)
            ));
        }

        List<CellBaseDataResult<U>> cellBaseDataResults = new ArrayList<>(idList.size());
        for (Future<CellBaseDataResponse<U>> responseFuture : futureList) {
            try {
                while (!responseFuture.isDone()) {
                    Thread.sleep(5);
                }
                cellBaseDataResults.addAll(responseFuture.get().getResponses());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            } catch (ExecutionException e) {
                throw new IOException(e);
            }
        }

        CellBaseDataResponse<U> finalResponse = new CellBaseDataResponse<>();
        finalResponse.setResponses(cellBaseDataResults);
        executorService.shutdown();

        return finalResponse;
    }

    private <U> CellBaseDataResponse<U> fetchData(List<String> idList, String resource, QueryOptions options, Class<U> clazz,
                                           boolean post) throws IOException {

        if (options == null) {
            options = new QueryOptions();
        }
        int limit;
        if (options.containsKey(QueryOptions.LIMIT)) {
            limit = options.getInt(QueryOptions.LIMIT);
        } else {
            limit = LIMIT;
            // Do not modify input QueryOptions!
            options = new QueryOptions(options);
            options.put(QueryOptions.LIMIT, limit);
        }

        Map<String, Integer> idMap = new HashMap<>();
        List<String> prevIdList = idList;
        List<String> newIdsList = null;
        boolean call = true;
        int skip = 0;
        CellBaseDataResponse<U> finalDataResponse = null;
        while (call) {
            CellBaseDataResponse<U> queryResponse = robustRestCall(idList, resource, options, clazz, post);

            // First iteration we set the response object, no merge needed
            // Create id -> finalDataResponse-position map, so that we can know in forthcoming iterations where to
            // save corresponding lists of query results
            if (finalDataResponse == null) {
                finalDataResponse = queryResponse;
                idMap = new HashMap<>();
                // WARN: assuming the order of CellBaseDataResults in queryResponse corresponds to the order of ids in idList
                // i.e. queryResponse[0] contains CellBaseDataResult for idList[0], queryResponse[1] for idList[1], etc.
                for (int i = 0; i < idList.size(); i++) {
                    idMap.put(idList.get(i), i);
                }
            } else {    // merge query responses
//                if (newIdsList != null && newIdsList.size() > 0) {
                if (newIdsList.size() > 0) {
                    for (int i = 0; i < newIdsList.size(); i++) {
                        finalDataResponse.getResponses().get(idMap.get(newIdsList.get(i))).getResults()
                                .addAll(queryResponse.getResponses().get(i).getResults());
                    }
                }
            }

            // check if we need to call again
            if (newIdsList != null) {
                prevIdList = newIdsList;
            }
            newIdsList = new ArrayList<>();
            if (queryResponse.getResponses() != null) {
                for (int i = 0; i < queryResponse.getResponses().size(); i++) {
                    if (queryResponse.getResponses().get(i).getNumResults() == limit) {
                        newIdsList.add(prevIdList.get(i));
                    }
                }
            }

            if (newIdsList.isEmpty()) {
                // this breaks the while condition
                call = false;
            } else {
                idList = newIdsList;
                skip += limit;
                options.put("skip", skip);
            }
        }
        logger.debug("queryResponse: {"
                + "time: " + finalDataResponse.getTime() + ", "
                + "apiVersion: " + finalDataResponse.getApiVersion() + ", "
                + "responses: " + finalDataResponse.getResponses().size() + ", "
                + "events: " + finalDataResponse.getEvents() + "}");

        return finalDataResponse;
    }

    private <U> CellBaseDataResponse<U> robustRestCall(List<String> idList, String resource, QueryOptions queryOptions,
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
        CellBaseDataResponse<U> queryResponse;
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
                    case BAD_GATEWAY:
                    case INTERNAL_SERVER_ERROR:
                        // Do not propagate this error
                        // TODO: Add a counter?
                        break;
                    default:
                        throw e;
                }
            }
        }

        if (queryResponse != null && queryResponse.getResponses() != null && queryResponse.getResponses().size() != idList.size()) {
            logger.warn("DataResponse size (" + queryResponse.getResponses().size() + ") != id list size ("
                    + idList.size() + ").");
        }

        if (queryError) {
            if (idList.size() == 1) {
                logger.warn("CellBase REST warning. Skipping id. {}", idList.get(0));
                Event event = new Event(Event.Type.ERROR, "CellBase REST error. Skipping id " + idList.get(0));
                CellBaseDataResult result = new CellBaseDataResult<U>(idList.get(0), 0, Collections.emptyList(), 0, null, 0);
                return new CellBaseDataResponse<U>(configuration.getVersion(), 0, getApiKey(), 0, Collections.singletonList(event),
                        new ObjectMap(queryOptions), Collections.singletonList(result));
            }
            List<CellBaseDataResult<U>> cellBaseDataResultList = new LinkedList<>();
            queryResponse = new CellBaseDataResponse<U>(configuration.getVersion(), 0, getApiKey(), -1, null, queryOptions,
                    cellBaseDataResultList);
            logger.info("Re-attempting to solve the query - trying to identify any problematic id to skip it");
            List<String> idList1 = idList.subList(0, idList.size() / 2);
            if (!idList1.isEmpty()) {
                cellBaseDataResultList.addAll(robustRestCall(idList1, resource, queryOptions, clazz, post).getResponses());
            }
            List<String> idList2 = idList.subList(idList.size() / 2, idList.size());
            if (!idList2.isEmpty()) {
                cellBaseDataResultList.addAll(robustRestCall(idList2, resource, queryOptions, clazz, post).getResponses());
            }
        }
        return queryResponse;
    }

    private <U> CellBaseDataResponse<U> restCall(List<String> hosts, String version, String ids, String resource, QueryOptions queryOptions,
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
            if (dataRelease != null && StringUtils.isEmpty(queryOptions.getString(DATA_RELEASE_PARAM))) {
                callUrl = callUrl.queryParam(DATA_RELEASE_PARAM, dataRelease);
            }
            if (apiKeyParam != null && apiKey != null && StringUtils.isEmpty(queryOptions.getString(apiKeyParam))) {
                callUrl = callUrl.queryParam(apiKeyParam, apiKey);
            }
        } else {
            if (assembly != null) {
                callUrl = callUrl.queryParam("assembly", assembly);
            }
            if (dataRelease != null) {
                callUrl = callUrl.queryParam(DATA_RELEASE_PARAM, dataRelease);
            }
            if (apiKeyParam != null && apiKey != null) {
                callUrl = callUrl.queryParam(apiKeyParam, apiKey);
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

    private void lazyInit() throws IOException {
        // Lazy init other variables.
        if (apiKey != null) {
            String serverVersion = getServerVersion();

            // Attention: parameter 'token' was renamed to 'apiKey' in CelBase v5.7
            if (VersionUtils.isMinVersion("5.7.0", serverVersion)) {
                apiKeyParam = API_KEY_PARAM;
            } else if (VersionUtils.isMinVersion("5.4.0", serverVersion)) {
                apiKeyParam = TOKEN_PARAM;
            } // else { throw exception, api key not supported } ???
        }
    }

    /**
     * Get CellBase server version.
     * @return CellBase full version
     * @throws IOException on IOException
     */
    public String getServerVersion() throws IOException {
        if (serverVersion == null) {
            initServerVersion();
        }
        return serverVersion;
    }

    private synchronized void initServerVersion() throws IOException {
        if (serverVersion == null) {
            ObjectMap about = new MetaClient(species, assembly, dataRelease, null, configuration).about().firstResult();
            serverVersion = about.getString("Version");
        }
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

    private static <U> CellBaseDataResponse<U> parseResult(String json, Class<U> clazz) throws IOException {
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructParametrizedType(CellBaseDataResponse.class,
                        CellBaseDataResult.class, clazz));
        return reader.readValue(json);
    }

}
