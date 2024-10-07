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
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.download.DownloadFile;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fjlopez on 03/06/16.
 */
public final class EtlCommons {

    // Commons
    public static final String HOMO_SAPIENS = "Homo sapiens";
    public static final String HSAPIENS = "hsapiens";
    public static final String MUS_MUSCULUS = "Mus musculus";
    public static final String RATTUS_NORVEGICUS = "Rattus norvegicus";
    public static final String BOS_TAURUS = "Bos taurus";
    public static final String DANIO_RERIO = "Danio rerio";

    public static final String GRCH38_NAME = "GRCh38";
    public static final String GRCH37_NAME = "GRCh37";
    public static final String HG38_NAME = "hg38";
    public static final String HG19_NAME = "hg19";

    public static final String MANUAL_PREFIX = "manual@";
    public static final String SCRIPT_PREFIX = "script:";

    public static final String SUFFIX_VERSION_FILENAME = "Version.json";

    public static final String XLSX_EXTENSION = ".xlsx";
    public static final String CSV_EXTENSION = ".csv";
    public static final String TBI_EXTENSION = ".tbi";
    public static final String FAI_EXTENSION = ".fai";
    public static final String GZ_EXTENSION = ".gz";

    public static final String OK_MSG = "Ok.";
    public static final String DONE_MSG = "Done.";
    public static final String DATA_NOT_SUPPORTED_MSG = "Data '{}' not supported for species '{}'";

    // Ensembl
    public static final String ENSEMBL_DATA = "ensembl";
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
    public static final String ENSEMBL_DESCRIPTION_FILE_ID = "DESCRIPTION";
    public static final String ENSEMBL_XREFS_FILE_ID = "XREFS";
    public static final String ENSEMBL_CANONICAL_FILE_ID = "CANONICAL";
    public static final String GENOME_INFO_FILE_ID = "GENOME_INFO";
    public static final String VARIATION_FILE_ID = "VARIATION";
    public static final String STRUCTURAL_VARIATIONS_FILE_ID = "STRUCTURAL_VARIATIONS";

    // Genome
    public static final String GENOME_DATA = "genome";
    public static final String GENOME_SEQUENCE_COLLECTION_NAME = "genome_sequence";
    public static final String GENOME_INFO_DATA = "genome_info";

    // Gene
    public static final String GENE_DATA = "gene";
    public static final String GENE_ANNOTATION_DATA = "gene_annotation";
    public static final String GENE_DISEASE_ANNOTATION_DATA = "gene_disease_annotation";

    // RefSeq
    public static final String REFSEQ_DATA = "refseq";
    // Must match the configuration file
    public static final String REFSEQ_GENOMIC_GTF_FILE_ID = "GENOMIC_GTF";
    public static final String REFSEQ_GENOMIC_FNA_FILE_ID = "GENOMIC_FNA";
    public static final String REFSEQ_PROTEIN_FAA_FILE_ID = "PROTEIN_FAA";
    public static final String REFSEQ_RNA_FNA_FILE_ID = "RNA_FNA";

