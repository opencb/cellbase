__author__ = 'fjlopez'

import string
import os
import sys
from .exceptions import InvalidQueryTypeException,InvalidQueryMethodException,InvalidQuerySpeciesException,InvalidQueryOptionsException
from . import CellBaseConfiguration
from . import CellBaseClient

class CellBaseCLIExecutor():

    ENABLEDQUERYSUBTYPES = ["clinical","exon","gene","variant","chromosome","meta","protein","snp","tf","id"]
    ENABLEDQUERYMETHODS = {"clinical" : set(["all","help","listAcc","phenotype-gene"]),
                           "exon" : set(["stats","first","help","model","count","aminos","info","region","sequence","transcript"]),
                           "gene" : set(["tfbs","biotype","count","first","help","list","model","stats","clinical","info","mirna_target","mutation","next","ppi","protein","snp","stats","all","transcript","count","first","fullinfo","function_prediction","gene","mutation","region","sequence","variation"]),
                           "chromosome" : set(["all","help","list","model","ctyoband","info","size"]),
                           "meta" : set(["help","species","versions"]),
                           "protein" : set(["gene","help","model","fullinfo","all","info","name","reference","sequence","transcript"]),
                           "region" : set(["exon","help","model","clinical","conservation","conserved_region","cpg_island","cytoband","tfbs","gene","mutation","phenotype","regulatory","sequence","snp","structural_variation","transcript"]),
                           "snp" : set(["xref","consequence_types","count","first","help","model","sequence","phenotypes","regulatory","stats","info","next","phenotype","population_frequency","consequence_type"]),
                           "species" : set(["help","info"]),
                           "tf" : set(["annotation","help","tfbs"]),
                           "id" : set(["help","model","contains","gene","info","snp","starts_with","xref"])
                           }
    ENABLEDQUERYSPECIES = {"hsapiens", "mmusculus", "drerio", "rnorvegicus", "ptroglodytes", "ggorilla", "pabelii", "mmulatta", "csabaeus", "sscrofa", "cfamiliaris", "ecaballus", "ocuniculus", "ggallus", "btaurus", "fcatus", "cintestinalis", "oaries", "olatipes", "ttruncatus", "lafricana", "cjacchus", "nleucogenys", "aplatyrhynchos", "falbicollis", "celegans", "dmelanogaster", "dsimulans", "dyakuba", "agambiae", "adarlingi", "nvectensis", "spurpuratus", "bmori", "aaegypti", "apisum", "scerevisiae", "spombe", "afumigatus", "aniger", "anidulans", "aoryzae", "foxysporum", "pgraminis", "ptriticina", "moryzae", "umaydis", "ssclerotiorum", "cneoformans", "ztritici", "pfalciparum", "lmajor", "ddiscoideum", "glamblia", "pultimum", "alaibachii", "athaliana", "alyrata", "bdistachyon", "osativa", "gmax", "vvinifera", "zmays", "hvulgare", "macuminata", "sbicolor", "sitalica", "taestivum", "brapa", "ptrichocarpa", "slycopersicum", "stuberosum", "smoellendorffii", "creinhardtii", "cmerolae"}

    def __init__(self, queryCommandOptions):
        self.__queryCommandOptions = queryCommandOptions
        self.__configFile = queryCommandOptions.conf
        self.__configuration = None

    def run(self):
        self.__checkParameters()
        self.loadCellBaseConfiguration()
        cellBaseClient = CellBaseClient.CellBaseClient(self.__configuration)
        queryResponse = cellBaseClient.get(self.__species, self.__type, self.__method, self.__id, self.__options)
        print(queryResponse)

    def loadCellBaseConfiguration(self):
        if(self.__configFile!=None):
            self.__configuration = CellBaseConfiguration.CellBaseConfiguration(self.__configFile)
        else:
            self.__configuration = CellBaseConfiguration.CellBaseConfiguration(
                os.path.dirname(CellBaseConfiguration.__file__)+"/resources/configuration.json")

    def __checkParameters(self):
        if(self.__queryCommandOptions.type!=None and
                   self.__queryCommandOptions.type.lower() in CellBaseCLIExecutor.ENABLEDQUERYSUBTYPES):
            self.__type = self.__queryCommandOptions.type.lower()
        else:
            raise InvalidQueryTypeException.InvalidQueryTypeException(
                self.__queryCommandOptions.type+" is not a valid query type. Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYSUBTYPES, sep=", ")+"}")
        if(self.__queryCommandOptions.method!=None and
                  self.__queryCommandOptions.method.lower() in CellBaseCLIExecutor.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type]):
            self.__method = self.__queryCommandOptions.method.lower()
        else:
            raise InvalidQueryMethodException.InvalidQueryMethodException(
                self.__queryCommandOptions.method+" is not a valid query method for type "+self.__queryCommandOptions.type+". Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type], sep=", ")+"}")
        self.__id = self.__queryCommandOptions.id
        if(self.__queryCommandOptions.species!=None and
                   self.__queryCommandOptions.species.lower() not in CellBaseCLIExecutor.ENABLEDQUERYSPECIES):
            raise InvalidQuerySpeciesException.InvalidQuerySpeciesException(
                self.__queryCommandOptions.species+" is not a valid species. Please provide one of the following: {"+
                string.join(CellBaseCLIExecutor.ENABLEDQUERYSPECIES, sep=", ")+"}")
        else:
            self.__species = self.__queryCommandOptions.species.lower()
        if(self.__validQueryOptions(self.__queryCommandOptions.options)):
            self.__options = self.__queryCommandOptions.options
        else:
            raise InvalidQueryOptionsException.InvalidQueryOptionsException(
                "Incorrect format provided for query options. Please, provide a list of filter pairs. For example: source=clinvar skip=10 limit=200")

    def __validQueryOptions(self,options):
        if (options != None):
            i = 0
            while(i<len(options) and len(options[i].split("="))==2):
                i += 1
            return i == len(options)
        else:
            return True


