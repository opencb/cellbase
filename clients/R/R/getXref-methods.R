###############################################################################
#' getXref
#' 
#' A method to query cross reference data from Cellbase web services.
#' @details This method retrieves cross references for genomic identifiers, eg
#' ENSEMBL ids, it also provide starts_with service that is useful for
#' autocomplete services.
#' @aliases getXref
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, any crossrefereable
#'  ID, gene names, transcript ids, 
#' uniprot ids,etc.
#' @param resource a character vector to specify the resource to be queried
#' @param param a object of class CellBaseParam specifying additional param
#'  for the query
#' @return a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- getXref(object=cb, ids="ENST00000373644", resource="xref")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getXref", "CellBaseR", definition = function(object, ids, resource,
                                                        param=NULL) {
    
    categ <- "feature"
    subcateg<- "id"
    ids <- toupper(ids)
    resource <- resource
    if (!is.null(param)) {
      param <- c(genome=param@genome, gene=param@gene,
                   region=param@region, rs=param@rs,so=param@so,
                   phenotype=param@phenotype, limit=param@limit, 
                   include=param@include, exclude=param@exclude,
                   limit=param@limit)
      param <- paste(param, collapse = "&")
    }
    result <- fetchCellbase(object=object, file=NULL, meta=NULL, categ=categ,
                            subcateg=subcateg,
                            ids=ids, resource=resource, param=param)
    return(result)
})
