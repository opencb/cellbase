from distutils.core import setup


# Getting client version
def get_version():
    version = 'Undefined'
    init_fhand = open('pycellbase/__init__.py', 'r')
    for line in init_fhand:
        if line.startswith('__version__'):
            exec(line.strip())
            break
    init_fhand.close()
    return version


# Getting long description
def get_long_description():
    with open('README.rst') as f:
        long_desc = f.read()
    return long_desc


setup_kwargs = {
    'name': 'pycellbase',
    'version': get_version(),
    'description': 'Python client for CellBase',
    'long_description': get_long_description(),
    'author': 'Daniel Perez-Gil',
    'author_email': 'daniel.perez@incliva.es',
    'url': 'https://github.com/opencb/cellbase',
    'packages': ['pycellbase'],
    'package_dir': {'pycellbase': 'pycellbase'},
    'install_requires': ['pyyaml']
}

setup(**setup_kwargs)