    // Gene annotation
    public static final String ENSEMBL_CANONICAL_DATA = "ensembl_canonical";
    public static final String GENE_EXTRA_INFO_DATA = "gene_extra_info";
    //   - MANE Select
    public static final String MANE_SELECT_DATA = "MANE Select";
    // Must match the configuration file
    public static final String MANE_SELECT_FILE_ID = "MANE_SELECT";
    //   - LRG
    public static final String LRG_DATA = "lrg";
    // Must match the configuration file
    public static final String LRG_FILE_ID = "LRG";
    //   - HGNC
    public static final String HGNC_DATA = "hgnc";
    // Must match the configuration file
    public static final String HGNC_FILE_ID = "HGNC";
    //   - Cancer HotSpot
    public static final String CANCER_HOTSPOT_DATA = "cancer_hotspot";
    // Must match the configuration file
    public static final String CANCER_HOTSPOT_FILE_ID = "CANCER_HOTSPOT";
    //   - DGID (drug)
    public static final String DGIDB_DATA = "dgidb";
    // Must match the configuration file
    public static final String DGIDB_FILE_ID = "DGIDB";
    //   - UniProt Xref
    public static final String UNIPROT_XREF_DATA = "uniprot_xref";
    // Must match the configuration file
    public static final String UNIPROT_XREF_FILE_ID = "UNIPROT_XREF";
    //   - Gene Expression Atlas
    public static final String GENE_EXPRESSION_ATLAS_DATA = "gene_expression_atlas";
    // Must match the configuration file
    public static final String GENE_EXPRESSION_ATLAS_FILE_ID = "GENE_EXPRESSION_ATLAS";
    //   - Gene Disease Annotation
    public static final String GENE_DISEASE_ANNOTATION_NAME = "Gene Disease Annotation";
    //     - HPO
    public static final String HPO_DISEASE_DATA = "hpo_disease";
    // Must match the configuration file
    public static final String HPO_FILE_ID = "HPO";
    //     - DISGENET
    public static final String DISGENET_DATA = "disgenet";
    // Must match the configuration file
    public static final String DISGENET_FILE_ID = "DISGENET";
    //   - gnomAD Constraints
    public static final String GNOMAD_CONSTRAINTS_DATA = "gnomad_constraints";
    // Must match the configuration file
    public static final String GNOMAD_CONSTRAINTS_FILE_ID = "GNOMAD_CONSTRAINTS";
    //   - GO Annotation
    public static final String GO_ANNOTATION_DATA = "go_annotation";
    // Must match the configuration file
    public static final String GO_ANNOTATION_FILE_ID = "GO_ANNOTATION";
    //   - Cancer Gene Census
    public static final String CANCER_GENE_CENSUS_DATA = "cancer_gene_census";
    // Must match the configuration file
    public static final String CANCER_GENE_CENSUS_FILE_ID = "CANCER_GENE_CENSUS";

    // Variation
    public static final String VARIATION_DATA = "variation";
    public static final String DBSNP_DATA = "dbsnp";
    public static final String SNP_DATA = "snp";

    // Pharmacogenomics
    public static final String PHARMACOGENOMICS_DATA = "pharmacogenomics";
    // PharmGKB
    public static final String PHARMGKB_DATA = "pharmgkb";
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
    public static final String MISSENSE_VARIATION_SCORE_DATA = "missense_variation_functional_score";
    // Revel
    public static final String REVEL_DATA = "revel";
    // Must match the configuration file
    public static final String REVEL_FILE_ID = "REVEL";

    // Clinical variants data
    public static final String CLINICAL_VARIANT_DATA = "clinical_variants";
    public static final String CLINICAL_VARIANTS_BASENAME = "clinicalVariants";
    // ClinVar
    public static final String CLINVAR_DATA = "clinvar";
    public static final String CLINVAR_CHUNKS_SUBDIRECTORY = "clinvar_chunks";
    // Must match the configuration file
    public static final String CLINVAR_FULL_RELEASE_FILE_ID = "FULL_RELEASE";
    public static final String CLINVAR_SUMMARY_FILE_ID = "SUMMARY";
    public static final String CLINVAR_ALLELE_FILE_ID = "ALLELE";
    public static final String CLINVAR_EFO_TERMS_FILE_ID = "EFO_TERMS";
    // COSMIC
    public static final String COSMIC_DATA = "cosmic";
    // Must match the configuration file
    public static final String COSMIC_FILE_ID = "COSMIC";
    // HGMD
    public static final String HGMD_DATA = "hgmd";
    // Must match the configuration file
    public static final String HGMD_FILE_ID = "HGMD";
    // GWAS
    public static final String GWAS_DATA = "gwas";
    // Must match the configuration file
    public static final String GWAS_FILE_ID = "GWAS";
    public static final String GWAS_DBSNP_FILE_ID = "DBSNP";

    // Repeats
    public static final String REPEATS_DATA = "repeats";
    // Simple repeats
    public static final String TRF_DATA = "trf";
    // Must match the configuration file
    public static final String SIMPLE_REPEATS_FILE_ID = "SIMPLE_REPEATS";
    // Genomic super duplications
    public static final String GSD_DATA = "gsd";
    // Must match the configuration file
    public static final String GENOMIC_SUPER_DUPS_FILE_ID = "GENOMIC_SUPER_DUPS";
    // Window masker
    public static final String WM_DATA = "wm";
    // Must match the configuration file
    public static final String WINDOW_MASKER_FILE_ID = "WINDOW_MASKER";

