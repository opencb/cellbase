import unittest

from pycellbase.commons import get
from pycellbase.cbconfig import ConfigClient
from requests import Session


class CommonsTest(unittest.TestCase):
    """Tests the commons functions"""
    def setUp(self):
        """Initializes config params"""
        cc = ConfigClient()
        self._host = cc.host
        self._version = cc.version
        self._species = cc.species
        self._session = Session()

    def tearDown(self):
        self._session.close()

    def test_url_creation(self):
        """Tests the correct creation of the url"""
        # Normal query
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1', options={'limit': 200})
        assert len(res[0]['result']) == 200

        # Query with a dict option
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 200, 'include': ['chromosome', 'start']})
        assert len(res[0]['result']) == 200

    def test_get_single_id(self):
        """Tests the retrieval of information for one ID"""
        # Normal query
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1')
        assert len(res[0]['result']) == 8152

        # Query with limit
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 2042})
        assert len(res[0]['result']) == 2042

        # Query with skip
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'skip': 3000})
        assert len(res[0]['result']) == 5152

        # Query with limit and skip
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1',
                  options={'limit': 2000, 'skip': 42})
        assert len(res[0]['result']) == 2000

    def test_get_multiple_ids(self):
        """Tests the retrieval of information for multiple IDs"""

        # Normal query with string ids
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        self.check_counts(res)

    def test_get_multiple_ids_with_list_ids(self):
        """Normal query with list ids"""

        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id=['BRCA1', 'BRCA2', 'LDLR'])
        self.check_counts(res)

    def test_get_multiple_ids_with_list_ids(self):
        """Query with limit"""
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4108

    def test_get_multiple_ids_with_list_ids(self):
        """Query with skip"""
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4152
        assert len(res[1]['result']) == 6419
        assert len(res[2]['result']) == 108

    def test_get_multiple_ids_with_list_ids(self):
        """Query with limit and skip"""
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4152
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 108

    def test_multiple_threads(self):
        """Tests the retrieval of information with multiple threads"""

        # Modifying the number of ids needed for multithreading
        get.__globals__['_CALL_BATCH_SIZE'] = 2

        # Normal query
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR')
        self.check_counts(res)

    def test_multiple_threads_with_limit(self):
        """Tests the retrieval of information with multiple threads with limit"""
        # Query with limit
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 8000})
        assert len(res) == 3
        assert len(res[0]['result']) == 8000
        assert len(res[1]['result']) == 8000
        assert len(res[2]['result']) == 4108

    def test_multiple_threads_with_skip(self):
        """Tests the retrieval of information with multiple threads with skip"""
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4152
        assert len(res[1]['result']) == 6419
        assert len(res[2]['result']) == 108

    def test_multiple_threads_with_limit_and_skip(self):
        """Tests the retrieval of information with multiple threads with limit and skip"""
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id='BRCA1,BRCA2,LDLR',
                  options={'limit': 6000, 'skip': 4000})
        assert len(res) == 3
        assert len(res[0]['result']) == 4152
        assert len(res[1]['result']) == 6000
        assert len(res[2]['result']) == 108

    def test_duplicated_ids(self):
        """Tests the retrieval of information for duplicated IDs"""

        # Modifying the number of ids needed for multithreading
        get.__globals__['_CALL_BATCH_SIZE'] = 5

        # Normal query
        n = 10  # Number of duplications
        res = get(session=self._session, host=self._host, version=self._version,
                  species=self._species, category='feature', subcategory='gene',
                  resource='snp', query_id=','.join(['LDLR']*n))
        assert len(res) == n
        # Checking that every response has the same number of results
        for i, r in enumerate(res):
            assert len(r['result']) == 4108

    def check_counts(self, res):
        self.assertEquals(len(res), 3)
        self.assertEquals(len(res[0]['result']), 8152)
        self.assertEquals(len(res[1]['result']), 10419)
        self.assertEquals(len(res[2]['result']), 4108)
