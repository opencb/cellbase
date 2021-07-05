import sys
import subprocess

from pycellbase.commons import get, deprecated, wrapper


class _ParentRestClient(object):
    """Queries the REST service given the different query params"""
    def __init__(self, session, configuration, subcategory, category):
        self._session = session
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category
        retries = 5

        # decorates the REST verbs with retries
        self._get = wrapper(self._get, retries)
        self._post = wrapper(self._post, retries)
        
    def _get(self, resource, query_id=None, options=None):
        """Queries the REST service and returns the result"""
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=self._subcategory,
                       query_id=query_id,
                       resource=resource,
                       options=options)
        return response

    def _post(self, resource, query_id=None, options=None, data=None):
        """Queries the REST service and returns the result"""
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=self._subcategory,
                       query_id=query_id,
                       resource=resource,
                       options=options,
                       method='post',
                       data=data)
        return response

    def get_help(self, method_name=None, show_params=False, **options):

        # Getting swagger documentation
        response = get(session=self._session,
                       host=self._configuration.host,
                       version='swagger.json',
                       species=None,
                       category=None,
                       subcategory=None,
                       resource=None,
                       options=options)
        sys.stdout.write(self.__class__.__name__ + '\n')

        # Getting class methods
        methods = [
            func for func in dir(self)
            if callable(getattr(self, func)) and not func.startswith('_')
        ]

        text = '{indent}{bullet} {name}: {desc}\n'
        for method in methods:
            if method_name is not None and method_name != method:
                continue
            # Getting method info
            method_swag = (method[4:] if method.startswith('get_') else method)
            path_info = [response['paths'][path] for path in response['paths']
                         if self._category in path and self._subcategory in path
                         and method_swag in path]
            if not path_info:
                continue
            if 'summary' not in path_info[0]['get']:
                continue
            desc = path_info[0]['get']['summary']
            desc = desc.replace('http://bioinfo.hpc.cam.ac.uk/cellbase',
                                self._configuration.host)
            sys.stdout.write(text.format(indent=' ' * 4,
                                         bullet='-',
                                         name=method,
                                         desc=desc))
            # Getting method parameters
            if not show_params:
                continue
            for param in path_info[0]['get']['parameters']:
                if param['name'] in ['version', 'species', 'Output format']:
                    continue
                desc = param['description'] if 'description' in param else '-'
                desc = desc.replace('http://bioinfo.hpc.cam.ac.uk/cellbase',
                                    self._configuration.host)
                sys.stdout.write(text.format(indent=' ' * 8,
                                             bullet='*',
                                             name=param['name'],
                                             desc=desc))


class _Feature(_ParentRestClient):
    """Queries the RESTful service for feature data"""
    def __init__(self, session, configuration, subcategory):
        _category = 'feature'
        super(_Feature, self).__init__(session, configuration, subcategory,
                                       _category)

    def get_model(self):
        """Returns the model for a specific element"""
        return self._get('model')


class _Genomic(_ParentRestClient):
    """Queries the RESTful service for genomic data"""
    def __init__(self, session, configuration, subcategory):
        _category = 'genomic'
        super(_Genomic, self).__init__(session, configuration, subcategory,
                                       _category)

    def get_model(self):
        """Returns the model for a specific element"""
        return self._get('model')


