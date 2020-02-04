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

    public static final String VERSION_DESCRIPTION = "Possible values: v4, v5";
    public static final String DEFAULT_VERSION = "v5";

    public static final String COUNT_DESCRIPTION = "Get the total number of results matching the query. ";

    public static final String INCLUDE_DESCRIPTION = "Fields included in the response, whole JSON path must be provided";
    public static final String EXCLUDE_DESCRIPTION = "Fields excluded in the response, whole JSON path must be provided";
    public static final String LIMIT_DESCRIPTION = "Number of results to be returned";
    public static final String SKIP_DESCRIPTION = "Number of results to skip";
    public static final String SORT_DESCRIPTION = "Sort returned results by a certain data model attribute";

    public static final String SPECIES_DESCRIPTION = "Name of the species, e.g. hsapiens. For a full list "
            + "of potentially available species ids, please refer to: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species";
    public static final String DATA_MODEL_DESCRIPTION = "Get JSON specification of the data model";

    public static final String REGION_DESCRIPTION = "Comma separated list of genomic regions to be queried, "
            + "e.g. 1:6635137-6635325";

    public static final String CHROMOSOMES = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT";

    public static final String GROUP_BY_FIELDS = "Comma separated list of field(s) to group by, e.g. biotype.";

    // ---------------------------------------------

    public static final String GENE_IDS = "Comma separated list of gene ids, e.g. ENSG00000268020,BRCA2"
            + " Exact text matches will be returned";
    public static final String GENE_ENSEMBL_IDS = "Comma separated list of ENSEMBL gene ids, "
            + "e.g. ENST00000380152,ENSG00000155657. Exact text matches will be returned";
    public static final String GENE_XREF_IDS = "Comma separated list gene/transcript xrefs ids, e.g. "
            + " ENSG00000145113,35912_at,GO:0002020.  Exact text matches will be returned";
    public static final String GENE_NAMES = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
            + " Exact text matches will be returned";
    public static final String GENE_BIOTYPES = "Comma separated list of gene gencode biotypes,"
            + " e.g. protein_coding,miRNA,lincRNA. Exact text matches will be returned";

    // ---------------------------------------------


    public static final String TRANSCRIPT_IDS = "Comma separated list of transcript IDs, e.g.  ENST00000342992. Other transcript symbols "
            + "such as HGNC symbols are allowed as well, e.g.: BRCA2-001";
    public static final String TRANSCRIPT_BIOTYPES = "Comma separated list of transcript gencode biotypes, "
            + "e.g. protein_coding,miRNA,lincRNA. Exact text matches will be returned";
    public static final String TRANSCRIPT_XREFS = "Comma separated list transcript xrefs ids, "
            + "e.g. ENSG00000145113,35912_at,GO:0002020. Exact text matches will be returned";
    public static final String TRANSCRIPT_ENSEMBL_IDS = "Comma separated list of ENSEMBL transcript ids, "
            + "e.g. ENST00000342992,ENST00000380152,ENST00000544455. Exact text matches will be returned";
    public static final String TRANSCRIPT_NAMES = "Comma separated list of transcript names, e.g. BRCA2-201,TTN-003."
            + " Exact text matches will be returned";
    public static final String TRANSCRIPT_ANNOTATION_FLAGS = "Comma separated list of annotation flags that must be "
            + "present in the transcripts returned within the gene model, e.g. basic,CCDS. Exact text matches will "
            + "be returned";
    public static final String TRANSCRIPT_TFBS_NAMES = "Comma separated list of TFBS names, e.g. CTCF,Gabp."
            + " Exact text matches will be returned";

    // ---------------------------------------------

    public static final String ANNOTATION_DISEASES_IDS = "Comma separated list of phenotype ids (OMIM, UMLS), "
            + "e.g. umls:C0030297,OMIM:613390,OMIM:613390. Exact text matches will be returned";
    public static final String ANNOTATION_DISEASES_NAMES = "Comma separated list of phenotypes, "
            + "e.g. Cryptorchidism,Absent thumb,Stage 5 chronic kidney disease. Exact text matches will be returned";
    public static final String ANNOTATION_EXPRESSION_GENE = "Comma separated list of ENSEMBL gene ids for which "
            + "expression values are available, e.g. ENSG00000139618,ENSG00000155657. "
            + "Exact text matches will be returned";
    public static final String ANNOTATION_EXPRESSION_TISSUE = "Comma separated list of tissues for which "
            + "expression values are available, e.g. adipose tissue,heart atrium,tongue. "
            + "Exact text matches will be returned";
    public static final String ANNOTATION_EXPRESSION_VALUE = "Must be used together with annotation.expression.tissue -"
            + "value of interest, either UP or DOWN";
    public static final String ANNOTATION_DRUGS_NAME = "Comma separated list of drug names, "
            + "e.g. BMN673,OLAPARIB,VELIPARIB. Exact text matches will be returned";
    public static final String ANNOTATION_DRUGS_GENE = "Comma separated list of gene names for which "
            + "drug data is available, e.g. BRCA2,TTN. Exact text matches will be returned";

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
    public static final String PROTEIN_ACCESSIONS = "Comma separated list of UniProt accession ids, "
            + "e.g. Q9UL59,B2R8Q1,Q9UKT9. Exact text matches will be returned";
    public static final String PROTEIN_NAMES = "Comma separated list of protein names, e.g.: ZN214_HUMAN,MKS1_HUMAN"
            + "Exact text matches will be returned";

    // ---------------------------------------------

    public static final String RS_IDS = "Comma separated list of rs ids, e.g.: rs6025, rs666"
            + " Exact text matches will be returned";

    // ---------------------------------------------

    public static final String SOURCE = "Comma separated list of database sources of the documents to be returned."
            + " Possible values are clinvar,cosmic or iarctp53. E.g.: clinvar,cosmic";
    public static final String SEQUENCE_ONTOLOGY = "Comma separated list of sequence ontology term names, "
            + "e.g. missense_variant. Exact text matches will be returned. A list of searchable SO term names can be"
            + " accessed at https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/feature/variation/consequence_types";
    public static final String FEATURE_IDS = "Comma separated list of feature ids, which can be either ENSEMBL gene "
            + "ids, HGNC gene symbols, transcript symbols or ENSEMBL transcript ids, e.g.: BRCA2, ENST00000409047. "
            + "Exact text matches will be returned.";
    public static final String TRAITS = "Keywords search. Comma separated (no spaces in between) list of "
            + "keywords describing required phenotype/disease. All variants related somehow with all those keywords "
            + "(case insensitive) will be returned, e.g: carcinoma,lung or acute,myeloid,leukaemia. WARNING: returned "
            + "numTotalResults will always be -1 when searching by trait keywords.";
    public static final String VARIANT_ACCESSIONS = "Comma separated list of database accessions, "
            + "e.g. RCV000033215,COSM306824 Exact text  matches will be returned.";
    public static final String VARIANT_IDS = "Comma separated list of ids, e.g. rs6025,COSM306824. "
            + "Exact text matches will be returned.";
    public static final String VARIANT_TYPES = "Comma separated list of variant types, e.g. \"SNV\" A list of "
            + "searchable types can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/type";
    public static final String CONSISTENCY_STATUS = "Comma separated list of consistency labels. A list of searchable "
            + "consistency labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/consistency_labels";
    public static final String CLINICAL_SIGNFICANCE = "Comma separated list of clinical significance labels. "
            + "A list of searchable clinical  significance labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/clinsig_labels"
            + " WARNING: returned numTotalResults will always be -1 if more than 1 label is provided.";
    public static final String MODE_INHERITANCE = "Comma separated list of mode of inheritance labels. A list of "
            + "searchable mode of inheritance labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/mode_inheritance_labels";
    public static final String ALLELE_ORIGIN = "Comma separated list of allele origin labels. A list of searchable "
            + "allele origin  labels can be accessed at "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/allele_origin_labels";


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
}
