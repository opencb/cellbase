# Python

## Overview

CellBase implements a Python client library called **PyCellBase** to query data through REST web services API. PyCellBase provides programmatic access to **all implemented** REST web services, providing an easy, lightweight, fast and intuitive access to all CellBase data in a user-friendly way without the need of installing any local database, you just need to configure the remote CellBase REST URL. Data is always available by a high-availability cluster and queries have been tuned to ensure a real-time performance. PyCellBase offers the convenience of an object-oriented scripting language and provides the ability to integrate the obtained results into other Python applications. More info about this package in the Python client tutorial section. PyCellBase uses _multithreading_ to improve performance when the number of queries exceed a specific limit.

PyCellBase implements a _CellBaseClient_ class factory can create all the **different clients** to the data we want to query \(e.g. gene, transcript, variation, protein, genomic region, variant\). Each of these clients implement different functions to query all REST web services. Most of these methods will need to be provided with **comma-separated IDs or list of IDs**. Optional filters and extra options can be added as key-value parameters. **Responses** are retrieved as **JSON formatted data**. Therefore, fields can be queried by key. **Configuration** data as host, API version, or species is stored in a ConfigClient object. A custom configuration can be passed to CellBaseClient with a ConfigClient object provided with a JSON or YAML config file. If you want to change the configuration on the fly you can directly modify the ConfigClient object.

PyCellBase is open-source and code can be found at [`https://github.com/opencb/cellbase/tree/develop/clients/python/pycellbase`](https://github.com/opencb/cellbase/tree/develop/clients/python/pycellbase). PyCellbase be easily installed using PyPI. Please, find more details on how to use the python library at: [Python client tutorial](http://docs.opencb.org/display/cellbase/Python+client+library)

## Installation

Python client requires **Python 3.x,** although most of the code is fully compatible with Python 2.7. You can install PyCellBase either from [PyPI](https://pypi.org/) repository or from the source code.

### PyPI

PyCellBase client is deployed at [PyPI](https://pypi.org/) and available at [https://pypi.org/project/pycellbase](https://pypi.org/project/pycellbase/). It can be easily installed using _pip_ by executing:

|  |
| :--- |


### Source Code

PyCellBase can be installed from source code. You can get CellBase source code by cloning [GitHub CellBase](https://github.com/opencb/cellbase) repository and executing _setup.py_:

|  |
| :--- |


## Getting Started

### Configuration

Configuration stores the REST services host, API version and species.

Getting the default configuration:

|  |
| :--- |


Showing the configuration parameters being used at the moment:

|  |
| :--- |


A custom configuration can be passed to CellBaseClient with a ConfigClient object. JSON and YML files are supported:

|  |
| :--- |


A custom configuration can also be passed as a dictionary:

|  |
| :--- |


If you want to change the configuration on the fly you can directly modify the ConfigClient object:

|  |
| :--- |


### Querying data

The first step is to import the module and initialize the CellBaseClient:

|  |
| :--- |


_CellBaseClient_ factory object allows to create all the other clients. The next step is to create a specific client to query CellBase, for instance to call to _Gene_ web services:

|  |
| :--- |


And now, you can start asking to the CellBase RESTful service by providing a query ID:

|  |
| :--- |


```text

```

Responses are retrieved as JSON formatted data. Therefore, fields can be queried by key:

|  |
| :--- |


Data can be accessed specifying comma-separated IDs or a list of IDs:

|  |
| :--- |


If there is an available resource, but there is not an available method in this python package, the CellBaseClient can be used to create the URL of interest and query the RESTful service:

|  |
| :--- |


Optional filters and extra options can be added as key-value parameters \(value can be a comma-separated string or a list\):

|  |
| :--- |


If there is an available resource, but there is not an available method in this python package, the **CellBaseClient** class can be used to **create the URL of interest**. This class is able to access the RESTful Web Services through the _get_ method it implements. In this case, this method needs to be provided with those parameters which are required by the URL: category \(e.g. feature\), subcategory \(e.g. gene\), ID to search for \(e.g. BRCA1\) and method to query \(e.g. search\).

### Integrated Help

The best way to know which data can be retrieved for each client is either checking out the [RESTful web services](https://github.com/opencb/cellbase/wiki/RESTful-web-services) section of the CellBase Wiki or the [CellBase web services](http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/)

If we do not know which method is the most adequate for our task, we can get helpful information for each data-specific client:

|  |
| :--- |


We can get the accepted parameters and filters for a specific method of interest by using the _get\_help_ method:

|  |
| :--- |


## PyCellBase API

PyCellBase implements a _CellBaseClient_ object that acts as a factory to create all different clients such as RegionClient or GeneClient. Each of this clients implements a Python function to query each REST web service, you can see all web services at [http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/](http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/)

