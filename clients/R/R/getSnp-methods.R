###############################################################################
#' getSnp
#' 
#' A method to query genomic variation data from Cellbase web services. 
#' @details .
#' @details  This method retrieves known genomic variants (snps) and their
#' annotations including population frequncies from 1k genomes and Exac projects
#' as well as clinical data and various other annotations
#' @aliases getSnp
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid rsid,
#' for example 'rs6025'
#' @param resource a character vector to specify the resource to be queried
#' @param param a object of class CellBaseParam specifying additional param
#'  for the query
#' @return a dataframe with the results of the query
#' @examples
#' cb <- CellBaseR()
#' res <- getSnp(object=cb, ids="rs6025", resource="info")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getSnp", "CellBaseR", definition = function(object, ids, resource,
                                                       param=NULL) {
    categ <- "feature"
    subcateg<- "variation"
    ids <- ids
    resource <- resource
    if (!is.null(param)) {
      param <- c(genome=param@genome, gene=param@gene,
                   region=param@region, rs=param@rs,so=param@so,
                   phenotype=param@phenotype, limit=param@limit, 
                   include=param@include, exclude=param@exclude,
                   limit=param@limit)
      param <- paste(param, collapse = "&")
    }
    result <- fetchCellbase(object=object, file=NULL, meta=NULL, categ=categ,
                            subcateg=subcateg, ids=ids, resource=resource,
                            param=param)
    return(result)
})
