#!/usr/bin/env python

import os
import sys
import argparse
import logging
import gzip
from pycellbase.cbclient import ConfigClient
from pycellbase.cbclient import CellBaseClient

_DEFAULT_HOST = 'https://ws.zettagenomics.com/cellbase'
_DEFAULT_API_VERSION = 'v5'
_DEFAULT_SPECIES = 'hsapiens'
_DEFAULT_ASSEMBLY = 'GRCh38'
# Reference sequence notation
# http://www.hgvs.org/mutnomen/standards.html
_HGVS_REF_SEQ_LETTER = {
    'genomic': 'g.', 'cdna': 'c.', 'rna': 'r.', 'protein': 'p.',
    'mitochondrial': 'm.', 'noncoding': 'n.'
}
_CANONICAL_CHROMOSOMES = list(map(str, range(1, 23))) + ['X', 'Y', 'MT']
_INFO_TEMPLATE = ('##INFO=<ID={id},Number=.,Type=String,Description="{desc}'
                  ' from CellBase. Format: {fields}">')
_ANNOT = {
    'consequences': {
        'id': 'CBCT',
        'cellbase_key': 'consequenceTypes',
        'desc': 'Consequence types',
        'parseable': ['geneId', 'transcriptId', 'strand', 'biotype'],
        'non-parseable': ['transcriptFlags', 'so_accesion:so_name']
    },
    'clinvar': {
        'id': 'CBCV',
        'cellbase_key': None,
        'desc': 'ClinVar data',
        'parseable': ['id'],
        'non-parseable': ['alleleOrigin', 'additionalProperties', 'heritableTraits', 'xrefs']
    },
    'cosmic': {
        'id': 'CBCO',
        'cellbase_key': None,
        'desc': 'COSMIC data',
        'parseable': ['id'],
        'non-parseable': ['alleleOrigin', 'additionalProperties', 'somaticInformation',
                          'heritableTraits', 'xrefs']
    },
    'gene_traits': {
        'id': 'CBGT',
        'cellbase_key': 'geneTraitAssociation',
        'desc': 'Gene trait associations',
        'parseable': ['source', 'id', 'hpo', 'name', 'score',
                      'numberOfPubmeds'],
        'non-parseable': []
    },
    'gene_drugs': {
        'id': 'CBGD',
        'cellbase_key': 'geneDrugInteraction',
        'desc': 'Gene drug interactions',
        'parseable': ['source', 'geneName', 'drugName', 'chemblId'],
        'non-parseable': []
    },
    'conservation_scores': {
        'id': 'CBCS',
        'cellbase_key': 'conservation',
        'desc': 'Conservation scores',
        'parseable': ['source', 'score'],
        'non-parseable': []
    },
    'population_frequencies': {
        'id': 'CBPF',
        'cellbase_key': 'populationFrequencies',
        'desc': 'Population frequencies',
        'parseable': ['study', 'population', 'refAllele', 'altAllele',
                      'refAlleleFreq', 'altAlleleFreq', 'refHomGenotypeFreq',
                      'hetGenotypeFreq', 'altHomGenotypeFreq'],
        'non-parseable': []
    },
    'gene_expression': {
        'id': 'CBGE',
        'cellbase_key': 'geneExpression',
        'desc': 'Gene expression',
        'parseable': ['geneName', 'experimentalFactor', 'factorValue',
                      'experimentId', 'technologyPlatform', 'expression',
                      'pvalue'],
        'non-parseable': []
    },
    'functional_scores': {
        'id': 'CBFS',
        'cellbase_key': 'functionalScore',
        'desc': 'Functional scores',
        'parseable': ['source', 'score'],
        'non-parseable': []
    },
    'repeats': {
        'id': 'CBRE',
        'cellbase_key': 'repeat',
        'desc': 'Repeats',
        'parseable': ['source', 'id', 'chromosome', 'start', 'end',
                      'copyNumber', 'percentageMatch', 'consensusSize',
                      'period', 'score', 'sequence'],
        'non-parseable': []
    },
    'cytobands': {
        'id': 'CBCB',
        'cellbase_key': 'cytoband',
        'desc': 'Cytobands',
        'parseable': ['chromosome', 'start', 'end', 'stain', 'name'],
        'non-parseable': []
    },
}


def _parse_xref(subparser):
    """Parse xref tool arguments"""
    xref_parser = subparser.add_parser(
        'xref',
        description='Returns all available IDs in CellBase for any given ID',
        formatter_class=argparse.RawTextHelpFormatter
    )
    xref_parser.set_defaults(which='xref')

    _parse_config(xref_parser)

    xref_parser.add_argument(
        'input',
        help='input file path or comma-separated string\n(e.g. "BRCA1")'
    )
    xref_parser.add_argument(
        '-o', '--output', dest='output_fpath', default=sys.stdout,
        help='output file path (default: stdout)'
    )