class GeneClient(_Feature):
    """Queries the RESTful service for gene data"""
    def __init__(self, session, configuration):
        _subcategory = 'gene'
        super(GeneClient, self).__init__(session, configuration, _subcategory)

    def count(self):
        """Returns the number of entries"""
        return self._get('count')

    @deprecated
    def get_biotypes(self, **options):
        return self.get_biotype(**options)

    def get_biotype(self, **options):
        """Returns the different gene biotypes"""
        return self._get('biotype', None, options)

    def get_first(self):
        """Returns the first result"""
        return self._get('first')

    def get_group(self, **options):
        """Returns the different gene biotypes"""
        return self._get('group', None, options)

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    @deprecated
    def get_clinical(self, query_id, **options):
        """Returns clinical data of the gene"""
        return self._get('clinical', query_id, options)

    def get_list(self, **options):
        """Returns all gene Ensembl IDs"""
        return self._get('list', None, options)

    def get_ppi(self, query_id, **options):
        """Returns the protein-protein interactions for this gene"""
        return self._get('ppi', query_id, options)

    def get_protein(self, query_id, **options):
        """Returns the proteins codified by the gene"""
        return self._get('protein', query_id, options)

    def get_snp(self, query_id, **options):
        """Returns the SNPs present in the gene"""
        return self._get('snp', query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the transcription factor binding sites (TFBSs) of the gene"""
        return self._get('tfbs', query_id, options)

    def get_transcript(self, query_id, **options):
        """Returns the transcripts codified by the gene"""
        return self._get('transcript', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class ProteinClient(_Feature):
    """Queries the RESTful service for protein data"""
    def __init__(self, session, configuration):
        _subcategory = 'protein'
        super(ProteinClient, self).__init__(session, configuration,
                                            _subcategory)

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    def get_sequence(self, query_id, **options):
        """Returns the protein sequence"""
        return self._get('sequence', query_id, options)

    def get_substitution_scores(self, query_id, **options):
        """Returns the protein substitution scores for its aminoacids"""
        return self._get('substitution_scores', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class TranscriptClient(_Feature):
    """Queries the RESTful service for transcript data"""
    def __init__(self, session, configuration):
        _subcategory = 'transcript'
        super(TranscriptClient, self).__init__(session, configuration,
                                               _subcategory)

    def count(self):
        """Returns the number of entries"""
        return self._get('count')

    def get_first(self):
        """Returns the first result"""
        return self._get('first')

    def get_function_prediction(self, query_id, **options):
        """Returns the function predictions of the transcript"""
        return self._get('function_prediction', query_id, options)

    def get_gene(self, query_id, **options):
        """Returns the genes which codify the transcript"""
        return self._get('gene', query_id, options)

    def get_protein(self, query_id, **options):
        """Returns the proteins codified by the transcript"""
        return self._get('protein', query_id, options)

    def get_sequence(self, query_id, **options):
        """Returns the transcript sequence"""
        return self._get('sequence', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class VariationClient(_Feature):
    """Queries the RESTful service for variation data"""
    def __init__(self, session, configuration):
        _subcategory = 'variation'
        super(VariationClient, self).__init__(session, configuration,
                                              _subcategory)

    def count(self):
        """Returns the number of entries"""
        return self._get('count')

    def get_consequence_type(self, query_id, **options):
        """Returns the different consequences for a variation"""
        return self._get('consequence_type', query_id, options)

    def get_consequence_types(self, **options):
        """Returns a variation consequence list"""
        return self._get('consequence_types', None, options)

    def get_first(self):
        """Returns the first result"""
        return self._get('first')

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class XrefClient(_Feature):
    """Queries the RESTful service for xref data"""
    def __init__(self, session, configuration):
        _subcategory = 'id'
        super(XrefClient, self).__init__(session, configuration, _subcategory)

    def contains(self, query_id, **options):
        """Returns gene HGNC symbols containing the given ID"""
        return self._get('contains', query_id, options)

    def get_dbnames(self, **options):
        """Returns a list of databases from which xref ids were collected"""
        return self._get('dbnames', None, options)

    def get_gene(self, query_id, **options):
        """Returns the genes for the given IDs"""
        return self._get('gene', query_id, options)

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    def get_xref(self, query_id, **options):
        """Returns all the external references related with the given ID"""
        return self._get('xref', query_id, options)

    def starts_with(self, query_id, **options):
        """Returns gene HGNC symbols starting with the given ID"""
        return self._get('starts_with', query_id, options)


class RegionClient(_Genomic):
    """Queries the RESTful service for genomic region data"""
    def __init__(self, session, configuration):
        _subcategory = 'region'
        super(RegionClient, self).__init__(session, configuration, _subcategory)

    def get_clinical(self, query_id, **options):
        """Returns clinical data of the genomic region"""
        return self._get('clinical', query_id, options)

    def get_conservation(self, query_id, **options):
        """Returns conservation data of the genomic region"""
        return self._get('conservation', query_id, options)

    def get_gene(self, query_id, **options):
        """Returns the genes present in the genomic region"""
        return self._get('gene', query_id, options)

    def get_regulatory(self, query_id, **options):
        """Returns the regulatory element present in the genomic region"""
        return self._get('regulatory', query_id, options)

    def get_repeat(self, query_id, **options):
        """Returns repeats present in the genomic region"""
        return self._get('repeat', query_id, options)

    def get_sequence(self, query_id, **options):
        """Returns the genomic region sequence"""
        return self._get('sequence', query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the transcription factor binding sites (TFBSs) present in the
         genomic region"""
        return self._get('tfbs', query_id, options)

    def get_transcript(self, query_id, **options):
        """Returns the transcripts present in the genomic region"""
        return self._get('transcript', query_id, options)

    def get_variation(self, query_id, **options):
        """Returns the variations present in the genomic region"""
        return self._get('variation', query_id, options)


class VariantClient(_Genomic):
    """Queries the RESTful service for variant data"""
    def __init__(self, session, configuration):
        _subcategory = 'variant'
        super(VariantClient, self).__init__(session, configuration,
                                            _subcategory)

    def get_annotation(self, query_id, method='get', **options):
        """Returns annotation data of the variant"""
        if method == 'get':
            return self._get('annotation', query_id, options)
        else:
            return self._post('annotation', None, data=query_id,
                              options=options)

    def get_cadd(self, query_id, **options):
        """Returns cadd score of the variant"""
        return self._get('cadd', query_id, options)


class GenomeSequenceClient(_Genomic):
    """Queries the RESTful service for genomic sequence data"""
    def __init__(self, session, configuration):
        _subcategory = 'chromosome'
        super(GenomeSequenceClient, self).__init__(session, configuration,
                                                   _subcategory)

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    def get_list(self, **options):
        """Returns chromosomes names"""
        return self._get('list', None, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class ClinicalClient(_ParentRestClient):
    """Queries the RESTful service for clinical data"""
    def __init__(self, session, configuration):
        _category = 'clinical'
        _subcategory = 'variant'
        super(ClinicalClient, self).__init__(session, configuration,
                                             _subcategory, _category)

    def get_allele_origin_labels(self, **options):
        """Returns all available allele origin labels"""
        return self._get('allele_origin_labels', None, options)

    def get_clinsig_labels(self, **options):
        """Returns all available clinical significance labels"""
        return self._get('clinsig_labels', None, options)

    def get_consistency_labels(self, **options):
        """Returns all available review consistency labels"""
        return self._get('consistency_labels', None, options)

    def get_mode_inheritance_labels(self, **options):
        """Returns all available mode of inheritance labels"""
        return self._get('mode_inheritance_labels', None, options)

    def get_type(self, **options):
        """Returns all available variant types"""
        return self._get('type', None, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class TFBSClient(_ParentRestClient):
    """Queries the RESTful service for TFBS data"""
    def __init__(self, session, configuration):
        _category = 'regulation'
        _subcategory = 'tf'
        super(TFBSClient, self).__init__(session, configuration, _subcategory,
                                         _category)

    def get_feature_class(self, **options):
        """Returns list of available regulatory feature classes"""
        return self._get('featureClass', None, options)

    def get_feature_type(self, **options):
        """Returns list of available regulatory feature types"""
        return self._get('featureType', None, options)

    def get_gene(self, query_id, **options):
        """Returns the genes for the TFBS"""
        return self._get('gene', query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the desired TFBS"""
        return self._get('tfbs', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class RegulationClient:
    """Queries the RESTful service for regulation data"""
    def __init__(self, session, configuration):
        self._session = session
        self._configuration = configuration
        self._category = 'regulatory'

    def get_feature_class(self, **options):
        """Returns list of available regulatory feature classes"""
        # This particular REST endpoint follows the structure
        # /{version}/{species}/regulatory/featureClass
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=None,
                       resource='featureClass',
                       options=options)
        return response

    def get_feature_type(self, **options):
        """Returns list of available regulatory feature types"""
        # This particular REST endpoint follows the structure
        # /{version}/{species}/regulatory/featureType
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=None,
                       resource='featureType',
                       options=options)
        return response

    def search(self, **options):
        """Returns regulatory elements"""
        # This particular REST endpoint follows the structure
        # /{version}/{species}/regulatory/search
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=None,
                       resource='search',
                       options=options)
        return response


class SpeciesClient:
    """Queries the RESTful service for species data"""
    def __init__(self, session, configuration):
        self._session = session
        self._configuration = configuration

    def get_info(self, **options):
        """Returns general information"""
        # This particular REST endpoint follows the structure
        # /{version}/{species}/info
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=None,
                       subcategory=None,
                       resource='info',
                       options=options)
        return response


class MetaClient:
    """Queries the RESTful service for metadata"""
    def __init__(self, session, configuration):
        self._session = session
        self._configuration = configuration
        self._category = 'meta'

    def about(self, **options):
        # This particular REST endpoint follows the structure
        # /{version}/meta/about
        """Returns info about CellBase code"""
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=None,
                       category=self._category,
                       subcategory=None,
                       resource='about',
                       options=options)
        return response

    def get_category(self, query_id, **options):
        # This particular REST endpoint follows the structure
        # /{version}/meta/{category}
        """Returns source version metadata, including source urls"""
        categories = ['feature', 'genomic', 'network', 'regulatory']
        if query_id not in categories:
            msg = 'Query has to be one of the following: ' + str(categories)
            raise ValueError(msg)

        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=None,
                       category=self._category,
                       subcategory=None,
                       resource=query_id,
                       options=options)
        return response

    def get_species(self, **options):
        """Returns available species"""
        # This particular REST endpoint follows the structure
        # /{version}/meta/species
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=None,
                       category=self._category,
                       subcategory=None,
                       resource='species',
                       options=options)
        return response

    def get_versions(self, **options):
        # This particular REST endpoint follows the structure
        # /{version}/meta/{species}/versions
        """Returns source version metadata, including source urls"""
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=None,
                       category=self._category,
                       subcategory=self._configuration.species,
                       resource='versions',
                       options=options)
        return response

    def ping(self, **options):
        """Checks if service is alive"""
        # This particular REST endpoint follows the structure
        # /{version}/meta/ping
        response = get(session=self._session,
                       host=self._configuration.host,
                       version=self._configuration.version,
                       species=None,
                       category=self._category,
                       subcategory=None,
                       resource='ping',
                       options=options)
        return response
