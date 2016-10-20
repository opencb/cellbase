import unittest

from pycellbase.cbclient import CellBaseClient
from pycellbase.cbconfig import ConfigClient


class ConfigClientTest(unittest.TestCase):
    """Tests the ConfigClient class"""
    def test_init_config(self):
        """Checks retrieval of configuration params from config files"""
        # Default parameters
        cc = ConfigClient()
        assert cc.species == 'hsapiens'
        assert cc.version == 'v4'

        # Retrieving config params from YAML config file
        cc = ConfigClient('../resources/config.yml')
        assert cc.species == 'mmusculus'
        assert cc.version == 'v4'

        # Retrieving config params from JSON config file
        cc = ConfigClient('../resources/config.json')
        assert cc.species == 'celegans'
        assert cc.version == 'v3'

    def test_change_config(self):
        """Checks configuration customization"""
        # Initialization
        cc = ConfigClient()
        cbc = CellBaseClient(cc)

        # Checking some default config params
        assert cbc.get_config()['species'] == 'hsapiens'
        assert cbc.get_config()['version'] == 'v4'

        # Checking some setters for config params
        cc.species = 'mmusculus'
        assert cbc.get_config()['species'] == 'mmusculus'
        cc.version = 'v3'
        assert cbc.get_config()['version'] == 'v3'


if __name__ == '__main__':
    unittest.main()
