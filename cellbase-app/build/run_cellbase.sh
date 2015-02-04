#!/bin/bash

INDIR=$1
FASTA=$2
OUTDIR=$3

java -Xmx8000m -jar libs/cellbase-build-3.1.0.jar --build genome-sequence --fasta-file $FASTA -o $OUTDIR

java -Xmx8000m -jar libs/cellbase-build-3.1.0.jar --build gene --indir $INDIR/gene --fasta-file $FASTA -o $OUTDIR

java -Xmx14000m -jar libs/cellbase-build-3.1.0.jar --build variation --indir $INDIR/variation -o $OUTDIR

#java -jar libs/cellbase-build-3.1.0.jar --build conservation --indir $INDIR/conservation -o $OUTDIR

for i in $OUTDIR/*.json;
do
	gzip $i
done

