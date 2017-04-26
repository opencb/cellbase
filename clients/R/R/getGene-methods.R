################################################################################
#' getGene
#' 
#'   A method to query gene data from Cellbase web services.
#' @details This method retrieves various gene annotations including transcripts
#' and exons data as well as gene expression and clinical data
#' @aliases getGene
#' @param object an object of class CellBaseR
#' @param ids a character vector of gene ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param param an object of class CellBaseParam specifying additional param
#'  for the CellBaseR
#' @return a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- getGene(object=cb, ids=c("TP73","TET1"), resource="info")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("getGene", "CellBaseR", definition = function(object, ids, 
                                                             resource, 
                                                             param=NULL)
  {
 
    categ <- "feature"
    subcateg<- "gene"
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
    # TODO: param are not enabled
    result <- fetchCellbase(object=object,file=NULL, meta=NULL, 
                            categ=categ, subcateg=subcateg, 
                            ids=ids, resource=resource,
                            param=param)
    return(result)
})
