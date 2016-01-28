#!/usr/bin/python

__author__ = 'fjlopez'


import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))) # Adds pycellbase root dir to the PYTHONPATH

import argparse
from lib import CellBaseCLIExecutor

def main():

    parser = argparse.ArgumentParser(description='CellBase client 1.0')
    parser.add_argument('--type', type=str, required=True,
                        help='String indicating the type of data to be queried', metavar='T', dest='type')
    parser.add_argument('--method', type=str, required=True,
                        help='String indicating the method to be queried', metavar='M', dest='method')
    parser.add_argument('--id', nargs='+', type=str, required=False,
                        help='String indicating the id(s) to be queried (if needed)', metavar='I', dest='id')
    parser.add_argument('--species', default="hsapiens", type=str, required=False,
                        help='String indicating the species to query', metavar='S', dest='species')
    parser.add_argument('--options', nargs='+', type=str, required=False,
                        help='String with a list of &-separated filtering options. For example: source=clinvar&skip=10&limit=200', metavar='O', dest='options')
    parser.add_argument('--conf', default=None, type=str,
                        required=False,
                        help='Path to a .json file containing CellBase client configuration (if needed)', metavar='I',
                        dest='conf')

    args = parser.parse_args()

    cellbaseCLIExecutor = CellBaseCLIExecutor.CellBaseCLIExecutor(args)
    if(cellbaseCLIExecutor!=None):
        cellbaseCLIExecutor.run()

if __name__=='__main__':
    main()

