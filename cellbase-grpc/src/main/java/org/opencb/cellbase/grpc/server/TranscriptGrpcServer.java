package org.opencb.cellbase.grpc.server;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.grpc.service.GenericServiceModel;
import org.opencb.cellbase.grpc.service.TranscriptServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 18/12/15.
 */
public class TranscriptGrpcServer extends GenericGrpcServer implements TranscriptServiceGrpc.TranscriptService {

    @Override
    public void count(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.LongResponse> responseObserver) {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = transcriptDBAdaptor.count(query);
        Long value = Long.valueOf(queryResult.getResult().get(0).toString());
        ServiceTypesModel.LongResponse count = ServiceTypesModel.LongResponse.newBuilder()
                .setValue(value)
                .build();
        responseObserver.onNext(count);
        responseObserver.onCompleted();
    }

    @Override
    public void distinct(GenericServiceModel.Request request,
                         StreamObserver<ServiceTypesModel.StringArrayResponse> responseObserver) {

    }

    @Override
    public void first(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {

    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {

    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.GroupResponse> responseObserver) {

    }

//    @Override
//    public void getCdna(GenericServiceModel.CellbaseRequest request, StreamObserver<ServiceTypesModel.StringResponse> responseObserver) {
//        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());
//
//        Query query = createQuery(request);
//        QueryResult queryResult = transcriptDBAdaptor.getCdna(query.getString("id"));
//        String cdna = String.valueOf(queryResult);
//        ServiceTypesModel.StringResponse value = ServiceTypesModel.StringResponse.newBuilder()
//                .setValue(cdna)
//                .build();
//        responseObserver.onNext(value);
//        responseObserver.onCompleted();
//    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());
        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = transcriptDBAdaptor.nativeIterator(query, queryOptions);
        int count = 0;
        int limit = queryOptions.getInt("limit", 0);
        while (iterator.hasNext()) {
            Document gene = (Document) iterator.next();
            List<Document> transcripts = (List<Document>) gene.get("transcripts");
            if (limit > 0) {
                for (int i = 0; i < transcripts.size() && count < limit; i++) {
                    responseObserver.onNext(ProtoConverterUtils.createTranscript(transcripts.get(i)));
                    count++;
                }
            } else {
                for (Document doc : transcripts) {
                    responseObserver.onNext(ProtoConverterUtils.createTranscript(doc));
                }
            }
        }
        responseObserver.onCompleted();
    }

//    private TranscriptModel.Transcript convert(Document document) {
//        TranscriptModel.Transcript.Builder builder = TranscriptModel.Transcript.newBuilder()
//                .setId(document.getString("id"))
//                .setName(document.getString("name"))
//                .setBiotype(document.getString("biotype"))
//                .setStatus(document.getString("status"))
//                .setChromosome(document.getString("chromosome"))
//                .setStart(document.getInteger("start"))
//                .setEnd(document.getInteger("end"))
//                .setCdnaSequence((String) document.getOrDefault("cDnaSequence", ""));
//
//        List<Document> xrefs = (List<Document>) document.get("xrefs");
//        if (xrefs != null) {
//            for (Document doc : xrefs) {
//                TranscriptModel.Xref xrefBuilder = TranscriptModel.Xref.newBuilder()
//                        .setId(doc.getString("id"))
//                        .setDbName(doc.getString("dbName"))
//                        .setDbDisplayName(doc.getString("dbDisplayName"))
//                        .build();
//                builder.addXrefs(xrefBuilder);
//            }
//        }
//
//        List<Document> exons = (List<Document>) document.get("exons");
//        for (Document doc : exons) {
//            TranscriptModel.Exon exonBuilder = TranscriptModel.Exon.newBuilder()
//                    .setId(doc.getString("id"))
//                    .setChromosome(doc.getString("chromosome"))
//                    .setStart(doc.getInteger("start"))
//                    .setEnd(doc.getInteger("end"))
//                    .setStrand(doc.getString("strand"))
//                    .setExonNumber(doc.getInteger("exonNumber"))
//                    .setSequence(doc.getString("sequence"))
//                    .build();
//            builder.addExons(exonBuilder);
//        }
//
////        List<org.opencb.biodata.models.core.protobuf.TranscriptModel.Exon> exonList = ...
////        builder.addAllExons(exonList)
//
//
//        return builder.build();
//    }

}
