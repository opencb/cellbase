require(methods)
#' @include cellbase_functions.R
###
#' @title This class defines the CellnBaseR object
#' 
#' @details This S4 class holds the default configuration required by
#' CellBaseR methods to connect to the cellbase web services.
#' By default it is configured to query human. Please, visit
#' https://github.com/opencb/cellbase/wiki and
#' bioinfodev.hpc.cam.ac.uk/cellbase/webservices/ for more details on
#' following parameters.
#' data based on the GRCh37 genome assembly
#' @slot host a character specifying the host url. Default "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @slot version a character specifying the API version. Default "v4"
#' @slot species a character specifying the species to be queried. Default "hsapiens"
#' @slot batch_size if multiple queries are raised by a single method call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size of these batches. Default 200
#' @slot num_threads the number of threads. Default 8
    setClass("CellBaseR",
    slots = c(host="character", version="character", species="character",
    batch_size="numeric", num_threads="numeric"), prototype = prototype(
    host="http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/"
    , version = "v4/", species="hsapiens/", batch_size=200, num_threads=8)
    )
### CellbaseR constructor function
#' @aliases CellBaseR
#' @title 
#' A constructor function for CellBaseR object
#' @details
#' This class defines the CellBaseR object. It holds the default
#' configuration required by CellBaseR methods to connect to the
#' cellbase web services. By defult it is configured to query human
#' data based on the GRCh37 genome assembly. Please, visit
#' https://github.com/opencb/cellbase/wiki and
#' bioinfodev.hpc.cam.ac.uk/cellbase/webservices/ for more details on
#' following parameters.
#' @param species A character should be     a species supported by cellbase
#' run cbSpeciesClient to see avaiable species and their corresponding data
#' @import methods 
#' @param  host A character the default host url for cellbase webservices,
#' e.g. "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @param  version A character the cellbae API version, e.g. "V4"
#' @param  species a character specifying the species to be queried, e.g. "hsapiens"
#' @param  batch_size if multiple queries are raised by a single method call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size of these batches, e.g. 200
#' @return An object of class CellBaseR
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    print(cb)
#' @export 
CellBaseR <- function(species=character()){
    if(length(species)>0){
        species<-paste0(species,"/",sep="")
    }else{
        species <-"hsapiens/" 
    }
    new("CellBaseR", species=species )
}

##### Methods for CellBaseR objects
#    The show method for CellBaseR class
setMethod("show",signature = "CellBaseR",definition = function(object){
cat("An object of class ", class(object), "\n", sep = "")
cat("| it holds the configuration for querying the Cellbase databases\n")
cat("| to change the default species from human use CellBaseR(species='')")
})
#' The generic method for getCellbase
#' 
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
setGeneric("getCellbase", function(object, file=NULL,categ, subcateg,ids,
    resource,filters=NULL, ...) standardGeneric("getCellbase"))
#' The generic method for getCellbase
#' 
#' @aliases getCellbase
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
#' @param object an object of class CellBaseR
#' @param file a path to a bgzipped and tabix indexed vcf file,
#' @param category character to specify the category to be queried
#' this could be "feature", "genomic", "regulatory", or "network"
#' @param subcategory character to specify the subcategory to be queried
#' @param ids a character vector of the ids to be queried
#' @param resource a character to specify the resource to be queried
#' @param filters an object of class CellBaseParam specifying additional 
#' filterss for the CellBaseR
#' @param ... any extra arguments
#' 
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR
#'    res <- getCellbase(object=cb,categ="feature",subcateg="gene",ids="TET1",
#'    resource="info")
#'
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getCellbase", "CellBaseR", definition = function(object, 
    file=NULL, category, subcategory, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- categ
    subcateg<- subcateg
    ids <- ids
    resource <- resource
    filters <- c(genome=genome, gene=gene,region=region,rs=rs,so=so, 
                 phenotype=phenotype,limit=limit, include=include, 
                 exclude=exclude, limit=limit)
    filters <- paste(filters, collapse = "&")
    result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL, 
    species=species, categ=categ, subcateg=subcateg,ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
##############
##############
#############
# cbClinicalClientClient
setGeneric("cbClinicalClient", function(object, filters,...)
    standardGeneric("cbClinicalClient"))