def _parse_hgvs(subparser):
    """Parse hgvs tool arguments"""
    hgvs_parser = subparser.add_parser(
        'hgvs',
        description='Returns variant HGVS notation',
        formatter_class=argparse.RawTextHelpFormatter
    )
    hgvs_parser.set_defaults(which='hgvs')

    _parse_config(hgvs_parser)

    hgvs_parser.add_argument(
        'input',
        help=('input file path or comma-separated string'
              '\n(e.g. "19:45411941:T:C")')
    )
    hgvs_parser.add_argument(
        '-o', '--output', dest='output_fpath', default=sys.stdout,
        help='output file path (default: stdout)'
    )
    hgvs_parser.add_argument(
        '-r', '--ref_seq_type', dest='ref_seq_type',
        choices=_HGVS_REF_SEQ_LETTER.keys(),
        help='reference sequence type'
    )


def _parse_annotate(subparser):
    """Parse annotate tool arguments"""
    annotate_parser = subparser.add_parser(
        'annotate',
        description='Annotates VCF files',
        formatter_class=argparse.RawTextHelpFormatter
    )
    annotate_parser.set_defaults(which='annotate')

    _parse_config(annotate_parser)

    annotate_parser.add_argument(
        'input_fpath', help='VCF file path'
    )
    annotate_parser.add_argument(
        '-o', '--output', dest='output_fpath', default=sys.stdout,
        help='output file path (default: stdout)'
    )

    inex_group = annotate_parser.add_mutually_exclusive_group()
    inex_group.add_argument(
        '--include', dest='include',
        help=('comma-separated list of annotation features to include.'
              '\nChoices: {' + ','.join(_ANNOT.keys()) + '}'),
    )
    inex_group.add_argument(
        '--exclude', dest='exclude',
        help=('comma-separated list of annotation features to exclude.'
              '\nChoices: {' + ','.join(_ANNOT.keys()) + '}'),
    )


def _parse_config(parser):
    """Parse config arguments"""
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


def _parse_arguments():
    """Parse arguments"""

    desc = 'This script provides different PyCellBase bioinformatics tools'
    parser = argparse.ArgumentParser(
        description=desc,
        formatter_class=argparse.RawTextHelpFormatter
    )
    subparsers = parser.add_subparsers(help='available tools')

    # XREF conversor
    _parse_xref(subparsers)

    # HGVS calculator
    _parse_hgvs(subparsers)

    # VCF annotator
    _parse_annotate(subparsers)

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

    return args


def _set_logger(verbosity=False):
    """Set logging system"""

    if verbosity:
        logging.basicConfig(format="[%(levelname)s] %(message)s",
                            level=logging.DEBUG)
    else:
        logging.basicConfig(format="[%(levelname)s] %(message)s",
                            level=logging.WARNING)


def _get_species_list(cbc, assembly):
    """Return all available species in CellBase"""
    mc = cbc.get_meta_client()
    res = mc.get_species(assembly=assembly)[0]['result'][0]
    sps = [species['id'] for kingdom in res for species in res[kingdom]]
    return sorted(sps)


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


def convert_ids(input_data, output_fpath, cellbase_client, assembly):
    """Prints all IDs for a given a file with one ID per line"""

    # Getting xref client
    xc = cellbase_client.get_xref_client()

    # Checking output
    if output_fpath is sys.stdout:
        output_fhand = output_fpath
    else:
        output_fhand = open(output_fpath, 'w')

    # Creating input generator
    input_fhand = None
    if os.path.isfile(input_data):
        input_fhand = open(input_data, 'r')
        input_gen = ([line.rstrip()] for line in input_fhand)
    else:
        input_data = input_data.split(',')
        input_gen = [i for i in input_data]

    # Querying CellBase and writing results
    dbs = []
    all_ids = []
    for query in input_gen:
        response = xc.get_info(query, assembly=assembly, debug=True)
        for query_response in response:
            ids = {}
            id_list = []

            # Skipping if ID is not found in CellBase
            if 'result' in query_response:
                query_response['results'] = query_response.pop('result')
            if not query_response['results']:
                msg = 'ID not found in CellBase: "' + str(query) + '"'
                logging.warning(msg)
                continue

            # Getting list of all IDs
            for item in query_response['results']:
                if item['dbName'] not in dbs:
                    dbs.append(item['dbName'])
                ids.setdefault(item['dbName'], []).append(item['id'])
            all_ids.append({query_response['id']: ids})

    # Writing output
    if all_ids:
        # Writing header
        output_fhand.write('\t'.join(['QUERY'] + [db for db in dbs]) + '\n')
        for ids in all_ids:
            line = []
            for query in ids:
                line.append(query)
                line += ['|'.join(list(set(ids[query][db])))
                         if db in ids[query] else '.'
                         for db in dbs]
            print('\t'.join(line))

    output_fhand.close()


