from distutils.core import setup
reqs = [
    'requests',
    'pyyaml'
]

setup(
    name='pycellbase',
    version='0.4.0',
    packages=['pycellbase'],
    url='https://github.com/opencb/cellbase',
    author='Daniel Perez-Gil',
    author_email='opencb@googlegroups.com',
    description=('This Python package enables to query and obtain'
                 ' biological information from the exhaustive'
                 ' RESTful Web service API that has been implemented'
                 ' for the CellBase database'),
    install_requires=reqs
)
