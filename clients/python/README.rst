.. contents::

PyCellBase
==========

- This Python package makes use of the exhaustive RESTful Web service API that has been implemented for the `CellBase`_ database.

- It enables to query and obtain a wealth of biological information from a single database, saving a lot of time.

- As all information is integrated, queries about different biological topics can be easily and all this information can be linked together.

- Currently *Homo sapiens*, *Mus musculus* and a total of 48 species are available and many others will be included soon.

- More info about this package in the `Python client`_ section of the `CellBase Wiki`_

Installation
------------

Cloning
```````
PyCellBase can be cloned in your local machine by executing in your terminal::

   $ git clone https://github.com/opencb/cellbase.git

Once you have downloaded the project you can install the library::

   $ cd cellbase/clients/python
   $ python setup.py install

Usage
-----

Getting started
```````````````
The first step is to import the module and initialize the CellBaseClient:

.. code-block:: python

    >>> from pycellbase.cbclient import CellBaseClient
    >>> cbc = CellBaseClient()

The second step is to create the specific client for the data we want to query (in this example we want to obtain information for a gene):

.. code-block:: python

   >>> gc = cbc.get_gene_client()

And now, you can start asking to the CellBase RESTful service by providing a query ID:

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')  # Obtaining TFBS for this gene

Responses are retrieved as JSON formatted data. Therefore, fields can be queried by key:

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')
    >>> tfbs_responses[0]['result'][0]['tfName']
    'E2F4'

    >>> transcript_responses = gc.get_transcript('BRCA1')
    >>> 'Number of transcripts: %d' % (len(transcript_responses[0]['result']))
    'Number of transcripts: 27'

    >>> for tfbs_response in gc.get_tfbs('BRCA1,BRCA2,LDLR'):
    ...     print('Number of TFBS for "%s": %d' % (tfbs_response['id'], len(tfbs_response['result'])))
    'Number of TFBS for "BRCA1": 175'
    'Number of TFBS for "BRCA2": 43'
    'Number of TFBS for "LDLR": 141'

Data can be accessed specifying comma-separated IDs or a list of IDs:

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')
    >>> len(tfbs_responses)
    1

    >>> tfbs_responses = gc.get_tfbs('BRCA1,BRCA2')
    >>> len(tfbs_responses)
    2

    >>> tfbs_responses = gc.get_tfbs(['BRCA1', 'BRCA2'])
    >>> len(tfbs_responses)
    2

If there is an available resource in the CellBase Webservices, but there is not an available method in this python package, the CellBaseClient can be used to create the URL of interest and query the RESTful service:

.. code-block:: python

    >>> tfbs_responses = cbc.get(category='feature', subcategory='gene', query_id='BRCA1', resource='tfbs')
    >>> tfbs_responses[0]['result'][0]['tfName']
    'E2F4'

Optional filters and extra options can be added as key-value parameters (value can be a comma-separated string or a list):

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', include='name,id')
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', include = ['name', 'id'])
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', limit=100)
    >>> len(res[0]['result'])
    100

    >>> tfbs_responses = gc.get_tfbs('BRCA1', skip=100)
    >>> len(res[0]['result'])
    75

What can I ask for?
```````````````````
The best way to know which data can be retrieved for each client is either checking out the `RESTful web services`_ section of the CellBase Wiki or the `CellBase web services`_

Configuration
`````````````

Configuration stores the REST services host, API version and species.

Default configuration:

.. code-block:: python

    >>> cbc.get_config()
    {'host': 'bioinfo.hpc.cam.ac.uk:80/cellbase',
     'version': 'v4',
     'species': 'hsapiens'}

A custom configuration can be passed to CellBaseClient with a ConfigClient object. JSON and YML files are supported:

.. code-block:: python

    >>> from pycellbase.cbconfig import ConfigClient
    >>> from pycellbase.cbclient import CellBaseClient

    >>> cc = ConfigClient('config.json')
    >>> cbc = CellBaseClient(cc)

A custom configuration can also be passed as a dictionary:

.. code-block:: python

    >>> from pycellbase.cbconfig import ConfigClient
    >>> from pycellbase.cbclient import CellBaseClient

    >>> custom_config = {'hosts': ['bioinfo.hpc.cam.ac.uk:80/cellbase'], 'version': 'v4', 'species': 'hsapiens'}
    >>> cc = ConfigClient(custom_config)
    >>> cbc = CellBaseClient(cc)

If you want to change the configuration on the fly you can directly modify the ConfigClient object:

.. code-block:: python

    >>> cc = ConfigClient()
    >>> cbc = CellBaseClient(cc)

    >>> cbc.get_config()['version']
    'v4'

    >>> cc.version = 'v3'
    >>> cbc.get_config()['version']
    'v3'

.. _CellBase: https://github.com/opencb/cellbase
.. _CellBase Wiki: https://github.com/opencb/cellbase/wiki
.. _Python client: https://github.com/opencb/cellbase/wiki/Python-client
.. _RESTful web services: https://github.com/opencb/cellbase/wiki/RESTful-web-services
.. _CellBase web services: http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
