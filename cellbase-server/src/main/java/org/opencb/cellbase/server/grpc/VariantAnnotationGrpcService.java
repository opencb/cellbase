package org.opencb.cellbase.server.grpc;

import io.grpc.stub.StreamObserver;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.protobuf.VariantAnnotationProto;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.server.grpc.service.GenericServiceModel;
import org.opencb.cellbase.server.grpc.service.VariantAnnotationServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by swaathi on 17/08/16.
 */
public class VariantAnnotationGrpcService extends VariantAnnotationServiceGrpc.VariantAnnotationServiceImplBase implements IGrpcService {

    private DBAdaptorFactory dbAdaptorFactory;

    public VariantAnnotationGrpcService(DBAdaptorFactory dbAdaptorFactory) {
        this.dbAdaptorFactory = dbAdaptorFactory;
    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<VariantAnnotationProto.VariantAnnotation> responseObserver) {
        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        VariantAnnotationCalculator variantAnnotationCalculator =
                new VariantAnnotationCalculator(request.getSpecies(), request.getAssembly(),
                dbAdaptorFactory, true);
        List<QueryResult<VariantAnnotation>> queryResultList = null;
        List<Variant> variantList = Variant.parseVariants(query.getString("id"));
        try {
            queryResultList = variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (queryResultList != null) {
            for (QueryResult queryResult : queryResultList) {
                List<VariantAnnotation> results = queryResult.getResult();
                for (VariantAnnotation obj: results) {
                    responseObserver.onNext(ProtoConverterUtils.createVariantAnnotation(obj));
                }
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getCadd(GenericServiceModel.Request request, StreamObserver<VariantAnnotationProto.Score> responseObserver) {
        VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        List<QueryResult<Score>> queryResults =
                variantDBAdaptor.getFunctionalScoreVariant(Variant.parseVariants(query.getString("id")), queryOptions);
        for (QueryResult queryResult : queryResults) {
            List<Score> results = queryResult.getResult();
            for (Score obj: results) {
                responseObserver.onNext(ProtoConverterUtils.createVariantAnnotationScore(obj));
            }
        }
        responseObserver.onCompleted();
    }
}