#' A method to query Clinical data from Cellbase web services
#' 
#' A method to query Clinical data from Cellbase web services
#' @aliases cbClinicalClientClient
#' @param object an object of class CellBaseR
#' @param filters a object of class CellBaseParam specifying the paramters 
#' limiting the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#' cb <- CellBaseR()
#' cbParam <- CellBaseParam(gene=c("TP73","TET1"))
#' res <- cbClinicalClient(object=cb,filters=cbParam)
#' 
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbClinicalClient", "CellBaseR",    definition = function(object, 
    filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "clinical"
    ids <- NULL
    resource <- "all"
    
    filters <- c(genome=filters@genome, gene=filters@gene,
    region=filters@region,rs=filters@rs,so=filters@so, 
    phenotype=filters@phenotype, include=filters@include,
    exclude=filters@exclude, limit=filters@limit)
    filters <- paste(filters, collapse = "&")
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource
    , filters=filters,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})

###
setGeneric("cbGeneClient", function(object,ids,resource,filter, ...)
    standardGeneric("cbGeneClient"))
#' A method to query gene data from Cellbase web services
#' 
#' @aliases cbGeneClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried
#' @param resource a character vector to specify the ids to be queried

#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#' cb <- CellBaseR()
#' res <- cbGeneClient(object=cb, ids=c("TP73","TET1"), resource="clinical")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbGeneClient", "CellBaseR", definition = function(object,ids,resource
    , filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "gene"
    ids <- ids
    resource <- resource
    filters <- c(genome=genome, gene=gene,region=region,rs=rs,so=so, 
                 phenotype=phenotype,limit=limit, include=include, 
                 exclude=exclude, limit=limit)
    filters <- paste(filters, collapse = "&")
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource, 
    filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})


###
setGeneric("cbRegionClient", function(object,ids,resource,filters, ...)
    standardGeneric("cbRegionClient"))
#' A method to query region data from Cellbase web services
#' 
#' @aliases cbRegionClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, for example, 
#' "1:1000000-1200000' should always be in the form 'chr:start-end'
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbRegionClient(object=cb, ids="17:1000000-1200000", resource="gene")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbRegionClient", "CellBaseR",    definition = function(object, ids, 
    resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "region"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})

###
setGeneric("cbSnpClient", function(object,ids,resource,filters, ...)
    standardGeneric("cbSnpClient"))
#' A method to query snp data from Cellbase web services
#' @aliases cbSnpClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid rsid,
#' for example 'rs6025'
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbSnpClient(object=cb, ids="rs6025", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbSnpClient", "CellBaseR",    definition = function(object, ids, 
    resource, filters=NULL,...) {

    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "snp"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})

###
setGeneric("cbVariantClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbVariantClient"))
#' A method to query variant data from Cellbase web services
#' 
#' @aliases cbVariantClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be in the 
#' following format 'chr:start:ref:alt', for example, '1:128546:A:T'
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbVariantClient(object=cb, ids="19:45411941:T:C", resource="annotation")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbVariantClient", "CellBaseR",    definition = function(object, ids, 
    resource, filters=NULL,...) {

    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "variant"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource, 
    filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
###
setGeneric("cbTfbsClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbTfbsClient"))
#' A method to query transcription factors binding sites data from Cellbase 
#' web services
#' 
#' @aliases cbTfbsClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid 
#' transcription factor name, for example, Pdr1, and Oaf1
#' @param resource a character vector to specify the ids to be queried

#' @param filters a object of class CellBaseParam specifying additional filters
#' for the CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbTfbsClient(object=cb, ids="PAX1", resource="gene")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbTfbsClient", "CellBaseR",    definition = function(object, ids, 
    resource, filters=NULL,...) {

    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "regulation"
    subcateg<- "tf"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})

###
setGeneric("cbTranscriptClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbTranscriptClient"))
#' A method to query transcript data from Cellbase web services
#' @aliases cbTranscriptClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, for example,
#' ensemble transccript ID, like ENST00000380152
#' @param resource a character vector to specify the resource to be queried can
#'    be any of "info", "function_prediction", "gene", "sequence", "variation"
#'    , or "protein"
#' @param filters a object of class CellBaseParam specifying additional filters
#' for the CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbTranscriptClient(object=cb, ids="ENST00000373644", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbTranscriptClient", "CellBaseR",    definition = function(object, 
      ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "transcript"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL, host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})



###
setGeneric("cbXrefClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbXrefClient"))
#' A method to query cross reference data from Cellbase web services
#' 
#' @aliases cbXrefClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, any crossrefereable 
#' ID, gene names, transcript ids, uniprot ids,etc.
#' @param resource a character vector to specify the ids to be queried can 
#' be any of "xref", "gene", "starts"with", or "contains"
#' @param filters a object of class CellBaseParam specifying additional filters
#' for the CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbXrefClient(object=cb, ids="ENST00000373644", resource="xref")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbXrefClient", "CellBaseR",    definition = function(object, ids, 
    resource,filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "id"
    ids <- toupper(ids)
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
   , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
