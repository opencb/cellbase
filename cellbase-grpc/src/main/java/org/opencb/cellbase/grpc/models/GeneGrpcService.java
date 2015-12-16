package org.opencb.cellbase.grpc.models;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GeneServiceModel;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Iterator;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneGrpcService extends GenericGrpcServer implements GeneServiceGrpc.GeneService {

    @Override
    public void get(GeneServiceModel.Query request, StreamObserver<GeneModel.Gene> responseObserver) {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor("hsapiens", "grch37");
        Query query = new Query();
        Iterator<Document> iterator = geneDBAdaptor.nativeIterator(new Query(), new QueryOptions());
        while (iterator.hasNext()) {
            Document document = iterator.next();
            GeneModel.Gene gene = GeneModel.Gene.newBuilder()
                    .setName(document.getString("name"))
                    .setChromosome(document.getString("chromosome"))
                    .setBiotype(document.getString("biotype"))
                    .build();
            responseObserver.onNext(gene);
        }
        responseObserver.onCompleted();
    }

}
