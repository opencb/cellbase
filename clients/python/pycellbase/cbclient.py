import requests
import pycellbase.features as fts
import pycellbase.config as config
from pycellbase.commons import create_rest_url


class CellBaseClient(object):
    """Initializes the CellBase client and allows the creation of the
     different query clients"""
    def __init__(self, species=None, version=None, host=None, port=None):
        # Client storage; If a client is already created, then it is returned
        self._clients = {}

        # Setting up config params
        self._configuration = config.ConfigClient()
        if species is not None:
            self._configuration.species = species
        if version is not None:
            self._configuration.version = version
        if host is not None:
            self._configuration.host = host
        if port is not None:
            self._configuration.port = port

    def get_configuration(self):
        """Returns current configuration parameters"""
        conf = self._configuration.__dict__.items()
        conf_formatted = {}
        for k, v in conf:
            conf_formatted[k.replace('_', '')] = v
        return conf_formatted

    def get(self, category, subcategory, query_id, resource, species=None,
            host=None, port=None, version=None, **options):
        """Creates the URL for querying the REST service"""

        if species is None:
            species = self._configuration.species
        if host is None:
            host = self._configuration.host
        if port is None:
            port = self._configuration.port
        if version is None:
            version = self._configuration.version

        url = create_rest_url(host,
                              port,
                              version,
                              species,
                              category,
                              subcategory,
                              query_id,
                              resource,
                              options)

        response = requests.get(url, headers={"Accept-Encoding": "gzip"})
        return response.json()

    def get_gene_client(self):
        """Creates the gene client"""
        if 'GENE' not in self._clients:
            self._clients['GENE'] = fts.GeneClient(self._configuration)
        return self._clients['GENE']

    def get_protein_client(self):
        """Creates the protein client"""
        if 'PROTEIN' not in self._clients:
            self._clients['PROTEIN'] = fts.ProteinClient(self._configuration)
        return self._clients['PROTEIN']

    def get_variation_client(self):
        """Creates the variation client"""
        if 'VARIATION' not in self._clients:
            self._clients['VARIATION'] =\
                fts.VariationClient(self._configuration)
        return self._clients['VARIATION']

    def get_genomic_client(self):
        """Creates the genomic region client"""
        if 'GENOMIC' not in self._clients:
            self._clients['GENOMIC'] = \
                fts.GenomicRegionClient(self._configuration)
        return self._clients['GENOMIC']
