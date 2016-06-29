import requests


def _create_rest_url(host, port, version, species, category, subcategory,
                    resource, query_id, options):
    """Creates the URL for querying the REST service"""

    # cellbase_rest = 'cellbase/webservices/rest'
    cellbase_rest = 'cellbase-dev-v4.0/webservices/rest'

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
    opt_params = ['include', 'exclude', 'skip', 'limit', 'count']
    if options is not None:
        opts = []
        for k, v in options.items():
            if k in opt_params:
                opts.append(k + '=' + str(v))
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
    print(url)
    return response
