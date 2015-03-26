#!/usr/bin/python

import sys


f = open(sys.argv[1], 'r')

for line in f:
	fields = line.split("\t")
	transcripts = fields[19].split(" ")
	for t in transcripts:
		print '{0}\t{1}\t{2}\t{3}'.format(t, fields[0], 'uniprotkb_acc', 'UniProtKB ACC')
		print '{0}\t{1}\t{2}\t{3}'.format(t, fields[1], 'uniprotkb_id', 'UniProtKB ID')
f.close()
