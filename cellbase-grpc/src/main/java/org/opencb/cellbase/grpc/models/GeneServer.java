package org.opencb.cellbase.grpc.models;

import io.grpc.stub.StreamObserver;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GeneServiceModel;

/**
 * Created by swaathi on 16/12/15.
 */
public class GeneServer {

    public static void main(String[] args) throws Exception {
        return;
    }

    private class GeneServiceImpl implements GeneServiceGrpc.GeneService {
        @Override
        public void get(GeneServiceModel.Query request, StreamObserver<GeneModel.Gene> responseObserver) {
            GeneModel.Gene.Builder reply = GeneModel.Gene.newBuilder();
//            dbadaptor.nativeIterator(request, options)
//                    .setName("Name" + request.getName())
//                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
