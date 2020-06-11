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

package org.opencb.cellbase.core;

public class ParamConstants {

    public static final String DOT_NOTATION_NOTE = "Parameters can be camel case (e.g. transcriptsBiotype) "
            + "or dot notation (e.g. transcripts.biotype).";
    public static final String VERSION_DESCRIPTION = "Possible values: v4, v5";
    public static final String DEFAULT_VERSION = "v5";
    public static final String ASSEMBLY_DESCRIPTION = "Set the reference genome assembly, e.g. grch38. For a full list of "
            + "potentially available assemblies, please refer to: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species";

    public static final String COUNT_DESCRIPTION = "Get the total number of results matching the query. ";

    public static final String SPLIT_RESULT_DESCRIPTION = "If TRUE, each id provided will be a separate result, even if no records are "
            + "found. If FALSE, one merged result will be returned that includes all records.";
    public static final String SPLIT_RESULT_PARAM = "splitResultById";

    public static final String INCLUDE_DESCRIPTION = "Fields included in the response, whole JSON path must be provided";
    public static final String EXCLUDE_DESCRIPTION = "Fields excluded in the response, whole JSON path must be provided";
    public static final String LIMIT_DESCRIPTION = "Number of results to be returned";
    public static final String DEFAULT_LIMIT = "10";
    public static final String SKIP_DESCRIPTION = "Number of results to skip";
    public static final String DEFAULT_SKIP = "0";
    public static final String SORT_DESCRIPTION = "Sort returned results by a certain data model attribute";
    public static final String ORDER_DESCRIPTION = "Results are in ascending order by default";

    public static final String SPECIES_DESCRIPTION = "Name of the species, e.g. hsapiens. For a full list "
            + "of potentially available species ids, please refer to: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species";
    public static final String DATA_MODEL_DESCRIPTION = "Get JSON specification of the data model";

    public static final String REGION_DESCRIPTION = "Comma separated list of genomic regions to be queried, "
            + "e.g. 1:6635137-6635325. Use the chromosome name only to filter by the entire chromosome, e.g. 1";

    public static final String CHROMOSOMES = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT";

    public static final String GROUP_BY_FIELDS = "Comma separated list of field(s) to group by, "
            + "e.g. biotype, transcriptsBiotype, transcripts.biotype";

    // ---------------------------------------------

    public static final String GENE_IDS = "Comma separated list of gene ids, e.g. ENSG00000268020,BRCA2"
            + " Exact text matches will be returned";
    public static final String GENE_ENSEMBL_IDS = "Comma separated list of ENSEMBL gene ids, "
            + "e.g. ENSG00000132170,ENSG00000155657. Exact text matches will be returned";
    public static final String GENE_XREF_IDS = "Comma separated list gene/transcript xrefs ids, e.g. "
            + " ENSG00000145113,35912_at,GO:0002020.  Exact text matches will be returned";
    public static final String GENE_NAMES = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
            + " Exact text matches will be returned";
    public static final String GENE_BIOTYPES = "Comma separated list of gene gencode biotypes,"
            + " e.g. protein_coding,miRNA,lncRNA. Exact text matches will be returned";


    // ---------------------------------------------

    public static final String ONTOLOGY_DESCRIPTION = "Comma separated list of ontology ids or term names, "
            + "e.g. GO:0008343,HP:0001251,histone kinase activity";
    public static final String ONTOLOGY_PARAM = "ontology";

