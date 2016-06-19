########################################################################################################################
#' A method to query gene data from Cellbase web services.
#' @details Please, for details on possible values for the parameters 
#' and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API 
#' documentation  http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbGeneClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of gene ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param filters an object of class CellBaseParam specifying additional filters for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbGeneClient(object=cb, ids=c("TP73","TET1"), resource="info")
#' @export
setMethod("cbGeneClient", "CellBaseR", definition = function(object, ids, resource, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "gene"
    ids <- ids
    resource <- resource
    if (!is.null(filters)) {
        filters <- c(genome=filters@genome, gene=filters@gene,region=filters@region,rs=filters@rs,so=filters@so,
                     phenotype=filters@phenotype,limit=filters@limit, include=filters@include, exclude=filters@exclude,
                     limit=filters@limit)
        filters <- paste(filters, collapse = "&")
    }
    # TODO: filters are not enabled
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ,
                            subcateg=subcateg,ids=ids,resource=resource, filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
