import requests
from pycellbase.commons import create_rest_url


class RestClient(object):
    """Queries the REST service given the different query params"""
    def __init__(self, configuration, subcategory=None, category=None):
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category

    def _get(self, resource, query_id=None, options=None):
        """Queries the REST service and returns the result"""

        url = create_rest_url(self._configuration.host,
                              self._configuration.port,
                              self._configuration.version,
                              self._configuration.species,
                              self._category,
                              self._subcategory,
                              query_id,
                              resource,
                              options)

        response = requests.get(url, headers={"Accept-Encoding": "gzip"})
        return response.json()

    def get_methods(self):
        """Returns available methods for an object"""
        ms = [method for method in dir(self) if callable(getattr(self, method))]
        return [m for m in ms if not m.startswith('_')]

    def get_help(self):
        """Returns help for a specific element"""
        return self._get('help')
