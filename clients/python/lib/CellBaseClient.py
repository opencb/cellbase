__author__ = 'fjlopez'

import os
import json
import string
import zlib
import urllib.request
#import requests

class CellBaseClient:

    PATH = "/cellbase/webservices/rest/"
    ENABLEDQUERYTYPES = {"clinical":"feature","exon":"feature","gene":"feature","chromosome":"genomic","meta":None,
                         "protein":"feature","region":"genomic","snp":"feature","species":None,"tf":"regulatory",
                         "id":"feature"}

    def __init__(self, configuration):
        self.__configuration = configuration

    def get(self, species, subtype, method, id, options):
        # Prepare the call to the server
        url = self.__createUrl(species, subtype, method, id, options)

        print(url)

        req = urllib.request.Request(url)

        # Inform to the server we accept gzip compression
        req.add_header("Accept-Encoding", "gzip")

        # Execute the call and read the result
        response = urllib.request.urlopen(req)
        json_data = response.read()

        # Uncompress the gzip result
        data = zlib.decompress(json_data, 16 + zlib.MAX_WBITS)
        #print(data)

        return json.loads(data)

    def __createUrl(self, species, subtype, method, id, options):
        url = "http://" + self.__configuration.getHost() + ":" + str(self.__configuration.getPort()) + \
              CellBaseClient.PATH + self.__configuration.getVersion() + "/" + species
        if (CellBaseClient.ENABLEDQUERYTYPES[subtype] != None):
            url += "/" + CellBaseClient.ENABLEDQUERYTYPES[subtype]
        url += "/" + subtype
        if (id != None):
            url += "/" + str.join(",", id)
        url += "/" + method
        if (options != None):
            k, v in options.items():
            url += "?" + k + "=" + str.join(",", v) + "&"

    url += "?" + str.join("&", options)

    return url