    public static final String ONTOLOGY_IDS_DESCRIPTION = "Comma separated list of ontology ids, e.g. GO:0008343,HP:0001251";
    public static final String ONTOLOGY_IDS_PARAM = "ontologyId";
    public static final String ONTOLOGY_NAMES = "Comma separated list of ontology term names, "
            + "e.g. Diabetes mellitus,histone kinase activity";
    public static final String ONTOLOGY_NAMESPACES = "Comma separated list of namespaces, e.g. human_phenotype,biological_process. "
            + "For a full list of potentially available namespaces, please refer to: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/feature/ontology/distinct?field=namespace";
    public static final String ONTOLOGY_SYNONYMS = "Comma separated list of synonyms, e.g. Cerebellar ataxia";
    public static final String ONTOLOGY_XREFS = "Comma separated list of cross references, e.g. MSH:D002524";
    public static final String ONTOLOGY_PARENTS = "Comma separated list of ontology terms, e.g. GO:0019882";
    public static final String ONTOLOGY_CHILDREN = "Comma separated list of ontology terms, e.g. GO:0019882";

    // ---------------------------------------------

    public static final String TRANSCRIPT_BIOTYPES_DESCRIPTION = "Comma separated list of transcript gencode biotypes, "
            + "e.g. protein_coding,miRNA,lncRNA. Exact text matches will be returned";
    public static final String TRANSCRIPT_BIOTYPES_PARAM = "transcriptBiotype";
    public static final String TRANSCRIPT_XREFS_DESCRIPTION = "Comma separated list transcript xrefs ids, "
            + "e.g. ENSG00000145113,35912_at,GO:0002020. Exact text matches will be returned";
    public static final String TRANSCRIPT_XREFS_PARAM = "xref";
    public static final String TRANSCRIPT_XREF = "String indicating one ENSEMBL transcript id e.g.: ENST00000536068. "
            + "Exact text matches will be returned";
    public static final String TRANSCRIPT_IDS_DESCRIPTION = "Comma separated list of ENSEMBL transcript ids, "
            + "e.g. ENST00000342992,ENST00000380152,ENST00000544455. Exact text matches will be returned";
    public static final String TRANSCRIPT_IDS_PARAM = "transcriptId";
    public static final String TRANSCRIPT_NAMES_DESCRIPTION = "Comma separated list of transcript names, e.g. BRCA2-201,TTN-003."
            + " Exact text matches will be returned";
    public static final String TRANSCRIPT_NAMES_PARAM = "transcriptName";
    public static final String TRANSCRIPT_ANNOTATION_FLAGS_DESCRIPTION = "Comma separated list of annotation flags that must be "
            + "present in the transcripts returned within the gene model, e.g. basic,CCDS. Exact text matches will "
            + "be returned";
    public static final String TRANSCRIPT_ANNOTATION_FLAGS_PARAM = "transcriptAnnotationFlags";
    public static final String TRANSCRIPT_TFBS_IDS_DESCRIPTION = "Comma separated list of TFBS ids, "
            + "e.g. ENSM00526048956,ENSM00522505783."
            + " Exact text matches will be returned";
    public static final String TRANSCRIPT_TFBS_IDS_PARAM = "tfbsId";
    public static final String TRANSCRIPT_TFBS_PFMIDS_DESCRIPTION = "Comma separated list of TFBS pfm ids, "
            + "e.g. ENSPFM0571,ENSPFM0402."
            + " Exact text matches will be returned";
    public static final String TRANSCRIPT_TFBS_PFMIDS_PARAM = "pfmId";
    public static final String TRANSCRIPT_TRANSCRIPTION_FACTORS_DESCRIPTION = "Comma separated list of transcription factors, "
            + "e.g. MYBL1,MAX"
            + " Exact text matches will be returned";
    public static final String TRANSCRIPT_TRANSCRIPTION_FACTORS_PARAM = "transcriptionFactor";
    public static final String TRANSCRIPT_SUPPORT_LEVEL_DESCRIPTION = "This highlights the well-supported and poorly-supported "
            + "transcript models. Valid values are 1 (all splice junctions of the transcript are supported) "
            + "to 5 (no single transcript supports the model structure) and NA (not analysed), "
            + "see https://www.ensembl.org/info/genome/genebuild/transcript_quality_tags.html for details.";
    public static final String TRANSCRIPT_SUPPORT_LEVEL_PARAM = "transcriptSupportLevel";

