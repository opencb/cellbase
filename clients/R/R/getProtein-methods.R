##############################################################################
#' getProtein
#' 
#' A method to query protein data from Cellbase web services.
#' @details  This method retrieves various protein annotations including 
#' protein description, features, sequence, substitution scores, evidence,  etc.
#' @aliases getProtein
#' @param object an object of class CellBaseR
#' @param ids a character vector of uniprot ids to be queried, should be one
#'  or more of uniprot ids, for example O15350.
#' @param resource a character vector to specify the resource to be queried
#' @param param a object of class CellBaseParam specifying additional param
#'  for the query
#' @return an object of class CellBaseResponse which holds a dataframe with th
#' e results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- getProtein(object=cb, ids="O15350", resource="info")
#' @export
setMethod("getProtein", "CellBaseR", definition = function(object, ids,
                                                                resource, 
                                                                param=
                                                                  NULL) {

    categ <- "feature"
    subcateg<- "protein"
    ids <- ids
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
                            subcateg=subcateg, ids=ids, resource=resource, 
                            param=param)
    
    return(result)
})
