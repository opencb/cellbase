#' A method to query transcription factors binding sites data from Cellbase
#' web services
#'
#' @aliases cbTfbsClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid
#' transcription factor name, for example, Pdr1, and Oaf1
#' @param resource a character vector to specify the ids to be queried

#' @param filters a object of class CellBaseParam specifying additional filters
#' for the CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbTfbsClient(object=cb, ids="PAX1", resource="gene")
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbTfbsClient", "CellBaseR",    definition = function(object, ids,
resource, filters=NULL,...) {

    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "regulation"
    subcateg<- "tf"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
