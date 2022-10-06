import sys
import time
import warnings
import requests
import threading
import itertools
try:
    from Queue import Queue
except ImportError:
    from queue import Queue

_CALL_BATCH_SIZE = 200
_NUM_THREADS_DEFAULT = 4


def _create_rest_url(host, version, species, category, subcategory,
                     resource, query_id, options):
    """Creates the URL for querying the REST service"""

    # Creating the basic URL
    url_items = [host, 'webservices/rest', version, species, category,
                 subcategory, query_id, resource]
    url_items = filter(None, url_items)  # Some url items can be empty
    url = ('/'.join(url_items))

    # Checking optional params
    if options is not None:
        opts = []
        for k, v in options.items():
            if k == 'debug':
                continue
            if isinstance(v, list):
                opts.append(k + '=' + ','.join(map(str, v)))
            else:
                opts.append(k + '=' + str(v))
        if opts:
            url += '?' + '&'.join(opts)

    return url


def _fetch(session, host, version, species, category, subcategory, resource,
           query_id=None, options=None, method='get', data=None):
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
    next_id_indexes = []  # Ids position in the final response
    if query_id is not None:
        total_id_list = query_id.split(',')

    # If some query has more than 'call_limit' results, the server will be
    # queried again to retrieve the next 'call_limit results'
    call = True
    current_query_id = None  # Current REST query
    current_id_list = None  # Current list of ids
    time_out_counter = 0  # Number of times a query is repeated due to time-out
    while call:
        # Check 'limit' parameter if there is a maximum limit of results
        if max_limit is not None and max_limit <= call_limit:
                opts['limit'] = max_limit

        # Updating query_id and list of ids to query
        if query_id is not None:
            if current_query_id is None:
                current_query_id = query_id
                current_id_list = total_id_list
                current_id_indexes = range(len(total_id_list))
            else:
                current_query_id = ','.join(next_id_list)
                current_id_list = next_id_list
                current_id_indexes = next_id_indexes

        # Retrieving url
        url = _create_rest_url(host=host,
                               version=version,
                               species=species,
                               category=category,
                               subcategory=subcategory,
                               query_id=current_query_id,
                               resource=resource,
                               options=opts)

        # DEBUG
        if options is not None:
            if 'debug' in options and options['debug']:
                sys.stderr.write(url + '\n')

        # Getting REST response
        if method == 'get':
            r = session.get(url)
        elif method == 'post':
            r = session.post(url, data=data)
        else:
            msg = 'Method "' + method + '" not implemented'
            raise NotImplementedError(msg)

        if r.status_code == 504:  # Gateway Time-out
            if time_out_counter == 99:
                msg = 'Server not responding in time'
                raise requests.ConnectionError(msg)
            time_out_counter += 1
            time.sleep(1)
            continue
        time_out_counter = 0

        try:
            json_obj = r.json()

            # TODO Remove deprecated response and result in future release. Added for backwards compatibility
            if 'response' in json_obj:
                json_obj['responses'] = json_obj['response']
            for query_result in json_obj['responses']:
                if 'result' in query_result:
                    query_result['results'] = query_result['result']

            response = json_obj['responses']

        except ValueError:
            msg = 'Bad JSON format retrieved from server'
            raise ValueError(msg)

        # Setting up final_response
        if final_response is None:
            final_response = response
        # Concatenating results
        else:
            if query_id is not None:
                for index, res in enumerate(response):
                    id_index = current_id_indexes[index]
                    final_response[id_index]['results'] += res['results']
            else:
                final_response[0]['results'] += response[0]['results']

        if query_id is not None:
            # Checking which ids are completely retrieved
            next_id_list = []
            next_id_indexes = []
            for index, res in enumerate(response):
                if res['numResults'] == call_limit:
                    next_id_list.append(current_id_list[index])
                    next_id_indexes.append(current_id_indexes[index])
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


def _worker(queue, results, session, host, version, species, category,
            subcategory, resource, options=None, method='get', data=None):
    """Manages the queue system for the threads"""
    while True:
        # Fetching new element from the queue
        index, query_id = queue.get()
        response = _fetch(session, host, version, species, category,
                          subcategory, resource, query_id, options, method,
                          data)
        # Store data in results at correct index
        results[index] = response
        # Signaling to the queue that task has been processed
        queue.task_done()


def get(session, host, version, species, category, subcategory, resource,
        query_id=None, options=None, method='get', data=None):
    """Queries the REST service using multiple threads if needed"""

    # If query_id is an array, convert to comma-separated string
    if query_id is not None and isinstance(query_id, list):
        query_id = ','.join(query_id)

    # If data is an array, convert to comma-separated string
    if data is not None and isinstance(data, list):
        data = ','.join(data)

    # Multithread if the number of queries is greater than _CALL_BATCH_SIZE
    if query_id is None or len(query_id.split(',')) <= _CALL_BATCH_SIZE:
        response = _fetch(session, host, version, species, category,
                          subcategory, resource, query_id, options, method,
                          data)
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
                                         'session': session,
                                         'host': host,
                                         'version': version,
                                         'species': species,
                                         'category': category,
                                         'subcategory': subcategory,
                                         'resource': resource,
                                         'options': options,
                                         'method': method,
                                         'data': data})
            # Setting threads as "daemon" allows main program to exit eventually
            # even if these do not finish correctly
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


def deprecated(func):
    """Prints a warning for functions marked as deprecated"""
    def new_func(*args, **kwargs):
        warnings.simplefilter('always', DeprecationWarning)  # turn off filter
        warnings.warn('Call to deprecated function "{}".'.format(func.__name__),
                      category=DeprecationWarning, stacklevel=2)
        warnings.simplefilter('default', DeprecationWarning)  # reset filter
        return func(*args, **kwargs)
    return new_func
