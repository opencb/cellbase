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

package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.config.CellBaseConfiguration;


public class MongoDBAdaptorFactory extends DBAdaptorFactory {


    /**
     * MongoDataStoreManager acts as singleton by keeping a reference to all databases connections created.
     */

    private CellBaseConfiguration cellBaseConfiguration;
//    private static Map<String, MongoDataStore> mongoDatastoreFactory;

    public MongoDBAdaptorFactory(CellBaseConfiguration cellBaseConfiguration) {
        super(cellBaseConfiguration);
        this.cellBaseConfiguration = cellBaseConfiguration;
    }


    @Override
    public void open(String species, String assembly) {

    }

    @Override
    public void close() {

    }


    @Override
    public GenomeDBAdaptor getGenomeDBAdaptor(String species) {
        return getGenomeDBAdaptor(species, null);
    }

    @Override
    public GenomeDBAdaptor getGenomeDBAdaptor(String species, String assembly) {
        return new GenomeMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }

    @Override
    public CellBaseDBAdaptor<Document> getMetaDBAdaptor(String species) {
        return getMetaDBAdaptor(species, null);
    }

    @Override
    public CellBaseDBAdaptor<Document> getMetaDBAdaptor(String species, String assembly) {
        return new MetaMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species) {
        return getGeneDBAdaptor(species, null);
    }

    @Override
    public GeneDBAdaptor getGeneDBAdaptor(String species, String assembly) {
        GeneMongoDBAdaptor geneMongoDBAdaptor = new GeneMongoDBAdaptor(species, assembly, cellBaseConfiguration);
//        geneMongoDBAdaptor.setClinicalDBAdaptor(getClinicalDBAdaptor(species, assembly));
        return geneMongoDBAdaptor;
    }


    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species) {
        return getTranscriptDBAdaptor(species, null);
    }

    @Override
    public TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly) {
        return new TranscriptMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


    @Override
    public ConservationDBAdaptor getConservationDBAdaptor(String species) {
        return getConservationDBAdaptor(species, null);
    }

    @Override
    public ConservationDBAdaptor getConservationDBAdaptor(String species, String assembly) {
        return new ConservationMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


    @Override
    public XRefDBAdaptor getXRefDBAdaptor(String species) {
        return getXRefDBAdaptor(species, null);
    }

    @Override
    public XRefDBAdaptor getXRefDBAdaptor(String species, String assembly) {
        return new XRefMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


    @Override
    public VariantDBAdaptor getVariationDBAdaptor(String species) {
        return getVariationDBAdaptor(species, null);
    }

    @Override
    public VariantDBAdaptor getVariationDBAdaptor(String species, String assembly) {
        return new VariantMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }

//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
//        return getVariantAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new VariantAnnotationCalculator(species, assembly, mongoDatastore, this);
//    }


//    @Override
//    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species) {
//        return getVariantFunctionalScoreDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new VariantFunctionalScoreMongoDBAdaptor(species, assembly, mongoDatastore);
//    }


    @Override
    public ClinicalDBAdaptor getClinicalLegacyDBAdaptor(String species) {
        return getClinicalLegacyDBAdaptor(species, null);
    }

    @Override
    public ClinicalDBAdaptor getClinicalLegacyDBAdaptor(String species, String assembly) {
        return new ClinicalLegacyMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }

    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species) {
        return getClinicalDBAdaptor(species, null);
    }

    @Override
    public ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly) {
        return new ClinicalMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }

    @Override
    public RepeatsDBAdaptor getRepeatsDBAdaptor(String species, String assembly) {
        return new RepeatsMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species) {
//        return getVariantAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = new VariantAnnotationMongoDBAdaptor(species, assembly,
//                mongoDatastore);
//        variantAnnotationDBAdaptor.setGeneDBAdaptor(getGeneDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setRegulationDBAdaptor(getRegulatoryRegionDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantDBAdaptor(getVariationDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantClinicalDBAdaptor(getClinicalLegacyDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setProteinDBAdaptor(getProteinDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setConservationDBAdaptor(getConservedRegionDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setVariantFunctionalScoreDBAdaptor(getVariantFunctionalScoreDBAdaptor(species, assembly));
//        variantAnnotationDBAdaptor.setGenomeDBAdaptor(getGenomeDBAdaptor(species, assembly));
//
//        return variantAnnotationDBAdaptor;
//    }
//
//


    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species) {
        return getProteinDBAdaptor(species, null);
    }

    @Override
    public ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly) {
        return new ProteinMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species) {
        return getProteinProteinInteractionDBAdaptor(species, null);
    }

    @Override
    public ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly) {
        return new ProteinProteinInteractionMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }


    @Override
    public RegulationDBAdaptor getRegulationDBAdaptor(String species) {
        return getRegulationDBAdaptor(species, null);
    }

    @Override
    public RegulationDBAdaptor getRegulationDBAdaptor(String species, String assembly) {
        return new RegulationMongoDBAdaptor(species, assembly, cellBaseConfiguration);
    }
//
//    @Override
//    public TfbsDBAdaptor getTfbsDBAdaptor(String species) {
//        return getTfbsDBAdaptor(species, null);
//    }
//
//    @Override
//    public TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new TfbsMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public PathwayDBAdaptor getPathwayDBAdaptor(String species) {
//        return getPathwayDBAdaptor(species, null);
//    }
//
//    @Override
//    public PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return new PathwayMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species) {
//        return getVariationPhenotypeAnnotationDBAdaptor(species, null);
//    }
//
//    @Override
//    public VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species, String assembly) {
//        MongoDataStore mongoDatastore = createMongoDBDatastore(species, assembly);
//        return (VariationPhenotypeAnnotationDBAdaptor) new VariationPhenotypeAnnotationMongoDBAdaptor(species, assembly, mongoDatastore);
//    }
//
//
//    @Override
//    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String assembly) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public MirnaDBAdaptor getMirnaDBAdaptor(String species) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public MirnaDBAdaptor getMirnaDBAdaptor(String species, String assembly) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
}
