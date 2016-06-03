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
