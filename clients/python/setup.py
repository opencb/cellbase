try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup
from codecs import open  # To use a consistent encoding
from os import path

here = path.abspath(path.dirname(__file__))

# Get the long description from the README file
with open(path.join(here, 'README.rst'), encoding='utf-8') as f:
    long_description = f.read()

setup_kwargs = {
    'name': 'pycellbase',
    'version': '4.9.0',
    'description': 'Python client for CellBase',
    'long_description': long_description,
    'long_description_content_type': 'text/x-rst',
    'url': 'https://github.com/opencb/cellbase/tree/develop/clients/python',
    'author': 'Daniel Perez-Gil',
    'author_email': 'dperezgil89@gmail.com',
    'license': 'Apache Software License',
    'classifiers': [
        'Development Status :: 5 - Production/Stable',
        'Intended Audience :: Developers',
        'Topic :: Scientific/Engineering :: Bio-Informatics',
        'License :: OSI Approved :: Apache Software License',
        'Programming Language :: Python :: 2.7',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.5',
    ],
    'keywords': 'opencb cellbase bioinformatics database',
    'project_urls': {
        'Documentation': 'http://docs.opencb.org/display/cellbase/RESTful+Web+Services',
        'Tutorial': 'http://docs.opencb.org/display/cellbase/Python+client+library',
        'Source': 'https://github.com/opencb/cellbase/tree/develop/clients/python',
        'CellBase': 'https://github.com/opencb/cellbase',
        'CellBase Documentation': 'http://docs.opencb.org/display/cellbase/CellBase+Home',
        'Bug Reports': 'https://github.com/opencb/cellbase/issues',
    },
    'packages': ['pycellbase'],
    'install_requires': ['requests', 'pyyaml', 'retrying'],
    'scripts': ['tools/cbtools.py']
}

setup(**setup_kwargs)
