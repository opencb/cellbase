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
    # HERE BE DRAGONS
    final_response = None

    # Setting up skip and limit default parameters
    call_skip = 0
    call_limit = 1000
    max_limit = None
    if options is None:
        options = {'skip': call_skip, 'limit': call_limit}
    else:
        if 'skip' not in options:
            options['skip'] = call_skip
        # If 'limit' is specified, a maximum of 'limit' results will be returned
        if 'limit' in options:
            max_limit = options['limit']
        # Server must be always queried for results in groups of 1000
        options['limit'] = call_limit

    # If there is a query_id, the next variables will be used
    total_id_list = []  # All initial ids
    next_id_list = []  # Ids which should be queried again for more results
    if query_id is not None:
        total_id_list = query_id.split(',')

    # If some query has more than 'call_limit' results, the server will be
    # queried again to retrieve the next 'call_limit results'
    call = True
    current_query_id = None  # Current REST query
    while call:

        # Check 'limit' parameter if there is a maximum limit of results
        if max_limit is not None and max_limit <= call_limit:
                options['limit'] = max_limit

        # Updating query_id and list of ids to query
        if query_id is not None:
            if current_query_id is None:
                current_query_id = query_id
                current_id_list = total_id_list
            else:
                current_query_id = ','.join(next_id_list)
                current_id_list = next_id_list

        # Retrieving url
        url = _create_rest_url(host=host,
                               port=port,
                               version=version,
                               species=species,
                               category=category,
                               subcategory=subcategory,
                               query_id=current_query_id,
                               resource=resource,
                               options=options)
        print(url)

        # Getting REST response
        r = requests.get(url, headers={"Accept-Encoding": "gzip"})
        response = r.json()['response']

        # Setting up final_response
        if final_response is None:
            final_response = response
        # Concatenating results
        else:
            if query_id is not None:
                for index, res in enumerate(response):
                    id_name = current_id_list[index]
                    id_index = total_id_list.index(id_name)
                    final_response[id_index]['result'] += res['result']
            else:
                final_response[0]['result'] += response[0]['result']

        if query_id is not None:
            # Checking which ids are completely retrieved
            next_id_list = []
            for index, res in enumerate(response):
                if res['numResults'] == call_limit:
                    next_id_list.append(current_id_list[index])

            # Ending REST calling when there are no more ids to retrieve
            if not next_id_list:
                call = False
        else:
            # Ending REST calling when there are no more results to retrieve
            if response[0]['numResults'] != call_limit:
                call = False

        # Skipping the first 'limit' results to retrieve the next ones
        options['skip'] += call_limit

        # Subtracting the number of returned results from the maximum goal
        if max_limit is not None:
            max_limit -= call_limit

    return final_response
