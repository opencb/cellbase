package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.TranscriptServiceGrpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 17/08/16.
 */
public class TranscriptGrpcClient extends ParentGrpcClient {

    private TranscriptServiceGrpc.TranscriptServiceBlockingStub transcriptServiceBlockingStub;

    public TranscriptGrpcClient(ManagedChannel channel) {
        super(channel);
        transcriptServiceBlockingStub = TranscriptServiceGrpc.newBlockingStub(channel);
    }

    public Long count(Map<String, String> query) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return transcriptServiceBlockingStub.count(request).getValue();
    }

    public TranscriptModel.Transcript first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return transcriptServiceBlockingStub.first(request);
    }

    public Iterator<TranscriptModel.Transcript> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return transcriptServiceBlockingStub.get(request);
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return transcriptServiceBlockingStub.distinct(request);
    }

    public ServiceTypesModel.StringResponse getSequence(Map<String, String> query) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        return transcriptServiceBlockingStub.getCdna(request);
    }
}
