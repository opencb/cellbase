#' A method for getting the avaiable species from the cellbase web services
#'
#' @aliases cbSpeciesClient
#' @param object An object of class CellBaseR
#' @details A method for getting the avaiable species from the cellbase
#' web services
#' @examples
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbSpeciesClient(object=cb)
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbSpeciesClient", "CellBaseR",    definition = function(object,...){
    host <- object@host
    species <- "species"
    version <- object@version
    meta <- "meta/"
    categ <- NULL
    subcateg<- NULL
    ids <- NULL
    resource <- NULL
    result <- fetchCellbase(file=NULL,host=host, version=version,meta=meta,
    species=species, categ=categ, subcateg=subcateg, ids=ids, resource=resource
    , filters=NULL,...)
    data <- lapply(result, function(x)as.data.frame(x))
    data <- rbind.pages(data)
    data <- CellBaseResponse(cbData=data)
    return(data)
})
