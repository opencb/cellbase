########################################################################################################################
#' This method is a convience method to annoatate a vcf files
#' @aliases cbAnnotateVcf
#' @details This methods is ideal for annotating small to medium sized vcf
#'    files
#' @param object An object of class CellBaseR
#' @param ... any extra arguments
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @return an annotated dataframe
#' @export
setMethod("cbAnnotateVcf", "CellBaseR",    definition = function(object, file
, ...) {
    # host <- object@host
    # version <- object@version
    # species <- object@species
    # batch_size <- object@batch_size
    # num_threads <- object@num_threads
    # categ <- "genomic"
    # subcateg<- "variant"
    # ids <- NULL
    # resource <- "/annotation"
    # result <- fetchCellbase(host, file=file,version,meta=NULL, species, categ,
    # subcateg, ids=NULL, resource, filters=NULL, batch_size=batch_size,
    # num_threads=num_threads)
    result <- Annovcf(object=object, file=file, batch_size, num_threads )
    data <- CellBaseResponse(cbData=result)
    return(data)
})
