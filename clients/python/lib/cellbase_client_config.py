import json

__author__ = 'fjlopez'


class CellBaseClientConfig(dict):
    def __init__(self, config_file_name):
        fdw = open(config_file_name)
        dictionary_string = fdw.read()
        fdw.close()
        dict.__init__(self, json.loads(dictionary_string))

    def get_host(self):
        return self["database"]["hosts"]["first"]

    def get_port(self):
        return self["database"]["port"]

    def get_version(self):
        return self["version"]

    def check_host(self):
        pass

