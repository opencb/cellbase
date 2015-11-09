
__author__ = 'fjlopez'

import os
import json
import string
import zlib
import urllib2
from lib import CellBaseConfiguration
from lib.exceptions import InvalidQueryTypeException,InvalidQueryMethodException,InvalidQuerySpeciesException,InvalidQueryOptionsException

class CellBaseClient:

    PATH = "/cellbase/webservices/rest/"
    ENABLEDQUERYTYPES = {"clinical":"feature","exon":"feature","gene":"feature","chromosome":"genomic","meta":None,
                         "protein":"feature","region":"genomic","snp":"feature","species":None,"tf":"regulatory",
                         "id":"feature"}
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


    def loadCellBaseConfiguration(self):
        if(self.__configFile!=None):
            self.__configuration = CellBaseConfiguration.CellBaseConfiguration(self.__configFile)
        else:
            self.__configuration = CellBaseConfiguration.CellBaseConfiguration(
                os.path.dirname(CellBaseConfiguration.__file__)+"/resources/configuration.json")

    def executeCLI(self):
        self.__checkParameters()
        queryResponse = self.get(self.__species, self.__type, self.__method, self.__id, self.__options)
        print(queryResponse)

    def __checkParameters(self):
        if(self.__queryCommandOptions.type!=None and
                   self.__queryCommandOptions.type.lower() in CellBaseClient.ENABLEDQUERYSUBTYPES):
            self.__type = self.__queryCommandOptions.type.lower()
        else:
            raise InvalidQueryTypeException.InvalidQueryTypeException(
                self.__queryCommandOptions.type+" is not a valid query type. Please provide one of the following: {"+
                string.join(CellBaseClient.ENABLEDQUERYSUBTYPES, sep=", ")+"}")
        if(self.__queryCommandOptions.method!=None and
                  self.__queryCommandOptions.method.lower() in CellBaseClient.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type]):
            self.__method = self.__queryCommandOptions.method.lower()
        else:
            raise InvalidQueryMethodException.InvalidQueryMethodException(
                self.__queryCommandOptions.method+" is not a valid query method for type "+self.__queryCommandOptions.type+". Please provide one of the following: {"+
                string.join(CellBaseClient.ENABLEDQUERYMETHODS[self.__queryCommandOptions.type], sep=", ")+"}")
        self.__id = self.__queryCommandOptions.id
        if(self.__queryCommandOptions.species!=None and
                   self.__queryCommandOptions.species.lower() not in CellBaseClient.ENABLEDQUERYSPECIES):
            raise InvalidQuerySpeciesException.InvalidQuerySpeciesException(
                self.__queryCommandOptions.species+" is not a valid species. Please provide one of the following: {"+
                string.join(CellBaseClient.ENABLEDQUERYSPECIES, sep=", ")+"}")
        else:
            self.__species = self.__queryCommandOptions.species.lower()
        if(self.__validQueryOptions(self.__queryCommandOptions.options)):
            self.__options = self.__queryCommandOptions.options
        else:
            raise InvalidQueryOptionsException.InvalidQueryOptionsException(
                "Incorrect format provided for query options. Please, provide a list of filter pairs. For example: source=clinvar skip=10 limit=200")
#            raise InvalidQueryOptionsException.InvalidQueryOptionsException(
#                "Incorrect format provided for query options. Please, provide a list of &-separated filters. For example: source=clinvar&skip=10&limit=200")

    def __validQueryOptions(self,options):
        i = 0
        while(i<len(options) and len(options[i].split("="))==2):
            i += 1
        return i==len(options)

    def get(self, species, subtype, method, id, options):
        # Prepare the call to the server
        url = self.__buildUrl(species, subtype,method,id,options)
        req = urllib2.Request(url)

        # Inform to the server we accept gzip compression
        req.add_header("Accept-Encoding", "gzip")

        # Excute the call and read the result
        response = urllib2.urlopen(req)
        json_data = response.read()

        # Uncompress the gzip result
        data = zlib.decompress(json_data, 16+zlib.MAX_WBITS)

        return json.loads(data)

    def __buildUrl(self, species, subtype, method, id, options):
        url = "http://"+self.__configuration.getHost()+":"+str(self.__configuration.getPort())+\
                   CellBaseClient.PATH+self.__configuration.getVersion()+"/"+species
        if(CellBaseClient.ENABLEDQUERYTYPES[subtype]!=None):
            url += "/"+CellBaseClient.ENABLEDQUERYTYPES[subtype]
        url += "/"+subtype
        if(id!=None):
            url += "/"+string.join(id,sep=",")
        url += "/"+method
        if(options!=None):
            url += "?"+string.join(options,sep="&")

        return url