    // ---------------------------------------------

    public static final String ANNOTATION_DISEASES_DESCRIPTION = "Comma separated list of disease IDs or names, "
            + "e.g. umls:C0030297,OMIM:613390,OMIM:613390,Cryptorchidism,Absent thumb,Stage 5 chronic kidney disease. "
            + "Exact text matches will be returned";
    public static final String ANNOTATION_DISEASES_PARAM = "diseaseId";
    public static final String ANNOTATION_DISEASES_IDS_DESCRIPTION = "Comma separated list of phenotype ids (OMIM, UMLS), "
            + "e.g. umls:C0030297,OMIM:613390,OMIM:613390. Exact text matches will be returned";
    public static final String ANNOTATION_DISEASES_IDS_PARAM = "diseaseId";
    public static final String ANNOTATION_DISEASES_NAMES_DESCRIPTION = "Comma separated list of phenotypes, "
            + "e.g. Cryptorchidism,Absent thumb,Stage 5 chronic kidney disease. Exact text matches will be returned";
    public static final String ANNOTATION_DISEASES_NAMES_PARAM = "diseaseName";
    public static final String ANNOTATION_EXPRESSION_GENE = "Comma separated list of ENSEMBL gene ids for which "
            + "expression values are available, e.g. ENSG00000139618,ENSG00000155657. "
            + "Exact text matches will be returned";
    public static final String ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION = "Comma separated list of tissues for which "
            + "expression values are available, e.g. adipose tissue,heart atrium,tongue. "
            + "Exact text matches will be returned";
    public static final String ANNOTATION_EXPRESSION_TISSUE_PARAM = "expressionTissue";
    public static final String ANNOTATION_EXPRESSION_VALUE_DESCRIPTION = "Must be used together with annotation.expression.tissue -"
            + "value of interest, either UP or DOWN";
    public static final String ANNOTATION_EXPRESSION_VALUE_PARAM = "expressionValue";
    public static final String ANNOTATION_DRUGS_NAME_DESCRIPTION = "Comma separated list of drug names, "
            + "e.g. BMN673,OLAPARIB,VELIPARIB. Exact text matches will be returned";
    public static final String ANNOTATION_DRUGS_NAME_PARAM = "drugName";
    public static final String ANNOTATION_DRUGS_GENE = "Comma separated list of gene names for which "
            + "drug data is available, e.g. BRCA2,TTN. Exact text matches will be returned";

    public static final String ANNOTATION_CONSTRAINTS_PARAM = "constraint";
    public static final String ANNOTATION_CONSTRAINTS_DESCRIPTION = "Name of constraint and desired value, "
            + "e.g. exac_oe_lof<=1.0,exac_pLI>0. Allowed values for names are exac_oe_lof, exac_pLI, oe_lof, oe_mis, oe_syn";
//    public static final String ANNOTATION_CONSTRAINTS_VALUE_DESCRIPTION = "Value for gnomAD constraints, e.g. >0.1 or <=1.0";
//    public static final String ANNOTATION_CONSTRAINTS_VALUE_PARAM = "constraintValue";
//    public static final String ANNOTATION_CONSTRAINTS_NAME_PARAM = "constraintName";
    public static final String ANNOTATION_TARGETS_DESCRIPTION = "e.g. MIRT001919 or hsa-miR-146a-5p";
    public static final String ANNOTATION_TARGETS_PARAM = "miRnaTarget";

    public static final String MIRNA_DESCRIPTION = "Id or accession for miRNA, e.g. MI0022666 or hsa-mir-8069-1";


    // ---------------------------------------------

    public static final String REFERENCE = "Comma separated list of possible reference to be queried, e.g. A,T";
    public static final String ALTERNATE = "Comma separated list of possible alternate to be queried, e.g. A,T";
    public static final String CONSEQUENCE_TYPE = "Comma separated list of possible SO names describing consequence"
            + " types to be queried, e.g. missense_variant,downstream_variant. Exact text matches will be retrieved.";

