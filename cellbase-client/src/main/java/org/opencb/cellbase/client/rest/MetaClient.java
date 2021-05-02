package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.config.SpeciesProperties;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Created by fjlopez on 06/07/17.
 */
public class MetaClient extends ParentRestClient<ObjectMap> {

    public MetaClient(String species, String assembly, ClientConfiguration clientConfiguration) {
        super(species, assembly, clientConfiguration);

        this.clazz = ObjectMap.class;

        this.category = META;
        this.subcategory = null;
    }

    public QueryResponse<ObjectMap> about() throws IOException {
        return execute("about", new Query(),  new QueryOptions(), ObjectMap.class);
    }

    public QueryResponse<SpeciesProperties> species() throws IOException {
        return species(species);
    }

    public QueryResponse<SpeciesProperties> species(String species) throws IOException {
        return execute("species", new Query(),  new QueryOptions(), SpeciesProperties.class);
    }

    public QueryResponse<ObjectMap> versions() throws IOException {
        return versions(species);
    }

    public QueryResponse<ObjectMap> versions(String species) throws IOException {
        return execute(species, "versions", new QueryOptions(), ObjectMap.class);
    }

    @Override
    protected WebTarget getBaseUrl(List<String> hosts, String version) {
        return client
                .target(URI.create(hosts.get(0)))
                .path(WEBSERVICES)
                .path(REST)
                .path(version)
                .path(category);
    }

}
