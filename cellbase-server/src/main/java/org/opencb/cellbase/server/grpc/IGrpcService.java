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

package org.opencb.cellbase.server.grpc;

import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

/**
 * Created by imedina on 09/08/16.
 */
public interface IGrpcService {


    default Query createQuery(GenericServiceModel.Request request) {
        Query query = new Query();
        request.getQueryMap().keySet().stream()
                .filter(key -> request.getQueryMap().get(key) != null)
                .forEach(key -> {
                    query.put(key, request.getQueryMap().get(key));
                });
        return query;
    }

    default QueryOptions createQueryOptions(GenericServiceModel.Request request) {
        QueryOptions queryOptions = new QueryOptions();
        request.getOptionsMap().keySet().stream()
                .filter(key -> request.getOptionsMap().get(key) != null)
                .forEach(key -> {
                    queryOptions.put(key, request.getOptionsMap().get(key));
                });
        return queryOptions;
    }
}
