__author__ = 'fjlopez'

import json
import string

class CellBaseClientConfig(dict):
    def __init__(self, configFileName):
        fdw = open(configFileName)
        dictionaryString = fdw.read()
        fdw.close()
        dict.__init__(self, json.loads(dictionaryString))

    def getHost(self):
        return self["database"]["hosts"]["first"]

    def getPort(self):
        return self["database"]["port"]

    def getVersion(self):
        return self["version"]
    def CheckHost(self):
        pass

