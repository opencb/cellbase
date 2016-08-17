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
        ServiceTypesModel.LongResponse count = variantServiceBlockingStub.count(request);
        return count.getValue();
    }

    public VariantProto.Variant first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        VariantProto.Variant variant = variantServiceBlockingStub.first(request);
        return variant;
    }

    public Iterator<VariantProto.Variant> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<VariantProto.Variant> variantIterator = variantServiceBlockingStub.get(request);
        return variantIterator;
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        ServiceTypesModel.StringArrayResponse values = variantServiceBlockingStub.distinct(request);
        return values;
    }
}
