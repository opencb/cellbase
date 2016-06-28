import pycellbase.features as fts
import pycellbase.config as config
from pycellbase.commons import get


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

    def get(self, category, subcategory, resource, query_id=None, **options):
        """Creates the URL for querying the REST service"""
        response = get(host=self._configuration.host,
                       port=self._configuration.port,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=category,
                       subcategory=subcategory,
                       query_id=query_id,
                       resource=resource,
                       options=options)

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