###
###
setGeneric("cbProteinClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbProteinClient"))
#' A method to query protein data from Cellbase web services
#' 
#' @aliases cbProteinClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of uniprot ids to be queried, should be one
#' or more of uniprot ids, for example O15350.
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbProteinClient(object=cb, ids="O15350", resource="sequence")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbProteinClient", "CellBaseR",    definition = function(object, ids, 
    resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "protein"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})


###
setGeneric("cbChromosomeInfoClient", function(object,ids,resource,filters=NULL, ...)
    standardGeneric("cbChromosomeInfoClient"))
#' A method to query sequence data from Cellbase web services
#' 
#' @aliases cbChromosomeInfoClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#' for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbChromosomeInfoClient(object=cb, ids="22", resource="info")
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbChromosomeInfoClient", "CellBaseR",    definition = function(object
    , ids, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "chromosome"
    ids <- ids
    resource <- "info"
    result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL,
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
# setGeneric("cbMeta", function(object,file=NULL,host=NULL, version=NULL, 
# species, categ, subcateg,ids,resource,filters=NULL, ...)
# standardGeneric("cbMeta"))
# setMethod("cbMeta", "CellBaseR",    definition=function(object, file=NULL
#   , host=NULL, version=NULL,meta=NULL, species=NULL, categ, subcateg, ids, 
# resource, filters=NULL,...) {
#     host <- object@host
#     species <- species
#     version <- object@version
#     meta <- paste0("meta","/",sep="")
#     categ <- NULL
#     subcateg<- NULL
#     ids <- NULL
#     resource <- NULL
#     result <- fetchCellbase(file=NULL,host=host, version=version,meta=meta, 
# species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource,
# filters=NULL,...)
#     data <- lapply(result, function(x)as.data.frame(x))
#     return(data)
# })

###
setGeneric("cbSpeciesClient", function(object, ...) standardGeneric("cbSpeciesClient"))
#' A method for getting the avaiable species from the cellbase web services
#' 
#' @aliases cbSpeciesClient
#' @param object An object of class CellBaseR
#' @details A method for getting the avaiable species from the cellbase 
#' web services
#' @examples 
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbSpeciesClient(object=cb)
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbSpeciesClient", "CellBaseR",    definition = function(object,...){
    host <- object@host
    species <- "species"
    version <- object@version
    meta <- "meta/"
    categ <- NULL
    subcateg<- NULL
    ids <- NULL
    resource <- NULL
    result <- fetchCellbase(file=NULL,host=host, version=version,meta=meta, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- lapply(result, function(x)as.data.frame(x))
    data <- rbind.pages(data)
    data <- CellBaseResponse(cbData=data)
    return(data)
})

##############################################################################
setGeneric("cbAnnotateVcf", function(object,file, ...)
    standardGeneric("cbAnnotateVcf"))
#' This method is a convience method to annoatate a vcf files
#' @aliases cbAnnotateVcf
#' @details This methods is ideal for annotating small to medium sized vcf
#'    files
#' @param object An object of class CellBaseR
#' @param ... any extra arguments
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @return an annotated dataframe
#' @export
setMethod("cbAnnotateVcf", "CellBaseR",    definition = function(object, file
    , ...) {
    # host <- object@host
    # version <- object@version
    # species <- object@species
    # batch_size <- object@batch_size
    # num_threads <- object@num_threads
    # categ <- "genomic"
    # subcateg<- "variant"
    # ids <- NULL
    # resource <- "/annotation"
    # result <- fetchCellbase(host, file=file,version,meta=NULL, species, categ, 
    # subcateg, ids=NULL, resource, filters=NULL, batch_size=batch_size, 
    # num_threads=num_threads)
    result <- Annovcf(object=object, file=file, batch_size, num_threads )
    data <- CellBaseResponse(cbData=result)
    return(data)
})

##############################################################################
#' @title The CellBaseResponse class defintion
#' @details This class stores the CellBaseR query methods, an object of
#' class CellBaseResponse is automatically generated when you call any of
#' CellbaseR methods
#' @slot cbData an R dataframe
#' @export
CellBaseResponse <-setClass("CellBaseResponse", slots=c(cbData="data.frame"))
#    The show method for CellBaseResponse class
setMethod("show",signature = "CellBaseResponse", definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat(" containing ", nrow(object@cbData), " rows and ",
            ncol(object@cbData), " columns.\n", sep = "")
    cat(" to get the annotated dataframe use cbData()")
})

# An accessor method to get CellBaseResponse cbData slot cbData

setGeneric("cbData", function(object, ...)
    standardGeneric("cbData"))
