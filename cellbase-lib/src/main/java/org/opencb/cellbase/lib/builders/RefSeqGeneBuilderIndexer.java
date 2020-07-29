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
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RefSeqGeneBuilderIndexer {

    private RocksDB rocksdb;
    private static final String PROTEIN_SEQUENCE_SUFFIX = "_protein_fasta";

    private RocksDbManager rocksDbManager;
    protected Logger logger;
    private Options dbOption = null;
    private String dbLocation = null;

    public RefSeqGeneBuilderIndexer(Path refSeqDirectoryPath) {
        init(refSeqDirectoryPath);
    }

    private void init(Path refSeqDirectoryPath) {
        rocksDbManager = new RocksDbManager();
        dbLocation = refSeqDirectoryPath.toString() + "/integration.idx";
        rocksdb = rocksDbManager.getDBConnection(dbLocation);
        dbOption = new Options().setCreateIfMissing(true);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void index(Path proteinFastaFile)
            throws IOException, RocksDBException, FileFormatException {
        indexProteinSequences(proteinFastaFile);
    }

    private void indexProteinSequences(Path proteinFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading ENSEMBL's protein sequences...");
        if (proteinFastaFile != null && Files.exists(proteinFastaFile) && !Files.isDirectory(proteinFastaFile)
                && Files.size(proteinFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for (Fasta fasta : fastaList) {
                rocksDbManager.update(rocksdb, fasta.getId() + PROTEIN_SEQUENCE_SUFFIX, fasta.getSeq());
            }
        } else {
            logger.warn("Protein fasta file " + proteinFastaFile + " not found");
            logger.warn("RefSeq's protein sequences not loaded");
        }
    }

    public String getProteinFasta(String id) throws RocksDBException {
        String key = id + PROTEIN_SEQUENCE_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    protected void close() throws IOException {
        rocksDbManager.closeIndex(rocksdb, dbOption, dbLocation);
    }
}
