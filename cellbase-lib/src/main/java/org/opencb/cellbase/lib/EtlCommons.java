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

package org.opencb.cellbase.lib;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 03/06/16.
 */
public final class EtlCommons {

    // Ensembl
    public static final String ENSEMBL_NAME = "Ensembl";
    public static final String PUT_RELEASE_HERE_MARK = "put_release_here";
    public static final String PUT_SPECIES_HERE_MARK = "put_species_here";
    public static final String PUT_CAPITAL_SPECIES_HERE_MARK = "put_capital_species_here";
    public static final String PUT_ASSEMBLY_HERE_MARK = "put_assembly_here";
    public static final String PUT_CHROMOSOME_HERE_MARK = "put_chromosome_here";
    // Must match the configuration file
    public static final String ENSEMBL_PRIMARY_FA_FILE_ID = "PRIMARY_FA";
    public static final String ENSEMBL_GTF_FILE_ID = "GTF";
    public static final String ENSEMBL_PEP_FA_FILE_ID = "PEP_FA";
    public static final String ENSEMBL_CDNA_FA_FILE_ID = "CDNA_FA";
    public static final String ENSEMBL_REGULATORY_BUILD_FILE_ID = "REGULATORY_BUILD";
    public static final String ENSEMBL_MOTIF_FEATURES_FILE_ID = "MOTIF_FEATURES";
    public static final String ENSEMBL_MOTIF_FEATURES_INDEX_FILE_ID = "MOTIF_FEATURES_INDEX";

    public static final String HOMO_SAPIENS_NAME= "Homo sapiens";

    public static final String GRCH38_NAME = "GRCh38";
    public static final String GRCH37_NAME = "GRCh37";
    public static final String HG38_NAME = "hg38";
    public static final String HG19_NAME = "hg19";

    public static final String SUFFIX_VERSION_FILENAME = "Version.json";

    // Genome (Ensembl)
    public static final String GENOME_NAME = "Genome";
    public static final String GENOME_DATA = "genome";
    public static final String GENOME_SUBDIRECTORY = GENOME_DATA;
    public static final String GENOME_VERSION_FILENAME = GENOME_DATA + SUFFIX_VERSION_FILENAME;

    // Gene (Ensembl)
    public static final String GENE_DATA = "gene";
    public static final String ENSEMBL_CORE_VERSION_FILENAME = "ensemblCore" + SUFFIX_VERSION_FILENAME;

    // RefSeq
    public static final String REFSEQ_NAME = "RefSeq";
    public static final String REFSEQ_DATA = "refseq";
    public static final String REFSEQ_VERSION_FILENAME = "refSeq" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String REFSEQ_GENOMIC_GTF_FILE_ID = "GENOMIC_GTF";
    public static final String REFSEQ_GENOMIC_FNA_FILE_ID = "GENOMIC_FNA";
    public static final String REFSEQ_PROTEIN_FAA_FILE_ID = "PROTEIN_FAA";
    public static final String REFSEQ_RNA_FNA_FILE_ID = "RNA_FNA";

    // MANE Select
    public static final String MANE_SELECT_NAME = "MANE Select";
    public static final String MANE_SELECT_VERSION_FILENAME = "maneSelect" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String MANE_SELECT_FILE_ID = "MANE_SELECT";

    // LRG
    public static final String LRG_NAME = "LRG";
    public static final String LRG_VERSION_FILENAME = "lrg" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String LRG_FILE_ID = "LRG";

    // HGNC
    public static final String HGNC_NAME = "HGNC Gene";
    public static final String HGNC_VERSION_FILENAME = "hgnc" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String HGNC_FILE_ID = "HGNC";

    // Cancer HotSpot
    public static final String CANCER_HOTSPOT_NAME = "Cancer HotSpot";
    public static final String CANCER_HOTSPOT_VERSION_FILENAME = "cancerHotSpot" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String CANCER_HOTSPOT_FILE_ID = "CANCER_HOTSPOT";

