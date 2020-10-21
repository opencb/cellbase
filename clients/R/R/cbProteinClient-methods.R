########################################################################################################################
#' A method to query protein data from Cellbase web services.
#' @details  Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbProteinClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of uniprot ids to be queried, should be one or more of uniprot ids, for example O15350.
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- cbProteinClient(object=cb, ids="O15350", resource="info")
#' @export
setMethod("cbProteinClient", "CellBaseR", definition = function(object, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "protein"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ, 
                            subcateg=subcateg, ids=ids, resource=resource, filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
