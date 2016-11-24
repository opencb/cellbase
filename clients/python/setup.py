from distutils.core import setup


setup_kwargs = {
    'name': 'pycellbase',
    'version': '0.1.1',
    'description': 'Python client for CellBase',
    'long_description': ('This Python package enables to query and obtain'
                         ' biological information from the exhaustive'
                         ' RESTful Web service API that has been implemented'
                         ' for the CellBase database'),
    'keywords': 'opencb cellbase client',
    'author': 'Daniel Perez-Gil',
    'author_email': 'opencb@googlegroups.com',
    'url': 'https://github.com/opencb/cellbase',
    'packages': ['pycellbase'],
    'package_dir': {'pycellbase': 'pycellbase'},
    'install_requires': ['pyyaml', 'requests']
}

setup(**setup_kwargs)
