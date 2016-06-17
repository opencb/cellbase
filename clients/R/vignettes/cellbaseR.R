## ----setup, include=FALSE------------------------------------------------
knitr::opts_chunk$set(echo = TRUE)

## ---- eval=FALSE, message=FALSE------------------------------------------
#  # to get the default CellbaseR object (human data, from genome GRCh37)
#  library(cellbaseR)
#  # A default cellbaseR object is created as follows
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
str(sp,1)


## ---- message=FALSE, warning=FALSE---------------------------------------
library(cellbaseR)
cb <- CellBaseR()
genes <- c("TP73","TET1")

res <- cbGeneClient(object = cb, ids = genes, resource = "info")
# to get the resulting data.frame run cellbaseData()
res <- cbData(object = res)
str(res,2)
# as you can see the res dataframe also contains a transcriots column 
# which is in fact a list column of nested dataframes, to get the
# trasnscripts data.frame for first gene
TET1_transcripts <- res$transcripts[[1]]
str(TET1_transcripts,1)

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
str(res,1)

## ---- eval=FALSE,message=FALSE, warning=FALSE----------------------------
#  library(cellbaseR)
#  cb <- CellBaseR()
#  res2 <- cbVariantClient(object=cb, ids="1:169549811:A:G", resource="annotation")
#  # to get the data
#  res2 <- cbData(res2)
#  str(res2, 1)

## ---- eval=TRUE, message=FALSE, warning=FALSE----------------------------
library(cellbaseR)
cb <- CellBaseR()
# First we have to specify aour filters, we do that by creating an object of
# class CellbaseParam
cbparam <- CellBaseParam(gene="TET1", genome="GRCh38")
cbparam
# Note that cbClinical does NOT require any Ids to be passed, only the filters
# and of course the CellbaseQuery object
res <- cbClinicalClient(object=cb,filters=cbparam)
res
res <- cbData(res)
str(res,1)


## ----message=FALSE, warning=FALSE, eval=FALSE----------------------------
#  library(cellbaseR)
#  cb <- CellBaseR()
#  test <- createGeneModel(object = cb, region = "17:1500000-1550000")
#  library(Gviz)
#  testTrack <- GeneRegionTrack(test)
#  plotTracks(testTrack, transcriptAnnotation='symbol')
#  # then you can use this track in conjunction with any other tracks
#  ideoTrack <- IdeogramTrack(genome = "hg19", chromosome = "chr17")
#  axisTrack <- GenomeAxisTrack()
#  from <- min(test$start)-5000
#  to <- max(test$end)+5000
#  plotTracks(list(ideoTrack,axisTrack,testTrack),from = from, to = to, transcriptAnnotation='symbol')
#  
#  

## ---- message=FALSE, warning=FALSE, eval=FALSE---------------------------
#  library(cellbaseR)
#  cb <- CellBaseR()
#  fl <- system.file("extdata", "chr22.vcf.gz", package="VariantAnnotation")
#  res <- cbAnnotateVcf(object=cb, file=fl)
#  res <- cbData(res)
#  res
#  