def _get_hgvs(query, cellbase_client, ref_seq_type, assembly):

    # Setting up CellBase Variant client
    vc = cellbase_client.get_variant_client()

    response = vc.get_annotation(query, include='hgvs', assembly=assembly)

    for i, query_response in enumerate(response):
        hgvs_list = []

        # Getting list of all hgvs notations
        if 'result' in query_response:
            query_response['results'] = query_response.pop('result')
        for hgvs in query_response['results'][0]['hgvs']:
            hgvs_list.append(hgvs)

        # Setting up list to filter hgvs notations
        filtering = [1]*len(hgvs_list)

        # Checking reference sequence type
        if ref_seq_type is not None:
            for j, hgvs in enumerate(hgvs_list):
                notation = hgvs.split(':')[1]
                if _HGVS_REF_SEQ_LETTER[ref_seq_type] not in notation:
                    filtering[j] = 0

        yield (query.split(',')[i],
               [hgvs for i, hgvs in enumerate(hgvs_list) if filtering[i]])


def calculate_hgvs(input_data, output_fpath, cbc, ref_seq_type, assembly):

    # Checking output
    if output_fpath is sys.stdout:
        output_fhand = output_fpath
    else:
        output_fhand = open(output_fpath, 'w')

    # Creating input generator
    number_of_items = 100
    input_fhand = None
    if os.path.isfile(input_data):
        input_fhand = open(input_data, 'r')
        input_gen = ([line.rstrip()] for line in input_fhand)
    else:
        input_data = input_data.split(',')
        input_gen = [input_data[i:i+number_of_items]
                     for i in range(0, len(input_data), number_of_items)]

    for query in input_gen:
        query = ','.join(query)
        for variant, hgvs in _get_hgvs(query, cbc, ref_seq_type, assembly):
            if not hgvs:
                hgvs = ['.']
            output_fhand.write('\t'.join([variant, ';'.join(hgvs)]) + '\n')

    output_fhand.close()
    if input_fhand is not None:
        input_fhand.close()


def get_chromosome(chrom):
    """Get chromosome name"""
    if chrom == 'chrM':
        return 'MT'
    else:
        return chrom.lstrip('chr')


def parse_list_of_dicts(result, feature, fields):
    """Parse list of dictionaries"""
    items = []
    if feature in result and result[feature]:
        for _dict in result[feature]:
            item = []
            for field in fields:
                item.append(str(_dict[field]) if field in _dict else '')
            items.append('|'.join(item))
        return ','.join(items)


