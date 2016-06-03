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