    // Ontology
    public static final String ONTOLOGY_DATA = "ontology";
    // HPO
    public static final String HPO_OBO_DATA = "hpo";
    // Must match the configuration file
    public static final String HPO_OBO_FILE_ID = "HPO";
    // GO
    public static final String GO_OBO_DATA = "go";
    // Must match the configuration file
    public static final String GO_OBO_FILE_ID = "GO";
    // DOID
    public static final String DOID_OBO_DATA = "doid";
    // Must match the configuration file
    public static final String DOID_OBO_FILE_ID = "DOID";
    // MONDO
    public static final String MONDO_OBO_DATA = "mondo";
    // Must match the configuration file
    public static final String MONDO_OBO_FILE_ID = "MONDO";


    public static final String PFM_DATA = "regulatory_pfm";

    // Variation functional score
    public static final String VARIATION_FUNCTIONAL_SCORE_DATA = "variation_functional_score";
    // CADD scores
    public static final String CADD_DATA = "cadd";
    public static final String CADD_RAW_DATA = "cadd_raw";
    public static final String CADD_SCALED_DATA = "cadd_scaled";
    // Must match the configuration file
    public static final String CADD_FILE_ID = "CADD";

    // Regulation
    public static final String REGULATION_DATA = "regulation";
    // Regulatory build and motif features (see Ensembl files: regulatory build and motif features files)
    public static final String REGULATORY_BUILD_DATA = "regulatory_build";
    // Motif features (see Ensembl files)
    public static final String MOTIF_FEATURES_DATA = "motif_features";
    // miRBase
    public static final String MIRBASE_DATA = "mirbase";
    // Must match the configuration file
    public static final String MIRBASE_FILE_ID = "MIRBASE";
    // miRTarBase
    public static final String MIRTARBASE_DATA = "mirtarbase";
    // Must match the configuration file
    public static final String MIRTARBASE_FILE_ID = "MIRTARBASE";

    // Load specific data options
    public static final String PROTEIN_FUNCTIONAL_PREDICTION_DATA = "protein_functional_prediction";

    // Protein
    public static final String PROTEIN_DATA = "protein";
    // UniProt
    public static final String UNIPROT_DATA = "uniprot";
    public static final String UNIPROT_CHUNKS_SUBDIRECTORY = "uniprot_chunks";
    // Must match the configuration file
    public static final String UNIPROT_FILE_ID = "UNIPROT";
    // InterPro
    public static final String INTERPRO_DATA = "interpro";
    // Must match the configuration file
    public static final String INTERPRO_FILE_ID = "INTERPRO";
    // IntAct
    public static final String INTACT_DATA = "intact";
    // Must match the configuration file
    public static final String INTACT_FILE_ID = "INTACT";

    // Conservation scores
    public static final String CONSERVATION_DATA = "conservation";
    // GERP
    public static final String GERP_DATA = "gerp";
    // Must match the configuration file
    public static final String GERP_FILE_ID = "GERP";
    // PHASTCONS
    public static final String PHASTCONS_DATA = "phastCons";
    // Must match the configuration file
    public static final String PHASTCONS_FILE_ID = "PHASTCONS";
    // PHYLOP
    public static final String PHYLOP_DATA = "phylop";
    // Must match the configuration file
    public static final String PHYLOP_FILE_ID = "PHYLOP";

    // Splice scores
    public static final String SPLICE_SCORE_DATA = "splice_score";
    // MMSplice
    public static final String MMSPLICE_DATA = "mmsplice";
    // SpliceAI
    public static final String SPLICEAI_DATA = "spliceai";

    /**
     * @deprecated (when refactoring downloaders, builders and loaders)
     */
    @Deprecated
    public static final String GERP_FILE = "gerp_conservation_scores.homo_sapiens.GRCh38.bw";
    public static final String CLINICAL_VARIANTS_JSON_FILE = "clinical_variants.json.gz";
    public static final String CLINICAL_VARIANTS_ANNOTATED_JSON_FILE = "clinical_variants.full.json.gz";
    public static final String DOCM_NAME = "DOCM";
    public static final String HPO_VERSION_FILE = "hpo" + SUFFIX_VERSION_FILENAME;
    public static final String GO_VERSION_FILE = "go" + SUFFIX_VERSION_FILENAME;
    public static final String DO_VERSION_FILE = "do" + SUFFIX_VERSION_FILENAME;
    public static final String MONDO_VERSION_FILE = "mondo" + SUFFIX_VERSION_FILENAME;

