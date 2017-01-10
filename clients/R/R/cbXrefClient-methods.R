########################################################################################################################
#' A method to query cross reference data from Cellbase web services.
#' @details Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbXrefClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, any crossrefereable ID, gene names, transcript ids, 
#' uniprot ids,etc.
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- cbXrefClient(object=cb, ids="ENST00000373644", resource="xref")
#' @export
setMethod("cbXrefClient", "CellBaseR", definition = function(object, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "id"
    ids <- toupper(ids)
    resource <- resource
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ, subcateg=subcateg,
                            ids=ids, resource=resource, filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
