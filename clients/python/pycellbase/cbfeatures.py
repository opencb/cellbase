from pycellbase.restclient import RestClient


class _Feature(RestClient):
    def __init__(self, configuration, subcategory):
        _category = "feature"
        super(_Feature, self).__init__(configuration, subcategory, _category)

    def search(self, query_id, **options):
        return super(_Feature, self)._get('search', query_id, options)


class GeneClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "gene"
        super(GeneClient, self).__init__(configuration, _subcategory)

    def get_biotypes(self, **options):
        return super(GeneClient, self)._get("biotype", None, options)

    def get_clinical(self, query_id, **options):
        return super(GeneClient, self)._get("clinical", query_id, options)

    def get_protein(self, query_id, **options):
        return super(GeneClient, self)._get("protein", query_id, options)

    def get_transcript(self, query_id, **options):
        return super(GeneClient, self)._get("transcript", query_id, options)

    def get_tfbs(self, query_id, **options):
        return super(GeneClient, self)._get("tfbs", query_id, options)

    def get_snp(self, query_id, **options):
        return super(GeneClient, self)._get("snp", query_id, options)

    def get_info(self, query_id, **options):
        return super(GeneClient, self)._get("info", query_id, options)


class ProteinClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "protein"
        super(ProteinClient, self).__init__(configuration, _subcategory)

    def get_substitution_scores(self, query_id, **options):
        return super(ProteinClient, self)._get("substitution_scores", query_id,
                                               options)


class VariationClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "variation"
        super(VariationClient, self).__init__(configuration, _subcategory)


class GenomicRegionClient(RestClient):
    def __init__(self, configuration):
        _category = "genomic"
        _subcategory = "region"
        super(GenomicRegionClient, self).__init__(configuration, _subcategory,
                                                  _category)

    def get_gene(self, query_id, **options):
        return super(GenomicRegionClient, self)._get("gene", query_id, options)

    def get_transcript(self, query_id, **options):
        return super(GenomicRegionClient, self)._get("transcript", query_id,
                                                     options)

    def get_variation(self, query_id, **options):
        return super(GenomicRegionClient, self)._get("variation", query_id,
                                                     options)

    def get_sequence(self, query_id, **options):
        return super(GenomicRegionClient, self)._get("sequence", query_id,
                                                     options)
