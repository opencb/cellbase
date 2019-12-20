import unittest

from pycellbase.cbclient import CellBaseClient
from pycellbase.cbconfig import ConfigClient


class ConfigClientTest(unittest.TestCase):
    """Tests the ConfigClient class"""
    def test_init_config_file(self):
        """Checks retrieval of configuration params from config files"""
        # Default parameters
        cc = ConfigClient()
        assert cc.species == 'hsapiens'
        assert cc.version == 'v4'

        # Retrieving config params from YAML config file
        cc = ConfigClient('./tests/testresources/config.yml')
        assert cc.species == 'mmusculus'
        assert cc.version == 'v4'

        # Retrieving config params from JSON config file
        cc = ConfigClient('./tests/testresources/config.json')
        assert cc.species == 'celegans'
        assert cc.version == 'v3'

    def test_init_config_dict(self):
        """Checks retrieval of configuration params from config files"""
        # Default parameters
        cc = ConfigClient()
        assert cc.species == 'hsapiens'
        assert cc.version == 'v4'

        # Retrieving config params from config dict
        cc = ConfigClient({'species': 'mmusculus'})
        assert cc.species == 'mmusculus'
        assert cc.version == 'v4'
        cc = ConfigClient({'species': 'mmusculus', 'version': 'v3'})
        assert cc.species == 'mmusculus'
        assert cc.version == 'v3'

    def test_change_config(self):
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

    def test_get_default_config(self):
        # Initialization
        cc = ConfigClient()
        cbc = CellBaseClient(cc)

        assert cc.get_default_configuration() == cbc.get_default_configuration()


if __name__ == '__main__':
    unittest.main()
