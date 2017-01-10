package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.GenomicRegionServiceGrpc;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by swaathi on 19/08/16.
 */

public class GenomicRegionGrpcClient extends ParentGrpcClient {

    private GenomicRegionServiceGrpc.GenomicRegionServiceBlockingStub genomicRegionServiceBlockingStub;

    public GenomicRegionGrpcClient(ManagedChannel channel) {
        super(channel);
    }

    public Iterator<GeneModel.Gene> getGene(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getGene(request);
    }

    public Iterator<TranscriptModel.Transcript> getTranscript(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getTranscript(request);
    }

    public ServiceTypesModel.StringResponse getSequence(Map<String, String> query, Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getSequence(request);
    }

    public Iterator<RegulatoryRegionModel.RegulatoryRegion> getRegulatoryRegion(Map<String, String> query,
                                                                                Map<String, String> queryOptions) {
        GenericServiceModel.Request request = buildRequest(query, queryOptions);
        return genomicRegionServiceBlockingStub.getRegulatoryRegion(request);
    }
}
