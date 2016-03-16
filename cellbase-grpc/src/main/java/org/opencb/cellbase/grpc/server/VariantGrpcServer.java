/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.grpc.server;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.variant.protobuf.VariantProto;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.grpc.service.GenericServiceModel;
import org.opencb.cellbase.grpc.service.VariantServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 16/12/15.
 */
public class VariantGrpcServer extends GenericGrpcServer implements VariantServiceGrpc.VariantService {


    @Override
    public void count(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.LongResponse> responseObserver) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = variationDBAdaptor.count(query);
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
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(request.getSpecies(), request.getAssembly());
        Query query = createQuery(request);
        QueryResult queryResult = variationDBAdaptor.distinct(query, request.getOptions().get("distinct"));
        List values = queryResult.getResult();
        ServiceTypesModel.StringArrayResponse distinctValues = ServiceTypesModel.StringArrayResponse.newBuilder()
                .addAllValues(values)
                .build();
        responseObserver.onNext(distinctValues);
        responseObserver.onCompleted();
    }

    @Override
    public void first(GenericServiceModel.Request request, StreamObserver<VariantProto.Variant> responseObserver) {
        VariantDBAdaptor variationDBAdaptor =
                dbAdaptorFactory.getVariationDBAdaptor(request.getSpecies(), request.getAssembly());

        QueryOptions queryOptions = createQueryOptions(request);
        QueryResult first = variationDBAdaptor.first(queryOptions);
        responseObserver.onNext(ProtoConverterUtils.createVariant((Document) first.getResult().get(0)));
        responseObserver.onCompleted();

    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<VariantProto.Variant> responseObserver) {

    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<VariantProto.Variant> responseObserver) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = variationDBAdaptor.nativeIterator(query, queryOptions);
        int count = 0;
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            responseObserver.onNext(ProtoConverterUtils.createVariant(document));
            if (++count % 1000 == 0) {
                System.out.println(count);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.GroupResponse> responseObserver) {

    }

//    private VariantProto.Variant convert(Document document) {
//        VariantProto.Variant.Builder builder = VariantProto.Variant.newBuilder()
//                .setChromosome(document.getString("chromosome"))
//                .setStart(document.getInteger("start"))
//                .setEnd(document.getInteger("end"))
//                .setReference((String) document.getOrDefault("reference", ""))
//                .setReference((String) document.getOrDefault("alternate", ""))
//                .setStrand(document.getString("strand"));
////                .setId(document.getString("id"))
////                .setName(document.getString("name"))
////                .setBiotype(document.getString("biotype"))
////                .setStatus(document.getString("status"))
////                .setSource(document.getString("source"));
////                .addAllTranscripts()
//
//        return builder.build();
//    }
}
