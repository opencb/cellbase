########################################################################################################################
#' A method to query sequence data from Cellbase web services.
#' @details  Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbChromosomeInfoClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of chromosome ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbChromosomeInfoClient(object=cb, ids="22", resource="info")
#' @export
setMethod("cbChromosomeInfoClient", "CellBaseR", 
          definition = function(object , ids, filters=NULL,...) {
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
    data <- result[[1]][[1]]
    data <- CellBaseResponse(cbData=data)
    return(data)
})
