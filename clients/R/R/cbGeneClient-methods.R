#' A method to query gene data from Cellbase web services
#'
#' @aliases cbGeneClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried
#' @param resource a character vector to specify the ids to be queried
#' @param filters a object of class CellBaseParam specifying additional filters
#'    for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#' library(cellbaseR)
#' cb <- CellBaseR()
#' res <- cbGeneClient(object=cb, ids=c("TP73","TET1"), resource="clinical")
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("cbGeneClient", "CellBaseR", definition = function(object,ids,resource
, filters=NULL,...) {
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "feature"
    subcateg<- "gene"
    ids <- ids
    resource <- resource
    if (!is.null(filters)) {
        filters <- c(genome=filters@genome, gene=filters@gene,region=filters@region,rs=filters@rs,so=filters@so,
        phenotype=filters@phenotype,limit=filters@limit, include=filters@include,
        exclude=filters@exclude, limit=filters@limit)
        filters <- paste(filters, collapse = "&")
    }
    # TODO: filters are not enabled
    result <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL,
    species=species, categ=categ, subcateg=subcateg,ids=ids,resource=resource,
    filters=NULL,...)
    data <- CellBaseResponse(cbData=result)
    return(data)
})
