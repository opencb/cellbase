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

var cellbase = {
    hosts: ["bioinfodev.hpc.cam.ac.uk/cellbase-4.5.0-rc"], //"localhost:8080/cellbase-4.5.0-rc", "www.ebi.ac.uk/cellbase"
    version: "v4",
    species: "hsapiens"
};

var populationFrequencies = {
    // This is based on this figure:
    // http://www.dialogues-cns.com/wp-content/uploads/2015/03/DialoguesClinNeurosci-17-69-g001.jpg
    color: {
        veryRare: "red",
        rare: "yellow",
        average: "orange",
        common: "blue"
    },
    studies: [
        {
            id: "1KG_PHASE3",
            title: "1000 Genomes",
            populations: [
                {
                    id: "ALL",
                    title: "All populations [ALL]",
                    active: true
                },
                {
                    id: "EUR",
                    title: "European [EUR]"
                },
                {
                    id: "AMR",
                    title: "American [AMR]"
                },
                {
                    id: "AFR",
                    title: "African [AFR]"
                },
                {
                    id: "SAS",
                    title: "South Asian [SAS]"
                },
                {
                    id: "EAS",
                    title: "East Asian [EAS]"
                }
            ]
        },
        {
            id: "EXAC",
            title: "ExAC",
            populations: [
                {
                    id: "ALL",
                    title: "ExAC [ALL]"
                },
                {
                    id: "NFE",
                    title: "South Asian [SAS]"
                },
                {
                    id: "AMR",
                    title: "South Asian [SAS]"
                },
                {
                    id: "SAS",
                    title: "South Asian [SAS]"
                }
            ]
        },
        {
            id: "ESP6500",
            title: "ESP 6500",
            populations: [
                {
                    id: "EA",
                    title: "European American",
                    active: true
                },
                {
                    id: "AA",
                    title: "African American",
                    active: true
                }
            ]
        }
    ]
};

var proteinSubstitutionScores = {
    // This is to show the predictions in respective colors
    sift: {
        deleterious: "red",
        tolerated: "green"
    },
    polyphen: {
        probablyDamaging: "red",
        possiblyDamaging: "orange",
        benign: "green",
        unknown: "black"
    }
};

