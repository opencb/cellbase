package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.cellbase.server.grpc.service.GeneServiceGrpc;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;

import java.util.Map;

/**
 * Created by swaathi on 27/05/16.
 */
public class GeneGrpcClient extends ParentGrpcClient {

    private GeneServiceGrpc.GeneServiceBlockingStub geneServiceBlockingStub;

    public GeneGrpcClient(ManagedChannel channel) {
        super(channel);
        geneServiceBlockingStub = GeneServiceGrpc.newBlockingStub(channel);
    }

    public Long count(Map<String, String> query) {
        GenericServiceModel.Request request = GenericServiceModel.Request.newBuilder()
                .setSpecies("hsapiens")
                .setAssembly("GRCh37")
                .putAllQuery(query)
                .build();
        ServiceTypesModel.LongResponse count = geneServiceBlockingStub.count(request);
        return count.getValue();
    }

}
