################################################################################
#' This class defines the CellBaseR object
#' 
#' This is an S4 class  which defines the CellBaseR object
#' @details This S4 class holds the default configuration required by CellBaseR 
#' methods to connect to the cellbase web 
#' services. By default it is configured to query human data based on the GRCh37
#' genome assembly. Please, visit https://github.com/opencb/cellbase/wiki and 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/ for more details on
#' following parameters.
#' @slot host a character specifying the host url. Default 
#' "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @slot version a character specifying the API version. Default "v4"
#' @slot species a character specifying the species to be queried. Default
#'  "hsapiens"
#' @slot batch_size if multiple queries are raised by a single method call, e.g.
#'  getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size 
#' of these batches. Default 200
#' @slot num_threads the number of threads. Default 8
#' @export
setClass("CellBaseR", 
         slots = c(host="character", version="character", species="character", 
                   batch_size="numeric", num_threads="numeric"),
         prototype = prototype(host="http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/",
                               version = "v4/",species="hsapiens/", batch_size=200, num_threads=8)
)

################################################################################
#' The CellBaseResponse class defintion
#'  
#' This class holds the response data from CellBaseR Methods

#' @details This class stores a response of CellBaseR query methods. An object 
#' of class CellBaseResponse is automatically generated when you call any of 
#' CellbaseR methods.
#' @slot cbData an R dataframe which contains the result field within the 
#' response object returned by CellBase web services.
#' @export
CellBaseResponse <-setClass("CellBaseResponse", slots=c(cbData="data.frame"))

###############################################################################
#' This Class defines a CellBaseParam object
#' 
#' This class  defines a CellBaseParam object to hold filtering parameters
#' @details This class stores filtering parameters to be used by CellBaseR query
#'  methods. Not all the slots will be
#' used by all query methods, please have a look at 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/ and the 
#' Reference Manual for more information.
#' @slot genome A character the genome build to query, e.g.GRCh37(default)
#' @slot gene A character vector denoting the gene/s to be queried
#' @slot region A character vector denoting the region/s to be queried must be 
#' in the form 1:100000-1500000
#' @slot rs A character vector denoting the rs ids to be queried
#' @slot so A character vector denoting sequence ontology to be queried
#' @slot phenotype A character vector denoting the phenotype to be queried
#' @slot include A character vector denoting the fields to be returned
#' @slot exclude A character vector denoting the fields to be excluded
#' @slot limit A number limiting the number of results to be returned
#' @export
setClass("CellBaseParam",slots = c(genome="character", gene="character", 
                                   region="character", rs="character", 
                                   so="character", phenotype="character", 
                                   include ="character", exclude = "character", 
                                   limit="character"), 
                                  prototype = prototype(genome=character(0),
                                                        gene=character(0),
                                                        region=character(0), 
                                                        rs=character(0), 
                                                        so=character(0), 
                                                        phenotype=character(0),
                                                        include=character(),
                                                        exclude=character(),
                                                        limit="1000"))
