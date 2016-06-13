#!/usr/bin/env python3

from distutils.core import setup

setup(name='pycellbase',
      version='0.1',
      description='A Python client for CellBase data',
      author='Javier Lopez',
      author_email='javier.lopez@genomicsengland.co.uk',
      url='https://github.com/opencb/cellbase',
      packages=['pycellbase'],
      package_dir={'pycellbase': 'lib'}
      )