var consequenceTypes = {
    // This is the impact color. It allows to customise both the impact categories and desired colors
    color: {
        high: "red",
        moderate: "orange",
        low: "blue",
        modifier: "green"
    },
    /* 'Title' is optional. if there is no title provided then 'name' is going to be used.
     There are two more optional properties - 'checked' and 'color'. They can be set to display them default in web application.
     Similarly 'description' is optional as well.
     */
    categories: [
        {
            id: "",
            name: "",
            title: "Intergenic",
            description: "",
            terms: [
                {
                    id: "SO:0001631",
                    name: "upstream_gene_variant",
                    title: "upstream gene variant",
                    description: "A sequence variant located 5' of a gene",
                    impact: "modifier"
                },
                {
                    id: "SO:0001636",
                    name: "2KB_upstream_variant",
                    description: "A sequence variant located within 2KB 5' of a gene",
                    impact: "modifier"
                    // checked: true
                },
                {
                    id: "SO:0001632",
                    name: "downstream_gene_variant",
                    description: "A sequence variant located 3' of a gene",
                    impact: "modifier"
                },
                {
                    id: "SO:0002083",
                    name: "2KB_downstream_variant",
                    description: "A sequence variant located within 2KB 3' of a gene",
                    impact: "modifier"
                    // checked: true
                },
                {
                    id: "SO:0001628",
                    name: "intergenic_variant",
                    description: "A sequence variant located in the intergenic region, between genes",
                    impact: "modifier"
                }
            ]
        },
        {
            title: "Regulatory",
            terms: [
                {
                    id: "SO:0001620",
                    name: "mature_miRNA_variant",
                    description: "A transcript variant located with the sequence of the mature miRNA",
                    impact: "modifier"
                },
                {
                    id: "SO:0001894",
                    name: "regulatory_region_ablation",
                    description: "A feature ablation whereby the deleted region includes a regulatory region",
                    impact: "moderate"
                },
                {
                    id: "SO:0001891",
                    name: "regulatory_region_amplification",
                    description: "A feature amplification of a region containing a regulatory region",
                    impact: "modifier"
                },
                {
                    id: "SO:0001566",
                    name: "regulatory_region_variant",
                    description: "A sequence variant located within a regulatory region",
                    impact: "modifier"
                },
                {
                    id: "SO:0001782",
                    name: "TF_binding_site_variant",
                    description: "A sequence variant located within a transcription factor binding site",
                    impact: "modifier"
                },
                {
                    id: "SO:0001895",
                    name: "TFBS_ablation",
                    description: "A feature ablation whereby the deleted region includes a transcription factor binding site",
                    impact: "modifier"
                },
                {
                    id: "SO:0001892",
                    name: "TFBS_amplification",
                    description: "A feature amplification of a region containing a transcription factor binding site",
                    impact: "modifier"
                },
            ]
        },
        {
            title: "Coding",
            terms: [
                {
                    id: "SO:0001580",
                    name: "coding_sequence_variant",
                    description: "A sequence variant that changes the coding sequence",
                    impact: "modifier"
                },
                {
                    id: "SO:0001907",
                    name: "feature_elongation",
                    description: "A sequence variant that causes the extension of a genomic feature, with regard to the reference sequence",
                    impact: "modifier"
                },
                {
                    id: "SO:0001906",
                    name: "feature_truncation",
                    description: "A sequence variant that causes the reduction of a genomic feature, with regard to the reference sequence",
                    impact: "modifier"
                },
                {
                    id: "SO:0001589",
                    name: "frameshift_variant",
                    description: "A sequence variant which causes a disruption of the translational reading frame, because the number of nucleotides inserted or deleted is not a multiple of three",
                    impact: "high"
                },
                {
                    id: "SO:0001626",
                    name: "incomplete_terminal_codon_variant",
                    description: "A sequence variant where at least one base of the final codon of an incompletely annotated transcript is changed",
                    impact: "low"

                },
                {
                    id: "SO:0001822",
                    name: "inframe_deletion",
                    description: "An inframe non synonymous variant that deletes bases from the coding sequence",
                    impact: "moderate"
                },
                {
                    id: "SO:0001821",
                    name: "inframe_insertion",
                    description: "An inframe non synonymous variant that inserts bases into in the coding sequence",
                    impact: "moderate"
                },
                {
                    id: "SO:0001583",
                    name: "missense_variant",
                    description: "A sequence variant, that changes one or more bases, resulting in a different amino acid sequence but where the length is preserved",
                    impact: "moderate"
                },
                {
                    id: "SO:0001621",
                    name: "NMD_transcript_variant",
                    description: "A variant in a transcript that is the target of NMD",
                    impact: "modifier"
                },
                {
                    id: "SO:0001818",
                    name: "protein_altering_variant",
                    description: "A sequence_variant which is predicted to change the protein encoded in the coding sequence",
                    impact: "moderate"
                },
                {
                    id: "SO:0001819",
                    name: "synonymous_variant",
                    description: "A sequence variant where there is no resulting change to the encoded amino acid",
                    impact: "low"
                },
                {
                    id: "SO:0002012",
                    name: "start_lost",
                    description: "A codon variant that changes at least one base of the canonical start codon",
                    impact: "high"
                },
                {
                    id: "SO:0001587",
                    name: "stop_gained",
                    description: "A sequence variant whereby at least one base of a codon is changed, resulting in a premature stop codon, leading to a shortened transcript",
                    impact: "high"
                },
                {
                    id: "SO:0001578",
                    name: "stop_lost",
                    description: "A sequence variant where at least one base of the terminator codon (stop) is changed, resulting in an elongated transcript",
                    impact: "high"
                },
                {
                    id: "SO:0001567",
                    name: "stop_retained_variant",
                    description: "A sequence variant where at least one base in the terminator codon is changed, but the terminator remains",
                    impact: "low"
                },
            ]
        },
        {
            title: "Non-coding",
            terms: [
                {
                    id: "SO:0001624",
                    name: "3_prime_UTR_variant",
                    description: "A UTR variant of the 3' UTR",
                    impact: "modifier"
                },
                {
                    id: "SO:0001623",
                    name: "5_prime_UTR_variant",
                    description: "A UTR variant of the 5' UTR",
                    impact: "modifier"
                },
                {
                    id: "SO:0001627",
                    name: "intron_variant",
                    description: "A transcript variant occurring within an intron",
                    impact: "modifier"
                },
                {
                    id: "SO:0001792",
                    name: "non_coding_transcript_exon_variant",
                    description: "A sequence variant that changes non-coding exon sequence in a non-coding transcript",
                    impact: "modifier"
                }
            ]
        },
        {
            title: "Splice",
            terms: [
                {
                    id: "SO:0001574",
                    name: "splice_acceptor_variant",
                    description: "A splice variant that changes the 2 base region at the 3' end of an intron",
                    impact: "high"
                },
                {
                    id: "SO:0001575",
                    name: "splice_donor_variant",
                    description: "A splice variant that changes the 2 base pair region at the 5' end of an intron",
                    impact: "high"
                },
                {
                    id: "SO:0001630",
                    name: "splice_region_variant",
                    description: "A sequence variant in which a change has occurred within the region of the splice site, either within 1-3 bases of the exon or 3-8 bases of the intron",
                    impact: "low"
                }
            ]
        },
        {
            id: "SO:0001893",
            name: "transcript_ablation",
            description: "A feature ablation whereby the deleted region includes a transcript feature",
            impact: "high"
        },
        {
            id: "SO:0001889",
            name: "transcript_amplification",
            description: "A feature amplification of a region containing a transcript",
            impact: "high"
        }
    ]
};

