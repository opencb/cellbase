package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.common.Species;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 06/07/17.
 */
public class MetaClient extends ParentRestClient<Map> {

    private static final String META = "meta";

    public MetaClient(String species, String assembly, ClientConfiguration clientConfiguration) {
        super(species, assembly, clientConfiguration);

        this.clazz = Map.class;

        this.category = META;
        this.subcategory = null;
    }

    public QueryResponse<Map> about() throws IOException {
        return execute("about", new Query(),  new QueryOptions(), Map.class);
    }

    public QueryResponse<Species> species() throws IOException {
        return species(species);
    }

    public QueryResponse<Species> species(String species) throws IOException {
        return execute("species", new Query(),  new QueryOptions(), Species.class);
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
