
__author__ = 'fjlopez'

import os
import json
import string
import zlib
import urllib2

class CellBaseClient:

    PATH = "/cellbase/webservices/rest/"
    ENABLEDQUERYTYPES = {"clinical":"feature","exon":"feature","gene":"feature","chromosome":"genomic","meta":None,
                         "protein":"feature","region":"genomic","snp":"feature","species":None,"tf":"regulatory",
                         "id":"feature"}

    def __init__(self, configuration):
        self.__configuration = configuration

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



