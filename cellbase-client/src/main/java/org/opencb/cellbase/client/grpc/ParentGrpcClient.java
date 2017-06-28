package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;

import java.util.Map;

/**
 * Created by swaathi on 27/05/16.
 */
public class ParentGrpcClient {

    protected ManagedChannel channel;

    public ParentGrpcClient(ManagedChannel channel) {
        this.channel = channel;
    }

    protected GenericServiceModel.Request buildRequest(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = GenericServiceModel.Request.newBuilder()
                .setSpecies("hsapiens")
                .setAssembly("GRCh37")
                .putAllQuery(query)
                .putAllOptions(queryOptions)
                .build();
        return request;
    }

}

