package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.variant.protobuf.VariantProto;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.VariantServiceGrpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 17/08/16.
 */
public class VariationGrpcClient extends ParentGrpcClient {

    private VariantServiceGrpc.VariantServiceBlockingStub variantServiceBlockingStub;

    public VariationGrpcClient(ManagedChannel channel) {
        super(channel);
        variantServiceBlockingStub = VariantServiceGrpc.newBlockingStub(channel);
    }

    public Long count(Map<String, String> query) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return variantServiceBlockingStub.count(request).getValue();
    }

    public VariantProto.Variant first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return variantServiceBlockingStub.first(request);
    }

    public Iterator<VariantProto.Variant> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return variantServiceBlockingStub.get(request);
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return variantServiceBlockingStub.distinct(request);
    }
}
