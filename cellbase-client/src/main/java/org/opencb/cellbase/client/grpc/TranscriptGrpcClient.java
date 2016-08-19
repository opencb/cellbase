package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.TranscriptServiceGrpc;

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
        ServiceTypesModel.LongResponse count = transcriptServiceBlockingStub.count(request);
        return count.getValue();
    }

    public TranscriptModel.Transcript first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        TranscriptModel.Transcript transcript = transcriptServiceBlockingStub.first(request);
        return transcript;
    }

    public Iterator<TranscriptModel.Transcript> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<TranscriptModel.Transcript> transcripts = transcriptServiceBlockingStub.get(request);
        return transcripts;
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        ServiceTypesModel.StringArrayResponse values = transcriptServiceBlockingStub.distinct(request);
        return values;
    }

    public ServiceTypesModel.StringResponse getSequence(Map<String, String> query) {
        GenericServiceModel.Request request = buildRequest(query, new HashMap<>());
        ServiceTypesModel.StringResponse sequence = transcriptServiceBlockingStub.getCdna(request);
        return sequence;
    }
}
