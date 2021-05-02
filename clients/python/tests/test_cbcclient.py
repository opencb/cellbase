import unittest

from pycellbase.cbclient import CellBaseClient
from pycellbase.cbconfig import ConfigClient


class CellBaseClientTest(unittest.TestCase):
    """Tests the CellBaseClient class"""
    def test_config(self):
        """Checks configuration customization"""
        # Initialization
        cc = ConfigClient()
        cbc = CellBaseClient(cc)

        # Checking some default config params
        assert cbc.show_configuration()['species'] == 'hsapiens'
        assert cbc.show_configuration()['version'] == 'v4'

        # Checking some setters for config params
        cc.species = 'mmusculus'
        assert cbc.show_configuration()['species'] == 'mmusculus'
        cc.version = 'v3'
        assert cbc.show_configuration()['version'] == 'v3'

    def test_get(self):
        """"Checks generic fetcher for RESTful service"""
        # Initialization
        cc = ConfigClient()
        cbc = CellBaseClient(cc)

        res = cbc.get('feature', 'gene', 'protein', 'BRCA1')
        assert res[0]['result'][0]['name'][0] == 'BRCA1_HUMAN'


if __name__ == '__main__':
    unittest.main()
