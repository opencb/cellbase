###############################################################################
#' getMeta
#' 
#' A method for getting the available metadata from the cellbase web services
#'
#' @details This method is for getting information about the avaialable species
#' and available annotation, assembly for each species from the cellbase web 
#' services.
#' @aliases getMeta
#' @param object an object of class CellBaseR
#' @param resource the resource you want to query it metadata
#' @return a dataframe with the
#'  results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- getMeta(object=cb, resource="species")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getMeta", "CellBaseR",    definition = function(object, resource){
    # host <- object@host
    object@species <- ""
    meta <- "meta/"
    categ <- NULL
    subcateg<- NULL
    ids <- NULL
    resource <- resource
    result <- fetchCellbase(object=object, file=NULL, meta=meta, categ=categ,
                            subcateg=subcateg,
                            ids=ids, resource=resource, param=NULL)
    data <- lapply(result, function(x)as.data.frame(x))
    result <- rbind.pages(data)
    return(result)
})
