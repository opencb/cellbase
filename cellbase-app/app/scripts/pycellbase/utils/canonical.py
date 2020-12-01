#!/usr/bin/env python3

import gzip
import csv
import argparse

# Adds "canonical" flags to any transcript in the specified UCSC file

from pymongo import MongoClient

parser = argparse.ArgumentParser()
parser.add_argument('--path',
                    help="absolute path to the downloaded canonical data file, e.g. /data/knownCanonical.txt.gz",
                    required=True)
parser.add_argument('--db', help="database name, e.g. cellbase_hsapiens_grch38_v5", required=True)

args = parser.parse_args()

mongo_client = MongoClient('mongodb://localhost:27017')
db = mongo_client[args.db]
collection = db["gene"]

# Downloaded from http://hgdownload.soe.ucsc.edu/goldenPath/hg38/database/knownCanonical.txt.gz
filename = args.path


def update_flags(transcript_id):
    results = collection.find_one_and_update(
        {'transcripts.id': transcript_id},
        {'$addToSet': {'transcripts.$.annotationFlags': "canonical"}}
    )
    return bool(results)


found = 0
not_found = 0

with gzip.open(filename, mode='rt') as tsvfile:
    tsvreader = csv.reader(tsvfile, delimiter="\t")
    for line in tsvreader:
        transcript_id = line[4]
        document_exists = update_flags(transcript_id)
        # the data file has the versions in the ensembl transcript Ids. remove them or not
        # transcript_id_split = line[4].split(".")
        # transcript_id = transcript_id_split[0]
        # document_exists = update_flags(transcript_id)
        if not document_exists:
            print("ERROR: Transcript not found " + transcript_id)
            not_found += 1
        else:
            found += 1

print("Processed: " + str(found) + " transcripts")
print("Not found: " + str(not_found) + " transcripts")

tsvfile.close()
