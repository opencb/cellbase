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
