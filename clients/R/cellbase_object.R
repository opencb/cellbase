library(methods)
source("Client_plan.R")

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

CellbaseResult <- setClass("CellbaseResult",slots = c(cellbaseData="data.frame"))

setMethod("show",signature = "CellbaseResult",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat(" containing ", nrow(object@cellbaseData), " rows and ",
      ncol(object@cellbaseData), " columns.\n", sep = "")
})
#### Methods for cellbaseQuery
setMethod("show",signature = "CellbaseQuery",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat("| slot config containing the following:",paste(names(object@config),collapse = ","),"\n")
  cat("| slot hosts containing the following:",object@hosts,"\n")
  cat("| slot species containing the following:",paste(names(object@species),collapse = ","),"\n")
  cat("| slot categories containing the following:",paste(object@categories,collapse = ","),"\n")
})
setGeneric("getCellbase", function(object, ...) standardGeneric("getCellbase"))
setMethod("getCellbase", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL, species=NULL, categ, subcateg,ids,resource,filter=NULL,...) {
  stopifnot(class(object)=="CellbaseQuery")
  if(is.null(host)){
    host <- object@config$host
  }else{
    host=host
  }
  if(is.null(species)){
    species <- object@config$species
  }else{
    species=species
  }
  version <- object@config$version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filter=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
}
)
setGeneric("getSnpsByGene", function(object, ...) standardGeneric("getSnpsByGene"))
setMethod("getSnpsByGene", "CellbaseQuery",  definition = function(object,file=NULL,host, version, species, categ, subcateg,ids,resource,filter=NULL,...) {
  
    host <- object@config$host
    version <- object@config$version
    species <- object@config$species
    categ <- "feature"
    subcateg<- "gene"
    ids <- ids
    resource <- "snp"
    result <- fetchCellbase(host, file=NULL,version, species, categ, subcateg,ids,resource,filter=NULL,...)
    data <- CellbaseResult(cellbaseData=result)
    return(data)
  
 
}
)
# Need more work
# setGeneric("annotateVcf", function(object, ...) standardGeneric("annotateVcf"))
# setMethod("annotateVcf", "CellbaseQuery",  definition = function(object,file=file,host, version, species, categ, subcateg,ids=NULL,resource,filter=NULL,...) {
#   stopifnot(class(object)=="CellbaseQuery")
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
setGeneric("species<-",
             function(object, value) standardGeneric("species<-"))
setMethod("species<-","CellbaseQuery",function(object,value){
  value =paste0(value,"/",sep="")
  object@config$species <- value
  return(object)
})

#### Methods for cellbaseResult
setGeneric("cellbaseData", function(object, ...) standardGeneric("cellbaseData"))
setMethod("cellbaseData", "CellbaseResult",  definition = function(object) object@cellbaseData)
setMethod("[","CellbaseResult",definition = function(x,i,j,drop="missing")
{
  .cellbaseData <- x@cellbaseData[i, j]
  CellbaseResult(cellbaseData = .cellbaseData)
})


