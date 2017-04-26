###############################################################################
#' getVariant
#' 
#' A method to query variant annotation data from Cellbase web services from
#'  Cellbase web services.
#' @details This method retrieves extensive genomic annotations for variants
#' including consequence types, conservation data, population frequncies from 1k
#'  genomes and Exac projects, etc.
#' as well as clinical data and various other annotations
#' @aliases getVariant
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be in the 
#' following format 'chr:start:ref:alt', for 
#' example, '1:128546:A:T'
#' @param resource a character vector to specify the resource to be queried
#' @param param a object of class CellBaseParam specifying additional param
#'  for the query
#' @return a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- getVariant(object=cb, ids="19:45411941:T:C", resource="annotation")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getVariant", "CellBaseR", definition = function(object, ids, 
                                                           resource, 
                                                           param=NULL) {
    categ <- "genomic"
    subcateg<- "variant"
    ids <- ids
    resource <- resource
    if(object@species!='hsapiens'&resource=='cadd'){
      stop('cadd scores are only avaialable for hsapiens')
    }
    if (!is.null(param)) {
      param <- c(genome=param@genome, gene=param@gene,
                   region=param@region, rs=param@rs,so=param@so,
                   phenotype=param@phenotype, limit=param@limit, 
                   include=param@include, exclude=param@exclude,
                   limit=param@limit)
      param <- paste(param, collapse = "&")
    }
    result <- fetchCellbase(object=object, file=NULL, meta=NULL, 
                            categ=categ,  subcateg=subcateg,
                            ids=ids, resource=resource, param=param)

    return(result)
})
