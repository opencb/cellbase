## ----setup, include=FALSE------------------------------------------------
knitr::opts_chunk$set(echo = TRUE)

## ---- eval=FALSE---------------------------------------------------------
#  library(cellbaseR)
#  

## ---- eval=TRUE----------------------------------------------------------
cb <- cellbaseR::CellbaseQuery()
cb


## ---- eval=FALSE, message=FALSE, warning=FALSE---------------------------
#  sp <- cbSpecies(object = cb) # This will give you a list of dataframes corresponding
#  # to avaiable groups
#  # See the available groups
#  names(sp)
#  # Get the information about the vertberates group
#  vertebrates <- (sp[["vertebrates"]])
#  head(vertebrates)
#  # Later will see How to change the default species from human to your species of interest
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
#  
#  res <- cbRegion(object = cb,ids = "17:1000000-1100000",resource = "clinical")
#  # to get the data
#  res <- cellbaseData(res)
#  

## ----eval=FALSE----------------------------------------------------------
#  res2 <- cbVariant(object =cb,ids = "1:169549811:A:G",resource = "annotation")
#  # to get the data
#  res2 <- cellbaseR::cellbaseData(res2)