    // ---------------------------------------------

    public static final String TFBS_IDS = "String containing a comma separated list of TF names to search, e.g. CTCF";

    // ---------------------------------------------

    public static final String PROTEIN_KEYWORD = "Comma separated list of keywords that may be associated with the"
            + " protein(s), e.g. Transcription,Zinc. Exact text matches will be returned";
    public static final String PROTEIN_XREF_IDS = "Comma separated list of xrefs ids, e.g. CCDS31418.1,Q9UL59,"
            + " ENST00000278314. Exact text matches will be returned";
    public static final String PROTEIN_XREF_ID = "String indicating one xref id, e.g.: Q9UL59, Exact text matches will be returned";
    public static final String PROTEIN_ACCESSIONS = "Comma separated list of UniProt accession ids, "
            + "e.g. Q9UL59,B2R8Q1,Q9UKT9. Exact text matches will be returned";
    public static final String PROTEIN_ACCESSION = "A UniProt accession id, e.g. Q9UL59.";
    public static final String PROTEIN_NAMES = "Comma separated list of protein names, e.g.: ZN214_HUMAN,MKS1_HUMAN. "
            + "Exact text matches will be returned";

    // ---------------------------------------------

    public static final String RS_IDS = "Comma separated list of rs ids, e.g.: rs6025, rs666"
            + " Exact text matches will be returned";

    // ---------------------------------------------

    public static final String SOURCE_DESCRIPTION = "Comma separated list of database sources of the documents to be returned."
            + " Possible values are clinvar or cosmic";
    public static final String SOURCE_PARAM = "source";
    public static final String SEQUENCE_ONTOLOGY_DESCRIPTION = "Comma separated list of sequence ontology term names, "
            + "e.g. missense_variant. Exact text matches will be returned. A list of searchable SO term names can be"
            + " accessed at https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/feature/variation/consequence_types";
    public static final String SEQUENCE_ONTOLOGY_PARAM = "so";
    public static final String FEATURE_IDS_DESCRIPTION = "Comma separated list of feature ids, which can be either ENSEMBL gene "
            + "ids, HGNC gene symbols, transcript symbols or ENSEMBL transcript ids, e.g.: BRCA2,ENST00000409047. "
            + "Exact text matches will be returned.";
    public static final String FEATURE_IDS_PARAM = "feature";
    public static final String TRAITS_DESCRIPTION = "Keywords search. Comma separated (no spaces in between) list of "
            + "keywords describing required phenotype/disease. All variants related somehow with all those keywords "
            + "(case insensitive) will be returned, e.g: carcinoma,lung or acute,myeloid,leukaemia. WARNING: returned "
            + "numTotalResults will always be -1 when searching by trait keywords.";
    public static final String TRAITS_PARAM = "trait";
    public static final String VARIANT_ACCESSIONS_DESCRIPTION = "Comma separated list of database accessions, "
            + "e.g. RCV000033215,COSM306824 Exact text  matches will be returned.";
    public static final String VARIANT_ACCESSIONS_PARAM = "accession";
    public static final String VARIANT_IDS_DESCRIPTION = "Comma separated list of ids, e.g. rs6025,COSM306824. "
            + "Exact text matches will be returned.";
    public static final String VARIANT_IDS_PARAM = "id";
    public static final String VARIANT_TYPES_DESCRIPTION = "Comma separated list of variant types, e.g. \"SNV\" A list of "
            + "searchable types can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/type";
    public static final String VARIANT_TYPES_PARAM = "type";
    public static final String CONSISTENCY_STATUS_DESCRIPTION = "Comma separated list of consistency labels. A list of searchable "
            + "consistency labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/consistency_labels";
    public static final String CONSISTENCY_STATUS_PARAM = "consistencyStatus";
    public static final String CLINICAL_SIGNFICANCE_DESCRIPTION = "Comma separated list of clinical significance labels. "
            + "A list of searchable clinical  significance labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/clinsig_labels"
            + " WARNING: returned numTotalResults will always be -1 if more than 1 label is provided.";
    public static final String CLINICAL_SIGNFICANCE_PARAM = "clinicalSignificance";
    public static final String MODE_INHERITANCE_DESCRIPTION = "Comma separated list of mode of inheritance labels. A list of "
            + "searchable mode of inheritance labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/mode_inheritance_labels";
    public static final String MODE_INHERITANCE_PARAM = "modeInheritance";
    public static final String ALLELE_ORIGIN_DESCRIPTION = "Comma separated list of allele origin labels. A list of searchable "
            + "allele origin  labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/allele_origin_labels";
    public static final String ALLELE_ORIGIN_PARAM = "alleleOrigin";

