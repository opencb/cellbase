package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.RegulatoryRegionServiceGrpc;

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
        ServiceTypesModel.LongResponse count = regulatoryRegionServiceBlockingStub.count(request);
        return count.getValue();
    }

    public RegulatoryRegionModel.RegulatoryRegion first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        RegulatoryRegionModel.RegulatoryRegion regulatoryRegion = regulatoryRegionServiceBlockingStub.first(request);
        return regulatoryRegion;
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<RegulatoryRegionModel.RegulatoryRegion> regulatoryRegionIterator = regulatoryRegionServiceBlockingStub.get(request);
        return regulatoryRegionIterator;
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        ServiceTypesModel.StringArrayResponse values = regulatoryRegionServiceBlockingStub.distinct(request);
        return values;
    }

}