# An accessor method to get CellBaseResponse cbData slot
#' @title An accessor method to get CellBaseResponse cbData slot
#' @aliases cbData
#' @param object an object of class CellBaseResponse
#' @return a dataframe
#'
#' @export
setMethod("cbData", "CellBaseResponse",    definition = function(object)
    object@cbData)
##
setMethod("[","CellBaseResponse",definition = function(x,i,j,drop="missing")
{
    .cbData <- x@cbData[i, j]
    CellBaseResponse(cbData = .cbData)
})

##############################################################################
#'This Class defines a CellBaseParam object
#'CellBaseParam
#'
#' @slot genome A character denoting the genome build to query,eg, GRCh37
#' (default),or GRCh38
#' @slot gene A character vector denoting the gene/s to be queried
#' @slot  region A character vector denoting the region/s to be queried must be 
#' in the form 1:100000-1500000 not chr1:100000-1500000
#' @slot rs A character vector denoting the rs ids to be queried
#' @slot  so A character vector denoting sequence ontology to be queried
#' @slot  phenotype A character vector denoting the phenotype to be queried
#' @slot  include A character vector denoting the fields to be returned
#' @slot  exclude A character vector denoting the fields to be excluded
#' @slot  limit A number limiting the number of results to be returned
    setClass("CellBaseParam",slots = c(genome="character", gene="character", 
    region="character", rs="character", so="character", phenotype="character", 
    include ="character", exclude = "character", limit="character"),
    prototype = prototype(genome=character(0),gene=character(0), 
    region=character(0), rs=character(0), so=character(0), 
    phenotype=character(0),
    include=character(),
    exclude=character(),
    limit="1000"
    ))
#' A constructor function for CellBaseParam
#' 
#' @details 
#'use the CellBaseParam object to control what results are returned from the 
#'CellBaseR methods
#' @param genome A character denoting the genome build to query,eg, GRCh37
#' (default),or GRCh38
#' @param gene A character vector denoting the gene/s to be queried
#' @param region A character vector denoting the region/s to be queried must be 
#' in the form 1:100000-1500000 not chr1:100000-1500000
#' @param rs A character vector denoting the rs ids to be queried
#' @param so A character vector denoting sequence ontology to be queried
#' @param phenotype A character vector denoting the phenotype to be queried
#' @param include A character vector denoting the fields to be returned
#' @param exclude A character vector denoting the fields to be excluded
#' @param limit A number limiting the number of results to be returned
#' @examples 
#' library(cellbaseR)
#' cbParam <- CellBaseParam(genome="GRCh38",gene=c("TP73","TET1"))
#' print(cbParam)
#' @seealso for more information about the cellbase webservices see 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
CellBaseParam <- function(genome=character(), gene=character(), 
    region=character(), rs=character(), so=character(), phenotype=character(),
    include=character(), exclude=character(), limit=character()){
  
  if(length(genome)>0){
    genome <- paste0(genome,collapse = ",")
    genome <- paste("genome=",genome,sep = "")
  }else{
    genome <- character()
  }
  if(length(gene)>0){
    gene <- paste0(gene,collapse = ",")
    gene <- paste("gene=",gene,sep = "")
  }else{
    gene <- character()
  }
  
  if(length(region)>0){
    region <- paste0(region,collapse = ",")
    region <- paste("region=",region,sep = "")
  }else{
    region <-character()
  }
  
  if(length(rs)>0){
    rs <- paste0(rs,collapse = ",")
    rs <- paste("rs=",rs,sep = "")
  }else{
    rs <- character()
  }
  if(length(so)>0){
    so <- paste0(so,collapse = ",")
    so <- paste("so=",so,sep = "")
  }else{
    so <- character()
  }
  
  if(length(phenotype)>0){
    phenotype <- paste0(phenotype,collapse = ",")
    phenotype <- paste("phenotype=",phenotype,sep = "")
  }else{
    phenotype <- character()
  }
  
  if(length(include)>0){
    include <- paste0(include,collapse = ",")
    include <- paste("include=",include,sep = "")
  }else{
    include <- character()
  }
  
  if(length(exclude)>0){
    exclude <- paste0(exclude,collapse = ",")
    exclude <- paste("exclude=",exclude,sep = "")
  }else{
    exclude <- character()
  }  
  if(length(limit)>0){
    limit=limit
    limit=paste("limit=", limit, sep="")
  }else{
    limit=paste("limit=", 1000, sep="")
  }
  
  
    
    new("CellBaseParam", genome=genome, gene=gene, region=region, rs=rs, so=so,
    phenotype=phenotype, include=include, exclude=exclude, limit=limit)

}



# Show method
setMethod("show",signature = "CellBaseParam",definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat("use this object to control what results are returned from the
    CellBaseR methods")
})
