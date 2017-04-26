###############################################################################
#' getClinical
#'
#'  A method to query Clinical data from Cellbase web services.
#' @details  This method retrieves clinicaly relevant variants annotations from
#' multiple resources including clinvar, cosmic and gwas catalog. Furthermore,
#'  the user can filter these data in many ways including phenotype, genes, rs,
#'  etc,.
#' @aliases getClinical
#' @param object an object of class CellBaseR
#' @param param a object of class CellBaseParam specifying the parameters
#'  limiting the CellBaseR
#' @return a dataframe with the results of the query
#' @examples
#'    cb <- CellBaseR()
#'    cbParam <- CellBaseParam(gene=c("TP73","TET1"))
#'    res <- getClinical(object=cb,param=cbParam)
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}   
#' @export
##############################################################################
setMethod("getClinical", "CellBaseR", definition = function(object,
                                                            param=NULL) {
   
    categ <- "feature"
    subcateg<- "clinical"
    ids <- NULL
    resource <- "search"

    param <- c(genome=param@genome, gene=param@gene,
    region=param@region,rs=param@rs,so=param@so,
    phenotype=param@phenotype, include=param@include,
    exclude=param@exclude, limit=param@limit)
    param <- paste(param, collapse = "&")
    result <- fetchCellbase(object=object,file=NULL, meta=NULL, categ=categ, 
                            subcateg=subcateg,ids=ids,resource=resource, 
                            param=param)
    
    return(result)
})
