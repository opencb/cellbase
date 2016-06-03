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
