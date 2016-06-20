import requests

_CELLBASE_REST = 'cellbase/webservices/rest/'


class RestClient(object):
    def __init__(self, configuration, subcategory=None, category=None):
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category

    def _create_url(self, species, category, subcategory, query_id, resource,
                    options):
        url = ('http://' + '/'.join([self._configuration.host, _CELLBASE_REST +
                                     self._configuration.version, species,
                                     category, subcategory, query_id,
                                     resource]))
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
        return url

    def get(self, resource, query_id, options):
        # TODO Species param should be customizable
        species = 'hsapiens'
        url = self._create_url(species, self._category, self._subcategory,
                               query_id, resource, options)
        response = requests.get(url, headers={"Accept-Encoding": "gzip"})

        return response.json()
