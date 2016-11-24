from pycellbase.commons import get


class _ParentRestClient(object):
    """Queries the REST service given the different query params"""
    def __init__(self, configuration, subcategory, category):
        self._configuration = configuration
        self._subcategory = subcategory
        self._category = category

    def _get(self, resource, query_id=None, options=None):
        """Queries the REST service and returns the result"""
        response = get(host=self._configuration.host,
                       version=self._configuration.version,
                       species=self._configuration.species,
                       category=self._category,
                       subcategory=self._subcategory,
                       query_id=query_id,
                       resource=resource,
                       options=options)

        return response

    def get_help(self):
        """Returns help for a specific element"""
        return self._get('help')

    def get_model(self):
        """Returns the model for a specific element"""
        return self._get('model')


class _Feature(_ParentRestClient):
    """Queries the RESTful service for feature data"""
    def __init__(self, configuration, subcategory):
        _category = 'feature'
        super(_Feature, self).__init__(configuration, subcategory, _category)

    def count(self):
        """Returns the number of entries"""
        return self._get('count')

    def get_first(self):
        """Returns the first result"""
        return self._get('first')

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get('info', query_id, options)

    def search(self, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', None, options)


class GeneClient(_Feature):
    """Queries the RESTful service for gene data"""
    def __init__(self, configuration):
        _subcategory = 'gene'
        super(GeneClient, self).__init__(configuration, _subcategory)

    def get_biotypes(self, **options):
        """Returns the different gene biotypes"""
        return self._get('biotype', None, options)

    def get_clinical(self, query_id, **options):
        """Returns clinical data of the gene"""
        return self._get('clinical', query_id, options)

    def get_list(self, **options):
        return self._get('list', None, options)

    def get_protein(self, query_id, **options):
        """Returns the proteins codified by the gene"""
        return self._get('protein', query_id, options)

    def get_transcript(self, query_id, **options):
        """Returns the transcripts codified by the gene"""
        return self._get('transcript', query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the transcription factor binding sites (TFBSs) of the gene"""
        return self._get('tfbs', query_id, options)

    def get_snp(self, query_id, **options):
        """Returns the SNPs present in the gene"""
        return self._get('snp', query_id, options)


class ProteinClient(_Feature):
    """Queries the RESTful service for protein data"""
    def __init__(self, configuration):
        _subcategory = 'protein'
        super(ProteinClient, self).__init__(configuration, _subcategory)

    def get_sequence(self, query_id, **options):
        """Returns the protein sequence"""
        return self._get('sequence', query_id, options)

    def get_substitution_scores(self, query_id, **options):
        """Returns the protein substitution scores for its aminoacids"""
        return self._get('substitution_scores', query_id, options)


class TranscriptClient(_Feature):
    """Queries the RESTful service for transcript data"""
    def __init__(self, configuration):
        _subcategory = 'transcript'
        super(TranscriptClient, self).__init__(configuration, _subcategory)

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


class VariationClient(_Feature):
    """Queries the RESTful service for variation data"""
    def __init__(self, configuration):
        _subcategory = 'variation'
        super(VariationClient, self).__init__(configuration, _subcategory)

    def get_consequence_types(self, query_id=None, **options):
        """Returns the different gene biotypes"""
        return self._get('consequence_types', query_id, options)


class GenomicRegionClient(_ParentRestClient):
    """Queries the RESTful service for genomic region data"""
    def __init__(self, configuration):
        _category = 'genomic'
        _subcategory = 'region'
        super(GenomicRegionClient, self).__init__(configuration, _subcategory,
                                                  _category)

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


class VariantClient(_ParentRestClient):
    """Queries the RESTful service for genomic region data"""
    def __init__(self, configuration):
        _category = 'genomic'
        _subcategory = 'variant'
        super(VariantClient, self).__init__(configuration, _subcategory,
                                            _category)

    def get_annotation(self, query_id, **options):
        """Returns annotation data of the variant"""
        return self._get('annotation', query_id, options)

    def get_cadd(self, query_id, **options):
        """Returns cadd score of the variant"""
        return self._get('cadd', query_id, options)
