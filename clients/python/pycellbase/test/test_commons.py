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
        assert len(res[0]['result']) == 8196

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
        assert len(res[0]['result']) == 5196

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 2000, 'skip': 42})
        assert len(res[0]['result']) == 2000

    def test_get_multiple_ids(self):
        """Tests the retrieval of information for multiple ID"""

        # Normal query with string ids
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        assert len(res) == 3
        assert len(res[0]['result']) == 8196
        assert len(res[1]['result']) == 10519
        assert len(res[2]['result']) == 4113

        # Normal query with list ids
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id=['BRCA1', 'BRCA2', 'LDLR'])
        assert len(res) == 3
        assert len(res[0]['result']) == 8196
        assert len(res[1]['result']) == 10519
        assert len(res[2]['result']) == 4113

        # Query with limit
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4113

        # Query with skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4196
        assert len(res[1]['result']) == 6519
        assert len(res[2]['result']) == 113

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4196
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 113

    def test_multiple_threads(self):
        # Modifying the number of ids needed for multithreading
        get.__globals__['_CALL_BATCH_SIZE'] = 2

        # Normal query
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        assert len(res) == 3
        assert len(res[0]['result']) == 8196
        assert len(res[1]['result']) == 10519
        assert len(res[2]['result']) == 4113

        # Query with limit
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4113

        # Query with skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4196
        assert len(res[1]['result']) == 6519
        assert len(res[2]['result']) == 113

        # Query with limit and skip
        res = get(host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4196
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 113
