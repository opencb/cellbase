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
import org.opencb.biodata.models.variant.protobuf.VariantAnnotationProto;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.VariantAnnotationServiceGrpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 17/08/16.
 */
public class VariantAnnotationGrpcClient extends ParentGrpcClient {

    private VariantAnnotationServiceGrpc.VariantAnnotationServiceBlockingStub stub;

    public VariantAnnotationGrpcClient(ManagedChannel channel) {
        super(channel);
        stub = VariantAnnotationServiceGrpc.newBlockingStub(channel);
    }

    public Iterator<VariantAnnotationProto.VariantAnnotation> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return stub.get(request);
    }

    public Iterator<VariantAnnotationProto.Score> getCadd(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return stub.getCadd(request);
    }
}
