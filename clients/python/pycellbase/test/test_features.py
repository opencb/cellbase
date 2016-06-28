import unittest
import pycellbase.features as fts
import pycellbase.config as config


class GeneClientTest(unittest.TestCase):
    def test_get_protein(self):
        gc = fts.GeneClient(config.ConfigClient())
        res = gc.get_protein('BRCA1,BRCA2')['response']
        assert res[0]['result'][0]['name'][0] == 'BRCA1_HUMAN'
        assert res[1]['result'][0]['name'][0] == 'BRCA2_HUMAN'

    def test_get_next(self):
        # TODO Write proper asserts
        gc = fts.GeneClient(config.ConfigClient())
        gc.get_next('BRCA2')
        gc.get_next('BRCA1,BRCA2')


class GenomicRegionTest(unittest.TestCase):
    def test_get_sequence(self):
        gr = fts.GenomicRegionClient(config.ConfigClient())
        s = gr.get_sequence('3:100-200')['response'][0]['result'][0]['sequence']
        assert len(s) == 101  # TODO Shouldn't this be 100?


if __name__ == '__main__':
    unittest.main()
