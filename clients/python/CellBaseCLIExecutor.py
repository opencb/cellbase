import string
import os
import json
from exceptions import InvalidQueryTypeException,InvalidQueryMethodException,InvalidQuerySpeciesException
from lib import CellBaseClientConfig
from lib import CellBaseClient

__author__ = 'fjlopez'


class CellBaseCLIExecutor:
    ENABLEDQUERYSUBTYPES = ["clinical", "exon", "gene", "variant", "chromosome", "meta", "protein", "snp", "tf", "id"]
    ENABLEDQUERYMETHODS = {"clinical": {"all", "help","listAcc","phenotype-gene"},
                           "exon": {"stats", "first", "help", "model", "count", "aminos", "info", "region",
                                        "sequence", "transcript"},
                           "gene": {"tfbs", "biotype", "count", "first", "help", "list", "model", "stats", "clinical",
                                    "info", "mirna_target", "mutation", "next", "ppi", "protein", "snp", "stats", "all",
                                    "transcript", "count", "first", "fullinfo", "function_prediction", "gene",
                                    "mutation", "region", "sequence", "variation"},
                           "chromosome": {"all", "help", "list", "model", "ctyoband", "info", "size"},
                           "meta": {"help","species","versions"},
                           "protein": {"gene","help", "model", "fullinfo", "all", "info", "name", "reference",
                                       "sequence", "transcript"},
                           "region": {"exon", "help", "model", "clinical", "conservation", "conserved_region", "cpg_island",
                                      "cytoband", "tfbs", "gene", "mutation", "phenotype", "regulatory", "sequence",
                                      "snp", "structural_variation", "transcript"},
                           "snp": {"xref", "consequence_types", "count", "first", "help", "model", "sequence",
                                   "phenotypes", "regulatory", "stats", "info", "next", "phenotype",
                                   "population_frequency", "consequence_type"},
                           "species": {"help", "info"},
                           "tf": {"annotation", "help", "tfbs"},
                           "id": {"help", "model", "contains", "gene", "info", "snp", "starts_with", "xref"}
                           }
    ENABLEDQUERYSPECIES = {"hsapiens", "mmusculus", "drerio", "rnorvegicus", "ptroglodytes", "ggorilla", "pabelii",
                           "mmulatta", "csabaeus", "sscrofa", "cfamiliaris", "ecaballus", "ocuniculus", "ggallus",
                           "btaurus", "fcatus", "cintestinalis", "oaries", "olatipes", "ttruncatus", "lafricana",
                           "cjacchus", "nleucogenys", "aplatyrhynchos", "falbicollis", "celegans", "dmelanogaster",
                           "dsimulans", "dyakuba", "agambiae", "adarlingi", "nvectensis", "spurpuratus", "bmori",
                           "aaegypti", "apisum", "scerevisiae", "spombe", "afumigatus", "aniger", "anidulans",
                           "aoryzae", "foxysporum", "pgraminis", "ptriticina", "moryzae", "umaydis", "ssclerotiorum",
                           "cneoformans", "ztritici", "pfalciparum", "lmajor", "ddiscoideum", "glamblia", "pultimum",
                           "alaibachii", "athaliana", "alyrata", "bdistachyon", "osativa", "gmax", "vvinifera",
                           "zmays", "hvulgare", "macuminata", "sbicolor", "sitalica", "taestivum", "brapa",
                           "ptrichocarpa", "slycopersicum", "stuberosum", "smoellendorffii", "creinhardtii", "cmerolae"}

    def __init__(self, query_command_options):
        self.__queryCommandOptions = query_command_options
        self.__configFile = query_command_options.conf
        self.__configuration = None
        self.__options = {}
        self.__file = None

    def run(self):
        self.__check_parameters()
        self.load_cellbase_client_configuration()
        cell_base_client = CellBaseClient.CellBaseClient(self.__configuration)
        query_response = cell_base_client.get(self.__species, self.__type, self.__method, self.__id, self.__options)
        if self.__file:
            fname = self.__file + ".json"
            f = open(fname, 'w')
            f.write("[\n")
            count = len(query_response["response"])
            for i in range(len(query_response["response"])):
                output = json.dumps(query_response["response"][i]["result"][0], sort_keys=True)
                f.write(output)
                if count > 1:
                    f.write(",\n")
                    count -= - 1
            f.write("\n]")
            print("Results has been written to", fname, "file")
        else:
            print(query_response)

    def load_cellbase_client_configuration(self):
        if self.__configFile is not None:
            self.__configuration = CellBaseClientConfig.CellBaseClientConfig(self.__configFile)
        else:
            self.__configuration = CellBaseClientConfig.CellBaseClientConfig(
                os.path.dirname(CellBaseClientConfig.__file__) + "/../configuration.json")

    def __check_parameters(self):
        if self.__queryCommandOptions.type is not None \
                and self.__queryCommandOptions.type.lower() in CellBaseCLIExecutor.ENABLEDQUERYSUBTYPES:
            self.__type = self.__queryCommandOptions.type.lower()
        else:
            raise InvalidQueryTypeException.InvalidQueryTypeException(
                self.__queryCommandOptions.type+" is not a valid query type. Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYSUBTYPES, sep=", ")+"}")
        if self.__queryCommandOptions.method is not None \
                and self.__queryCommandOptions.method.lower() in CellBaseCLIExecutor.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type]:
            self.__method = self.__queryCommandOptions.method.lower()
        else:
            raise InvalidQueryMethodException.InvalidQueryMethodException(
                self.__queryCommandOptions.method+" is not a valid query method for type "+self.__queryCommandOptions.type+". Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type], sep=", ")+"}")
        self.__id = self.__queryCommandOptions.id
        if self.__queryCommandOptions.species is not None \
                and self.__queryCommandOptions.species.lower() not in CellBaseCLIExecutor.ENABLEDQUERYSPECIES:
            raise InvalidQuerySpeciesException.InvalidQuerySpeciesException(
                self.__queryCommandOptions.species+" is not a valid species. Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYSPECIES, sep=", ")+"}")
        else:
            self.__species = self.__queryCommandOptions.species.lower()

        if self.__queryCommandOptions.include is not None:
            self.__options['include'] = self.__queryCommandOptions.include
        if self.__queryCommandOptions.exclude is not None:
            self.__options['exclude'] = self.__queryCommandOptions.exclude
        if self.__queryCommandOptions.limit is not None:
            self.__options['limit'] =self.__queryCommandOptions.limit
        if self.__queryCommandOptions.skip is not None:
            self.__options['skip'] = self.__queryCommandOptions.skip

        if self.__queryCommandOptions.output is not None:
            self.__file = self.__queryCommandOptions.output

    # def __valid_query_options(self, options):
    #     if options is not None:
    #         i = 0
    #         while i < len(options) and len(options[i].split("=")) == 2:
    #             i += 1
    #         return i == len(options)
    #     else:
    #         return True


