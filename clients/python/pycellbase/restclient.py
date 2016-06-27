import requests

_CELLBASE_REST = 'cellbase/webservices/rest'


class RestClient(object):
    """Queries the REST service given the different query params"""
    def __init__(self, configuration, subcategory=None, category=None):
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category

    def _create_url(self, category, subcategory, query_id, resource,
                    options):
        """Creates the URL for querying the REST service"""
        # Creating the basic URL
        url = ('http://' + '/'.join([self._configuration.host,
                                     _CELLBASE_REST,
                                     self._configuration.version,
                                     self._configuration.species,
                                     category,
                                     subcategory,
                                     query_id,
                                     resource]))

        # Checking optional params
        if options:
            opts = []
            if 'include' in options:
                opts.append('include=' + options['include'])
            if 'exclude' in options:
                opts.append('exclude=' + options['exclude'])
            if 'skip' in options:
                opts.append('skip=' + str(options['skip']))
            if 'limit' in options:
                opts.append('limit=' + str(options['limit']))
            if 'count' in options:
                opts.append('count=' + str(options['count']).lower())
            if opts:
                url += '?' + '&'.join(opts)
        print(url)
        return url

    def get(self, resource, query_id, options):
        """Queries the REST service and returns the result"""
        url = self._create_url(self._category, self._subcategory, query_id,
                               resource, options)
        response = requests.get(url, headers={"Accept-Encoding": "gzip"})

        return response.json()
