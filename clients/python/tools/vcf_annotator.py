#!/usr/bin/env python

import sys
import argparse
import logging
import gzip
from pycellbase.cbclient import ConfigClient
from pycellbase.cbclient import CellBaseClient

_DEFAULT_HOST = 'bioinfo.hpc.cam.ac.uk:80/cellbase'
_DEFAULT_API_VERSION = 'v4'
_DEFAULT_SPECIES = 'hsapiens'
_DEFAULT_ASSEMBLY = 'GRCh38'
_CANONICAL_CHROMOSOMES = map(str, range(1, 23)) + ['X', 'Y', 'MT']
_INFO_TEMPLATE = ('##INFO=<ID={id},Number=.,Type=String,Description="{desc}'
                  ' from CellBase. Format: {fields}">')
_ANNOT = {
    'consequences': {
        'id': 'CBCT',
        'cellbase_key': 'consequenceTypes',
        'desc': 'consequences annotations',
        'parseable': ['geneName', 'ensemblGeneId', 'ensemblTranscriptId',
                      'strand', 'biotype'],
        'non-parseable': ['transcriptAnnotationFlags', 'so_accesion:so_name']
    },
    'clinvar': {
        'id': 'CBCV',
        'cellbase_key': None,
        'desc': 'ClinVar data',
        'parseable': ['accession', 'clinicalSignificance', 'reviewStatus'],
        'non-parseable': ['traits', 'geneNames']
    },
    'cosmic': {
        'id': 'CBCO',
        'cellbase_key': None,
        'desc': 'COSMIC data',
        'parseable': ['mutationId', 'primarySite', 'siteSubtype',
                      'primaryHistology', 'histologySubtype', 'tumourOrigin',
                      'geneName', 'mutationSomaticStatus'],
        'non-parseable': []
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
        'parseable': ['source', 'geneName', 'drugName', 'studyType'],
        'non-parseable': []
    },
    'conservation_scores': {
        'id': 'CBCS',
        'cellbase_key': 'conservation',
        'desc': 'conservation_scores scores',
        'parseable': ['source', 'score'],
        'non-parseable': ['traits', 'geneNames']
    },
    'population_frequencies': {
        'id': 'CBPF',
        'cellbase_key': '',
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


def _parse_arguments():
    """Parse arguments"""

    desc = 'This tool annotates VCF files'
    parser = argparse.ArgumentParser(
        description=desc,
        formatter_class=argparse.RawTextHelpFormatter
    )

    parser.add_argument(
        dest='input_fpath', help='input file path'
    )
    parser.add_argument(
        '-o', '--output', dest='output_fpath', default=sys.stdout,
        help='output file path (default: stdout)'
    )

    inex_group = parser.add_mutually_exclusive_group()
    inex_group.add_argument(
        '--include', dest='include',
        help=('comma-separated list of annotation features to include.'
              ' Choices: ' + str(_ANNOT.keys())),
    )
    inex_group.add_argument(
        '--exclude', dest='exclude',
        help=('comma-separated list of annotation features to exclude.'
              ' Choices: ' + str(_ANNOT.keys())),
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

    annotation = []

    # Getting annotation from easily parseable sources
    for annot in _ANNOT:
        if _ANNOT[annot]['non-parseable']:
            continue
        if _ANNOT[annot]['cellbase_key'] is None:
            continue
        if annot not in include:
            continue
        key = _ANNOT[annot]['cellbase_key']
        if key in result and result[key]:
            annotation.append(_ANNOT[annot]['id'] + '=' +
                              parse_list_of_dicts(result, key,
                                                  _ANNOT[annot]['parseable']))

    # Consequences annotation
    if 'consequences' in include:
        if 'consequencesTypes' in result and result['consequencesTypes']:
            conseqs = []
            for ct in result['consequencesTypes']:
                conseq = []
                for field in _ANNOT['consequences']['parseable']:
                    conseq.append(str(ct[field]) if field in ct else '')

                if 'transcriptAnnotationFlags' in ct:
                    conseq.append('&'.join(ct['transcriptAnnotationFlags']))
                else:
                    conseq.append('')

                if 'sequenceOntologyTerms' in ct:
                    so_terms = []
                    for sot in ct['sequenceOntologyTerms']:
                        so_terms.append(sot['accession'] + ':' + sot['name'])
                    conseq.append('&'.join(so_terms))
                else:
                    conseq.append('')

                conseqs.append('|'.join(conseq))
            annotation.append(
                _ANNOT['consequences']['id'] + '=' + ','.join(conseqs)
            )

    # ClinVar
    if 'clinvar' in include:
        if ('variantTraitAssociation' in result and
                result['variantTraitAssociation']):
            vta = result['variantTraitAssociation']
            if 'clinvar' in vta and vta['clinvar']:
                vtraits = []
                for clinvar in vta['clinvar']:
                    vtrait = []
                    for field in _ANNOT['clinvar']['parseable']:
                        vtrait.append(str(clinvar[field])
                                      if field in clinvar else '')

                    if 'traits' in clinvar:
                        vtrait.append('&'.join(clinvar['traits']))
                    else:
                        vtrait.append('')

                    if 'geneNames' in clinvar:
                        vtrait.append('&'.join(clinvar['geneNames']))
                    else:
                        vtrait.append('')

                    vtraits.append('|'.join(vtrait))
                annotation.append(
                    _ANNOT['clinvar']['id'] + '=' + ','.join(vtraits)
                )

    # COSMIC
    if 'cosmic' in include:
        if ('variantTraitAssociation' in result and
                result['variantTraitAssociation']):
            vta = result['variantTraitAssociation']
            if 'cosmic' in vta and vta['cosmic']:
                annotation.append(
                    _ANNOT['cosmic']['id'] + '=' +
                    parse_list_of_dicts(vta, 'cosmic',
                                        _ANNOT['cosmic']['parseable'])
                )

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


def annotate_vcf(cellbase_client, input_fpath, output_fpath, include, assembly):
    """Annotate a VCF file"""

    # Checking output
    if output_fpath is sys.stdout:
        output_fhand = output_fpath
    else:
        output_fhand = open(output_fpath, 'w')

    # Opening file
    if input_fpath.endswith('.vcf.gz'):
        input_fhand = gzip.open(input_fpath, 'r')
    elif input_fpath.endswith('.vcf'):
        input_fhand = open(input_fpath, 'r')
    else:
        raise IOError('Input file must end in ".vcf" or ."vcf.gz"')

    # Initializing variant client
    vc = cellbase_client.get_variant_client()

    vcf_header = []
    print_header = True
    for line in input_fhand:
        line = line.rstrip()
        line_split = line.split()

        # Getting VCF header
        if line.startswith('#'):
            vcf_header.append(line)
            continue

        # Adding CellBase header
        if vcf_header and print_header:
            new_header = add_info_to_vcf_header(vcf_header, include)
            print_header = False
            for header_line in new_header:
                output_fhand.write(header_line + '\n')

        # Skipping non-canonical chromosomes
        chromosome = get_chromosome(line_split[0])
        if chromosome not in _CANONICAL_CHROMOSOMES:
            output_fhand.write(line + '\n')
            continue

        # Querying CellBase
        var = ':'.join([chromosome] + [line_split[1]] + line_split[3:5])
        response = vc.get_annotation(var, assembly=assembly)
        annotation = get_annotation(response[0]['result'][0], include)

        # Adding annotation to variant
        line_split[7] = ';'.join([line_split[7]] + annotation)
        line = '\t'.join(line_split)
        output_fhand.write(line + '\n')

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

    # Getting the list of annotations to add
    include = get_include_annots(args.include, args.exclude)

    # Annotate VCF
    annotate_vcf(cbc, args.input_fpath, args.output_fpath, include, assembly)


if __name__ == '__main__':
    sys.exit(main())
