import requests


def _create_rest_url(host, port, version, species, category, subcategory,
                    resource, query_id, options):
    """Creates the URL for querying the REST service"""

    cellbase_rest = 'cellbase/webservices/rest'

    # Creating the basic URL
    url = ('http://' + '/'.join([host + ':' + port,
                                 cellbase_rest,
                                 version,
                                 species,
                                 category,
                                 subcategory
                                 ]))

    # If subcategory is queried, query_id can be absent
    if query_id is not None:
        url += '/' + '/'.join([query_id, resource])
    else:
        url += '/' + resource

    # Checking optional params
    if options is not None:
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


def get(host, port, version, species, category, subcategory, resource,
        query_id=None, options=None):
    """Creates the URL for querying the REST service"""
    url = _create_rest_url(host=host,
                           port=port,
                           version=version,
                           species=species,
                           category=category,
                           subcategory=subcategory,
                           query_id=query_id,
                           resource=resource,
                           options=options)

    response = requests.get(url, headers={"Accept-Encoding": "gzip"})

    return response
