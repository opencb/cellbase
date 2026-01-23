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

package org.opencb.cellbase.lib.builders;

import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.opencb.cellbase.lib.EtlCommons.REFSEQ_DATA;
import static org.opencb.cellbase.lib.builders.GeneBuilder.*;

public class RefSeqGeneBuilderIndexer extends GeneBuilderIndexer {

    public RefSeqGeneBuilderIndexer(Path refSeqDirectoryPath) {
        super(refSeqDirectoryPath);
    }

    public void index(Map<String, Path> filesToIndex) throws IOException, RocksDBException, FileFormatException, CellBaseException {
        indexManeMapping(filesToIndex.get(MANE_FILE), REFSEQ_DATA);
        indexLrgMapping(filesToIndex.get(LRG_FILE), REFSEQ_DATA);
        indexProteinSequences(filesToIndex.get(PROTEIN_FASTA_FILE));
        indexCdnaSequences(filesToIndex.get(CDNA_FASTA_FILE));
        indexDrugs(filesToIndex.get(GENE_DRUG_FILE));
        indexDiseases(filesToIndex.get(HPO_FILE));
        indexConstraints(filesToIndex.get(GNOMAD_FILE), REFSEQ_DATA);
        indexMiRTarBase(filesToIndex.get(MIRTARBASE_FILE));
        indexCancerGeneCensus(filesToIndex.get(CANCER_GENE_CENSUS_FILE));
        indexCancerHotspot(filesToIndex.get(CANCER_HOTSPOT_FILE));
        indexImprintedGenes(filesToIndex.get(GENE_IMPRINT_FILE));
        indexChimerDb(filesToIndex.get(CHIMER_KB_FILE), filesToIndex.get(CHIMER_PUB_FILE), filesToIndex.get(CHIMER_SEQ_FILE));
    }
}
