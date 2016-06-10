########################################################################################################################
#' A method to query Clinical data from Cellbase web services.
#' @details  Please, for details on possible values for the parameters 
#' and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API 
#' documentation  http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbClinicalClient
#' @param object an object of class CellBaseR
#' @param filters a object of class CellBaseParam specifying the parameters limiting the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    cbParam <- CellBaseParam(gene=c("TP73","TET1"))
#'    res <- cbClinicalClient(object=cb,filters=cbParam)
#' @export
########################################################################################################################
setMethod("cbClinicalClient", "CellBaseR", definition = function(object, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "clinical"
    ids <- NULL
    resource <- "search"

    filters <- c(genome=filters@genome, gene=filters@gene,
    region=filters@region,rs=filters@rs,so=filters@so,
    phenotype=filters@phenotype, include=filters@include,
    exclude=filters@exclude, limit=filters@limit)
    filters <- paste(filters, collapse = "&")
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ, 
                            subcateg=subcateg,ids=ids,resource=resource, filters=filters,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