    // DGID (drug)
    public static final String DGIDB_NAME = "DGIdb";
    public static final String DGIDB_VERSION_FILENAME = "dgidb" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String DGIDB_FILE_ID = "DGIDB";

    // UniProt Xref
    public static final String UNIPROT_XREF_NAME = "UniProt Xref";
    public static final String UNIPROT_XREF_VERSION_FILENAME = "uniprotXref" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String UNIPROT_XREF_FILE_ID = "UNIPROT_XREF";

    // Gene Expression Atlas
    public static final String GENE_EXPRESSION_ATLAS_NAME = "Gene Expression Atlas";
    public static final String GENE_EXPRESSION_ATLAS_VERSION_FILENAME = "geneExpressionAtlas" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String GENE_EXPRESSION_ATLAS_FILE_ID = "GENE_EXPRESSION_ATLAS";

    // Gene Disease Annotation
    public static final String GENE_DISEASE_ANNOTATION_NAME = "Gene Disease Annotation";
    // HPO
    public static final String HPO_NAME = "HPO";
    public static final String HPO_VERSION_FILENAME = "hpo" + SUFFIX_VERSION_FILENAME;
    // DISGENET
    public static final String DISGENET_NAME = "DisGeNet";
    public static final String DISGENET_VERSION_FILENAME = "disGeNet" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String DISGENET_FILE_ID = "DISGENET";

    // gnomAD Constraints
    public static final String GNOMAD_CONSTRAINTS_NAME = "gnomAD Constraints";
    public static final String GNOMAD_CONSTRAINTS_VERSION_FILENAME = "gnomadConstraints" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String GNOMAD_CONSTRAINTS_FILE_ID = "GNOMAD_CONSTRAINTS";

    // GO Annotation
    public static final String GO_ANNOTATION_NAME = "EBI Gene Ontology Annotation";
    public static final String GO_ANNOTATION_VERSION_FILENAME = "goAnnotation" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String GO_ANNOTATION_FILE_ID = "GO_ANNOTATION";

    public static final String VARIATION_DATA = "variation";
    public static final String CLINICAL_VARIANTS_DATA = "clinical_variants";
    public static final String SPLICE_SCORE_DATA = "splice_score";

    // Pharmacogenomics
    public static final String PHARMACOGENOMICS_DATA = "pharmacogenomics";
    public static final String PHARMACOGENOMICS_SUBDIRECTORY = "pharmacogenomics";
    // PharmGKB
    public static final String PHARMGKB_NAME = "PharmGKB";
    public static final String PHARMGKB_DATA = "pharmgkb";
    public static final String PHARMGKB_SUBDIRECTORY = PHARMGKB_DATA;
    public static final String PHARMGKB_VERSION_FILENAME = PHARMGKB_DATA + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String PHARMGKB_GENES_FILE_ID = "GENES";
    public static final String PHARMGKB_CHEMICALS_FILE_ID = "CHEMICALS";
    public static final String PHARMGKB_VARIANTS_FILE_ID = "VARIANTS";
    public static final String PHARMGKB_GUIDELINE_ANNOTATIONS_FILE_ID = "GUIDELINE_ANNOTATIONS";
    public static final String PHARMGKB_VARIANT_ANNOTATIONS_FILE_ID = "VARIANT_ANNOTATIONS";
    public static final String PHARMGKB_CLINICAL_ANNOTATIONS_FILE_ID = "CLINICAL_ANNOTATIONS";
    public static final String PHARMGKB_CLINICAL_VARIANTS_FILE_ID = "CLINICAL_VARIANTS";
    public static final String PHARMGKB_DRUG_LABELS_FILE_ID = "DRUG_LABELS";
    public static final String PHARMGKB_RELATIONSHIPS_FILE_ID = "RELATIONSHIPS";

    // Missense variantion functional score
    public static final String MISSENSE_VARIATION_SCORE_NAME = "Missense Variation Functional Scores";
    public static final String MISSENSE_VARIATION_SCORE_DATA = "missense_variation_functional_score";
    // Revel
    public static final String REVEL_NAME = "Revel";
    public static final String REVEL_VERSION_FILENAME = "revel" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String REVEL_FILE_ID = "REVEL";

