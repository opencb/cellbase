package org.opencb.cellbase.grpc.server;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.grpc.GenericServiceModel;
import org.opencb.cellbase.grpc.RegulationModel;
import org.opencb.cellbase.grpc.RegulationServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 21/12/15.
 */
public class RegulationGrpcServer extends GenericGrpcServer implements RegulationServiceGrpc.RegulationService {

    @Override
    public void count(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.LongResponse> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = regulationDBAdaptor.count(query);
        Long value = Long.valueOf(queryResult.getResult().get(0).toString());
        GenericServiceModel.LongResponse count = GenericServiceModel.LongResponse.newBuilder()
                .setValue(value)
                .build();
        responseObserver.onNext(count);
        responseObserver.onCompleted();
    }

    @Override
    public void distinct(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.StringArrayResponse> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = regulationDBAdaptor.distinct(query, request.getOptions().get("distinct"));
        List values = queryResult.getResult();
        GenericServiceModel.StringArrayResponse distinctValues = GenericServiceModel.StringArrayResponse.newBuilder()
                .addAllValues(values)
                .build();
        responseObserver.onNext(distinctValues);
        responseObserver.onCompleted();
    }

    @Override
    public void first(GenericServiceModel.Request request, StreamObserver<RegulationModel.Regulation> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        QueryOptions queryOptions = createQueryOptions(request);
        QueryResult first = regulationDBAdaptor.first(queryOptions);
        responseObserver.onNext(convert((Document) first.getResult().get(0)));
        responseObserver.onCompleted();
    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<RegulationModel.Regulation> responseObserver) {

    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<RegulationModel.Regulation> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = regulationDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            responseObserver.onNext(convert(document));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getJson(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.StringResponse> responseObserver) {

    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<GenericServiceModel.GroupResponse> responseObserver) {

    }

    private RegulationModel.Regulation convert(Document document) {
        RegulationModel.Regulation.Builder builder = RegulationModel.Regulation.newBuilder()
                .setName((String) document.getOrDefault("name", ""))
                .setChromosome(document.getString("chromosome"))
                .setStart(document.getInteger("start"))
                .setEnd(document.getInteger("end"))
                .setSource(document.getString("source"))
                .setFeatureType((String) document.getOrDefault("featureType", ""))
                .setScore(document.getString("score"))
                .setStrand(document.getString("strand"))
                .setFrame(document.getString("frame"))
                .setFeatureClass((String) document.getOrDefault("featureClass", ""))
                .setAlias((String) document.getOrDefault("alias", ""));

        List<String> cellTypes = (List<String>) document.get("cellTypes");
        if (cellTypes != null) {
            builder.addAllCellTypes(cellTypes);
        }
        return builder.build();
    }
}
