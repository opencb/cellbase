import requests
import threading
import itertools
try:
    from Queue import Queue
except ImportError:
    from queue import Queue

_CALL_BATCH_SIZE = 2000
_NUM_THREADS_DEFAULT = 4


def _create_rest_url(host, version, species, category, subcategory,
                     resource, query_id, options):
    """Creates the URL for querying the REST service"""
    # Creating the basic URL
    url = ('http://' + '/'.join([host,
                                 'webservices/rest',
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
        for k, v in options.items():
            opts.append(k + '=' + str(v))
        if opts:
            url += '?' + '&'.join(opts)

    return url


def _worker(queue, results, host, version, species, category,
            subcategory, resource, options=None):
    """Manages the queue system for the threads"""
    while True:
        # Fetching new element from the queue
        index, query_id = queue.get()
        response = _fetch(host, version, species, category, subcategory,
                          resource, query_id, options)
        # Store data in results at correct index
        results[index] = response
        # Signaling to the queue that task has been processed
        queue.task_done()


def get(host, version, species, category, subcategory, resource,
        query_id=None, options=None):
    """Queries the REST service using multiple threads if needed"""

    if query_id is None or len(query_id.split(',')) <= _CALL_BATCH_SIZE:
        response = _fetch(host, version, species, category, subcategory,
                          resource, query_id, options)
        return response
    else:
        if options is not None and 'num_threads' in options:
            num_threads = options['num_threads']
        else:
            num_threads = _NUM_THREADS_DEFAULT

        # Splitting query_id into batches depending on the call batch size
        id_list = query_id.split(',')
        id_batches = [','.join(id_list[x:x+_CALL_BATCH_SIZE])
                      for x in range(0, len(id_list), _CALL_BATCH_SIZE)]

        # Setting up the queue to hold all the id batches
        q = Queue(maxsize=0)
        # Creating a size defined list to store thread results
        res = [''] * len(id_batches)

        # Setting up the threads
        for thread in range(num_threads):
            t = threading.Thread(target=_worker,
                                 kwargs={'queue': q,
                                         'results': res,
                                         'host': host,
                                         'version': version,
                                         'species': species,
                                         'category': category,
                                         'subcategory': subcategory,
                                         'resource': resource,
                                         'options': options})
            # Setting threads as "daemon" allows main program to exit eventually
            # even if these dont finish correctly
            t.setDaemon(True)
            t.start()

        # Loading up the queue with index and id batches for each job
        for index, batch in enumerate(id_batches):
            q.put((index, batch))  # Notice this is a tuple

        # Waiting until the queue has been processed
        q.join()

    # Joining all the responses into a one final response
    final_response = list(itertools.chain.from_iterable(res))

    return final_response


def _fetch(host, version, species, category, subcategory, resource,
           query_id=None, options=None):
    """Queries the REST service retrieving results until exhaustion or limit"""
    # HERE BE DRAGONS
    final_response = None

    # Setting up skip and limit default parameters
    call_skip = 0
    call_limit = 1000
    max_limit = None
    if options is None:
        opts = {'skip': call_skip, 'limit': call_limit}
    else:
        opts = options.copy()  # Do not modify original data!
        if 'skip' not in opts:
            opts['skip'] = call_skip
        # If 'limit' is specified, a maximum of 'limit' results will be returned
        if 'limit' in opts:
            max_limit = opts['limit']
        # Server must be always queried for results in groups of 1000
        opts['limit'] = call_limit

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
                opts['limit'] = max_limit

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
                               version=version,
                               species=species,
                               category=category,
                               subcategory=subcategory,
                               query_id=current_query_id,
                               resource=resource,
                               options=opts)
        # print(url)  # DEBUG

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
        opts['skip'] += call_limit

        # Subtracting the number of returned results from the maximum goal
        if max_limit is not None:
            max_limit -= call_limit
            # When 'limit' is 0 returns all the results. So, break the loop if 0
            if max_limit == 0:
                break

    return final_response
