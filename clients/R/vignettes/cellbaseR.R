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
#  # the following query will fetch all the clinically relevant variants for these genes
#  res <- cbGene(object = cb, ids = genes, resource = "info")
#  res
#  

## ---- eval=FALSE---------------------------------------------------------
#  
#  res <- cellbaseR::cbRegion(object = cb,ids = "17:1000000-1100000",resource = "clinical")
#  # to get the data
#  res <- cellbaseR::cellbaseData(res)
#  

## ----eval=FALSE----------------------------------------------------------
#  res2 <- cbVariant(object =cb,ids = "1:169549811:A:G",resource = "annotation")
#  # to get the data
#  res2 <- cellbaseR::cellbaseData(res2)

