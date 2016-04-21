require(methods)
#' @include cellbase_functions.R
CellbaseQuery <- setClass("CellbaseQuery",
                          slots = c(host="character", version="character", species="character",batch_size="numeric", num_threads="numeric"),
                          prototype = prototype(
                            host="http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/",
                            version = "v4/",
                            species="hsapiens/",
                            batch_size=200,
                            num_threads=8
                          )
)
###
#' @title 
#' This is a constructor function for CellbaseQuery object which holds the default
#' configuration for connecting to the cellbase web services
#' @details
#' This class defines the CellbaseQuery object which holds the default
#' configuration required by CellbaseQuery methods to connect to the
#' cellbase web services. By defult it is configured to query human
#' data based on the GRCh37 genome assembly
#' @param species A charcter should be one of the species supported by cellbase run cbSpecies
#' to see avaiable species and their corresponding data
#' @import methods 
#' @slot host A character the default host url for cellllbase webservices
#' @slot version A character the cellbae API version
#' @slot species A charcter should be one of the species supported by cellbase
#' @slot batch_size numeric a parameter for 
#' @return An object of class CellbaseQuery
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  print(cb)
#' @export CellbaseQuery
#' @exportClass CellbaseQuery
CellbaseQuery <- function(species=character()){
  if(length(species)>0){
    species<-paste0(species,"/",sep="")
  }else{
    species <-"hsapiens/" 
  }
  new("CellbaseQuery", species=species )
}

