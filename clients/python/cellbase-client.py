#!/usr/bin/env python3

import argparse
import CellBaseCLIExecutor

# Adds cellbase-client root dir to the PYTHONPATH
# sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

__author__ = 'fjlopez'


def main():
    parser = argparse.ArgumentParser(description='CellBase client 1.0')
    parser.add_argument('--type', type=str, required=True,
                        help='String indicating the subcategory of the IDs to be queried', metavar='T', dest='type')
    parser.add_argument('-o', '--output', type=str, help='store the results to a json file', dest="output")
    parser.add_argument('--resource', type=str, required=True, help='String indicating the method to be queried',
                        metavar='M', dest='method')
    parser.add_argument('--id', nargs='+', type=str, required=False,
                        help='String indicating the id(s) to be queried (if needed)', metavar='I', dest='id')
    parser.add_argument('--species', default="hsapiens", type=str, required=False,
                        help='String indicating the species to query', metavar='S', dest='species')
    parser.add_argument('--conf', default=None, type=str, required=False,
                        help='Path to a .json file containing CellBase client configuration (if needed)', metavar='I', dest='conf')
    group = parser.add_mutually_exclusive_group()
    group.add_argument('--include', nargs='+', type=str, required=False,
                       help='String indicating the field to be returned', metavar='F', dest='include')
    group.add_argument('--exclude', nargs='+', type=str, required=False,
                       help='String indicating the field to be excluded', metavar='X', dest='exclude')
    parser.add_argument('--limit', type=int, required=False,
                        help='Integer indicating the number of results to be returned (if neede)', dest='limit')
    parser.add_argument('--skip', type=int, required=False,
                        help='Integer indicating the number of results to skip (if needed)', dest='skip')
    args = parser.parse_args()

    cellbase_cli_executor = CellBaseCLIExecutor.CellBaseCLIExecutor(args)
    if cellbase_cli_executor is not None:
        cellbase_cli_executor.run()

if __name__ == '__main__':
    main()
