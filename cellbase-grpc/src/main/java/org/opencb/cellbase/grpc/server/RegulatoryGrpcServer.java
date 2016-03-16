package org.opencb.cellbase.grpc.server;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.grpc.service.GenericServiceModel;
import org.opencb.cellbase.grpc.service.RegulatoryRegionServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 11/02/16.
 */
public class RegulatoryGrpcServer extends GenericGrpcServer implements RegulatoryRegionServiceGrpc.RegulatoryRegionService {

    @Override
    public void count(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.LongResponse> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = regulationDBAdaptor.count(query);
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
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = regulationDBAdaptor.distinct(query, request.getOptions().get("distinct"));
        List values = queryResult.getResult();
        ServiceTypesModel.StringArrayResponse distinctValues = ServiceTypesModel.StringArrayResponse.newBuilder()
                .addAllValues(values)
                .build();
        responseObserver.onNext(distinctValues);
        responseObserver.onCompleted();
    }

    @Override
    public void first(GenericServiceModel.Request request,
                      StreamObserver<RegulatoryRegionModel.RegulatoryRegion> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        QueryOptions queryOptions = createQueryOptions(request);
        QueryResult first = regulationDBAdaptor.first(queryOptions);
        responseObserver.onNext(ProtoConverterUtils.createRegulatoryRegion((Document) first.getResult().get(0)));
        responseObserver.onCompleted();

    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<RegulatoryRegionModel.RegulatoryRegion> responseObserver) {

    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<RegulatoryRegionModel.RegulatoryRegion> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = regulationDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            responseObserver.onNext(ProtoConverterUtils.createRegulatoryRegion(document));
        }
        responseObserver.onCompleted();
    }

//    @Override
//    public void getJson(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.StringResponse> responseObserver) {
//        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());
//
//        Query query = createQuery(request);
//        QueryOptions queryOptions = createQueryOptions(request);
//        Iterator iterator = regulationDBAdaptor.nativeIterator(query, queryOptions);
//        while (iterator.hasNext()) {
//            Document document = (Document) iterator.next();
//            ServiceTypesModel.StringResponse response =
//                    ServiceTypesModel.StringResponse.newBuilder().setValue(document.toJson()).build();
//            responseObserver.onNext(response);
//        }
//        responseObserver.onCompleted();
//    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.GroupResponse> responseObserver) {

    }

//    private RegulatoryRegionModel.RegulatoryRegion convert(Document document) {
//        RegulatoryRegionModel.RegulatoryRegion.Builder builder = RegulatoryRegionModel.RegulatoryRegion.newBuilder()
//                .setId((String) document.getOrDefault("id", ""))
//                .setChromosome((String) document.getOrDefault("chromosome", ""))
//                .setSource((String) document.getOrDefault("source", ""))
//                .setFeatureType((String) document.getOrDefault("featureType", ""))
//                .setStart(document.getInteger("start", 0))
//                .setEnd(document.getInteger("end", 0))
//                .setScore((String) document.getOrDefault("score", ""))
//                .setStrand((String) document.getOrDefault("strand", ""))
//                .setFrame((String) document.getOrDefault("frame", ""))
//                .setItemRGB((String) document.getOrDefault("itemRGB", ""))
//                .setName((String) document.getOrDefault("name", ""))
//                .setFeatureClass((String) document.getOrDefault("featureClass", ""))
//                .setAlias((String) document.getOrDefault("alias", ""));
//        List<String> cellTypes = (List<String>) document.get("cellTypes");
//        if (cellTypes != null) {
//            builder.addAllCellTypes(cellTypes);
//        }
//        builder.setMatrix((String) document.getOrDefault("matrix", ""));
//        return builder.build();
//    }
}