    // ---------------------------------------------

    public static final String VARIANTS = "Comma separated list of variants to annotate, e.g. "
            + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
            + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT,1:816505-825225:<CNV>";
    public static final String NORMALISE = "Boolean to indicate whether input variants shall be "
            + "normalized or not. Normalization process does NOT include decomposing ";
    public static final String SKIP_DECOMPOSE = "Boolean to indicate whether input MNVs should be "
            + "decomposed or not as part of the normalisation step. MNV decomposition is strongly encouraged.";
    public static final String IGNORE_PHASE = "Boolean to indicate whether phase data should be taken into account.";
    public static final String PHASED = "DEPRECATED. Will be removed in next release. Please, use ignorePhase instead. "
            + " Boolean to indicate whether phase should be considered during the annotation process";
    public static final String IMPRECISE = "Boolean to indicate whether imprecise search must be  used or not";
    public static final String SV_EXTRA_PADDING = "Integer to optionally provide the size of the extra"
            + " padding to be used when annotating imprecise (or not) structural variants";
    public static final String CNV_EXTRA_PADDING = "Integer to optionally provide the size of the extra"
            + " padding to be used when annotating imprecise (or not) CNVs";

    // ---------------------------------------------

    public static final String STRAND = "Strand to query, either 1 or -1";

    // ---------------------------------------------

    public static final String SUBSTITUTION_SCORE_NOTE = "Schema of returned objects will vary depending on provided query parameters. "
            + "If the amino acid position is provided, all scores will be returned for every possible amino acid change occurring at that "
            + "position. If the alternate aminoacid is provided as well, Score objects as specified at "
            + "https://github.com/opencb/biodata/blob/develop/biodata-models/src/main/resources/avro/variantAnnotation.avdl shall be "
            + "returned. If none of these parameters are provided, the whole list of scores for every possible amino acid change in "
            + "the protein shall be returned.";
    public static final String AA_DESCRIPTION = "Alternate aminoacid to check. Please, use upper-case letters and three letter encoding "
            + "of aminoacid names, e.g.: CYS";
    public static final String POSITION_DESCRIPTION = "Integer indicating the aminoacid position to check";

    // ---------------------------------------------

    public static final String XREF_DBNAMES = "Comma separated list of source DB names"
            + " to include in the search, e.g.: ensembl_gene,vega_gene,havana_gene."
            + " Available db names are shown by this web service: "
            + " https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/#!/Xref/getDBNames";

    // ---------------------------------------------

//    public static final String REGULATION_FEATURE_CLASSES = "Comma separated list of regulatory region classes, e.g.:"
//            + "Histone,Transcription Factor. Exact text matches will be returned. For a full"
//            + "list of available regulatory types: "
//            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/regulatory/featureClass";
    public static final String REGULATION_FEATURE_TYPES = "Comma separated list of regulatory region types, e.g.: "
            + "TF_binding_site,histone_acetylation_site. Exact text matches will be returned. For a full"
            + "list of available regulatory types: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/regulatory/featureType";

    public static final String CELLTYPE = "Type of cell.";

}
