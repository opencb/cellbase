from pycellbase.restclient import RestClient


class _Feature(RestClient):
    """Queries the RESTful service for feature data"""
    def __init__(self, configuration, subcategory):
        _category = "feature"
        super(_Feature, self).__init__(configuration, subcategory, _category)

    def count(self):
        """Returns the number of entries"""
        return self._get('count')

    def first(self):
        """Returns the first result"""
        return self._get('first')

    def get_info(self, query_id, **options):
        """Returns general information"""
        return self._get("info", query_id, options)

    def search(self, query_id, **options):
        """Searches for specific entries given a set of filters"""
        return self._get('search', query_id, options)


class GeneClient(_Feature):
    """Queries the RESTful service for gene data"""
    def __init__(self, configuration):
        _subcategory = "gene"
        super(GeneClient, self).__init__(configuration, _subcategory)

    def get_biotypes(self, **options):
        """Returns the different gene biotypes"""
        return self._get("biotype", None, options)

    def get_clinical(self, query_id, **options):
        """Returns clinical data of the gene"""
        return self._get("clinical", query_id, options)

    def get_protein(self, query_id, **options):
        """Returns the proteins codified by the gene"""
        return self._get("protein", query_id, options)

    def get_transcript(self, query_id, **options):
        """Returns the transcripts codified by the gene"""
        return self._get("transcript", query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the transcription factor binding sites (TFBSs) of the gene"""
        return self._get("tfbs", query_id, options)

    def get_snp(self, query_id, **options):
        """Returns the SNPs present in the gene"""
        return self._get("snp", query_id, options)


class ProteinClient(_Feature):
    """Queries the RESTful service for protein data"""
    def __init__(self, configuration):
        _subcategory = "protein"
        super(ProteinClient, self).__init__(configuration, _subcategory)

    def get_sequence(self, query_id, **options):
        """Returns the protein sequence"""
        return self._get("sequence", query_id, options)

    def get_substitution_scores(self, query_id, **options):
        """Returns the protein substitution scores for its aminoacids"""
        return self._get("substitution_scores", query_id, options)


class TranscriptClient(_Feature):
    """Queries the RESTful service for transcript data"""
    def __init__(self, configuration):
        _subcategory = "transcript"
        super(TranscriptClient, self).__init__(configuration, _subcategory)

    def get_function_prediction(self, query_id, **options):
        return self._get("function_prediction", query_id, options)

    def get_gene(self, query_id, **options):
        """Returns the genes which codify the transcript"""
        return self._get("gene", query_id, options)

    def get_protein(self, query_id, **options):
        """Returns the proteins codified by the transcript"""
        return self._get("protein", query_id, options)

    def get_sequence(self, query_id, **options):
        """Returns the transcript sequence"""
        return self._get("sequence", query_id, options)


class VariationClient(_Feature):
    """Queries the RESTful service for variation data"""
    def __init__(self, configuration):
        _subcategory = "variation"
        super(VariationClient, self).__init__(configuration, _subcategory)


class GenomicRegionClient(RestClient):
    """Queries the RESTful service for genomic region data"""
    def __init__(self, configuration):
        _category = "genomic"
        _subcategory = "region"
        super(GenomicRegionClient, self).__init__(configuration, _subcategory,
                                                  _category)

    def get_clinical(self, query_id, **options):
        """Returns clinical data of the genomic region"""
        return self._get("clinical", query_id, options)

    def get_conservation(self, query_id, **options):
        """Returns conservation data of the genomic region"""
        return self._get("conservation", query_id, options)

    def get_gene(self, query_id, **options):
        """Returns the genes present in the genomic region"""
        return self._get("gene", query_id, options)

    def get_regulatory(self, query_id, **options):
        """Returns the regulatory element present in the genomic region"""
        return self._get("regulatory", query_id, options)

    def get_sequence(self, query_id, **options):
        """Returns the genomic region sequence"""
        return self._get("sequence", query_id, options)

    def get_tfbs(self, query_id, **options):
        """Returns the transcription factor binding sites (TFBSs) present in the
         genomic region"""
        return self._get("tfbs", query_id, options)

    def get_transcript(self, query_id, **options):
        """Returns the transcripts present in the genomic region"""
        return self._get("transcript", query_id, options)

    def get_variation(self, query_id, **options):
        """Returns the variations present in the genomic region"""
        return self._get("variation", query_id, options)
