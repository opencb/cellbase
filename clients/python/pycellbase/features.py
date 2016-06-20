from pycellbase.restclient import RestClient


class _Feature(RestClient):
    def __init__(self, configuration, subcategory):
        _category = "feature"
        super(_Feature, self).__init__(configuration, subcategory, _category)

    def get_next(self, query_id, **options):
        return super(_Feature, self).get('next', query_id, options)


class GeneClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "gene"
        super(GeneClient, self).__init__(configuration, _subcategory)

    def get_protein(self, query_id, **options):
        return super(GeneClient, self).get("protein", query_id, options)

    def get_transcript(self, query_id, **options):
        return super(GeneClient, self).get("transcript", query_id, options)

    def get_tfbs(self, query_id, **options):
        return super(GeneClient, self).get("tfbs", query_id, options)

    def get_variation(self, query_id, **options):
        return super(GeneClient, self).get("variation", query_id, options)


class ProteinClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "protein"
        super(ProteinClient, self).__init__(configuration, _subcategory)


class VariationClient(_Feature):
    def __init__(self, configuration):
        _subcategory = "protein"
        super(VariationClient, self).__init__(configuration, _subcategory)


class GenomicRegionClient(RestClient):
    def __init__(self, configuration):
        _category = "genomic"
        _subcategory = "region"
        super(GenomicRegionClient, self).__init__(configuration, _subcategory,
                                                  _category)

    def get_gene(self, query_id, **options):
        return super(GenomicRegionClient, self).get("gene", query_id, options)

    def get_transcript(self, query_id, **options):
        return super(GenomicRegionClient, self).get("transcript", query_id,
                                                    options)

    def get_variation(self, query_id, **options):
        return super(GenomicRegionClient, self).get("variation", query_id,
                                                    options)

    def get_sequence(self, query_id, **options):
        return super(GenomicRegionClient, self).get("sequence", query_id,
                                                    options)

    def get_regulatory(self, query_id, **options):
        return super(GenomicRegionClient, self).get(query_id, "regulatory",
                                                    options)

    def get_tfbs(self, query_id, **options):
        return super(GenomicRegionClient, self).get(query_id, "tfbs", options)

    def get_conservation(self, query_id, **options):
        return super(GenomicRegionClient, self).get(query_id, "conservation",
                                                    options)

