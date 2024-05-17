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

import static org.opencb.cellbase.lib.EtlCommons.REFSEQ_DATA;

public class RefSeqGeneBuilderIndexer extends GeneBuilderIndexer {

    public RefSeqGeneBuilderIndexer(Path refSeqDirectoryPath) {
        super(refSeqDirectoryPath);
    }

    public void index(Path maneFile, Path lrgFile, Path proteinFastaFile, Path cDnaFastaFile, Path geneDrugFile, Path hpoFilePath,
                      Path disgenetFile, Path miRTarBaseFile, Path cancerGeneGensus, Path cancerHotspot, Path tso500File,
                      Path eglhHaemOncFile) throws IOException, RocksDBException, FileFormatException, CellBaseException {
        indexManeMapping(maneFile, REFSEQ_DATA);
        indexLrgMapping(lrgFile, REFSEQ_DATA);
        indexProteinSequences(proteinFastaFile);
        indexCdnaSequences(cDnaFastaFile);
        indexDrugs(geneDrugFile);
        indexDiseases(hpoFilePath, disgenetFile);
        indexMiRTarBase(miRTarBaseFile);
        indexCancerGeneCensus(cancerGeneGensus);
        indexCancerHotspot(cancerHotspot);
        indexTSO500(tso500File);
        indexEGLHHaemOnc(eglhHaemOncFile);
    }
}
