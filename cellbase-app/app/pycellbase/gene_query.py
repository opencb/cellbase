#!/usr/bin/python

# Loading CellBase and configuration clients
from pycellbase.cbconfig import ConfigClient
from pycellbase.cbclient import CellBaseClient

# Initializing CellBaseClient
cc = ConfigClient("../conf/client-configuration.yml")
cbc = CellBaseClient(cc)

# Initializing gene client
gc = cbc.get_gene_client()

# Retrieving transcription factor binding sites (TFBS) for a gene list
gene_list = ['BRCA1', 'BRCA2', 'LDLR']
tfbs_responses = gc.get_tfbs(gene_list, include='id')

# Printing the number of TFBS found for each gene
for response in tfbs_responses:
    print('Number of TFBS for "%s": %d' % (response['id'], len(response['result'])))