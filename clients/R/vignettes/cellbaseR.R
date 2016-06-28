## ----setup, include=FALSE------------------------------------------------
knitr::opts_chunk$set(echo = TRUE)

## ---- eval=FALSE---------------------------------------------------------
#  library(cellbaseR)
#  

## ---- eval=FALSE, message=FALSE------------------------------------------
#  # to get the default CellbaseQuery object (human data, from genome GRCh37)
#  library(cellbaseR)
#  cb <- CellBaseR()
#  # to change the default species from human to mouse for example
#  mm <-CellBaseR(species = "mmsculus")
#  

## ---- message=FALSE, warning=FALSE---------------------------------------
library(cellbaseR)
cb <- CellBaseR()
sp <- cbSpeciesClient(object = cb)
# This will give you a CellbaseResult object
# to get the dataframe of all available species
sp <- cbData(sp)
colnames(sp)


## ---- message=FALSE, warning=FALSE---------------------------------------
library(cellbaseR)
cb <- CellBaseR()
genes <- c("TP73","TET1")

res <- cbGeneClient(object = cb, ids = genes, resource = "transcript")
# to get the resulting data.frame run cellbaseData()
res <- cbData(object = res)
names(res)
# as you can see the res dataframe also contains an exons column 
# which is in fact a list column of nested dataframes, to get the
# exons data.frame for first transcript
TET1_transcripts <- res$exons[[1]]
names(TET1_transcripts)

## ---- message=FALSE, warning=FALSE---------------------------------------
# making a query through cbRegion to get all the clinically relevant variants 
# in a specific region
library(cellbaseR)
cb <- CellBaseR()
res <- cbRegionClient(object=cb,ids="17:1000000-1100000",
resource="clinical")
# to get all conservation data in this region
res <- cbRegionClient(object=cb,ids="17:1000000-1100000",
resource="conservation")
#likewise to get all the regulatory data for the same region
res <- cbRegionClient(object=cb,ids="17:1000000-1100000", resource="regulatory")
res <- cbData(res)
names(res)

## ---- message=FALSE, warning=FALSE---------------------------------------
library(cellbaseR)
cb <- CellBaseR()
res2 <- cbVariantClient(object=cb, ids="1:169549811:A:G", resource="annotation")
# to get the data 
res2 <- cbData(res2)
names(res2)

## ---- eval=TRUE, message=FALSE, warning=FALSE----------------------------
library(cellbaseR)
cb <- CellBaseR()
# First we have to specify aour filters, we do that by creating an object of
# class CellbaseParam
cbparam <- CellBaseParam(gene=c("TP73","TET1"), genome="GRCh38")
cbparam
# Note that cbClinical does NOT require any Ids to be passed, only the filters
# and of course the CellbaseQuery object
res <- cbClinicalClient(object=cb,filters=cbparam)
res
res <- cbData(res)
names(res)


