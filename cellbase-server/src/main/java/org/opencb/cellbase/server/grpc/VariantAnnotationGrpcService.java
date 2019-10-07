/*
 * Copyright 2015-2019 OpenCB
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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.protobuf.VariantAnnotationProto;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.VariantAnnotationServiceGrpc;
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
                dbAdaptorFactory);
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