def get_annotation(result, include):
    """Get VCF annotation"""

    if not result:
        return []

    annotation = []

    # Getting annotation from easily parseable sources
    for annot in include:
        if _ANNOT[annot]['non-parseable'] or \
                _ANNOT[annot]['cellbase_key'] is None:
            continue
        key = _ANNOT[annot]['cellbase_key']
        if key in result and result[key]:
            annotation.append('{key}={value}'.format(
                    key=_ANNOT[annot]['id'],
                    value=parse_list_of_dicts(
                        result, key, _ANNOT[annot]['parseable']
                    )
            ))

    # Consequences annotation
    if 'consequences' in include:
        if 'consequenceTypes' in result and result['consequenceTypes']:
            conseqs = []
            for ct in result['consequenceTypes']:
                conseq = []
                for field in _ANNOT['consequences']['parseable']:
                    conseq.append(str(ct[field]) if field in ct else '')

                if 'transcriptFlags' in ct:
                    conseq.append('&'.join(ct['transcriptFlags']))
                else:
                    conseq.append('')

                if 'sequenceOntologyTerms' in ct:
                    so_terms = [sot['accession'] + ':' + sot['name']
                                for sot in ct['sequenceOntologyTerms']]
                    conseq.append('&'.join(so_terms))
                else:
                    conseq.append('')

                conseqs.append('|'.join(conseq))
            annotation.append('{key}={value}'.format(
                key=_ANNOT['consequences']['id'], value=','.join(conseqs)
            ))

    # ClinVar
    if 'clinvar' in include:
        if 'traitAssociation' in result and result['traitAssociation']:
            clinvar_traits = []
            for vta in result['traitAssociation']:
                if vta['source']['name'] == 'clinvar':
                    vtrait = []
                    for field in _ANNOT['clinvar']['parseable']:
                        vtrait.append(str(vta[field])
                                      if field in vta else '')

                    if 'alleleOrigin' in vta and vta['alleleOrigin']:
                        vtrait.append('&'.join(vta['alleleOrigin']))
                    else:
                        vtrait.append('')

                    if 'additionalProperties' in vta and vta['additionalProperties']:
                        aps = []
                        for ap in vta['additionalProperties']:
                            name = ap['name'].replace(', ', '_').replace(' ', '_')
                            value = ap['value'].replace(', ', '_').replace(' ', '_')
                            aps.append(':'.join([name, value]))
                        vtrait.append(','.join(aps))
                    else:
                        vtrait.append('')

                    if 'heritableTraits' in vta and vta['heritableTraits']:
                        hts = []
                        for ht in vta['heritableTraits']:
                            hts.append(ht['trait'].replace(', ', '_').replace(' ', '_').replace('|', '&'))
                        vtrait.append('&'.join(hts))
                    else:
                        vtrait.append('')

                    if 'genomicFeatures' in vta and vta['genomicFeatures']:
                        gfs = []
                        for gf in vta['genomicFeatures']:
                            gfs.append(gf['xrefs']['symbol'])
                        vtrait.append('&'.join(gfs))
                    else:
                        vtrait.append('')

                    clinvar_traits.append('|'.join(vtrait))

            annotation.append('{key}={value}'.format(
                key=_ANNOT['clinvar']['id'], value=','.join(clinvar_traits)
            ))

    # COSMIC
    if 'cosmic' in include:
        if 'traitAssociation' in result and result['traitAssociation']:
            vta = result['traitAssociation']
            if 'cosmic' in vta and vta['cosmic']:
                annotation.append('{key}={value}'.format(
                    key=_ANNOT['cosmic']['id'],
                    value=parse_list_of_dicts(
                        vta, 'cosmic', _ANNOT['cosmic']['parseable']
                    )
                ))

    if 'cosmic' in include:
        if 'traitAssociation' in result and result['traitAssociation']:
            cosmic_traits = []
            for vta in result['traitAssociation']:
                if vta['source']['name'] == 'cosmic':
                    vtrait = []
                    for field in _ANNOT['cosmic']['parseable']:
                        vtrait.append(str(vta[field])
                                      if field in vta else '')

                    if 'alleleOrigin' in vta and vta['alleleOrigin']:
                        vtrait.append('&'.join(vta['alleleOrigin']))
                    else:
                        vtrait.append('')

                    if 'somaticInformation' in vta and vta['somaticInformation']:
                        sis = []
                        for si in vta['somaticInformation']:
                            name = si.replace(', ', '_').replace(' ', '_')
                            value = vta['somaticInformation'][si].replace(', ', '_').replace(' ', '_')
                            sis.append(':'.join([name, value]))
                        vtrait.append(','.join(sis))
                    else:
                        vtrait.append('')

                    if 'additionalProperties' in vta and vta['additionalProperties']:
                        aps = []
                        for ap in vta['additionalProperties']:
                            name = ap['name'].replace(', ', '_').replace(' ', '_')
                            value = ap['value'].replace(', ', '_').replace(' ', '_')
                            aps.append(':'.join([name, value]))
                        vtrait.append(','.join(aps))
                    else:
                        vtrait.append('')

                    if 'heritableTraits' in vta and vta['heritableTraits']:
                        hts = []
                        for ht in vta['heritableTraits']:
                            hts.append(ht['trait'].replace(', ', '_').replace(' ', '_').replace('|', '&'))
                        vtrait.append('&'.join(hts))
                    else:
                        vtrait.append('')

                    if 'genomicFeatures' in vta and vta['genomicFeatures']:
                        gfs = []
                        for gf in vta['genomicFeatures']:
                            gfs.append(gf['xrefs']['symbol'])
                        vtrait.append('&'.join(gfs))
                    else:
                        vtrait.append('')

                    cosmic_traits.append('|'.join(vtrait))

            annotation.append('{key}={value}'.format(
                key=_ANNOT['cosmic']['id'], value=','.join(cosmic_traits)
            ))

    # Removing whitespaces
    for index, annot in enumerate(annotation):
        annotation[index] = annot.replace(' ', '_')

    return annotation