    // Clinical variants data
    public static final String CLINICAL_VARIANTS_SUBDIRECTORY = "clinicalVariant";
    // ClinVar
    public static final String CLINVAR_NAME = "ClinVar";
    public static final String CLINVAR_VERSION_FILENAME = "clinvar" + SUFFIX_VERSION_FILENAME;
    public static final String CLINVAR_CHUNKS_SUBDIRECTORY = "clinvar_chunks";
    // Must match the configuration file
    public static final String CLINVAR_FULL_RELEASE_FILE_ID = "FULL_RELEASE";
    public static final String CLINVAR_SUMMARY_FILE_ID = "SUMMARY";
    public static final String CLINVAR_ALLELE_FILE_ID = "ALLELE";
    public static final String CLINVAR_EFO_TERMS_FILE_ID = "EFO_TERMS";
    // COSMIC
    public static final String COSMIC_NAME = "COSMIC";
    public static final String COSMIC_VERSION_FILENAME = "cosmic" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String COSMIC_FILE_ID = "COSMIC";
    // HGMD
    public static final String HGMD_NAME = "HGMD";
    public static final String HGMD_VERSION_FILENAME = "hgmd" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String HGMD_FILE_ID = "HGMD";
    // GWAS
    public static final String GWAS_NAME = "GWAS catalog";
    public static final String GWAS_VERSION_FILENAME = "gwas" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String GWAS_FILE_ID = "GWAS";

    // Repeats
    public static final String REPEATS_NAME = "Repeats";
    public static final String REPEATS_DATA = "repeats";
    public static final String REPEATS_SUBDIRECTORY = GENOME_SUBDIRECTORY;
    /**
     * @deprecated (when refactoring downloaders, builders and loaders)
     */
    @Deprecated
    public static final String REPEATS_JSON = "repeats";
    // Simple repeats
    public static final String TRF_NAME = "Tandem Repeats Finder";
    public static final String TRF_VERSION_FILENAME = "simpleRepeat" + SUFFIX_VERSION_FILENAME;
    public static final String SIMPLE_REPEATS_FILE_ID = "SIMPLE_REPEATS";
    // Genomic super duplications
    public static final String GSD_NAME = "Genomic Super Duplications";
    public static final String GSD_VERSION_FILENAME = "genomicSuperDups" + SUFFIX_VERSION_FILENAME;
    public static final String GENOMIC_SUPER_DUPS_FILE_ID = "GENOMIC_SUPER_DUPS";
    // Window masker
    public static final String WM_NAME = "Window Masker";
    public static final String WM_VERSION_FILENAME = "windowMasker" + SUFFIX_VERSION_FILENAME;
    public static final String WINDOW_MASKER_FILE_ID = "WINDOW_MASKER";

    // Ontology
    public static final String ONTOLOGY_NAME = "Ontology";
    public static final String ONTOLOGY_DATA = "ontology";
    public static final String ONTOLOGY_SUBDIRECTORY = ONTOLOGY_DATA;
    // HPO
    public static final String HPO_OBO_NAME = "HPO";
    public static final String HPO_OBO_VERSION_FILENAME = "hpoObo" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String HPO_OBO_FILE_ID = "HPO";
    // GO
    public static final String GO_OBO_NAME = "GO";
    public static final String GO_OBO_VERSION_FILENAME = "goObo" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String GO_OBO_FILE_ID = "GO";
    // DOID
    public static final String DOID_OBO_NAME = "DOID";
    public static final String DOID_OBO_VERSION_FILENAME = "doidObo" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String DOID_OBO_FILE_ID = "DOID";
    // MONDO
    public static final String MONDO_OBO_NAME = "Mondo";
    public static final String MONDO_OBO_VERSION_FILENAME = "mondoObo" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String MONDO_OBO_FILE_ID = "MONDO";


    public static final String PFM_DATA = "regulatory_pfm";

