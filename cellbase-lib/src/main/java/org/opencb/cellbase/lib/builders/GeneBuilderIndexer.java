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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.biodata.models.clinical.ClinicalProperty;
import org.opencb.biodata.models.core.GeneCancerAssociation;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GeneBuilderIndexer {

    protected RocksDB rocksdb;
    protected RocksDbManager rocksDbManager;
    protected Logger logger;
    protected String dbLocation;
    protected Options dbOption;

    protected final String MANE_SUFFIX = "_mane";
    protected final String CANCER_GENE_CENSUS_SUFFIX = "_cgc";
    protected final String PROTEIN_SEQUENCE_SUFFIX = "_protein_fasta";
    protected final String CDNA_SEQUENCE_SUFFIX = "_cdna_fasta";
    protected final String DRUGS_SUFFIX = "_drug";
    protected final String DISEASE_SUFFIX = "_disease";
    protected final String MIRTARBASE_SUFFIX = "_mirtarbase";

    public GeneBuilderIndexer(Path genePath) {
        this.init(genePath);
    }

    private void init(Path genePath) {
        rocksDbManager = new RocksDbManager();
        dbLocation = genePath.resolve("integration.idx").toString();
        rocksdb = rocksDbManager.getDBConnection(dbLocation);
        dbOption = new Options().setCreateIfMissing(true);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected void indexCdnaSequences(Path cDnaFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading RefSeq's cDNA sequences...");
        FileUtils.checkPath(cDnaFastaFile);
        if (Files.size(cDnaFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(cDnaFastaFile);
            Fasta fasta;
            while ((fasta = fastaReader.read()) != null) {
                rocksDbManager.update(rocksdb, fasta.getId() + CDNA_SEQUENCE_SUFFIX, fasta.getSeq());
            }
            fastaReader.close();
        } else {
            logger.warn("RefSeq's cDNA sequences not loaded");
        }
    }

    public String getCdnaFasta(String id) throws RocksDBException {
        return getIndexEntry(id, CDNA_SEQUENCE_SUFFIX);
    }

    protected void indexProteinSequences(Path proteinFastaFile) throws IOException, FileFormatException, RocksDBException {
        logger.info("Loading ENSEMBL's protein sequences...");
        FileUtils.checkPath(proteinFastaFile);
        if (Files.size(proteinFastaFile) > 0) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            Fasta fasta;
            while ((fasta = fastaReader.read()) != null) {
                rocksDbManager.update(rocksdb, fasta.getId() + PROTEIN_SEQUENCE_SUFFIX, fasta.getSeq());
            }
            fastaReader.close();
        } else {
            logger.warn("ENSEMBL's protein sequences not loaded");
        }
    }

    protected String getProteinFasta(String id) throws RocksDBException {
        return getIndexEntry(id, PROTEIN_SEQUENCE_SUFFIX);
    }

    protected void indexManeMapping(Path maneMappingFile, String referenceId) throws IOException, RocksDBException {
        // #NCBI_GeneID    Ensembl_Gene    HGNC_ID      symbol   name    RefSeq_nuc      RefSeq_prot     Ensembl_nuc     Ensembl_prot
        // MANE_status     GRCh38_chr     chr_start       chr_end chr_strand
        logger.info("Indexing MANE mapping data ...");

        if (maneMappingFile != null && Files.exists(maneMappingFile) && Files.size(maneMappingFile) > 0) {
            int idColumn = referenceId.equalsIgnoreCase("ensembl") ? 7 : 5;
//            BufferedReader bufferedReader = FileUtils.newBufferedReader(maneMappingFile);
            try (BufferedReader bufferedReader = FileUtils.newBufferedReader(maneMappingFile)) {
                String line = bufferedReader.readLine();
                while (StringUtils.isNotEmpty(line)) {
                    String[] fields = line.split("\t", -1);
                    rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_refseq", fields[5]);
                    rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_refseq_protein", fields[6]);
                    rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_ensembl", fields[7]);
                    rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_ensembl_protein", fields[8]);
                    rocksDbManager.update(rocksdb, fields[idColumn] + MANE_SUFFIX + "_flag", fields[9]);

                    line = bufferedReader.readLine();
                }
            }
        } else {
            logger.warn("MANE mapping file " + maneMappingFile + " not found");
        }
    }

    public String getMane(String id, String field) throws RocksDBException {
        return getIndexEntry(id, MANE_SUFFIX, field);
    }

    protected void indexCancerGeneCensus(Path cgcFile) throws IOException, RocksDBException {
        Map<String, String> tissuesMap = new HashMap<>();
        tissuesMap.put("E", "epithelial");
        tissuesMap.put("L", "leukaemia/lymphoma");
        tissuesMap.put("M", "mesenchymal");
        tissuesMap.put("O", "other");
        Map<String, ClinicalProperty.ModeOfInheritance> moiMap = new HashMap<>();
        moiMap.put("Dom", ClinicalProperty.ModeOfInheritance.AUTOSOMAL_DOMINANT);
        moiMap.put("Rec", ClinicalProperty.ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        moiMap.put("Rec/X", ClinicalProperty.ModeOfInheritance.X_LINKED_RECESSIVE);
        moiMap.put("O", ClinicalProperty.ModeOfInheritance.UNKNOWN);
        Map<String, ClinicalProperty.RoleInCancer> roleInCancerMap = new HashMap<>();
        roleInCancerMap.put("oncogene", ClinicalProperty.RoleInCancer.ONCOGENE);
        roleInCancerMap.put("TSG", ClinicalProperty.RoleInCancer.TUMOR_SUPPRESSOR_GENE);
        roleInCancerMap.put("fusion", ClinicalProperty.RoleInCancer.FUSION);
        Map<String, String> mutationTypesMap = new HashMap<>();
        mutationTypesMap.put("A", "amplification");
        mutationTypesMap.put("D", "large deletion");
        mutationTypesMap.put("F", "frameshift");
        mutationTypesMap.put("N", "nonsense");
        mutationTypesMap.put("O", "other");
        mutationTypesMap.put("S", "splice site");
        mutationTypesMap.put("T", "translocation");
        mutationTypesMap.put("Mis", "missense");
        mutationTypesMap.put("PromoterMis", "missense");

        logger.info("Indexing CANCER GENE CENSUS data ...");
        if (cgcFile != null && Files.exists(cgcFile) && Files.size(cgcFile) > 0) {
            // Skip the first header line
            BufferedReader bufferedReader = FileUtils.newBufferedReader(cgcFile);
            bufferedReader.readLine();

            GeneCancerAssociation cancerGeneAssociation;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                // Find Ensembl Gene Id in the last comma-separated column
                List<String> synonyms = StringUtils.isNotEmpty(fields[19])
                        ? Arrays.stream(fields[19]
                        .replaceAll("\"", "")
                        .replaceAll(" ", "")
                        .split(","))
                        .collect(Collectors.toList())
                        : Collections.emptyList();

                String ensemblGeneId = null;
                for (String synonym: synonyms) {
                    if (synonym.startsWith("ENSG")) {
                        ensemblGeneId = synonym;
                        break;
                    }
                }
                if (StringUtils.isNotEmpty(ensemblGeneId)) {
                    boolean somatic = StringUtils.isNotEmpty(fields[7]) && fields[7].equalsIgnoreCase("yes");
                    boolean germline = StringUtils.isNotEmpty(fields[8]) && fields[8].equalsIgnoreCase("yes");
                    List<String> somaticTumourTypes = StringUtils.isNotEmpty(fields[9])
                            ? Arrays.asList(fields[9].replaceAll("\"", "").split(", "))
                            : new ArrayList<>();
                    List<String> germlineTumourTypes = StringUtils.isNotEmpty(fields[10])
                            ? Arrays.asList(fields[10].replaceAll("\"", "").split(", "))
                            : Collections.emptyList();
                    List<String> syndromes = StringUtils.isNotEmpty(fields[11])
                            ? Arrays.asList(fields[11].replaceAll("\"", "").split("; "))
                            : Collections.emptyList();
                    List<String> tissues = StringUtils.isNotEmpty(fields[12])
                            ? Arrays.stream(fields[12]
                            .replaceAll("\"", "")
                            .replaceAll(" ", "")
                            .split(","))
                            .map(tissuesMap::get)
                            .collect(Collectors.toList())
                            : Collections.emptyList();
                    List<ClinicalProperty.ModeOfInheritance> modeOfInheritance = StringUtils.isNotEmpty(fields[13])
                            ? fields[13].equalsIgnoreCase("Dom/Rec")
                                ? Arrays.asList(moiMap.get("Dom"), moiMap.get("Rec"))
                                : Collections.singletonList(moiMap.get(fields[13]))
                            : Collections.emptyList();
                    List<ClinicalProperty.RoleInCancer> roleInCancer = StringUtils.isNotEmpty(fields[14])
                            ? Arrays.stream(fields[14]
                            .replaceAll("\"", "")
                            .replaceAll(" ", "")
                            .split(","))
                            .map(roleInCancerMap::get)
                            .collect(Collectors.toList())
                            : Collections.emptyList();
                    List<String> mutationTypes = StringUtils.isNotEmpty(fields[15])
                            ? Arrays.stream(fields[15]
                            .replaceAll("\"", "")
                            .replaceAll(" ", "")
                            .split(","))
                            .map(mutationTypesMap::get)
                            .collect(Collectors.toList())
                            : Collections.emptyList();
                    List<String> translocationPartners = StringUtils.isNotEmpty(fields[16])
                            ? Arrays.stream(fields[16]
                            .replaceAll("\"", "")
                            .replaceAll(" ", "")
                            .split(","))
                            .collect(Collectors.toList())
                            : Collections.emptyList();
                    List<String> otherSyndromes = StringUtils.isNotEmpty(fields[18])
                            ? Arrays.stream(fields[18]
                            .replaceAll("\"", "")
                            .split("; "))
                            .collect(Collectors.toList())
                            : Collections.emptyList();

                    cancerGeneAssociation = new GeneCancerAssociation(ensemblGeneId, fields[0], "Cancer Gene Census",
                            fields[3], fields[6], fields[4], somatic, germline, somaticTumourTypes, germlineTumourTypes, syndromes, tissues,
                            modeOfInheritance, roleInCancer, mutationTypes, translocationPartners, otherSyndromes, synonyms);

                    rocksDbManager.update(rocksdb, fields[0] + CANCER_GENE_CENSUS_SUFFIX, cancerGeneAssociation);
                }
            }
            bufferedReader.close();
        } else {
            logger.warn("CANCER GENE CENSUS file " + cgcFile + " not found");
        }
    }

    public List<GeneCancerAssociation> getCancerGeneCensus(String geneName) throws RocksDBException, IOException {
        String key = geneName + CANCER_GENE_CENSUS_SUFFIX;
        return rocksDbManager.getGeneCancerAssociation(rocksdb, key);
    }

    private String getIndexEntry(String id, String suffix) throws RocksDBException {
        return getIndexEntry(id, suffix, "");
    }

    private String getIndexEntry(String id, String suffix, String field) throws RocksDBException {
        String key = id + suffix;
        if (StringUtils.isNotEmpty(field)) {
            key += "_" + field;
        }
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
