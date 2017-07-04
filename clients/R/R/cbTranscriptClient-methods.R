########################################################################################################################
#' A method to query transcript data from Cellbase web services.
#' @details  Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbTranscriptClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the transcript ids to be queried, for example, ensemble transccript ID
#' like ENST00000380152
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- cbTranscriptClient(object=cb, ids="ENST00000373644", resource="info")
#' @export
setMethod("cbTranscriptClient", "CellBaseR", definition = function(object, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "transcript"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL, host=host, version=version, meta=NULL, species=species, categ=categ, subcateg=subcateg,
                            ids=ids, resource=resource, filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
