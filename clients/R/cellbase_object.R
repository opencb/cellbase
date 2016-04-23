library(methods)
source("Client_plan.R")
#####################################################################################################
# The CellbaseQuery class defintion 
CellbaseQuery <- setClass("CellbaseQuery",
                          slots = c(config="list",hosts="vector",species="list",categories="vector"),
                          prototype = prototype(
                            config=list(host="http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/",
                                        version = "v4/",species="hsapiens/",batch_size=200,num_threads=4),
                            hosts=c("http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/"),
                            species=list(human="hsapiens",mouse="mmusculus",rat="rnorvegicus",chimp="ptroglodytes",
                                         gorilla="ggorilla", orangutan="pabelii",macaque="mmulatta",pig="sscrofa",
                                         dog="cfamiliaris",horse="ecaballus",rabbit="ocuniculus",chicken="ggallus",
                                         cow="btaurus",cat="fcatus",zebrafish="drerio",fruitfly="dmelanogaster",
                                         mosquito="agambiae",worm="celegans",yeast="scerevisiae",
                                         malaria_parasite="pfalciparum"),
                            categories=c("Genomic","Feature","Regulatory","Network")
                          )
)

##### Methods for CellbaseQuery objects
# Improve this by querying the meta field
#  The show method for cellbaseQuery class
setMethod("show",signature = "CellbaseQuery",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat("| slot config containing the following:",paste(names(object@config),collapse = ","),"\n")
  cat("| slot hosts containing the following:",object@hosts,"\n")
  cat("| slot species containing the following:",paste(names(object@species),collapse = ","),"\n")
  cat("| slot categories containing the following:",paste(object@categories,collapse = ","),"\n")
})
# A general wrapper method
setGeneric("getCellbase", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("getCellbase"))
setMethod("getCellbase", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
##############
##############
##############
setGeneric("cbClinical", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbClinical"))
setMethod("cbClinical", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbGene", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbGene"))
setMethod("cbGene", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbGenomeSequence", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbGenomeSequence"))
setMethod("cbGenomeSequence", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbRegion", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbRegion"))
setMethod("cbRegion", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbProtein", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbProtein"))
setMethod("cbProtein", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbSnp", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbSnp"))
setMethod("cbSnp", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("getCellbase", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("getCellbase"))
setMethod("getCellbase", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbTfbs", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbTfbs"))
setMethod("cbTfbs", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbTranscript", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbTranscript"))
setMethod("cbTranscript", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbVariant", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbVariant"))
setMethod("cbVariant", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbXref", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbXref"))
setMethod("cbXref", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

##############
##############
##############

# # A convienance method to get all snps in a specific gene or genes
# setGeneric("getSnpsByGene", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getSnpsByGene"))
# setMethod("getSnpsByGene", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "feature"
#   subcateg<- "gene"
#   ids <- ids
#   resource <- "snp"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# # A convienance method to get all snps that are associated withspecific disease or phenotype
# setGeneric("getSnpsByPhenotype", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getSnpsByPhenotype"))
# setMethod("getSnpsByPhenotype", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "variant"
#   ids <- ids
#   resource <- "phenotype"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# 
# # A convienance method to get all gene that are associated with specific clinical condition
# setGeneric("getGenesByClinical", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getGenesByClinical"))
# setMethod("getGenesByClinical", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "feature"
#   subcateg<- "gene"
#   ids <- ids
#   resource <- "clinical"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# # A convienance method to get all gene that are associated with specific clinical condition
# setGeneric("getProteinsByGene", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getProteinsByGene"))
# setMethod("getProteinsByGene", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "feature"
#   subcateg<- "gene"
#   ids <- ids
#   resource <- "protein"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# 
# # A convienance method to get all clinical variants in a specific region
# setGeneric("getClinvarByRegion", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getClinvarByRegion"))
# setMethod("getClinvarByRegion", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "region"
#   ids <- ids
#   resource <- "clinical"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# # A convienance method to get all variants in a specific region
# setGeneric("getSnpsByRegion", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getSnpsByRegion"))
# setMethod("getSnpsByRegion", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "region"
#   ids <- ids
#   resource <- "variation"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# # A convienance method to get all transcripts in a specific region
# setGeneric("getSnpsByRegion", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getSnpsByRegion"))
# setMethod("getSnpsByRegion", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "region"
#   ids <- ids
#   resource <- "variation"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# 
# # A convienance method to get all regulatory features in a specific region
# setGeneric("getRegulationByRegion", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getRegulationByRegion"))
# setMethod("getRegulationByRegion", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "region"
#   ids <- ids
#   resource <- "regulatory"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
# 
# # A convienance method to get all transcription factors binding sites in a specific region
# setGeneric("getTfbsByRegion", function(object, file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) standardGeneric("getTfbsByRegion"))
# setMethod("getTfbsByRegion", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   categ <- "genomic"
#   subcateg<- "region"
#   ids <- ids
#   resource <- "tfbs"
#   result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
#   
# })
#######################################################################
# A method to allow for updating the species of the CellbaseQuery config
setGeneric("species<-",
           function(object, value) standardGeneric("species<-"))
setMethod("species<-","CellbaseQuery",function(object,value){
  value =paste0(value,"/",sep="")
  object@config$species <- value
  return(object)
})

# A method to allow for updating the host of the CellbaseQuery config
setGeneric("host<-",
           function(object, value) standardGeneric("host<-"))
setMethod("host<-","CellbaseQuery",function(object,value){
  value =paste0(value,"/",sep="")
  object@config$host <- value
  return(object)
})
# Need more work
# setGeneric("annotateVcf", function(object, ...) standardGeneric("annotateVcf"))
# setMethod("annotateVcf", "CellbaseQuery",  definition = function(object,file=file,host, version, species, categ, subcateg,ids=NULL,resource,filter=NULL,...) {
#   host <- object@config$host
#   version <- object@config$version
#   species <- object@config$species
#   batch_size <- object@config$batch_size
#   num_threads <- object@config$num_threads
#   categ <- "genomic"
#   subcateg<- "variant"
#   ids <- ids
#   resource <- "/annotation"
#   result <- fetchCellbase(host, file=file,version, species, categ, subcateg,ids=NULL,resource,filter=NULL,...)
#   data <- CellbaseResult(cellbaseData=result)
#   return(data)
# })

#####################################################################################################
# The CellbaseResult class defintion 
CellbaseResult <- setClass("CellbaseResult",slots = c(cellbaseData="data.frame"))
#  The show method for CellbaseResult class
setMethod("show",signature = "CellbaseResult",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat(" containing ", nrow(object@cellbaseData), " rows and ",
      ncol(object@cellbaseData), " columns.\n", sep = "")
})

# An accessor method to get CellbaseResult cellbaseData slot
setGeneric("cellbaseData", function(object, ...) standardGeneric("cellbaseData"))
setMethod("cellbaseData", "CellbaseResult",  definition = function(object) object@cellbaseData)
# A method to allow for subsetting the cellbaseData
setMethod("[","CellbaseResult",definition = function(x,i,j,drop="missing")
{
  .cellbaseData <- x@cellbaseData[i, j]
  CellbaseResult(cellbaseData = .cellbaseData)
})