    // Variation functional score
    public static final String VARIATION_FUNCTIONAL_SCORE_DATA = "variation_functional_score";
    public static final String VARIATION_FUNCTIONAL_SCORE_SUBDIRECTORY = "variation_functional_score";
    // CADD scores
    public static final String CADD_NAME = "CADD";
    public static final String CADD_VERSION_FILENAME = "cadd" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String CADD_FILE_ID = "CADD";

    // Regulation
    public static final String REGULATION_NAME = "Regulation";
    public static final String REGULATION_DATA = "regulation";
    public static final String REGULATION_SUBDIRECTORY = REGULATION_DATA;
    public static final String REGULATORY_PFM_BASENAME = "regulatory_pfm";
    public static final String REGULATORY_REGION_BASENAME = "regulatory_region";
    // Regulatory build and motif features (see Ensembl files: regulatory build and motif features files)
    public static final String REGULATORY_BUILD_NAME = "Regulatory Build";
    public static final String REGULATORY_BUILD_VERSION_FILENAME = "regulatoryBuild" + SUFFIX_VERSION_FILENAME;
    // Motif features (see Ensembl files)
    public static final String MOTIF_FEATURES_NAME = "Motif Features";
    public static final String MOTIF_FEATURES_VERSION_FILENAME = "motifFeatures" + SUFFIX_VERSION_FILENAME;
    // miRBase
    public static final String MIRBASE_NAME = "miRBase";
    public static final String MIRBASE_VERSION_FILENAME = "mirbase" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String MIRBASE_FILE_ID = "MIRBASE";
    // miRTarBase
    public static final String MIRTARBASE_NAME = "miRTarBase";
    public static final String MIRTARBASE_VERSION_FILENAME = "mirTarBase" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String MIRTARBASE_FILE_ID = "MIRTARBASE";

    // Build specific data options
    public static final String GENOME_INFO_DATA = "genome_info";
    public static final String DISGENET_DATA = "disgenet";
    public static final String HPO_DATA = "hpo";
    public static final String CADD_DATA = "cadd";
    public static final String PPI_DATA = "ppi";
    public static final String DRUG_DATA = "drug";

    // Load specific data options
    public static final String PROTEIN_FUNCTIONAL_PREDICTION_DATA = "protein_functional_prediction";

    // Protein
    public static final String PROTEIN_NAME = "Protein";
    public static final String PROTEIN_DATA = "protein";
    public static final String PROTEIN_SUBDIRECTORY = "protein";
    // UniProt
    public static final String UNIPROT_NAME = "UniProt";
    public static final String UNIPROT_CHUNKS_SUBDIRECTORY = "uniprot_chunks";
    public static final String UNIPROT_VERSION_FILENAME = "uniprot" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String UNIPROT_FILE_ID = "UNIPROT";
    // InterPro
    public static final String INTERPRO_NAME = "InterPro";
    public static final String INTERPRO_VERSION_FILENAME = "interpro" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String INTERPRO_FILE_ID = "INTERPRO";
    // IntAct
    public static final String INTACT_NAME = "IntAct";
    public static final String INTACT_VERSION_FILENAME = "intact" + SUFFIX_VERSION_FILENAME;
    // Must match the configuration file
    public static final String INTACT_FILE_ID = "INTACT";

    // Conservation scores
    public static final String CONSERVATION_NAME = "Conservation";
    public static final String CONSERVATION_DATA = "conservation";
    public static final String CONSERVATION_SUBDIRECTORY = "conservation";
    // GERP
    public static final String GERP_NAME = "GERP++";
    public static final String GERP_SUBDIRECTORY = "gerp";
    public static final String GERP_VERSION_FILENAME = "gerp" + SUFFIX_VERSION_FILENAME;
    public static final String GERP_FILE_ID = "GERP";
    // PHASTCONS
    public static final String PHASTCONS_NAME = "PhastCons";
    public static final String PHASTCONS_DATA = "phastCons";
    public static final String PHASTCONS_SUBDIRECTORY = PHASTCONS_DATA;
    public static final String PHASTCONS_VERSION_FILENAME = PHASTCONS_DATA + SUFFIX_VERSION_FILENAME;
    public static final String PHASTCONS_FILE_ID = "PHASTCONS";
    // PHYLOP
    public static final String PHYLOP_NAME = "PhyloP";
    public static final String PHYLOP_DATA = "phylop";
    public static final String PHYLOP_SUBDIRECTORY = PHYLOP_DATA;
    public static final String PHYLOP_VERSION_FILENAME = PHYLOP_DATA + SUFFIX_VERSION_FILENAME;
    public static final String PHYLOP_FILE_ID = "PHYLOP";

