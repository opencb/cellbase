import pycellbase.features as fts
import pycellbase.config as config


class CellBaseClient(object):
    def __init__(self):
        self._configuration = config.ConfigClient()
        self._clients = {}

    def get_gene_client(self):
        if 'GENE' not in self._clients:
            self._clients['GENE'] = fts.GeneClient(self._configuration)
        return self._clients['GENE']

    def get_protein_client(self):
        if 'PROTEIN' not in self._clients:
            self._clients['PROTEIN'] = fts.ProteinClient(self._configuration)
        return self._clients['PROTEIN']

    def get_variation_client(self):
        if 'VARIATION' not in self._clients:
            self._clients['VARIATION'] = fts.VariationClient(self._configuration)
        return self._clients['VARIATION']

    def get_genomic_client(self):
        if 'GENOMIC' not in self._clients:
            self._clients['GENOMIC'] = \
                fts.GenomicRegionClient(self._configuration)
        return self._clients['GENOMIC']
