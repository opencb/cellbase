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
    data <- result$result[[1]]
    data <- CellBaseResponse(cbData=data)
    
    return(data)
})
