import unittest
from pycellbase.cbclient import CellBaseClient


class CellBaseClientTest(unittest.TestCase):
    def test_config(self):
        cbc = CellBaseClient()
        assert cbc.get_configuration()['species'] == 'hsapiens'
        assert cbc.get_configuration()['version'] == 'latest'

        cbc = CellBaseClient(species='mmusculus', version='v3')
        assert cbc.get_configuration()['species'] == 'mmusculus'
        assert cbc.get_configuration()['version'] == 'v3'

    def test_create_clients(self):
        cbc = CellBaseClient()

        gc = cbc.get_gene_client()
        assert isinstance(gc, object)

        gp = cbc.get_protein_client()
        assert isinstance(gp, object)

    def test_get(self):
        cbc = CellBaseClient()

        r = cbc.get('feature', 'gene', 'protein', 'BRCA1')
        assert r['response'][0]['result'][0]['name'][0] == 'BRCA1_HUMAN'


if __name__ == '__main__':
    unittest.main()
