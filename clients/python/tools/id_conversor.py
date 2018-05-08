#!/usr/bin/env python

import sys
import argparse
import logging
from itertools import islice
from pycellbase.cbclient import ConfigClient
from pycellbase.cbclient import CellBaseClient

_DEFAULT_HOST = 'bioinfo.hpc.cam.ac.uk:80/cellbase'
_DEFAULT_API_VERSION = 'v4'
_DEFAULT_SPECIES = 'hsapiens'
_DEFAULT_ASSEMBLY = 'GRCh38'


def _parse_arguments():
    """Parse arguments"""

    desc = 'This tool returns all available IDs in CellBase for given IDs'
    parser = argparse.ArgumentParser(
        description=desc,
        formatter_class=argparse.RawTextHelpFormatter
    )

    lists_group = parser.add_mutually_exclusive_group()
    lists_group.add_argument(
        '-D', '--dbs', dest='list_databases', action='store_true',
        help='show list of available databases'
    )
    lists_group.add_argument(
        '-S', '--sps', dest='list_species', action='store_true',
        help='show list of available species'
    )

    parser.add_argument(
        '-i', dest='input_fpath', help='input file path'
    )
    parser.add_argument(
        '-o', '--output', dest='output_fpath', default=sys.stdout,
        help='output file path (default: stdout)'
    )

    inex_group = parser.add_mutually_exclusive_group()
    inex_group.add_argument(
        '--include', dest='include', help='include databases'
    )
    inex_group.add_argument(
        '--exclude', dest='exclude', help='exclude databases'
    )

    parser.add_argument(
        '-v', '--verbose', dest='verbosity', action='store_true',
        help='increase output verbosity'
    )
    parser.add_argument(
        '--assembly', dest='assembly', choices=['grch37', 'grch38'],
        help='reference genome assembly (default: ' + _DEFAULT_ASSEMBLY + ')'
    )
    parser.add_argument(
        '--species', dest='species',
        help=('species name (default:' + _DEFAULT_SPECIES +
              '). Overrides configuration provided with "--config" parameter')
    )
    parser.add_argument(
        '--host', dest='host',
        help=('web services host (default: ' + _DEFAULT_HOST +
              '). Overrides configuration provided with "--config" parameter')
    )
    parser.add_argument(
        '--version', dest='api_version',
        help=('api version (default: ' + _DEFAULT_API_VERSION +
              '). Overrides configuration provided with "--config" parameter')
    )
    parser.add_argument(
        '--config', dest='config',
        help='CellBase configuration. Overrides default values.'
    )

    args = parser.parse_args()
    return args


def _check_arguments(args):
    """Check arguments validity"""

    if args.config and args.species:
        msg = ('Overriding species parameter provided in "--config" argument'
               ' with "--species" argument')
        logging.warning(msg)

    if args.config and args.host:
        msg = ('Overriding host parameter provided in "--config" argument'
               ' with "--host" argument')
        logging.warning(msg)

    if args.config and args.api_version:
        msg = ('Overriding api_version parameter provided in "--config"'
               ' argument with "--api_version" argument')
        logging.warning(msg)

    if not args.input_fpath and not any([args.list_species,
                                         args.list_databases]):
        msg = 'Input file path is required ("-i" option)'
        raise ValueError(msg)

    return args


def _set_logger(verbosity=False):
    """Set logging system"""

    if verbosity:
        logging.basicConfig(format="[%(levelname)s] %(message)s",
                            level=logging.DEBUG)
    else:
        logging.basicConfig(format="[%(levelname)s] %(message)s",
                            level=logging.WARNING)


def _read_file_in_chunks(fhand, number_of_lines=100, remove_empty=True):
    """Read file and retrieve a specified number of lines"""

    while True:
        n_lines = map(lambda x: x.rstrip(),
                      list(islice(fhand, number_of_lines)))

        # If end of file
        if not n_lines:
            break

        # Remove empty lines
        if remove_empty:
            n_lines = list(filter(None, n_lines))
        if not n_lines:
            continue

        yield n_lines


def _get_species_list(cbc, assembly):
    """Return all available species in CellBase"""
    mc = cbc.get_meta_client()
    res = mc.get_species(assembly=assembly)[0]['result'][0]
    sps = [species['id'] for kingdom in res for species in res[kingdom]]
    return sorted(sps)


