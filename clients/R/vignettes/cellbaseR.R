## ----setup, include=FALSE------------------------------------------------
knitr::opts_chunk$set(echo = TRUE)

## ---- eval=FALSE---------------------------------------------------------
#  library(cellbaseR)
#  

## ---- eval=FALSE, message=FALSE------------------------------------------
#  # to get the default CellbaseQuery object (human data, from genome GRCh37)
#  library(cellbaseR)
#  cb <- CellbaseQuery()
#  # to change the default species from human to mouse for example
#  mm <-CellbaseQuery(species = "mmsculus")
#  

## ---- eval=FALSE, message=FALSE, warning=FALSE---------------------------
#  library(cellbaseR)
#  cb <- CellbaseQuery()
#  sp <- cbSpecies(object = cb)
#  # This will give you a CellbaseResult object
#  # to get the dataframe of all available species
#  sp <- cellbaseData(sp)
#  names(sp)
#  # see all supported species
#  sp$scientificName
#  # See what categories of data are avaible for humans
#  sp$data[[1]]
#  
#  

## ---- eval=FALSE---------------------------------------------------------
#  cb <- CellbaseQuery()
#  genes <- c("TP73","TET1")
#  
#  res <- cbGene(object = cb, ids = genes, resource = "transcript")
#  # to get the resulting data.frame run cellbaseData()
#  res <- cellbaseData(object = res)
#  names(res)
#  # as you can see the res dataframe also contains an exons column
#  # which is in fact a list column of nested dataframes, to get the
#  # exons data.frame for first transcript
#  TET1_transcripts <- res$exons[[1]]
#  names(TET1_transcripts)

## ---- eval=FALSE---------------------------------------------------------
#  # making a query through cbRegion to get all the clinically relevant variants
#  # in a specific region
#  res <- cbRegion(object=cb,ids="17:1000000-1100000",
#  resource="clinical")
#  # to get the data
#  res <- cellbaseData(res)
#  # to get all conservation data in this region
#  res <- cbRegion(object=cb,ids="17:1000000-1100000",
#  resource="conservation")
#  #likewise to get all the regulatory data for the same region
#  res <- cbRegion(object=cb,ids="17:1000000-1100000",
#  resource="regulatory")
#  

## ----eval=FALSE----------------------------------------------------------
#  res2 <- cbVariant(object =cb,ids = "1:169549811:A:G",resource = "annotation")
#  # to get the data
#  res2 <- cellbaseR::cellbaseData(res2)

## ---- eval=FALSE---------------------------------------------------------
#  # First we have to specify aour filters, we do that by creating an object of
#  # class CellbaseParam
#  cbparam <- CellbaseParam(gene=c("TP73","TET1"), genome="GRCh38")
#  cbparam
#  # Note that cbClinical does NOT require any Ids to be passed, only the filters
#  # and of course the CellbaseQuery object
#  res <- cbClinical(object=cb,filters=cbparam)
#  

## ---- eval=FALSE---------------------------------------------------------
#  
#  

