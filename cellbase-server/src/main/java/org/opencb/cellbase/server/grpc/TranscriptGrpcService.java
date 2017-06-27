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

package org.opencb.cellbase.server.grpc;

import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.opencb.biodata.models.common.protobuf.service.ServiceTypesModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.TranscriptServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 18/12/15.
 */
public class TranscriptGrpcService extends TranscriptServiceGrpc.TranscriptServiceImplBase implements IGrpcService {

    private DBAdaptorFactory dbAdaptorFactory;

    public TranscriptGrpcService(DBAdaptorFactory dbAdaptorFactory) {
        this.dbAdaptorFactory = dbAdaptorFactory;
    }

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
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());
        Query query = createQuery(request);
        QueryResult queryResult = transcriptDBAdaptor.distinct(query, request.getOptionsMap().get("distinct"));
        List values = queryResult.getResult();
        ServiceTypesModel.StringArrayResponse distinctValues = ServiceTypesModel.StringArrayResponse.newBuilder()
                .addAllValues(values)
                .build();
        responseObserver.onNext(distinctValues);
        responseObserver.onCompleted();
    }

    @Override
    public void first(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());

        QueryOptions queryOptions = createQueryOptions(request);
        QueryResult first = transcriptDBAdaptor.first(queryOptions);
        responseObserver.onNext(ProtoConverterUtils.createTranscript((Document) first.getResult().get(0)));
        responseObserver.onCompleted();
    }

    @Override
    public void next(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {

    }

    @Override
    public void groupBy(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.GroupResponse> responseObserver) {

    }

    @Override
    public void getCdna(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.StringResponse> responseObserver) {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryResult queryResult = transcriptDBAdaptor.getCdna(query.getString("id"));
        String cdna = String.valueOf(queryResult.getResult().get(0));
        ServiceTypesModel.StringResponse value = ServiceTypesModel.StringResponse.newBuilder()
                .setValue(cdna)
                .build();
        responseObserver.onNext(value);
        responseObserver.onCompleted();
    }

    @Override
    public void get(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(request.getSpecies(), request.getAssembly());
        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = transcriptDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            responseObserver.onNext(ProtoConverterUtils.createTranscript(document));
        }
        responseObserver.onCompleted();
    }

}
