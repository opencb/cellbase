#' A method to query Clinical data from Cellbase web services
#'
#' A method to query Clinical data from Cellbase web services
#' @aliases cbClinicalClientClient
#' @param object an object of class CellBaseR
#' @param filters a object of class CellBaseParam specifying the paramters
#' limiting the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#' library(cellbaseR)
#' cb <- CellBaseR()
#' cbParam <- CellBaseParam(gene=c("TP73","TET1"))
#' res <- cbClinicalClient(object=cb,filters=cbParam)
#'
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbClinicalClient", "CellBaseR",    definition = function(object,
filters=NULL,...) {
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
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource
    , filters=filters,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
