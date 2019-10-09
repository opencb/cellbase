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
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.GenomicRegionServiceGrpc;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 19/08/16.
 */

public class GenomicRegionGrpcClient extends ParentGrpcClient {

    private GenomicRegionServiceGrpc.GenomicRegionServiceBlockingStub genomicRegionServiceBlockingStub;

    public GenomicRegionGrpcClient(ManagedChannel channel) {
        super(channel);
    }

    public Iterator<GeneModel.Gene> getGene(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getGene(request);
    }

    public Iterator<TranscriptModel.Transcript> getTranscript(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getTranscript(request);
    }

    public ServiceTypesModel.StringResponse getSequence(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getSequence(request);
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> getRegulatoryRegion(Map<String, String> query,
                                                                                Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getRegulatoryRegion(request);
    }
}
