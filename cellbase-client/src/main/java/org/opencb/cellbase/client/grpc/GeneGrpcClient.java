package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.core.grpc.service.GeneServiceGrpc;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;

import java.util.HashMap;
import java.util.Iterator;
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
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return geneServiceBlockingStub.count(request).getValue();
    }

    public GeneModel.Gene first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.first(request);
    }

    public Iterator<GeneModel.Gene> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.get(request);
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.distinct(request);
    }

    public Iterator<TranscriptModel.Transcript> getTranscripts(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.getTranscripts(request);
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> getRegulatoryRegions(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.getRegulatoryRegions(request);
    }

    public Iterator<TranscriptModel.TranscriptTfbs> getTranscriptTfbs(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return geneServiceBlockingStub.getTranscriptTfbs(request);
    }


}
