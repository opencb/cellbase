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
