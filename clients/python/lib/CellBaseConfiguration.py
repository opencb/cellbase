__author__ = 'fjlopez'

import json
import string

class CellBaseConfiguration(dict):
    def __init__(self, configFileName):
        fdw = open(configFileName)
        dictionaryString = string.join(fdw.readlines(),sep="")
        fdw.close()
        dict.__init__(self, json.loads(dictionaryString))

    def getHost(self):
        return self["database"]["host"]

    def getPort(self):
        return self["database"]["port"]

    def getVersion(self):
        return self["version"]