    // Splice scores
    public static final String MMSPLICE_SUBDIRECTORY = "mmsplice";
    public static final String MMSPLICE_VERSION_FILENAME = MMSPLICE_SUBDIRECTORY + SUFFIX_VERSION_FILENAME;
    public static final String SPLICEAI_SUBDIRECTORY = "spliceai";
    public static final String SPLICEAI_VERSION_FILENAME = SPLICEAI_SUBDIRECTORY + SUFFIX_VERSION_FILENAME;

    /**
     * @deprecated (when refactoring downloaders, builders and loaders)
     */
    @Deprecated
    public static final String GERP_FILE = "gerp_conservation_scores.homo_sapiens.GRCh38.bw";
    public static final String CLINICAL_VARIANTS_JSON_FILE = "clinical_variants.json.gz";
    public static final String CLINICAL_VARIANTS_ANNOTATED_JSON_FILE = "clinical_variants.full.json.gz";
    public static final String DOCM_NAME = "DOCM";

    public static final String OBO_JSON = "ontology";
    public static final String HPO_VERSION_FILE = "hpo" + SUFFIX_VERSION_FILENAME;
    public static final String GO_VERSION_FILE = "go" + SUFFIX_VERSION_FILENAME;
    public static final String DO_VERSION_FILE = "do" + SUFFIX_VERSION_FILENAME;
    public static final String MONDO_VERSION_FILE = "mondo" + SUFFIX_VERSION_FILENAME;

    public static final String HGMD_FILE = "hgmd.vcf";

    // PubMed
    public static final String PUBMED_NAME = "PubMed";
    public static final String PUBMED_DATA = "pubmed";
    public static final String PUBMED_SUBDIRECTORY = PUBMED_DATA;
    public static final String PUBMED_VERSION_FILENAME = PUBMED_DATA + SUFFIX_VERSION_FILENAME;
    public static final String PUBMED_REGEX_FILE_ID = "PUBMED";

