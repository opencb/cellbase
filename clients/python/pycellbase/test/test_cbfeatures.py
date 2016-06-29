import unittest
import pycellbase.cbfeatures as cbfts
import pycellbase.cbconfig as config


class GeneClientTest(unittest.TestCase):
    """Tests the GeneClient class"""
    def test_get_biotypes(self):
        """Checks retrieval of gene biotypes"""
        gc = cbfts.GeneClient(config.ConfigClient())
        res = gc.get_biotypes()['response']
        assert 'protein_coding' in res[0]['result']

    def test_get_protein(self):
        """Checks retrieval of protein info"""
        gc = cbfts.GeneClient(config.ConfigClient())
        res = gc.get_protein('BRCA1,BRCA2')['response']
        assert res[0]['result'][0]['name'][0] == 'BRCA1_HUMAN'
        assert res[1]['result'][0]['name'][0] == 'BRCA2_HUMAN'


class ProteinClientTest(unittest.TestCase):
    """Tests the ProteinClient class"""
    def test_get_substitution_scores(self):
        """Checks retrieval of protein substitution scores"""
        pc = cbfts.ProteinClient(config.ConfigClient())
        res = pc.get_substitution_scores('BRCA1_HUMAN')['response']
        assert (res[0]['result'][0]['1']['W'] ==
                {'pe': 0, 'ps': 0.995, 'ss': 0, 'se': 1})


class VariationClientTest(unittest.TestCase):
    """Tests the VariationClient class"""


class GenomicRegionTest(unittest.TestCase):
    """Tests the GenomicRegion class"""
    def test_get_transcript(self):
        """Checks retrieval of transcripts"""
        grc = cbfts.GenomicRegionClient(config.ConfigClient())
        res = grc.get_transcript('3:1000-100000')['response']
        assert res[0]['result'][0]['id'] == 'ENST00000440867'

    def test_get_variation(self):
        """Checks retrieval of variations"""
        grc = cbfts.GenomicRegionClient(config.ConfigClient())
        res = grc.get_variation('3:10000-100000')['response']
        assert res[0]['result'][0]['id'] == 'rs192023809'

    def test_get_sequence(self):
        """Checks retrieval of sequence"""
        grc = cbfts.GenomicRegionClient(config.ConfigClient())
        res = grc.get_sequence('3:100-200')['response']
        sequence = res[0]['result'][0]['sequence']
        assert len(sequence) == 101  # TODO Shouldn't this be 100?


if __name__ == '__main__':
    unittest.main()
