/*
 * Copyright 2015-2020 OpenCB
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
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.protobuf.GeneModel;
import org.opencb.biodata.models.core.protobuf.RegulatoryRegionModel;
import org.opencb.biodata.models.core.protobuf.TranscriptModel;
import org.opencb.biodata.models.variant.protobuf.VariantProto;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.grpc.service.GenericServiceModel;
import org.opencb.cellbase.core.grpc.service.GenomicRegionServiceGrpc;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Created by swaathi on 18/08/16.
 */
public class GenomicRegionGrpcService extends GenomicRegionServiceGrpc.GenomicRegionServiceImplBase implements IGrpcService{

    private DBAdaptorFactory dbAdaptorFactory;

    public GenomicRegionGrpcService(DBAdaptorFactory dbAdaptorFactory) {
        this.dbAdaptorFactory = dbAdaptorFactory;
    }

    @Override
    public void getGene(GenericServiceModel.Request request, StreamObserver<GeneModel.Gene> responseObserver) {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        Iterator iterator = geneDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document gene = (Document) iterator.next();
            responseObserver.onNext(ProtoConverterUtils.createGene(gene));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getTranscript(GenericServiceModel.Request request, StreamObserver<TranscriptModel.Transcript> responseObserver) {
        // TODO iteration goes on and on. Need to be fixed
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

    @Override
    public void getVariation(GenericServiceModel.Request request, StreamObserver<VariantProto.Variant> responseObserver) {
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
    public void getSequence(GenericServiceModel.Request request, StreamObserver<ServiceTypesModel.StringResponse> responseObserver) {
        GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(request.getSpecies(), request.getAssembly());
        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);

        List<QueryResult<GenomeSequenceFeature>> queryResults =
                genomeDBAdaptor.getSequence(Region.parseRegions(query.getString("region")), queryOptions);
        for (QueryResult<GenomeSequenceFeature> result : queryResults) {
            String value = result.getResult().get(0).getSequence();
            ServiceTypesModel.StringResponse sequence = ServiceTypesModel.StringResponse.newBuilder()
                    .setValue(value)
                    .build();
            responseObserver.onNext(sequence);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getRegulatoryRegion(GenericServiceModel.Request request,
                                    StreamObserver<RegulatoryRegionModel.RegulatoryRegion> responseObserver) {
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

    @Override
    public void getTfbs(GenericServiceModel.Request request, StreamObserver<RegulatoryRegionModel.RegulatoryRegion> responseObserver) {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(request.getSpecies(), request.getAssembly());

        Query query = createQuery(request);
        QueryOptions queryOptions = createQueryOptions(request);
        query.put(RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), RegulationDBAdaptor.FeatureType.TF_binding_site + ","
                + RegulationDBAdaptor.FeatureType.TF_binding_site_motif);
        Iterator iterator = regulationDBAdaptor.nativeIterator(query, queryOptions);
        while (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            responseObserver.onNext(ProtoConverterUtils.createRegulatoryRegion(document));
        }
        responseObserver.onCompleted();
    }
}
