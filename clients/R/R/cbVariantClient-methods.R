########################################################################################################################
#' A method to query variant annotation data from Cellbase web services from Cellbase web services.
#' @details Please, for details on possible values for the parameters  and  additional filters of this function refer to
#' https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbVariantClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be in the following format 'chr:start:ref:alt', for 
#' example, '1:128546:A:T'
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- cbVariantClient(object=cb, ids="19:45411941:T:C", resource="annotation")
#' @export
setMethod("cbVariantClient", "CellBaseR", definition = function(object, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "variant"
    ids <- ids
    resource <- resource
    result <- fetchCellbase(file=NULL, host=host, version=version, meta=NULL, 
                            species=species, categ=categ,  subcateg=subcateg,
                            ids=ids, resource=resource, filters=NULL,...)

    data <- CellBaseResponse(cbData=result)
    return(data)
})
