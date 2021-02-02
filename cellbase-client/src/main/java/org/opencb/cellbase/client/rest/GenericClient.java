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

import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;


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

    public <T> CellBaseDataResponse<T> get(String category, String resource, QueryOptions queryOptions,
                                   Class<T> clazz) throws IOException {
        return this.get(category, null, EMPTY_STRING, resource, queryOptions, clazz);
    }

    public <T> CellBaseDataResponse<T> get(String category, String subcategory, String ids, String resource,
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
