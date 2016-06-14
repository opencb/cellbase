import requests

__author__ = 'fjlopez'


class CellBaseClient:
    PATH = "/cellbase-dev-v4.0/webservices/rest/"
    ENABLEDQUERYTYPES = {"clinical": "feature", "exon": "feature", "gene": "feature", "chromosome": "genomic",
                         "meta": None, "protein": "feature", "region": "genomic", "snp": "feature", "species": None,
                         "tf": "regulatory", "id": "feature"}

    def __init__(self, configuration):
        self.__configuration = configuration

    def get(self, species, subtype, method, query_id, options):
        # Prepare the call to the server
        url = self.__create_url(species, subtype, method, query_id, options)

        headers = {"Accept-Encoding": "gzip"}
        response = requests.get(url, headers=headers)

        return response.json()

    def __create_url(self, species, subtype, method, query_id, query_options):
        url = "http://" + self.__configuration.get_host() + ":" + str(self.__configuration.get_port()) + \
              CellBaseClient.PATH + self.__configuration.get_version() + "/" + species
        if CellBaseClient.ENABLEDQUERYTYPES[subtype] is not None:
            url += "/" + CellBaseClient.ENABLEDQUERYTYPES[subtype]
        url += "/" + subtype
        if query_id is not None:
            url += "/" + str.join(",", query_id)
        url += "/" + method
        query_options_string = list()
        if query_options is not None:
            for k, v in query_options.items():
                if type(v) != int:
                    query_options_string.append(k + "=" + str.join(",", v))
                else:
                    query_options_string.append(k + "=" + str(v))
            url += "?" + str.join("&", query_options_string)

        return url
