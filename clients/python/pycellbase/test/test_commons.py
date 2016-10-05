import unittest

from pycellbase.commons import get
from pycellbase.cbconfig import ConfigClient


class CommonsTest(unittest.TestCase):
    """Tests the commons functions"""
    def setUp(self):
        """Initializes config params"""
        cc = ConfigClient()
        self._host = cc.host
        self._version = cc.version
        self._species = cc.species

    def test_get_single_id(self):
        """Tests the retrieval of information for one ID"""

        # Normal query
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1')
        assert len(res[0]['result']) == 8140

        # Query with limit
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 2042})
        assert len(res[0]['result']) == 2042

        # Query with skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'skip': 3000})
        assert len(res[0]['result']) == 5140

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 2000, 'skip': 42})
        assert len(res[0]['result']) == 2000

    def test_get_multiple_ids(self):
        """Tests the retrieval of information for multiple IDs"""

        # Normal query with string ids
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        assert len(res) == 3
        assert len(res[0]['result']) == 8140
        assert len(res[1]['result']) == 10419
        assert len(res[2]['result']) == 4108

        # Normal query with list ids
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id=['BRCA1', 'BRCA2', 'LDLR'])
        assert len(res) == 3
        assert len(res[0]['result']) == 8140
        assert len(res[1]['result']) == 10419
        assert len(res[2]['result']) == 4108

        # Query with limit
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4108

        # Query with skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4140
        assert len(res[1]['result']) == 6419
        assert len(res[2]['result']) == 108

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4140
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 108

    def test_multiple_threads(self):
        """Tests the retrieval of information with multiple threads"""

        # Modifying the number of ids needed for multithreading
        get.__globals__['_CALL_BATCH_SIZE'] = 2

        # Normal query
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        assert len(res) == 3
        assert len(res[0]['result']) == 8140
        assert len(res[1]['result']) == 10419
        assert len(res[2]['result']) == 4108

        # Query with limit
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4108

        # Query with skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4140
        assert len(res[1]['result']) == 6419
        assert len(res[2]['result']) == 108

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4140
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 108

    def test_duplicated_ids(self):
        """Tests the retrieval of information for duplicated IDs"""

        # Modifying the number of ids needed for multithreading
        get.__globals__['_CALL_BATCH_SIZE'] = 5

        # Normal query
        n = 10  # Number of duplications
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id=','.join(['LDLR']*n))
        assert len(res) == n
        # Checking that every response has the same number of results
        for i, r in enumerate(res):
            assert len(r['result']) == 4108