    public static final String HGMD_FILE = "hgmd.vcf";

    // PubMed
    public static final String PUBMED_DATA = "pubmed";
    // Must match the configuration file
    public static final String PUBMED_REGEX_FILE_ID = "PUBMED_REGEX";

    // Utilities maps
    private static Map<String, String> dataNamesMap = new HashMap<>();
    private static Map<String, String> dataCategoriesMap = new HashMap<>();
    private static Map<String, String> dataVersionFilenamesMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlCommons.class);

    static {

        // Populate data names map
        dataNamesMap.put(ENSEMBL_DATA, "Ensembl");
        dataNamesMap.put(REFSEQ_DATA, "RefSeq");
        dataNamesMap.put(GENOME_DATA, "Genome");
        dataNamesMap.put(GENOME_INFO_DATA, "Genome Info");
        dataNamesMap.put(GENE_DATA, "Gene");
        dataNamesMap.put(ENSEMBL_CANONICAL_DATA, "Ensembl canonical");
        dataNamesMap.put(GENE_EXTRA_INFO_DATA, "Gene extra info");
        dataNamesMap.put(GENE_ANNOTATION_DATA, "Gene Annotation");
        dataNamesMap.put(MANE_SELECT_DATA, "MANE Select");
        dataNamesMap.put(LRG_DATA, "LRG");
        dataNamesMap.put(HGNC_DATA, "HGNC Gene");
        dataNamesMap.put(CANCER_HOTSPOT_DATA, "Cancer HotSpot");
        dataNamesMap.put(DGIDB_DATA, "DGIdb");
        dataNamesMap.put(UNIPROT_XREF_DATA, "UniProt Xref");
        dataNamesMap.put(GENE_EXPRESSION_ATLAS_DATA, "Gene Expression Atlas");
        dataNamesMap.put(GENE_DISEASE_ANNOTATION_DATA, "Gene Disease Annotation");
        dataNamesMap.put(HPO_DISEASE_DATA, "HPO Disease");
        dataNamesMap.put(DISGENET_DATA, "DisGeNet");
        dataNamesMap.put(GNOMAD_CONSTRAINTS_DATA, "gnomAD Constraint");
        dataNamesMap.put(GO_ANNOTATION_DATA, "EBI Gene Ontology Annotation");
        dataNamesMap.put(CANCER_GENE_CENSUS_DATA, "Cancer Gene Census");
        dataNamesMap.put(PROTEIN_DATA, "Protein");
        dataNamesMap.put(UNIPROT_DATA, "UniProt");
        dataNamesMap.put(INTERPRO_DATA, "InterPro");
        dataNamesMap.put(INTACT_DATA, "IntAct");
        dataNamesMap.put(CONSERVATION_DATA, "Conservation");
        dataNamesMap.put(GERP_DATA, "GERP++");
        dataNamesMap.put(PHASTCONS_DATA, "PhastCons");
        dataNamesMap.put(PHYLOP_DATA, "PhyloP");
        dataNamesMap.put(REPEATS_DATA, "Repeats");
        dataNamesMap.put(TRF_DATA, "Tandem Repeats Finder");
        dataNamesMap.put(WM_DATA, "Window Masker");
        dataNamesMap.put(GSD_DATA, "Genomic Super Duplications");
        dataNamesMap.put(REGULATION_DATA, "Regulation");
        dataNamesMap.put(REGULATORY_BUILD_DATA, "Regulatory Build");
        dataNamesMap.put(MOTIF_FEATURES_DATA, "Motif Features");
        dataNamesMap.put(MIRBASE_DATA, "miRBase");
        dataNamesMap.put(MIRTARBASE_DATA, "miRTarBase");
        dataNamesMap.put(ONTOLOGY_DATA, "Ontology");
        dataNamesMap.put(HPO_OBO_DATA, "HPO");
        dataNamesMap.put(GO_OBO_DATA, "GO");
        dataNamesMap.put(DOID_OBO_DATA, "DOID");
        dataNamesMap.put(MONDO_OBO_DATA, "Mondo");
        dataNamesMap.put(PUBMED_DATA, "PubMed");
        dataNamesMap.put(PHARMACOGENOMICS_DATA, "Pharmacogenomics");
        dataNamesMap.put(PHARMGKB_DATA, "PharmGKB");
        dataNamesMap.put(VARIATION_FUNCTIONAL_SCORE_DATA, "Variant Functional Score");
        dataNamesMap.put(CADD_DATA, "CADD");
        dataNamesMap.put(MISSENSE_VARIATION_SCORE_DATA, "Missense Variation Score");
        dataNamesMap.put(REVEL_DATA, "Revel");
        dataNamesMap.put(CLINICAL_VARIANT_DATA, "Clinical Variant");
        dataNamesMap.put(CLINVAR_DATA, "ClinVar");
        dataNamesMap.put(COSMIC_DATA, "Cosmic");
        dataNamesMap.put(HGMD_DATA, "HGMD");
        dataNamesMap.put(GWAS_DATA, "GWAS Catalog");
        dataNamesMap.put(SPLICE_SCORE_DATA, "Splice Score");
        dataNamesMap.put(MMSPLICE_DATA, "MMSplice");
        dataNamesMap.put(SPLICEAI_DATA, "SpliceAI");
        dataNamesMap.put(VARIATION_DATA, "Variation");
        dataNamesMap.put(SNP_DATA, "SNP");
        dataNamesMap.put(DBSNP_DATA, "dbSNP");


        // Populate data categories map
        dataCategoriesMap.put(ENSEMBL_DATA, "Gene");
        dataCategoriesMap.put(REFSEQ_DATA, "Gene");
        dataCategoriesMap.put(GENOME_DATA, dataNamesMap.get(ENSEMBL_DATA));
        dataCategoriesMap.put(MANE_SELECT_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(LRG_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(HGNC_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(CANCER_HOTSPOT_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(DGIDB_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(UNIPROT_XREF_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(GENE_EXPRESSION_ATLAS_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(HPO_DISEASE_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(DISGENET_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(GNOMAD_CONSTRAINTS_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(GO_ANNOTATION_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(CANCER_GENE_CENSUS_DATA, dataNamesMap.get(GENE_ANNOTATION_DATA));
        dataCategoriesMap.put(UNIPROT_DATA, dataNamesMap.get(PROTEIN_DATA));
        dataCategoriesMap.put(INTERPRO_DATA, dataNamesMap.get(PROTEIN_DATA));
        dataCategoriesMap.put(INTACT_DATA, dataNamesMap.get(PROTEIN_DATA));
        dataCategoriesMap.put(GERP_DATA, dataNamesMap.get(CONSERVATION_DATA));
        dataCategoriesMap.put(PHASTCONS_DATA, dataNamesMap.get(CONSERVATION_DATA));
        dataCategoriesMap.put(PHYLOP_DATA, dataNamesMap.get(CONSERVATION_DATA));
        dataCategoriesMap.put(TRF_DATA, dataNamesMap.get(REPEATS_DATA));
        dataCategoriesMap.put(WM_DATA, dataNamesMap.get(REPEATS_DATA));
        dataCategoriesMap.put(GSD_DATA, dataNamesMap.get(REPEATS_DATA));
        dataCategoriesMap.put(REGULATORY_BUILD_DATA, dataNamesMap.get(REGULATION_DATA));
        dataCategoriesMap.put(MOTIF_FEATURES_DATA, dataNamesMap.get(REGULATION_DATA));
        dataCategoriesMap.put(MIRBASE_DATA, dataNamesMap.get(REGULATION_DATA));
        dataCategoriesMap.put(MIRTARBASE_DATA, dataNamesMap.get(REGULATION_DATA));
        dataCategoriesMap.put(HPO_OBO_DATA, dataNamesMap.get(ONTOLOGY_DATA));
        dataCategoriesMap.put(GO_OBO_DATA, dataNamesMap.get(ONTOLOGY_DATA));
        dataCategoriesMap.put(DOID_OBO_DATA, dataNamesMap.get(ONTOLOGY_DATA));
        dataCategoriesMap.put(MONDO_OBO_DATA, dataNamesMap.get(ONTOLOGY_DATA));
        dataCategoriesMap.put(PUBMED_DATA, "Publication");
        dataCategoriesMap.put(PHARMGKB_DATA, dataNamesMap.get(PHARMACOGENOMICS_DATA));
        dataCategoriesMap.put(CADD_DATA, dataNamesMap.get(VARIATION_FUNCTIONAL_SCORE_DATA));
        dataCategoriesMap.put(REVEL_DATA, dataNamesMap.get(MISSENSE_VARIATION_SCORE_DATA));
        dataCategoriesMap.put(CLINVAR_DATA, dataNamesMap.get(CLINICAL_VARIANT_DATA));
        dataCategoriesMap.put(COSMIC_DATA, dataNamesMap.get(CLINICAL_VARIANT_DATA));
        dataCategoriesMap.put(HGMD_DATA, dataNamesMap.get(CLINICAL_VARIANT_DATA));
        dataCategoriesMap.put(GWAS_DATA, dataNamesMap.get(CLINICAL_VARIANT_DATA));
        dataCategoriesMap.put(MMSPLICE_DATA, dataNamesMap.get(SPLICE_SCORE_DATA));
        dataCategoriesMap.put(SPLICEAI_DATA, dataNamesMap.get(SPLICE_SCORE_DATA));
        dataCategoriesMap.put(VARIATION_DATA, dataNamesMap.get(VARIATION_DATA));
        dataCategoriesMap.put(SNP_DATA, dataNamesMap.get(VARIATION_DATA));
        dataCategoriesMap.put(DBSNP_DATA, dataNamesMap.get(VARIATION_DATA));

        // Populate data version filenames Map
        dataVersionFilenamesMap.put(ENSEMBL_DATA, "ensemblCore" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(REFSEQ_DATA, "refSeqCore" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GENOME_DATA, "genome" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MANE_SELECT_DATA, "maneSelect" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(LRG_DATA, "lrg" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(HGNC_DATA, "hgnc" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(CANCER_HOTSPOT_DATA, "cancerHotSpot" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(DGIDB_DATA, "dgidb" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(UNIPROT_XREF_DATA, "uniProtXref" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GENE_EXPRESSION_ATLAS_DATA, "geneExpressionAtlas" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(HPO_DISEASE_DATA, "hpoDisease" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(DISGENET_DATA, "disGeNet" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GNOMAD_CONSTRAINTS_DATA, "gnomadConstraints" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GO_ANNOTATION_DATA, "goAnnotation" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(CANCER_GENE_CENSUS_DATA, "cancerGeneCensus" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(UNIPROT_DATA, "uniProt" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(INTERPRO_DATA, "interPro" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(INTACT_DATA, "intAct" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GERP_DATA, "gerp" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(PHASTCONS_DATA, "phastCons" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(PHYLOP_DATA, "phyloP" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(TRF_DATA, "simpleRepeat" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(WM_DATA, "windowMasker" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GSD_DATA, "genomicSuperDups" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(REGULATORY_BUILD_DATA, "regulatoryBuild" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MOTIF_FEATURES_DATA, "motifFeatures" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MIRBASE_DATA, "mirBase" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MIRTARBASE_DATA, "mirTarBase" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(HPO_OBO_DATA, "hpoObo" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GO_OBO_DATA, "goObo" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(DOID_OBO_DATA, "doidObo" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MONDO_OBO_DATA, "mondoObo" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(PUBMED_DATA, "pubMed" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(PHARMGKB_DATA, "pharmGkb" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(CADD_DATA, "cadd" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(REVEL_DATA, "revel" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(CLINVAR_DATA, "clinVar" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(COSMIC_DATA, "cosmic" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(HGMD_DATA, "hgmd" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(GWAS_DATA, "gwas" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(MMSPLICE_DATA, "mmSplice" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(SPLICEAI_DATA, "spliceAi" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(VARIATION_DATA, "variation" + SUFFIX_VERSION_FILENAME);
        dataVersionFilenamesMap.put(DBSNP_DATA, "dbSnp" + SUFFIX_VERSION_FILENAME);
    }

    private EtlCommons() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, Path logFile)
            throws IOException, InterruptedException, CellBaseException {

        ProcessBuilder builder = getProcessBuilder(workingDirectory, binPath, args, logFile);

        LOGGER.info("Executing command: {}", StringUtils.join(builder.command(), " "));
        Process process = builder.start();
        process.waitFor();

        // Check process output
//        if (process.exitValue() != 0) {
//            String msg = "Error executing command '" + binPath + "'; args = " + args + ", error code = " + process.exitValue()
//                    + ". More info in log file: " + logFilePath;
//            logger.error(msg);
//            throw new CellBaseException(msg);
//        }

        return true;
    }

    private static ProcessBuilder getProcessBuilder(File workingDirectory, String binPath, List<String> args, Path logFile) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(binPath);
        commandArgs.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(commandArgs);

        // working directoy and error and output log outputs
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        builder.redirectErrorStream(true);
        if (logFile != null) {
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        }

        return builder;
    }

    public static Path getFastaPath(Path gzFastaPath) throws CellBaseException {
        // Sanity check
        if (!Files.exists(gzFastaPath)) {
            throw new CellBaseException("Gzipped FASTA file " + gzFastaPath + " does not exist");
        }

        // Check FASTA and unzip if necessary
        Path fastaPath = gzFastaPath.getParent().resolve(gzFastaPath.getFileName().toString().replace(GZ_EXTENSION, ""));
        if (!fastaPath.toFile().exists()) {
            // Gunzip
            LOGGER.info("Gunzip file {}", gzFastaPath);
            try {
                List<String> params = Arrays.asList("--keep", gzFastaPath.toString());
                EtlCommons.runCommandLineProcess(null, "gunzip", params, null);
            } catch (IOException e) {
                throw new CellBaseException("Error executing gunzip in FASTA file " + gzFastaPath, e);
            } catch (InterruptedException e) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new CellBaseException("Error executing gunzip in FASTA file " + gzFastaPath, e);
            }
        }
        if (!fastaPath.toFile().exists()) {
            throw new CellBaseException("FASTA file " + fastaPath + " does not exist after executing gunzip");
        }
        return fastaPath;
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

    public static String getDataName(String data) throws CellBaseException {
        if (!dataNamesMap.containsKey(data)) {
            throw new CellBaseException("Name not found for data '" + data + "'");
        }
        return dataNamesMap.get(data);
    }

    public static String getDataCategory(String data) throws CellBaseException {
        if (!dataCategoriesMap.containsKey(data)) {
            throw new CellBaseException("Category not found for data '" + data + "'");
        }
        return dataCategoriesMap.get(data);
    }

    public static String getDataVersionFilename(String data) throws CellBaseException {
        if (!dataVersionFilenamesMap.containsKey(data)) {
            throw new CellBaseException("Version filename not found for data '" + data + "'");
        }
        return dataVersionFilenamesMap.get(data);
    }

    public static List<String> getUrls(List<DownloadFile> downloadFiles) {
        return downloadFiles.stream().map(DownloadFile::getUrl).collect(Collectors.toList());
    }

    public static String getManualUrl(DownloadProperties.URLProperties props, String fileId) {
        return getManualUrl(props.getHost(), props.getFiles().get(fileId));
    }

    public static String getManualUrl(String host, String file) {
        if (file.startsWith(MANUAL_PREFIX)) {
            return MANUAL_PREFIX + host + file.replace(MANUAL_PREFIX, "");
        }
        return null;
    }

    public static List<String> getDataList(String data, CellBaseConfiguration configuration, SpeciesConfiguration speciesConfiguration)
            throws CellBaseException {
        switch (data) {
            case REPEATS_DATA: {
                return getRepeatsDataList(configuration, speciesConfiguration);
            }
            default: {
                throw new CellBaseException("Unknown data " + data);
            }
        }
    }

    private static List<String> getRepeatsDataList(CellBaseConfiguration configuration, SpeciesConfiguration speciesConfiguration) {
        List<String> dataList = new ArrayList<>();
        String speciesId = speciesConfiguration.getId().toUpperCase(Locale.ROOT);
        if (speciesId.equalsIgnoreCase(HSAPIENS)) {
            return Arrays.asList(TRF_DATA, WM_DATA, GSD_DATA);
        }

        if (isDataSupported(configuration.getDownload().getSimpleRepeats(), speciesId)) {
            dataList.add(TRF_DATA);
        }
        if (isDataSupported(configuration.getDownload().getWindowMasker(), speciesId)) {
            dataList.add(WM_DATA);
        }
        if (isDataSupported(configuration.getDownload().getGenomicSuperDups(), speciesId)) {
            dataList.add(GSD_DATA);
        }
        return dataList;
    }

    public static boolean isDataSupported(DownloadProperties.URLProperties props, String prefix) {
        for (String key : props.getFiles().keySet()) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