def _get_databases_list(xc, assembly):
    """Return all available databases for xrefs in CellBase"""
    databases = xc.get_dbnames(assembly=assembly)[0]['result']
    dbs = [database for database in databases]
    return sorted(dbs)


def _filter_databases(databases, include=None, exclude=None):
    """Filter a list of databases given an inclusion/exclusion list"""
    dbs = []

    if include is not None:
        for db in include:
            if db in databases:
                dbs.append(db)
            else:
                msg = ('Database not found in CellBase: "' + str(db) + '"')
                logging.warning(msg)
        databases = sorted(dbs)

    if exclude is not None:
        for database in databases:
            if database not in exclude:
                dbs.append(database)
        databases = sorted(dbs)

    if not databases:
        msg = 'No databases selected'
        logging.warning(msg)

    return databases


def convert_ids(input_fpath, output_fpath, cellbase_client, assembly,
                databases):
    """Prints all IDs for a given a file with one ID per line"""

    # Getting xref client
    xc = cellbase_client.get_xref_client()

    # Checking output
    if output_fpath is sys.stdout:
        output_fhand = output_fpath
    else:
        output_fhand = open(output_fpath, 'w')

    if not databases:
        output_fhand.close()
        return

    # Writing header
    output_fhand.write('\t'.join([db for db in databases]) + '\n')

    # Querying CellBase and writing results
    with open(input_fpath, 'r') as input_fhand:
        # TODO Num of lines=1 due to CellBase BUG: query is not split by comma
        for lines in _read_file_in_chunks(input_fhand, number_of_lines=1):
            query = ','.join(lines)
            # TODO Limit increased due to CellBase BUG: "limit" doesn't work
            response = xc.get_xref(query,
                                   assembly=assembly,
                                   limit=5000)
            for query_response in response:
                ids = {}
                id_list = []

                # Skipping if ID is not found in CellBase
                if not query_response['result']:
                    msg = 'ID not found in CellBase: "' + str(query) + '"'
                    logging.warning(msg)
                    continue

                # Getting list of all IDs
                for item in query_response['result']:
                    ids.setdefault(item['dbName'], []).append(item['id'])
                    id_list = [set(ids[db]) if db in ids else ['.']
                               for db in databases]

                # Writing output
                if id_list:
                    id_list = [';'.join(id_group) for id_group in id_list]
                    output_fhand.write('\t'.join(id_list) + '\n')

    output_fhand.close()


def main():
    """The main function"""

    # Getting args
    args = _parse_arguments()

    # Check arguments
    args = _check_arguments(args)

    # Setting up logging system
    _set_logger(args.verbosity)

    # Setting up PyCellBase clients
    cc = ConfigClient(
        {"species": _DEFAULT_SPECIES, "version": _DEFAULT_API_VERSION,
         "rest": {"hosts": [_DEFAULT_HOST]}}
    )

    # Overriding config
    if args.config is not None:
        cc = ConfigClient(args.config)
    if args.species is not None:
        cc.species = args.species
    if args.api_version is not None:
        cc.version = args.api_version
    if args.host is not None:
        cc.host = args.host
    if args.assembly is not None:
        assembly = args.assembly
    else:
        assembly = _DEFAULT_ASSEMBLY

    # Setting up pycellbase clients
    cbc = CellBaseClient(cc)
    xc = cbc.get_xref_client()

    # Printing available species and databases lists
    if args.list_species:
        for species in _get_species_list(cbc, assembly):
            print(species)
        return
    if args.list_databases:
        for database in _get_databases_list(xc, assembly):
            print(database)
        return

    # Getting available databases
    databases = _get_databases_list(xc, assembly)

    # Converting IDs
    # Filtering databases with include and exclude
    include = args.include.split(',') if args.include is not None else None
    exclude = args.exclude.split(',') if args.exclude is not None else None
    databases = _filter_databases(databases, include=include,
                                  exclude=exclude)

    # Converting IDs
    convert_ids(input_fpath=args.input_fpath,
                output_fpath=args.output_fpath,
                cellbase_client=cbc,
                assembly=assembly,
                databases=databases)


if __name__ == '__main__':
    sys.exit(main())
