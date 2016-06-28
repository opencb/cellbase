#' A constructor function for CellBaseParam
#' 
#'use the CellBaseParam object to control what results are returned from the
#'CellBaseR methods
#' @param genome A character denoting the genome build to query,eg, GRCh37
#' (default),or GRCh38
#' @param gene A character vector denoting the gene/s to be queried
#' @param region A character vector denoting the region/s to be queried must be
#' in the form 1:100000-1500000 not chr1:100000-1500000
#' @param rs A character vector denoting the rs ids to be queried
#' @param so A character vector denoting sequence ontology to be queried
#' @param phenotype A character vector denoting the phenotype to be queried
#' @param include A character vector denoting the fields to be returned
#' @param exclude A character vector denoting the fields to be excluded
#' @param limit A number limiting the number of results to be returned
#' @examples
#' library(cellbaseR)
#' cbParam <- CellBaseParam(genome="GRCh38",gene=c("TP73","TET1"))
#' print(cbParam)
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
CellBaseParam <- function(genome=character(), gene=character(),
region=character(), rs=character(), so=character(), phenotype=character(),
include=character(), exclude=character(), limit=character()){

    if(length(genome)>0){
        genome <- paste0(genome,collapse = ",")
        genome <- paste("genome=",genome,sep = "")
    }else{
        genome <- character()
    }
    if(length(gene)>0){
        gene <- paste0(gene,collapse = ",")
        gene <- paste("gene=",gene,sep = "")
    }else{
        gene <- character()
    }

    if(length(region)>0){
        region <- paste0(region,collapse = ",")
        region <- paste("region=",region,sep = "")
    }else{
        region <-character()
    }

    if(length(rs)>0){
        rs <- paste0(rs,collapse = ",")
        rs <- paste("rs=",rs,sep = "")
    }else{
        rs <- character()
    }
    if(length(so)>0){
        so <- paste0(so,collapse = ",")
        so <- paste("so=",so,sep = "")
    }else{
        so <- character()
    }

    if(length(phenotype)>0){
        phenotype <- paste0(phenotype,collapse = ",")
        phenotype <- paste("phenotype=",phenotype,sep = "")
    }else{
        phenotype <- character()
    }

    if(length(include)>0){
        include <- paste0(include,collapse = ",")
        include <- paste("include=",include,sep = "")
    }else{
        include <- character()
    }

    if(length(exclude)>0){
        exclude <- paste0(exclude,collapse = ",")
        exclude <- paste("exclude=",exclude,sep = "")
    }else{
        exclude <- character()
    }
    if(length(limit)>0){
        limit=limit
        limit=paste("limit=", limit, sep="")
    }else{
        limit=paste("limit=", 1000, sep="")
    }

    new("CellBaseParam", genome=genome, gene=gene, region=region, rs=rs, so=so,
    phenotype=phenotype, include=include, exclude=exclude, limit=limit)

}
