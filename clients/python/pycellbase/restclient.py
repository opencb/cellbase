from pycellbase.commons import get


class RestClient(object):
    """Queries the REST service given the different query params"""
    def __init__(self, configuration, subcategory=None, category=None):
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category

    def _get(self, resource, query_id, options):
        """Queries the REST service and returns the result"""
        response = get(host=self._configuration.host,
                       port=self._configuration.port,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=self._subcategory,
                       query_id=query_id,
                       resource=resource,
                       options=options)

        return response.json()

    def get_methods(self):
        """Returns available methods for an object"""
        ms = [method for method in dir(self) if callable(getattr(self, method))]
        return [m for m in ms if not m.startswith('_')]

    def get_help(self):
        """Returns help for a specific element"""
        return self._get('help')
