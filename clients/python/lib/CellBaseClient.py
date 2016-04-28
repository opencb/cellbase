__author__ = 'fjlopez'

import os
import json
import string
import zlib
import urllib.request
import requests

class CellBaseClient:

    PATH = "/cellbase-dev-v4.0/webservices/rest/"
    ENABLEDQUERYTYPES = {"clinical":"feature","exon":"feature","gene":"feature","chromosome":"genomic","meta":None,
                         "protein":"feature","region":"genomic","snp":"feature","species":None,"tf":"regulatory",
                         "id":"feature"}

    def __init__(self, configuration):
        self.__configuration = configuration

    def get(self, species, subtype, method, id, options):
        # Prepare the call to the server
        url = self.__createUrl(species, subtype, method, id, options)

        # print(url)

        # headers = {"Accept-Encoding": "gzip"}
        # response = requests.get(url, headers=headers)
        response = requests.get(url)
        # print(response.request.headers)
        # print(response.headers)
        return response.json()

    def __createUrl(self, species, subtype, method, id, options):
        url = "http://" + self.__configuration.getHost() + ":" + str(self.__configuration.getPort()) + \
              CellBaseClient.PATH + self.__configuration.getVersion() + "/" + species
        if (CellBaseClient.ENABLEDQUERYTYPES[subtype] != None):
            url += "/" + CellBaseClient.ENABLEDQUERYTYPES[subtype]
        url += "/" + subtype
        if (id != None):
            url += "/" + str.join(",", id)
        url += "/" + method
        filter = list()
        if (options != None):
            for k, v in options.items():
                if (type(v)!=int):
                    filter.append(k + "=" + str.join(",", v) )
                else:
                    #url += "?" + k + "=" + str(v) + "&"
                    filter.append(k + "=" + str(v))
            url += "?" + str.join("&", filter)


        return url
