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
  cat("| it holds the configuration for querying the Cellbase databases\n")
  cat("| to get more information about the available species run cbSpecies()\n")
  cat("| to change the default species from human to any other species use Species()\n")
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
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
##############
##############
############# Partially implemented
setGeneric("cbClinical", function(object, genes=NULL,genome=NULL,region=NULL,so=NULL,rs=NULL,rcv=NULL,phenotype=NULL,...) standardGeneric("cbClinical"))
setMethod("cbClinical", "CellbaseQuery",  definition = function(object,genes=NULL,genome=NULL,region=NULL,so=NULL,rs=NULL,rcv=NULL,phenotype=NULL,...) {

  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "clinical"
  ids <- NULL
  resource <- "all"
  if(!is.null(genes)){
    genes <- paste0(genes,collapse = ",")
    genes <- paste("gene=",genes,sep = "")
  }
 
  #genome <- paste("genome=",genes,sep = "")
  if(!is.null(so)){
    so <- paste0(so,collapse = ",")
    so <- paste("so=",so,sep = "")
  }
  if(!is.null(region)){
    region <- paste0(region,collapse = ",")
    region <- paste("region=",region,sep = "")
  }
  if(!is.null(rs)){
    rs <- paste0(rs,collapse = ",")
    rs <- paste("rs=",rs,sep = "") 
  }
 if(!is.null(rcv)){
   rcv <- paste0(rcv,collapse = ",")
   rcv <- paste("rcv=",rcv,sep = "")
 }
  if(!is.null(phenotype)){
    phenotype <- paste0(phenotype,collapse = ",")
    phenotype <- paste("phenotype=",phenotype,sep = "")
  }
  limit <- "limit=1000"
  filter <- c(genes=genes,so=so,region=region,rs=rs,rcv=rcv,phenotype=phenotype,limit=limit)
  filter <- paste(filter,collapse = "&")
  result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=filter,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbGene", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=filter, ...) standardGeneric("cbGene"))
setMethod("cbGene", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "gene"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})


###
setGeneric("cbRegion", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbRegion"))
setMethod("cbRegion", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "genomic"
  subcateg<- "region"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbSnp", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbSnp"))
setMethod("cbSnp", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "snp"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbVariant", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbVariant"))
setMethod("cbVariant", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "genomic"
  subcateg<- "variant"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta =NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
###
setGeneric("cbTfbs", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbTfbs"))
setMethod("cbTfbs", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "regulation"
  subcateg<- "tf"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbTranscript", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbTranscript"))
setMethod("cbTranscript", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "transcript"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})



###
setGeneric("cbXref", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbXref"))
setMethod("cbXref", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "id"
  ids <- toupper(ids)
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
###
###
setGeneric("cbProtein", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbProtein"))
setMethod("cbProtein", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "feature"
  subcateg<- "protein"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})


###
setGeneric("cbGenomeSequence", function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbGenomeSequence"))
setMethod("cbGenomeSequence", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, meta=NULL,species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  
  host <- object@config$host
  species <- object@config$species
  version <- object@config$version
  categ <- "genomic"
  subcateg<- "chromosome"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
# setGeneric("cbMeta", function(object,file=NULL,host=NULL, version=NULL, species, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbMeta"))
# setMethod("cbMeta", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL,meta=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
#   
#   host <- object@config$host
#   species <- species
#   version <- object@config$version
#   meta <- paste0("meta","/",sep="")
#   categ <- NULL
#   subcateg<- NULL
#   ids <- NULL
#   resource <- NULL
#   result <- fetchCellbase(file=NULL,host=host, version=version,meta = meta, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
#   data <- lapply(result, function(x)as.data.frame(x))
#   return(data)
# })

### Not working because of server
setGeneric("cbSpecies", function(object,file=NULL,host=NULL, version=NULL,meta, species, categ, subcateg,ids,resource,filter=NULL, ...) standardGeneric("cbSpecies"))
setMethod("cbSpecies", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL,meta, species, categ=NULL, subcateg=NULL,ids=NULL,resource,filter=NULL,...) {

  host <- object@config$host
  species <- "species"
  version <- object@config$version
  meta <- "meta/"
  categ <- NULL
  subcateg<- NULL
  ids <- NULL
  resource <- NULL
  result <- fetchCellbase(file=NULL,host=host, version=version,meta = meta, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- lapply(result, function(x)as.data.frame(x))
  return(data)
})

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

