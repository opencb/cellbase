package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.variant.protobuf.VariantAnnotationProto;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.VariantAnnotationServiceGrpc;

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
        Iterator<VariantAnnotationProto.VariantAnnotation> variantAnnotationIterator = stub.get(request);
        return variantAnnotationIterator;
    }

    public Iterator<VariantAnnotationProto.Score> getCadd(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        Iterator<VariantAnnotationProto.Score> scoreIterator = stub.getCadd(request);
        return scoreIterator;
    }
}
