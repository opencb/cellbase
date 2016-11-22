import requests
import json
import yaml


class ConfigClient(object):
    """Sets up the default configuration for the CellBase client"""

    def __init__(self, config_fpath=None):
        # Default config params
        self._hosts = ['bioinfo.hpc.cam.ac.uk:80/cellbase',
                       'bioinfodev.hpc.cam.ac.uk:80/cellbase-4.5.0-beta']
        self._config = {
            'host': self._get_available_host(),
            'version': 'v4',
            'species': 'hsapiens',
        }

        # If config file is provided, override default config params
        if config_fpath is not None:
            self._override_config_params(config_fpath)

    def _override_config_params(self, config_fpath):
        """Overrides config params if config file is provided"""
        config_fhand = open(config_fpath, 'r')

        config_dict = None
        if config_fpath.endswith('.yml') or config_fpath.endswith('.yaml'):
            config_dict = yaml.safe_load(config_fhand)

        if config_fpath.endswith('.json'):
            config_dict = json.loads(config_fhand.read())

        if config_dict is not None:
            if 'hosts' in config_dict['rest']:
                self._hosts = config_dict['rest']['hosts']
                self._config['host'] = self._get_available_host()
            if 'version' in config_dict:
                self._config['version'] = config_dict['version']
            if 'species' in config_dict:
                self._config['species'] = config_dict['species']

        config_fhand.close()

    def _get_available_host(self):
        """Returns the first available host"""
        available_host = None
        for host in self._hosts:
            if not (host.startswith('http://') or host.startswith('https://')):
                host = 'http://' + host
            try:
                r = requests.head(host)
                if r.status_code == 302:  # Found
                    available_host = host
                    break
            except requests.ConnectionError:
                pass

        if available_host is None:
            msg = 'No available host found'
            raise requests.ConnectionError(msg)
        else:
            return available_host

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
        if not (new_host.startswith('http://') or
                    new_host.startswith('https://')):
            new_host = 'http://' + new_host
        self._config['host'] = new_host

    @property
    def species(self):
        return self._config['species']

    @species.setter
    def species(self, new_species):
        self._config['species'] = new_species

    @property
    def configuration(self):
        return self._config
