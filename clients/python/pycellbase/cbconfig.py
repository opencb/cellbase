
import requests
import json
import yaml
from retrying import retry

_DEFAULT_CONFIG = {
    "species": "hsapiens",
    "version": "v4",
    "rest": {"hosts": ["http://bioinfo.hpc.cam.ac.uk:80/cellbase",
                       "https://bioinfo.hpc.cam.ac.uk:80/cellbase"]}
}


class ConfigClient(object):
    """Sets up the default configuration for the CellBase client"""

    def __init__(self, config_input=None):
        # Default config params
        self._config = {
            'host': _DEFAULT_CONFIG['rest']['hosts'][0],
            'version': _DEFAULT_CONFIG['version'],
            'species': _DEFAULT_CONFIG['species']
        }

        # If config info is provided, override default config params
        if config_input is not None:
            if isinstance(config_input, dict):
                self._override_config_params_from_dict(config_input)
            else:
                self._override_config_params_from_file(config_input)

    def _override_config_params_from_file(self, config_fpath):
        """Overrides config params if config file is provided"""
        try:
            config_fhand = open(config_fpath, 'r')
        except:
            msg = 'Unable to read file "' + config_fpath + '"'
            raise IOError(msg)

        if config_fpath.endswith('.yml') or config_fpath.endswith('.yaml'):
            config_dict = yaml.safe_load(config_fhand)
        elif config_fpath.endswith('.json'):
            config_dict = json.loads(config_fhand.read())
        else:
            msg = 'Configuration file must end in ".json", ".yml" or ".yaml"'
            raise IOError(msg)

        self._override_config_params_from_dict(config_dict)

        config_fhand.close()

    def _override_config_params_from_dict(self, config_dict):
        """Overrides config params if a dict is provided"""
        if config_dict is not None:
            if 'rest' in config_dict:
                if 'hosts' in config_dict['rest']:
                    hosts = config_dict['rest']['hosts']
                    self._config['host'] = self._get_available_host(hosts)
            if 'version' in config_dict:
                self._config['version'] = config_dict['version']
            if 'species' in config_dict:
                self._config['species'] = config_dict['species']
            # Compatibility with java client config
            if 'defaultSpecies' in config_dict:
                self._config['species'] = config_dict['defaultSpecies']
        else:
            msg = 'No configuration parameters found'
            raise ValueError(msg)

    @staticmethod
    def _format_url(url):
        """"Formats URL strings"""
        if not (url.startswith('http://') or url.startswith('https://')):
            url = 'http://' + url
        return url

    def _get_available_host(self, hosts):
        """Returns the first available host"""
        available_host = None
        for host in hosts:
            host = self._format_url(host)
            if _check_host(host):
                available_host = host
                break

        if available_host is None:
            msg = 'No available host found in "' + str(hosts) + '"'
            raise requests.ConnectionError(msg)
        else:
            return available_host

    @staticmethod
    def get_default_configuration():
        return _DEFAULT_CONFIG

    @property
    def version(self):
        return self._config['version']

    @version.setter
    def version(self, new_version):
        self._config['version'] = new_version

    @property
    def host(self):
        return self._config['host']

    @host.setter
    def host(self, new_host):
        self._config['host'] = self._format_url(new_host)

    @property
    def species(self):
        return self._config['species']

    @species.setter
    def species(self, new_species):
        self._config['species'] = new_species

    @property
    def configuration(self):
        return self._config


def _returned_false(result):
    return result is False


@retry(
    wait_exponential_multiplier=10,
    wait_exponential_max=2000,
    retry_on_result=_returned_false,
    stop_max_attempt_number=5
)
def _check_host(host):
    """Checks host availability"""
    try:
        r = requests.head(host, timeout=1)
        if r.status_code == 302:  # Found
            return True
        else:
            return False
    except requests.ConnectionError:
        return False

