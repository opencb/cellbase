package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Created by fjlopez on 07/07/17.
 */
public class GenericClient extends ParentRestClient<ObjectMap> {

    public GenericClient(String species, String assembly, ClientConfiguration clientConfiguration) {
        super(species, assembly, clientConfiguration);
    }

    public <T> QueryResponse<T> get(String category, String resource, QueryOptions queryOptions,
                                   Class<T> clazz) throws IOException {
        return this.get(category, null, EMPTY_STRING, resource, queryOptions, clazz);
    }

    public <T> QueryResponse<T> get(String category, String subcategory, String ids, String resource,
                                    QueryOptions queryOptions, Class<T> clazz) throws IOException {
        this.category = category;
        this.subcategory = subcategory;
        return execute(ids, resource, queryOptions, clazz);
    }

    @Override
    protected WebTarget getBaseUrl(List<String> hosts, String version) {
         WebTarget webTarget = client
                .target(URI.create(hosts.get(0)))
                .path(WEBSERVICES)
                .path(REST)
                .path(version);

         if (!META.equals(category)) {
             webTarget = webTarget
                     .path(species)
                     .path(category)
                     .path(subcategory);
         } else {
             webTarget = webTarget
                     .path(category);
         }

         return webTarget;
    }

}