var DEFAULT_SPECIES = {
    "vertebrates": [
        {

            "id": "hsapiens",
            "scientificName": "Homo sapiens",
            "assembly": {

                "name": "GRCh37",
                "ensemblVersion": "75_37"

            },
            "assemblies": [

                {

                    "name": "GRCh37",
                    "ensemblVersion": "75_37"

                },

                {
                    "name": "GRCh38",
                    "ensemblVersion": "79_38"
                }

            ],
            "data": [
                "genome",
                "gene",
                "variation",
                "regulation",
                "protein",
                "conservation",
                "clinical",
                "gene2disease"
            ]

        }
    ]
};

var SPECIES = {

    "vertebrates":

        [

            {

                "id": "hsapiens",
                "scientificName": "Homo sapiens",

                "assembly": {

                    "name": "GRCh37",
                    "ensemblVersion": "75_37"

                },
                "assemblies":

                    [

                        {

                            "name": "GRCh37",
                            "ensemblVersion": "75_37"

                        },

                        {
                            "name": "GRCh38",
                            "ensemblVersion": "79_38"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "regulation",
                        "protein",
                        "conservation",
                        "clinical",
                        "gene2disease"
                    ]

            },
            {

                "id": "mmusculus",
                "scientificName": "Mus musculus",
                "assemblies":

                    [

                        {
                            "name": "GRCm38",
                            "ensemblVersion": "79_38"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "regulation",
                        "protein",
                        "conservation"
                    ]

            },
            {

                "id": "drerio",
                "scientificName": "Danio rerio",
                "assemblies":

                    [

                        {
                            "name": "Zv9",
                            "ensemblVersion": "79_9"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "rnorvegicus",
                "scientificName": "Rattus norvegicus",
                "assemblies":

                    [

                        {
                            "name": "Rnor_5.0",
                            "ensemblVersion": "79_5"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "ptroglodytes",
                "scientificName": "Pan troglodytes",
                "assemblies":

                    [

                        {
                            "name": "CHIMP2.1.4",
                            "ensemblVersion": "79_214"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ggorilla",
                "scientificName": "Gorilla gorilla",
                "assemblies":

                    [

                        {
                            "name": "gorGor3.1",
                            "ensemblVersion": "79_31"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "pabelii",
                "scientificName": "Pongo abelii",
                "assemblies":

                    [

                        {
                            "name": "PPYG2",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "mmulatta",
                "scientificName": "Macaca mulatta",
                "assemblies":

                    [

                        {
                            "name": "MMUL_1.0",
                            "ensemblVersion": "79_10"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "csabaeus",
                "scientificName": "Chlorocebus sabaeus",
                "assemblies":

                    [

                        {
                            "name": "ChlSab1.1",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "sscrofa",
                "scientificName": "Sus scrofa",
                "assemblies":

                    [

                        {
                            "name": "Sscrofa10.2",
                            "ensemblVersion": "79_102"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "cfamiliaris",
                "scientificName": "Canis familiaris",
                "assemblies":

                    [

                        {
                            "name": "CanFam3.1",
                            "ensemblVersion": "79_31"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ecaballus",
                "scientificName": "Equus caballus",
                "assemblies":

                    [

                        {
                            "name": "EquCab2",
                            "ensemblVersion": "79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ocuniculus",
                "scientificName": "Oryctolagus cuniculus",
                "assemblies":

                    [

                        {
                            "name": "oryCun2.0",
                            "ensemblVersion": "79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ggallus",
                "scientificName": "Gallus gallus",
                "assemblies":

                    [

                        {
                            "name": "Galgal4",
                            "ensemblVersion": "79_4"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "btaurus",
                "scientificName": "Bos taurus",
                "assemblies":

                    [

                        {
                            "name": "UMD3.1",
                            "ensemblVersion": "79_31"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "fcatus",
                "scientificName": "Felis catus",
                "assemblies":

                    [

                        {
                            "name": "Felis_catus_6.2",
                            "ensemblVersion": "79_62"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "cintestinalis",
                "scientificName": "Ciona intestinalis",
                "assemblies":

                    [

                        {
                            "name": "KH",
                            "ensemblVersion": "79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "oaries",
                "scientificName": "Ovis aries",
                "assemblies":

                    [

                        {
                            "name": "Oar_v3.1",
                            "ensemblVersion": "79_31"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "olatipes",
                "scientificName": "Oryzias latipes",
                "assemblies":

                    [

                        {
                            "name": "HdrR",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ttruncatus",
                "scientificName": "Tursiops truncatus",
                "assemblies":

                    [

                        {
                            "name": "turTru1",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "lafricana",
                "scientificName": "Loxodonta africana",
                "assemblies":

                    [

                        {
                            "name": "Loxafr3.0",
                            "ensemblVersion": "79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "cjacchus",
                "scientificName": "Callithrix jacchus",
                "assemblies":

                    [

                        {
                            "name": "C_jacchus3.2.1",
                            "ensemblVersion": "79_321"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "nleucogenys",
                "scientificName": "Nomascus leucogenys",
                "assemblies":

                    [

                        {
                            "name": "Nleu1.0",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "aplatyrhynchos",
                "scientificName": "Anas platyrhynchos",
                "assemblies":

                    [

                        {
                            "name": "BGI_duck_1.0",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "falbicollis",
                "scientificName": "Ficedula albicollis",
                "assemblies":

                    [

                        {
                            "name": "FicAlb_1.4",
                            "ensemblVersion": "79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]
            }

        ],
    "metazoa":
        [

            {

                "id": "celegans",
                "scientificName": "Caenorhabditis elegans",
                "assemblies":

                    [

                        {
                            "name": "WBcel235",
                            "ensemblVersion": "26_79_245"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "dmelanogaster",
                "scientificName": "Drosophila melanogaster",
                "assemblies":

                    [

                        {
                            "name": "BDGP6",
                            "ensemblVersion": "26_79_6"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "dsimulans",
                "scientificName": "Drosophila simulans",
                "assemblies":

                    [

                        {
                            "name": "WUGSC1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "dyakuba",
                "scientificName": "Drosophila yakuba",
                "assemblies":

                    [

                        {
                            "name": "dyak_r1.3",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "agambiae",
                "scientificName": "Anopheles gambiae",
                "assemblies":

                    [

                        {
                            "name": "AgamP4",
                            "ensemblVersion": "26_79_4"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "adarlingi",
                "scientificName": "Anopheles darlingi",
                "assemblies":

                    [

                        {
                            "name": "AdarC1",
                            "ensemblVersion": "26_79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "nvectensis",
                "scientificName": "Nematostella vectensis",
                "assemblies":

                    [

                        {
                            "name": "ASM20922v1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "spurpuratus",
                "scientificName": "Strongylocentrotus purpuratus",
                "assemblies":

                    [

                        {
                            "name": "GCA_000002235.2",
                            "ensemblVersion": "26_79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "bmori",
                "scientificName": "Bombyx mori",
                "assemblies":

                    [

                        {
                            "name": "Bmor1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "aaegypti",
                "scientificName": "Aedes aegypti",
                "assemblies":

                    [

                        {
                            "name": "AaegL3",
                            "ensemblVersion": "26_79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "apisum",
                "scientificName": "Acyrthosiphon pisum",
                "assemblies":

                    [

                        {
                            "name": "GCA_000142985.2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]
            }

        ],
    "fungi":
        [

            {

                "id": "scerevisiae",
                "scientificName": "Saccharomyces cerevisiae",
                "assemblies":

                    [

                        {
                            "name": "R64_1_1",
                            "ensemblVersion": "26_79_4"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "spombe",
                "scientificName": "Schizosaccharomyces pombe",
                "assemblies":

                    [

                        {
                            "name": "ASM294v2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "afumigatus",
                "scientificName": "Aspergillus fumigatus",
                "assemblies":

                    [

                        {
                            "name": "CADRE",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "aniger",
                "scientificName": "Aspergillus niger",
                "assemblies":

                    [

                        {
                            "name": "CADRE",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "anidulans",
                "scientificName": "Aspergillus nidulans",
                "assemblies":

                    [

                        {
                            "name": "ASM1142v1",
                            "ensemblVersion": "26_79_6"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "aoryzae",
                "scientificName": "Aspergillus oryzae",
                "assemblies":

                    [

                        {
                            "name": "CADRE2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "foxysporum",
                "scientificName": "Fusarium oxysporum",
                "assemblies":

                    [

                        {
                            "name": "FO2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "pgraminis",
                "scientificName": "Puccinia graminis",
                "assemblies":

                    [

                        {
                            "name": "ASM14992v1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ptriticina",
                "scientificName": "Puccinia triticina",
                "assemblies":

                    [

                        {
                            "name": "ASM15152v1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "moryzae",
                "scientificName": "Magnaporthe oryzae",
                "assemblies":

                    [

                        {
                            "name": "MG8",
                            "ensemblVersion": "26_79_8"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "umaydis",
                "scientificName": "Ustilago maydis",
                "assemblies":

                    [

                        {
                            "name": "UM1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ssclerotiorum",
                "scientificName": "Sclerotinia sclerotiorum",
                "assemblies":

                    [

                        {
                            "name": "ASM14694v1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "cneoformans",
                "scientificName": "Cryptococcus neoformans",
                "assemblies":

                    [

                        {
                            "name": "GCA_000091045.1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ztritici",
                "scientificName": "Zymoseptoria tritici",
                "assemblies":

                    [

                        {
                            "name": "MG2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]
            }

        ],
    "protist":
        [

            {

                "id": "pfalciparum",
                "scientificName": "Plasmodium falciparum",
                "assemblies":

                    [

                        {
                            "name": "ASM276v1",
                            "ensemblVersion": "26_79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "lmajor",
                "scientificName": "Leishmania major",
                "assemblies":

                    [

                        {
                            "name": "ASM272v2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ddiscoideum",
                "scientificName": "Dictyostelium discoideum",
                "assemblies":

                    [

                        {
                            "name": "dictybase.01",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "glamblia",
                "scientificName": "Giardia lamblia",
                "assemblies":

                    [

                        {
                            "name": "GCA_000002435.1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "pultimum",
                "scientificName": "Pythium ultimum",
                "assemblies":

                    [

                        {
                            "name": "pug",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "alaibachii",
                "scientificName": "Albugo laibachii",
                "assemblies":

                    [

                        {
                            "name": "ENA1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]
            }

        ],
    "plants":
        [

            {

                "id": "athaliana",
                "scientificName": "Arabidopsis thaliana",
                "assemblies":

                    [

                        {
                            "name": "TAIR10",
                            "ensemblVersion": "26_79_10"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation",
                        "protein"
                    ]

            },
            {

                "id": "alyrata",
                "scientificName": "Arabidopsis lyrata",
                "assemblies":

                    [

                        {
                            "name": "v.1.0",
                            "ensemblVersion": "26_79_10"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "bdistachyon",
                "scientificName": "Brachypodium distachyon",
                "assemblies":

                    [

                        {
                            "name": "v1.0",
                            "ensemblVersion": "26_79_12"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "osativa",
                "scientificName": "Oryza sativa",
                "assemblies":

                    [

                        {
                            "name": "IRGSP-1.0",
                            "ensemblVersion": "26_79_7"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "gmax",
                "scientificName": "Glycine max",
                "assemblies":

                    [

                        {
                            "name": "V1.0",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "vvinifera",
                "scientificName": "Vitis vinifera",
                "assemblies":

                    [

                        {
                            "name": "IGGP_12x",
                            "ensemblVersion": "26_79_3"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "zmays",
                "scientificName": "Zea mays",
                "assemblies":

                    [

                        {
                            "name": "AGPv3",
                            "ensemblVersion": "26_79_6"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "hvulgare",
                "scientificName": "Hordeum vulgare",
                "assemblies":

                    [

                        {
                            "name": "030312v2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "macuminata",
                "scientificName": "Musa acuminata",
                "assemblies":

                    [

                        {
                            "name": "MA1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "sbicolor",
                "scientificName": "Sorghum bicolor",
                "assemblies":

                    [

                        {
                            "name": "Sorbi1",
                            "ensemblVersion": "26_79_14"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "sitalica",
                "scientificName": "Setaria italica",
                "assemblies":

                    [

                        {
                            "name": "JGIv2.0",
                            "ensemblVersion": "26_79_21"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "taestivum",
                "scientificName": "Triticum aestivum",
                "assemblies":

                    [

                        {
                            "name": "IWGSC2",
                            "ensemblVersion": "26_79_2"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "brapa",
                "scientificName": "Brassica rapa",
                "assemblies":

                    [

                        {
                            "name": "IVFCAASv1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "ptrichocarpa",
                "scientificName": "Populus trichocarpa",
                "assemblies":

                    [

                        {
                            "name": "JGI2.0",
                            "ensemblVersion": "26_79_20"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "slycopersicum",
                "scientificName": "Solanum lycopersicum",
                "assemblies":

                    [

                        {
                            "name": "SL2.40",
                            "ensemblVersion": "26_79_240"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "stuberosum",
                "scientificName": "Solanum tuberosum",
                "assemblies":

                    [

                        {
                            "name": "3.0",
                            "ensemblVersion": "26_79_4"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "smoellendorffii",
                "scientificName": "Selaginella moellendorffii",
                "assemblies":

                    [

                        {
                            "name": "v1.0",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "creinhardtii",
                "scientificName": "Chlamydomonas reinhardtii",
                "assemblies":

                    [

                        {
                            "name": "v3.0",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]

            },
            {

                "id": "cmerolae",
                "scientificName": "Cyanidioschyzon merolae",
                "assemblies":

                    [

                        {
                            "name": "ASM9120v1",
                            "ensemblVersion": "26_79_1"
                        }

                    ],
                "data":

                    [
                        "genome",
                        "gene",
                        "variation"
                    ]
            }
        ]

};