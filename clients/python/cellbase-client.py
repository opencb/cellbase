#!/usr/bin/env python3

__author__ = 'fjlopez'

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))) # Adds pycellbase root dir to the PYTHONPATH

import argparse
from lib import CellBaseCLIExecutor

def main():

    parser = argparse.ArgumentParser(description='CellBase client 1.0')
    parser.add_argument('--type', type=str, required=True, help='String indicating the subcategory of the IDs to be queried', metavar='T', dest='type')
    parser.add_argument('--resource', type=str, required=True, help='String indicating the method to be queried', metavar='M', dest='method')
    parser.add_argument('--id', nargs='+', type=str, required=False, help='String indicating the id(s) to be queried (if needed)', metavar='I', dest='id')
    parser.add_argument('--species', default="hsapiens", type=str, required=False, help='String indicating the species to query', metavar='S', dest='species')
    parser.add_argument('--conf', default=None, type=str, required=False, help='Path to a .json file containing CellBase client configuration (if needed)', metavar='I', dest='conf')
    group = parser.add_mutually_exclusive_group()
    group.add_argument('--include', nargs='+', type=str, required=False, help='String indicating the field to be returned', metavar='F', dest='include')
    group.add_argument('--exclude', nargs='+', type=str, required=False, help='String indicating the field to be excluded', metavar='X', dest='exclude')

    args = parser.parse_args()

    # print(args)
    cellbaseCLIExecutor = CellBaseCLIExecutor.CellBaseCLIExecutor(args)
    if(cellbaseCLIExecutor != None):
        cellbaseCLIExecutor.run()

if __name__=='__main__':
    main()
