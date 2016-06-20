from distutils.core import setup


# Getting client version
def get_version():
    version = 'Undefined'
    for line in open('pycellbase/__init__.py'):
        if line.startswith('__version__'):
            exec(line.strip())
            break
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
    'package_dir': {'pycellbase': 'pycellbase'}
    # 'install_requires': []
}

setup(**setup_kwargs)
