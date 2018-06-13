.. contents::

PyCellBase
==========

- PyCellBase is a Python package that provides programmatic access to the comprehensive RESTful web service API that has been implemented for the `CellBase`_ database, providing an easy, lightweight, fast and intuitive access to it.
- This package can be used to access to relevant biological information in a user-friendly way without the need of local databases installations.
- Data is always available by a high-availability cluster and queries have been tuned to ensure a real-time performance.
- PyCellBase offers the convenience of an object-oriented scripting language and provides the ability to integrate the obtained results into other Python applications.
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

PyPI
````
PyCellBase is stored in PyPI and can be installed via pip::

   $ pip install pycellbase

Usage
-----

Getting started
```````````````
The first step is to import the module and initialize the **CellBaseClient**:

.. code-block:: python

    >>> from pycellbase.cbclient import CellBaseClient
    >>> cbc = CellBaseClient()

The second step is to create the **specific client** for the data we want to query (in this example we want to obtain information for a gene):

.. code-block:: python

   >>> gc = cbc.get_gene_client()

And now, you can start asking to the CellBase RESTful service by providing a **query ID**:

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')  # Obtaining TFBS for this gene

Responses are retrieved as **JSON** formatted data. Therefore, fields can be queried by key:

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

Data can be accessed specifying **comma-separated IDs** or a **list of IDs**:

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

Optional **filters and extra options** can be added as key-value parameters (value can be a comma-separated string or a list):

.. code-block:: python

    >>> tfbs_responses = gc.get_tfbs('BRCA1')
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', include='name,id')  # Return only name and id
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', include=['name', 'id'])  # Return only name and id
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', **{'include': 'name,id'])  # Return only name and id
    >>> len(res[0]['result'])
    175

    >>> tfbs_responses = gc.get_tfbs('BRCA1', limit=100)  # Limit to 100 results
    >>> len(res[0]['result'])
    100

    >>> tfbs_responses = gc.get_tfbs('BRCA1', skip=100)  # Skip first 100 results
    >>> len(res[0]['result'])
    75

What can I ask for?
```````````````````
The best way to know which data can be retrieved for each client is either checking out the `RESTful web services`_ section of the CellBase Wiki or the `CellBase web services`_

Configuration
`````````````

Configuration stores the REST services host, API version and species.

Getting the **default configuration**:

.. code-block:: python

    >>> ConfigClient().get_default_configuration()
    {'version': 'v4',
     'species': 'hsapiens',
     'rest': {'hosts': ['http://bioinfo.hpc.cam.ac.uk:80/cellbase']}}


Showing the configuration parameters being used at the moment:

.. code-block:: python

    >>> cbc.show_configuration()
    {'host': 'bioinfo.hpc.cam.ac.uk:80/cellbase',
     'version': 'v4',
     'species': 'hsapiens'}

A **custom configuration** can be passed to CellBaseClient using a **ConfigClient object**. JSON and YAML files are supported:

.. code-block:: python

    >>> from pycellbase.cbconfig import ConfigClient
    >>> from pycellbase.cbclient import CellBaseClient

    >>> cc = ConfigClient('config.json')
    >>> cbc = CellBaseClient(cc)

A **custom configuration** can also be passed as a dictionary:

.. code-block:: python

    >>> from pycellbase.cbconfig import ConfigClient
    >>> from pycellbase.cbclient import CellBaseClient

    >>> custom_config = {'rest': {'hosts': ['bioinfo.hpc.cam.ac.uk:80/cellbase']}, 'version': 'v4', 'species': 'hsapiens'}
    >>> cc = ConfigClient(custom_config)
    >>> cbc = CellBaseClient(cc)

If you want to change the configuration **on the fly** you can directly modify the ConfigClient object:

.. code-block:: python

    >>> cc = ConfigClient()
    >>> cbc = CellBaseClient(cc)

    >>> cbc.show_configuration()['version']
    'v4'
    >>> cc.version = 'v3'
    >>> cbc.show_configuration()['version']
    'v3'

Use case
````````
A use case where PyCellBase is used to obtain multiple kinds of data from different sources can be found in this `Jupyter Notebook`_


.. _CellBase: https://github.com/opencb/cellbase
.. _CellBase Wiki: https://github.com/opencb/cellbase/wiki
.. _Python client: https://github.com/opencb/cellbase/wiki/Python-client
.. _RESTful web services: https://github.com/opencb/cellbase/wiki/RESTful-web-services
.. _CellBase web services: http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
.. _Jupyter Notebook: http://nbviewer.jupyter.org/github/opencb/cellbase/blob/develop/clients/python/use_case.ipynb
