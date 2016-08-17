import unittest

import pycellbase.cbfeatures as cbfts
from pycellbase.cbconfig import ConfigClient


class GeneClientTest(unittest.TestCase):
    """Tests the GeneClient class"""
    def test_get_biotypes(self):
        """Checks retrieval of gene biotypes"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_biotypes()
        assert 'protein_coding' in res[0]['result']

    def test_get_clinical(self):
        """Checks retrieval of gene clinical info"""
        gc = cbfts.GeneClient(ConfigClient())
        # res = gc.get_clinical('BRCA1')
        res = gc.get_clinical('BRCA1')
        assert res[0]['result'][0]['chromosome'] == '17'

    def test_get_protein(self):
        """Checks retrieval of protein"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_protein('BRCA1,BRCA2')
        assert res[0]['result'][0]['name'][0] == 'BRCA1_HUMAN'
        assert res[1]['result'][0]['name'][0] == 'BRCA2_HUMAN'

    def test_get_transcript(self):
        """Checks retrieval of protein"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_transcript('BRCA1')
        assert res[0]['result'][0]['name'] == 'BRCA1-001'

    def test_get_tfbs(self):
        """Checks retrieval of protein"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_tfbs('BRCA1')
        assert res[0]['result'][0]['tfName'] == 'E2F4'

    def test_get_snp(self):
        """Checks retrieval of snp"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_snp('LDLR')
        assert res[0]['result'][0]['id'] == 'rs191244119'

    def test_get_info(self):
        """Checks retrieval of gene info"""
        gc = cbfts.GeneClient(ConfigClient())
        res = gc.get_info('BRCA1')
        assert res[0]['id'] == 'BRCA1'


class ProteinClientTest(unittest.TestCase):
    """Tests the ProteinClient class"""
    def test_get_substitution_scores(self):
        """Checks retrieval of protein substitution scores"""
        pc = cbfts.ProteinClient(ConfigClient())
        res = pc.get_substitution_scores('BRCA1_HUMAN')
        assert (res[0]['result'][0]['1']['W'] ==
                {'pe': 0, 'ps': 0.995, 'ss': 0, 'se': 1})


class VariationClientTest(unittest.TestCase):
    """Tests the VariationClient class"""
    vc = cbfts.VariationClient(ConfigClient())


class GenomicRegionTest(unittest.TestCase):
    """Tests the GenomicRegion class"""
    def test_get_transcript(self):
        """Checks retrieval of transcripts"""
        grc = cbfts.GenomicRegionClient(ConfigClient())
        res = grc.get_transcript('3:1000-100000')
        assert res[0]['result'][0]['id'] == 'ENST00000440867'

    def test_get_variation(self):
        """Checks retrieval of variations"""
        grc = cbfts.GenomicRegionClient(ConfigClient())
        res = grc.get_variation('3:10000-100000')
        assert res[0]['result'][0]['id'] == 'rs192023809'

    def test_get_sequence(self):
        """Checks retrieval of sequence"""
        grc = cbfts.GenomicRegionClient(ConfigClient())
        res = grc.get_sequence('3:100-200')
        sequence = res[0]['result'][0]['sequence']
        assert len(sequence) == 101


if __name__ == '__main__':
    unittest.main()
