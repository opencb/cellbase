import unittest
from pycellbase.cbclient import CellBaseClient
from pycellbase.config import ConfigClient


class ConfigClientTest(unittest.TestCase):
    def test_init_config(self):
        cc = ConfigClient()
        assert cc.species == 'hsapiens'
        cc = ConfigClient('../resources/config.yml')
        assert cc.species == 'hsapiens'
        cc = ConfigClient('../resources/config.json')
        assert cc.species == 'hsapiens'

    def test_change_config(self):
        cc = ConfigClient()
        cbc = CellBaseClient(cc)
        assert cbc.get_config()['species'] == 'hsapiens'
        cc.species = 'mmusculus'
        assert cbc.get_config()['species'] == 'mmusculus'


if __name__ == '__main__':
    unittest.main()
