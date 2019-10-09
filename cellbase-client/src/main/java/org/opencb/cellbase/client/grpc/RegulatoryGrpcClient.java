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

package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.RegulatoryRegionServiceGrpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 17/08/16.
 */
public class RegulatoryGrpcClient extends ParentGrpcClient {

    private RegulatoryRegionServiceGrpc.RegulatoryRegionServiceBlockingStub regulatoryRegionServiceBlockingStub;

    public RegulatoryGrpcClient(ManagedChannel channel) {
        super(channel);
        regulatoryRegionServiceBlockingStub = RegulatoryRegionServiceGrpc.newBlockingStub(channel);
    }

    public Long count(Map<String, String> query) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return regulatoryRegionServiceBlockingStub.count(request).getValue();
    }

    public RegulatoryRegionModel.RegulatoryRegion first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return regulatoryRegionServiceBlockingStub.first(request);
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return regulatoryRegionServiceBlockingStub.get(request);
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return regulatoryRegionServiceBlockingStub.distinct(request);
    }

}