def add_info_to_vcf_header(ori_header, include):
    """Adds extra INFO from CellBase to VCF header"""

    cellbase_header = []
    for annot in _ANNOT.keys():
        if annot not in include:
            continue
        cellbase_header.append(
            _INFO_TEMPLATE.format(
                id=_ANNOT[annot]['id'],
                desc=_ANNOT[annot]['desc'],
                fields='|'.join(_ANNOT[annot]['parseable'] +
                                _ANNOT[annot]['non-parseable'])
            )
        )

    new_header = ori_header[:-1] + cellbase_header + [ori_header[-1]]
    return new_header


def _get_vcf_header(vcf_fhand, include):
    vcf_header = []
    for line in vcf_fhand:
        line = str(line.rstrip())
        if line.startswith('#'):
            vcf_header.append(line)
        else:
            break

    new_header = []
    if vcf_header:
        new_header = add_info_to_vcf_header(vcf_header, include)

    return new_header


def _get_vcf_batches(vcf_fhand):
    variants = []
    split_lines = []
    for line in vcf_fhand:
        # Skipping header
        if line.startswith('#'):
            continue

        # Return batch every 800 variants
        if len(variants) == 100:
            yield split_lines, variants
            variants = []

        # Get variants id
        line_split = line.rstrip('\n').split('\t')
        split_lines.append(line_split)
        chrom = get_chromosome(line_split[0])
        variants.append(':'.join([chrom] + [line_split[1]] + line_split[3:5]))

    yield split_lines, variants


def annotate_vcf(cellbase_client, input_fpath, output_fpath, include, assembly):
    """Annotate a VCF file"""

    # Checking output
    if output_fpath is sys.stdout:
        output_fhand = output_fpath
    else:
        output_fhand = open(output_fpath, 'w')

    # Opening file
    if input_fpath.endswith('.vcf.gz'):
        input_fhand = gzip.open(input_fpath, 'rt')
    elif input_fpath.endswith('.vcf'):
        input_fhand = open(input_fpath, 'r')
    else:
        raise IOError('Input file must end in ".vcf" or ."vcf.gz"')

    # Getting new VCF header
    new_header = _get_vcf_header(input_fhand, include)
    for header_line in new_header:
        output_fhand.write(header_line + '\n')

    # Initializing variant client
    vc = cellbase_client.get_variant_client()

    # Querying CellBase
    input_fhand.seek(0)
    for vcf_line_batch, variant_batch in _get_vcf_batches(input_fhand):
        response = vc.get_annotation(variant_batch, assembly=assembly)
        while response:
            line_split = vcf_line_batch.pop(0)
            res = response.pop(0)
            # Skipping non-canonical chromosomes
            chromosome = get_chromosome(line_split[0])
            if chromosome not in _CANONICAL_CHROMOSOMES:
                output_fhand.write('\t'.join(line_split) + '\n')
                continue
            # Getting formatted annotation
            if 'result' in res:
                res['results'] = res.pop('result')
            annotation = get_annotation(res['results'][0], include)
            # Writing
            line_split[7] = ';'.join([line_split[7]] + annotation)
            output_fhand.write('\t'.join(line_split) + '\n')

    input_fhand.close()
    output_fhand.close()


def get_include_annots(include, exclude):
    """Select annotations to include in the VCF"""
    if include:
        return include.split(',')
    elif exclude:
        return [i for i in _ANNOT.keys() if i not in exclude.split(',')]
    return _ANNOT.keys()


def main():
    """The main function"""

    # Getting args
    args = _parse_arguments()

    # Check arguments
    args = _check_arguments(args)

    # Setting up logging system
    _set_logger(args.verbosity)

    # Setting up PyCellBase clients
    cc = ConfigClient()

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
    cbc = CellBaseClient(cc)

    if args.which == 'xref':
        convert_ids(input_data=args.input,
                    output_fpath=args.output_fpath,
                    cellbase_client=cbc,
                    assembly=assembly)
    if args.which == 'hgvs':
        calculate_hgvs(input_data=args.input,
                       output_fpath=args.output_fpath,
                       cbc=cbc,
                       ref_seq_type=args.ref_seq_type,
                       assembly=assembly)
    if args.which == 'annotate':
        # Getting the list of annotations to add
        include = get_include_annots(args.include, args.exclude)
        include = list(set(include).intersection(_ANNOT.keys()))

        # Annotate VCF
        annotate_vcf(cellbase_client=cbc,
                     input_fpath=args.input_fpath,
                     output_fpath=args.output_fpath,
                     include=include,
                     assembly=assembly)


if __name__ == '__main__':
    sys.exit(main())
