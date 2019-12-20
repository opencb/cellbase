import unittest

import pycellbase.cbrestclients as cbfts
from pycellbase.cbconfig import ConfigClient
from requests import Session


class GeneClientTest(unittest.TestCase):
    """Tests the GeneClient class"""
    def setUp(self):
        """Initializes the gene client"""
        self._gc = cbfts.GeneClient(Session(), ConfigClient())

    def test_get_biotypes(self):
        """Checks retrieval of gene biotypes"""
        res = self._gc.get_biotypes()
        assert len(res[0]['result']) == 29
        assert 'protein_coding' in res[0]['result']

    def test_get_list(self):
        """Checks retrieval of gene list"""
        res = self._gc.get_list(include="id", limit=10000)
        assert len(res[0]['result']) == 10000
        assert res[0]['result'][0]['id'] == 'ENSG00000223972'

    def test_get_protein(self):
        """Checks retrieval of protein"""
        res = self._gc.get_protein('BRCA1')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['name'][0] == 'BRCA1_HUMAN'

    def test_get_transcript(self):
        """Checks retrieval of protein"""
        res = self._gc.get_transcript('BRCA1')
        assert len(res[0]['result']) == 27
        assert res[0]['result'][0]['name'] == 'BRCA1-001'

    def test_get_tfbs(self):
        """Checks retrieval of protein"""
        res = self._gc.get_tfbs('BRCA1')
        assert len(res[0]['result']) == 175
        assert res[0]['result'][0]['tfName'] == 'E2F4'

    def test_get_snp(self):
        """Checks retrieval of snp"""
        res = self._gc.get_snp('LDLR')
        assert len(res[0]['result']) == 4108
        assert res[0]['result'][0]['id'] == 'rs191244119'

    def test_get_info(self):
        """Checks retrieval of gene info"""
        res = self._gc.get_info('BRCA1')
        assert len(res[0]['result']) == 1
        assert res[0]['id'] == 'BRCA1'

    def test_search(self):
        """Checks retrieval of gene info given a set of filters"""
        res = self._gc.search(name='BRCA1')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['id'] == 'ENSG00000012048'

        res = self._gc.search(name='BRCA1', include='chromosome')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['chromosome'] == '17'


class ProteinClientTest(unittest.TestCase):
    """Tests the ProteinClient class"""
    def setUp(self):
        """Initializes the protein client"""
        self._pc = cbfts.ProteinClient(Session(), ConfigClient())

    def test_get_substitution_scores(self):
        """Checks retrieval of protein substitution scores"""
        res = self._pc.get_substitution_scores('BRCA1_HUMAN')
        assert len(res[0]['result']) == 1
        assert (res[0]['result'][0]['1']['W'] ==
                {'pe': 0, 'ps': 0.995, 'ss': 0, 'se': 1})

    def test_get_sequence(self):
        """Checks retrieval of protein sequence"""
        res = self._pc.get_sequence('Q9UL59')
        assert len(res[0]['result']) == 1
        assert len(res[0]['result'][0]) == 606


class TrancriptClientTest(unittest.TestCase):
    """Tests the TrancriptClient class"""
    def setUp(self):
        """Initializes the transcript client"""
        self._tc = cbfts.TranscriptClient(Session(), ConfigClient())

    def test_get_function_prediction(self):
        """Checks retrieval of function predictions"""
        res = self._tc.get_function_prediction('ENST00000536068')
        assert len(res[0]['result']) == 1
        assert (res[0]['result'][0]['10']['E'] ==
                {'pe': 1, 'se': 1, 'ps': 0.497, 'ss': 0})

    def test_get_gene(self):
        """Checks retrieval of genes which codify the transcript"""
        res = self._tc.get_gene('ENST00000536068')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['name'] == 'ZNF214'

    def test_get_protein(self):
        """Checks retrieval of codified proteins"""
        res = self._tc.get_protein('ENST00000536068')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['name'][0] == 'ZN214_HUMAN'

    def test_get_sequence(self):
        """Checks retrieval of the transcript sequence"""
        res = self._tc.get_sequence('ENST00000536068')
        assert len(res[0]['result']) == 1
        assert len(res[0]['result'][0]) == 2562


class VariationClientTest(unittest.TestCase):
    """Tests the VariationClient class"""
    def setUp(self):
        """Initializes the variation client"""
        self._vc = cbfts.VariationClient(Session(), ConfigClient())

    def test_get_consequence_types(self):
        """Checks retrieval of consequence types list"""
        res = self._vc.get_consequence_types()
        assert 'coding_sequence_variant' in res[0]['result']

    def test_get_consequence_type(self):
        """Checks retrieval of consequence types for a variation"""
        res = self._vc.get_consequence_type('rs6025')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0] == 'missense_variant'


class GenomicRegionTest(unittest.TestCase):
    """Tests the GenomicRegion class"""
    def setUp(self):
        """Initializes the variation client"""
        self._gr = cbfts.RegionClient(Session(), ConfigClient())

    def test_get_clinical(self):
        """Checks retrieval of clinical data"""
        res = self._gr.get_clinical('3:100000-900000')
        assert len(res[0]['result']) == 469
        assert res[0]['result'][0]['mutationCDS'] == 'c.4G>A'

    def test_get_conservation(self):
        """Checks retrieval of conservation data"""
        res = self._gr.get_conservation('3:100000-900000')
        assert len(res[0]['result']) == 3
        assert res[0]['result'][0]['source'] == 'gerp'

    def test_get_gene(self):
        """Checks retrieval of genes"""
        res = self._gr.get_gene('3:100000-900000')
        assert len(res[0]['result']) == 8
        assert res[0]['result'][0]['name'] == 'AC090044.1'

    def test_get_regulatory(self):
        """Checks retrieval of regulatory elements"""
        res = self._gr.get_regulatory('3:100000-900000')
        assert len(res[0]['result']) == 677
        assert res[0]['result'][0]['name'] == 'H3K27me3'

    def test_get_sequence(self):
        """Checks retrieval of sequence"""
        res = self._gr.get_sequence('3:100-200')
        assert len(res[0]['result']) == 1
        assert len(res[0]['result'][0]['sequence']) == 101

    def test_get_tfbs(self):
        """Checks retrieval of transcription factor binding sites (TFBSs)"""
        res = self._gr.get_tfbs('3:100000-900000')
        assert len(res[0]['result']) == 239
        assert res[0]['result'][0]['name'] == 'CTCF'

    def test_get_transcript(self):
        """Checks retrieval of transcripts"""
        res = self._gr.get_transcript('3:1000-100000')
        assert len(res[0]['result']) == 2
        assert res[0]['result'][0]['id'] == 'ENST00000440867'

    def test_get_variation(self):
        """Checks retrieval of variations"""
        res = self._gr.get_variation('3:10000-100000')
        assert res[0]['result'][0]['id'] == 'rs192023809'


class VariantTest(unittest.TestCase):
    """Tests the Variant class"""
    def setUp(self):
        """Initializes the variation client"""
        self._vc = cbfts.VariantClient(Session(), ConfigClient())

    def test_get_annotation(self):
        """Checks retrieval of annotation data"""
        res = self._vc.get_annotation('19:45411941:T:C')
        assert len(res[0]['result']) == 1
        assert res[0]['result'][0]['id'] == 'rs429358'

    def test_get_cadd(self):
        """Checks retrieval of cadd"""
        res = self._vc.get_cadd('19:45411941:T:C')
        assert len(res[0]['result']) == 2
        assert res[0]['result'][0]['score'] == -1.1800003051757812


if __name__ == '__main__':
    unittest.main()
