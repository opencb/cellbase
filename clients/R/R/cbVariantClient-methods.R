#' A method to query variant data from Cellbase web services
#'
#' @aliases cbVariantClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be in the
#' following format 'chr:start:ref:alt', for example, '1:128546:A:T'
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbVariantClient(object=cb, ids="19:45411941:T:C", resource="annotation")
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbVariantClient", "CellBaseR",    definition = function(object, ids,
resource, filters=NULL,...) {

    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "variant"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,
    filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
