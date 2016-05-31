package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.server.grpc.service.GeneServiceGrpc;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;

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
        ServiceTypesModel.LongResponse count = geneServiceBlockingStub.count(request);
        return count.getValue();
    }

    public GeneModel.Gene first(Map<String, String> query, Map<String, String> queryOptions)  {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        GeneModel.Gene firstGene = geneServiceBlockingStub.first(request);
        return firstGene;
    }

    public Iterator<GeneModel.Gene> get(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<GeneModel.Gene> genes = geneServiceBlockingStub.get(request);
        return genes;
    }

    public ServiceTypesModel.StringArrayResponse distinct(Map<String, String> query, String field) {
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("distinct", field);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        ServiceTypesModel.StringArrayResponse values = geneServiceBlockingStub.distinct(request);
        return values;
    }

    public Iterator<TranscriptModel.Transcript> getTranscripts(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<TranscriptModel.Transcript> transcriptIterator = geneServiceBlockingStub.getTranscripts(request);
        return transcriptIterator;
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> getRegulatoryRegions(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<RegulatoryRegionModel.RegulatoryRegion> regulationIterator = geneServiceBlockingStub.getRegulatoryRegions(request);
        return regulationIterator;
    }

    public Iterator<TranscriptModel.TranscriptTfbs> getTranscriptTfbs(String id, Map<String, String> queryOptions) {
        Map<String, String> query = new HashMap<>();
        query.put("id", id);
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        Iterator<TranscriptModel.TranscriptTfbs> transcriptTfbsIterator = geneServiceBlockingStub.getTranscriptTfbs(request);
        return transcriptTfbsIterator;
    }


}