##### Methods for CellbaseQuery objects
#  The show method for cellbaseQuery class
setMethod("show",signature = "CellbaseQuery",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat("| it holds the configuration for querying the Cellbase databases\n")
  cat("| to get more information about the available species run cbSpecies()\n")
  cat("| to change the default species from human to any other species use Species()\n")
})
#' The generic method for getCellbase
#' 
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
setGeneric("getCellbase", function(object, file=NULL,categ, subcateg,ids,resource,filters=NULL, ...) standardGeneric("getCellbase"))
#' The generic method for getCellbase
#' 
#' @aliases getCellbase
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
#' @param object an object of class CellbaseQuery
#' @param file a path to a bgzipped and tabix indexed vcf file,
#' @param categ charcter to specify the category to be queried
#' this could be "feature", "genomic", "regulatory", or "network"
#' @param subcateg charcter to specify the subcategory to be queried
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter to specify the resource to be queried
#' @param filters an object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' 
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples
#'  library(cellbaseR)
#'  cb <- CellbaseQuery
#'  res <- getCellbase(object=cb,categ="feature",subcateg="gene",ids="TET1",resource="info")
#'
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("getCellbase", "CellbaseQuery",  definition = function(object,file=NULL, categ, subcateg,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- categ
  subcateg<- subcateg
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
##############
##############
#############

setGeneric("cbClinical", function(object, filters,...) standardGeneric("cbClinical"))
#' A method to query Clinical data from Cellbase web services
#' 
#' A method to query Clinical data from Cellbase web services
#' @aliases cbClinical
#' @param object an object of class CellbaseQuery
#' @param filters a object of class CellbaseParam specifying the paramters limiting the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#' cb <- CellbaseQuery()
#' cbparam <- CellbaseParam(gene=c("TP73","TET1"))
#' res <- cbClinical(object=cb,filters=cbParam)
#' 
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbClinical", "CellbaseQuery",  definition = function(object,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "clinical"
  ids <- NULL
  resource <- "all"
  if(length(filters@genome)>0){
    genome <- paste0(filters@genome,collapse = ",")
    genome <- paste("genome=",genome,sep = "")
  }else{
    genome <- character()
  }
  if(length(filters@gene)>0){
    genes <- paste0(filters@gene,collapse = ",")
    genes <- paste("gene=",genes,sep = "")
  }else{
    gene <- character()
  }

  if(length(filters@region)>0){
    region <- paste0(filters@region,collapse = ",")
    region <- paste("region=",region,sep = "")
  }else{
    region <-character()
  }

  if(length(filters@rs)>0){
    rs <- paste0(filters@rs,collapse = ",")
    rs <- paste("rs=",rs,sep = "")
  }else{
    rs <- character()
  }
  if(length(filters@so)>0){
    so <- paste0(filters@so,collapse = ",")
    so <- paste("so=",so,sep = "")
  }else{
    so <- character()
  }

  if(length(filters@phenotype)>0){
    phenotype <- paste0(filters@phenotype,collapse = ",")
    phenotype <- paste("phenotype=",phenotype,sep = "")
  }else{
    phenotype <- character()
  }

  limit <- "limit=1000"
  filters <- c(genome=genome, genes=genes,region=region,rs=rs,so=so,phenotype=phenotype,limit=limit)
  filters <- paste(filters,collapse = "&")
  result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=filters,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbGene", function(object,ids,resource,filter, ...) standardGeneric("cbGene"))
#' A method to query gene data from Cellbase web services
#' 
#' @aliases cbGene
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbGene(object=cb, ids=c("TP73","TET1"), resource="clinical")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbGene", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "gene"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})


###
setGeneric("cbRegion", function(object,ids,resource,filters, ...) standardGeneric("cbRegion"))
#' A method to query region data from Cellbase web services
#' 
#' @aliases cbRegion
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbRegion(object=cb, ids="17:1000000-1200000", resource="gene")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbRegion", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "genomic"
  subcateg<- "region"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbSnp", function(object,ids,resource,filters, ...) standardGeneric("cbSnp"))
#' A method to query snp data from Cellbase web services
#' @aliases cbSnp
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbSnp(object=cb, ids="rs6025", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbSnp", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "snp"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbVariant", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbVariant"))
#' A method to query variant data from Cellbase web services
#' 
#' @aliases cbVariant
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbVariant(object=cb, ids="19:45411941:T:C", resource="annotation")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbVariant", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "genomic"
  subcateg<- "variant"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta =NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
###
setGeneric("cbTfbs", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbTfbs"))
#' A method to query transcription factors binding sites data from Cellbase web services
#' 
#' @aliases cbTfbs
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbTfbs(object=cb, ids="PAX1", resource="gene")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbTfbs", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "regulation"
  subcateg<- "tf"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

###
setGeneric("cbTranscript", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbTranscript"))
#' A method to query transcript data from Cellbase web services
#' @aliases cbTranscript
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried can be
#' any of "info", "function_prediction", "gene", "sequence", "variation", or "protein"
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbTranscript(object=cb, ids="ENST00000373644", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbTranscript", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "transcript"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})



###
setGeneric("cbXref", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbXref"))
#' A method to query cross reference data from Cellbase web services
#' 
#' @aliases cbXref
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried can be any of "xref", "gene", "starts"with", or "contains"
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbXref(object=cb, ids="ENST00000373644", resource="xref")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbXref", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "id"
  ids <- toupper(ids)
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
###
###
setGeneric("cbProtein", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbProtein"))
#' A method to query protein data from Cellbase web services
#' 
#' @aliases cbProtein
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of uniprot ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbProtein(object=cb, ids="O15350", resource="sequence")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbProtein", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "feature"
  subcateg<- "protein"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})


###
setGeneric("cbGenomeSequence", function(object,ids,resource,filters=NULL, ...) standardGeneric("cbGenomeSequence"))
#' A method to query sequence data from Cellbase web services
#' 
#' @aliases cbGenomeSequence
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @param ... any extra arguments
#' @return an object of class CellbaseResult which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbGenomeSequence(object=cb, ids="22", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbGenomeSequence", "CellbaseQuery",  definition = function(object,ids,resource,filters=NULL,...) {

  host <- object@host
  species <- object@species
  version <- object@version
  categ <- "genomic"
  subcateg<- "chromosome"
  ids <- ids
  resource <- resource
  result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})
# setGeneric("cbMeta", function(object,file=NULL,host=NULL, version=NULL, species, categ, subcateg,ids,resource,filters=NULL, ...) standardGeneric("cbMeta"))
# setMethod("cbMeta", "CellbaseQuery",  definition = function(object,file=NULL,host=NULL, version=NULL,meta=NULL, species=NULL, categ, subcateg,ids,resource,filters=NULL,...) {
#
#   host <- object@host
#   species <- species
#   version <- object@version
#   meta <- paste0("meta","/",sep="")
#   categ <- NULL
#   subcateg<- NULL
#   ids <- NULL
#   resource <- NULL
#   result <- fetchCellbase(file=NULL,host=host, version=version,meta = meta, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
#   data <- lapply(result, function(x)as.data.frame(x))
#   return(data)
# })

###
setGeneric("cbSpecies", function(object, ...) standardGeneric("cbSpecies"))
#' A method for getting the avaiable species from the cellbase web services
#' 
#' @aliases cbSpecies
#' @param object An object of class CellbaseQuery
#' @details A method for getting the avaiable species from the cellbase web services
#' @examples 
#' library(cellbaseR)
#'  cb <- CellbaseQuery()
#'  res <- cbSpecies(object=cb)
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
setMethod("cbSpecies", "CellbaseQuery",  definition = function(object,...) {

  host <- object@host
  species <- "species"
  version <- object@version
  meta <- "meta/"
  categ <- NULL
  subcateg<- NULL
  ids <- NULL
  resource <- NULL
  result <- fetchCellbase(file=NULL,host=host, version=version,meta = meta, species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,filters=NULL,...)
  data <- lapply(result, function(x)as.data.frame(x))
  return(data)
})

#######################################################################
#' A method to allow for updating the species of the CellbaseQuery config
setGeneric("species<-",
           function(object, value) standardGeneric("species<-"))
#' A method to allow for updating the species of the CellbaseQuery config
#' @param object An object of class CellbaseQuery
#' @param value a character specifying the desired species
setMethod("species<-","CellbaseQuery",function(object,value){
  value =paste0(value,"/",sep="")
  object@species <- value
  return(object)
})

#' A method to allow for updating the host of the CellbaseQuery config
#' @param object An object of class CellbaseQuery
#' @param value a character specifying the desired host

setGeneric("host<-",
           function(object, value) standardGeneric("host<-"))
setMethod("host<-","CellbaseQuery",function(object,value){
  value =paste0(value,"/",sep="")
  object@host <- value
  return(object)
})
#Need more work
setGeneric("annotateVcf", function(object,file, ...) standardGeneric("annotateVcf"))
#' This method is a convience method to annoatate a vcf files
#'
#' @details This methods is ideal for annotating small to medium sized vcf files
#' @param object An object of class CellbaseQuery
#' @param ... any extra arguments
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @return an annotated dataframe
#' @export
setMethod("annotateVcf", "CellbaseQuery",  definition = function(object,file,...) {
  host <- object@host
  version <- object@version
  species <- object@species
  batch_size <- object@batch_size
  num_threads <- object@num_threads
  categ <- "genomic"
  subcateg<- "variant"
  ids <- NULL
  resource <- "/annotation"
  result <- fetchCellbase(host, file=file,version,meta=NULL, species, categ, subcateg,ids=NULL,resource,filters=NULL,batch_size=batch_size,num_threads=num_threads)
  data <- CellbaseResult(cellbaseData=result)
  return(data)
})

#####################################################################################################
# The CellbaseResult class defintion
#' @export
CellbaseResult <- setClass("CellbaseResult",slots = c(cellbaseData="data.frame"))
#  The show method for CellbaseResult class
setMethod("show",signature = "CellbaseResult",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat(" containing ", nrow(object@cellbaseData), " rows and ",
      ncol(object@cellbaseData), " columns.\n", sep = "")
  cat(" to get the annotated dataframe use cellbaseData()")
})

# An accessor method to get CellbaseResult cellbaseData slot
setGeneric("cellbaseData", function(object, ...) standardGeneric("cellbaseData"))
#' A method to query Clinical data from Cellbase web services
#' @param object an object of class CellbaseQuery
#' @param ids a charcter vector of the ids to be queried
#' @param resource a charcter vector to specify the ids to be queried
#' @param filters a object of class CellbaseParam specifying additional filterss for the CellbaseQuery
#' @return an object of class CellbaseResult which holds a dataframe
#'
#' @export
setMethod("cellbaseData", "CellbaseResult",  definition = function(object) object@cellbaseData)
#' A method to allow for subsetting the cellbaseData

#' @export
setMethod("[","CellbaseResult",definition = function(x,i,j,drop="missing")
{
  .cellbaseData <- x@cellbaseData[i, j]
  CellbaseResult(cellbaseData = .cellbaseData)
})

#####################################################################################################
#'This Class defines a CellbaseParam object
#'
 setClass("CellbaseParam",slots = c(genome="character",gene="character",region="character"
,rs="character",so="character",phenotype="character"),
prototype = prototype(genome=character(0),gene=character(0),region=character(0),
        rs=character(0),so=character(0),phenotype=character(0)))
#' A constructor function for CellbaseParam
#'use the CellbaseParam object to control what results are returned from the CellbaseQuery methods
#' @param genome A character denoting the genome build to query,eg, GRCh37(default),or GRCh38
#' @param gene A charcter vector denoting the gene/s to be queried
#' @param region A charcter vector denoting the region/s to be queried must be in the form 1:100000-1500000 not chr1:100000-1500000
#' @param rs A charcter vector denoting the rs ids to be queried
#' @param so A charcter vector denoting sequence ontology to be queried
#' @param phenotype A charcter vector denoting the phenotype to be queried
#' @examples 
#' library(cellbaseR)
#' cbParam <- CellbaseParam(genome="GRCh38",gene=c("TP73","TET1"))
#' print(cbParam)
#' @seealso for more information about the cellbase webservices see 
#' \url{https://github.com/opencb/cellbase/wiki}
#' @export
CellbaseParam <- function(genome=character(),gene=character(),region=character(),rs=character(),so=character(),phenotype=character()){
  new("CellbaseParam",genome=genome,gene=gene,region=region,rs=rs,so=so,phenotype=phenotype)

}



# Show method
setMethod("show",signature = "CellbaseParam",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat("use this object to control what results are returned from the CellbaseQuery methods")
})
