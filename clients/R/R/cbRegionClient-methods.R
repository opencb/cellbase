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
