package org.opencb.cellbase.grpc.models;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GenericServiceModel;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Iterator;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneGrpcServer extends GenericGrpcServer implements GeneServiceGrpc.GeneService {


    @Override
    public void count(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.LongResponse> responseObserver) {

    }

    @Override
    public void distinct(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.StringArrayResponse> responseObserver) {

    }

    @Override
    public void first(GenericServiceModel.Request request, StreamObserver<GeneModel.Gene> responseObserver) {

    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<GeneModel.Gene> responseObserver) {

    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<GeneModel.Gene> responseObserver) {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "grch37");

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);

        Iterator iterator = geneDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            GeneModel.Gene gene = GeneModel.Gene.newBuilder()
                    .setName(document.getString("name"))
                    .setChromosome(document.getString("chromosome"))
                    .setBiotype(document.getString("biotype"))
                    .build();
            responseObserver.onNext(gene);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.GroupResponse> responseObserver) {

    }

}
