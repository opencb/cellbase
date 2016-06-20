import unittest
import pycellbase.features as fts
import pycellbase.config as config


class GeneClientTest(unittest.TestCase):
    def test_get_protein(self):
        gc = fts.GeneClient(config.ConfigClient())
        assert gc.get_protein('BRCA2')['response'][0]['id'] == 'BRCA2'
        assert not gc.get_protein('BRCA2')['error']

        assert gc.get_protein('BRCA1,BRCA2')['response'][0]['id'] == 'BRCA1'
        assert gc.get_protein('BRCA1,BRCA2')['response'][1]['id'] == 'BRCA2'
        assert not gc.get_protein('BRCA1,BRCA2')['error']

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