    private EtlCommons() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, String logFilePath)
            throws IOException, InterruptedException, CellBaseException {

        Configurator.setRootLevel(Level.INFO);

        Logger logger = LoggerFactory.getLogger("EtlCommons");

        ProcessBuilder builder = getProcessBuilder(workingDirectory, binPath, args, logFilePath);

        if (logger.isDebugEnabled()) {
            logger.debug("Executing command: {}", StringUtils.join(builder.command(), " "));
        }
        Process process = builder.start();
        process.waitFor();

        // Check process output
        if (process.exitValue() != 0) {
            String msg = "Error executing command '" + binPath + "'; error code = " + process.exitValue() + ". More info in log file: "
                    + logFilePath;
            logger.error(msg);
            throw new CellBaseException(msg);
        }

        return true;
    }

    private static ProcessBuilder getProcessBuilder(File workingDirectory, String binPath, List<String> args, String logFilePath) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(binPath);
        commandArgs.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(commandArgs);

        // working directoy and error and output log outputs
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        builder.redirectErrorStream(true);
        if (logFilePath != null) {
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
        }

        return builder;
    }

    public static boolean isMissing(String string) {
        return !((string != null) && !string.isEmpty()
                && !string.replace(" ", "")
                .replace("not specified", "")
                .replace("NS", "")
                .replace("NA", "")
                .replace("na", "")
                .replace("NULL", "")
                .replace("null", "")
                .replace("\t", "")
                .replace(".", "")
                .replace("-", "").isEmpty());
    }

    public static Long countFileLines(Path filePath) throws IOException {
        try (BufferedReader bufferedReader1 = FileUtils.newBufferedReader(filePath)) {
            long nLines = 0;
            String line1 = bufferedReader1.readLine();
            while (line1 != null) {
                nLines++;
                line1 = bufferedReader1.readLine();
            }
            return nLines;
        }
    }

    public static String getEnsemblUrl(DownloadProperties.EnsemblProperties props, String ensemblRelease, String fileId, String species,
                                       String assembly, String chromosome) throws CellBaseException {
        if (!props.getUrl().getFiles().containsKey(fileId)) {
            throw new CellBaseException(getMissingFileIdMessage(fileId));
        }
        String url = props.getUrl().getHost() + props.getUrl().getFiles().get(fileId);

        // Change release, species, assembly, chromosome if necessary
        if (StringUtils.isNotEmpty(ensemblRelease)) {
            url = url.replace(PUT_RELEASE_HERE_MARK, ensemblRelease.split("-")[1]);
        }
        if (StringUtils.isNotEmpty(species)) {
            url = url.replace(PUT_SPECIES_HERE_MARK, species);
            url = url.replace(PUT_CAPITAL_SPECIES_HERE_MARK, Character.toUpperCase(species.charAt(0)) + species.substring(1));
        }
        if (StringUtils.isNotEmpty(assembly)) {
            url = url.replace(PUT_ASSEMBLY_HERE_MARK, assembly);
        }
        if (StringUtils.isNotEmpty(chromosome)) {
            url = url.replace(PUT_CHROMOSOME_HERE_MARK, chromosome);
        }
        return url;
    }

    public static String getUrl(DownloadProperties.URLProperties props, String fileId) throws CellBaseException {
        return getUrl(props, fileId, null, null, null);
    }

    public static String getUrl(DownloadProperties.URLProperties props, String fileId, String species, String assembly, String chromosome)
            throws CellBaseException {
        if (!props.getFiles().containsKey(fileId)) {
            throw new CellBaseException(getMissingFileIdMessage(fileId));
        }
        String url;
        String filesValue = props.getFiles().get(fileId);
        if (filesValue.startsWith("https://") || filesValue.startsWith("http://") || filesValue.startsWith("ftp://")) {
            url = filesValue;
        } else {
            url = props.getHost() + filesValue;
        }
        if (StringUtils.isNotEmpty(species)) {
            url = url.replace(PUT_SPECIES_HERE_MARK, species);
        }
        if (StringUtils.isNotEmpty(assembly)) {
            url = url.replace(PUT_ASSEMBLY_HERE_MARK, assembly);
        }
        if (StringUtils.isNotEmpty(chromosome)) {
            url = url.replace(PUT_CHROMOSOME_HERE_MARK, chromosome);
        }
        return url;
    }

    public static String getFilename(String prefix, String chromosome) {
        return prefix + "_" + chromosome;
    }

    public static boolean isExecutableAvailable(String executable) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("which", executable);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        // if exitCode is 0 then the executable is installed at + output.toString().trim()),
        // otherwise, it's not
        return (exitCode == 0);
    }

    public static String getFilenameFromProps(DownloadProperties.URLProperties props, String fileId) throws CellBaseException {
        if (!props.getFiles().containsKey(fileId)) {
            throw new CellBaseException(getMissingFileIdMessage(fileId));
        }
        return getFilenameFromUrl(props.getFiles().get(fileId));
    }

    public static String getFilenameFromUrl(String url) {
        return Paths.get(url).getFileName().toString();
    }

    public static void checkDirectory(Path path, String name) throws CellBaseException {
        if (path == null) {
            throw new CellBaseException(name + " directory is null");
        }
        if (!Files.exists(path)) {
            throw new CellBaseException(name + " directory " + path + " does not exist");
        }
        if (!Files.isDirectory(path)) {
            throw new CellBaseException(name + " directory " + path + " is not a directory");
        }
    }

    private static String getMissingFileIdMessage(String fileId) {
        return "File ID " + fileId + " is missing in the DownloadProperties.URLProperties within the CellBase configuration file";
    }
}
