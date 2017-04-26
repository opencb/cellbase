###############################################################################
#' getChromosomeInfo
#' 
#' A method to query sequence data from Cellbase web services.
#' @details A method to query sequence data from Cellbase web services. This 
#' method retrieves information about chromosomes, including its size and
#' detailed information about its different cytobands   
#' @aliases getChromosomeInfo
#' @param object an object of class CellBaseR
#' @param ids a character vector of chromosome ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param param a object of class CellBaseParam specifying additional param for
#'  the query
#' @return  a dataframe with the results of the query
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @examples
#'    cb <- CellBaseR()
#'    res <- getChromosomeInfo(object=cb, ids="22", resource="info")
#' @export
setMethod("getChromosomeInfo", "CellBaseR", 
          definition = function(object , ids, resource, param=NULL) {
    
    categ <- "genomic"
    subcateg<- "chromosome"
    ids <- ids
    resource <- "info"
    result <- fetchCellbase(object=object, file=NULL, meta = NULL,
    categ=categ, subcateg=subcateg, ids=ids, resource=resource 
    , param=NULL)
    data <- result[[1]][[1]]
    return(data)
})
