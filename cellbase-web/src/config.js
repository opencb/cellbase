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

var CELLBASE_HOSTS = ["bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0", "localhost:8080/cellbase-dev-v4.0", "www.ebi.ac.uk/cellbase"];
var CELLBASE_VERSION = "v4";
var CELLBASE_SPECIES = "hsapiens";

var CELLBASE_CLIENT;

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