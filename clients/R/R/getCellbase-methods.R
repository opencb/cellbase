########################################################################################################################
#' The generic method for getCellbase. This method allows the user to query the cellbase web services without any 
#' predefined categories, subcategries, or resources. Please, for details on possible values for the parameters and 
#' additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases getCellbase
#' @param object an object of class CellBaseR
#' @param category character to specify the category to be queried.
#' @param subcategory character to specify the subcategory to be queried
#' @param ids a character vector of the ids to be queried
#' @param resource a character to specify the resource to be queried
#' @param filters an object of class CellBaseParam specifying additional filters for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR
#'    res <- getCellbase(object=cb,categ="feature",subcateg="gene",ids="TET1",
#'    resource="info")
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getCellbase", "CellBaseR", definition = function(object, category, subcategory, ids, resource, 
                                                            filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- categ
    subcateg<- subcateg
    ids <- ids
    resource <- resource
    filters <- c(genome=genome, gene=gene,region=region,rs=rs,so=so,
    phenotype=phenotype,limit=limit, include=include,
    exclude=exclude, limit=limit)
    filters <- paste(filters, collapse = "&")
    result <- fetchCellbase(file=NULL,host=host, version=version, meta = NULL, species=species, categ=categ, 
                            subcateg=subcateg,ids=ids, resource=resource , filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
