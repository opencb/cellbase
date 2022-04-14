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

package org.opencb.cellbase.lib.impl.core;

import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.release.DataRelease;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

public class MongoDBAdaptorFactory {

    private final DataRelease dataRelease;
    private final MongoDataStore mongoDatastore;

    public MongoDBAdaptorFactory(DataRelease dataRelease, MongoDataStore mongoDatastore) {
        this.dataRelease = dataRelease;
        this.mongoDatastore = mongoDatastore;
    }

    public GenomeMongoDBAdaptor getGenomeDBAdaptor() {
        return new GenomeMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    @Deprecated
    public MetaMongoDBAdaptor getMetaDBAdaptor() {
        return new MetaMongoDBAdaptor(mongoDatastore);
    }

    public ReleaseMongoDBAdaptor getReleaseDBAdaptor() {
        return new ReleaseMongoDBAdaptor(mongoDatastore);
    }

    public GeneMongoDBAdaptor getGeneDBAdaptor() {
        return new GeneMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public TranscriptMongoDBAdaptor getTranscriptDBAdaptor() {
        return new TranscriptMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public ProteinMongoDBAdaptor getProteinDBAdaptor() {
        return new ProteinMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public XRefMongoDBAdaptor getXRefDBAdaptor() {
        return new XRefMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public VariantMongoDBAdaptor getVariationDBAdaptor() {
        return new VariantMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public ClinicalMongoDBAdaptor getClinicalDBAdaptor(GenomeManager genomeManager) throws CellBaseException {
        // FIXME temporarily add config so we can get to the manager. this should be removed when we move all
        // methods to the manager.
        return new ClinicalMongoDBAdaptor(dataRelease, mongoDatastore, genomeManager);
    }

    public RepeatsMongoDBAdaptor getRepeatsDBAdaptor() {
        return new RepeatsMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public OntologyMongoDBAdaptor getOntologyMongoDBAdaptor() {
        return new OntologyMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public RegulationMongoDBAdaptor getRegulationDBAdaptor() {
        return new RegulationMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public MissenseVariationFunctionalScoreMongoDBAdaptor getMissenseVariationFunctionalScoreMongoDBAdaptor() {
        return new MissenseVariationFunctionalScoreMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    public SpliceScoreMongoDBAdaptor getSpliceScoreDBAdaptor() {
        return new SpliceScoreMongoDBAdaptor(dataRelease, mongoDatastore);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoDBAdaptorFactory{");
        sb.append("mongoDatastore=").append(mongoDatastore);
        sb.append('}');
        return sb.toString();
    }

    public MongoDataStore getMongoDatastore() {
        return mongoDatastore;
    }

}
