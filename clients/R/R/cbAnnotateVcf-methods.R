########################################################################################################################
#' This method is a convience method to annotate a vcf files. This methods is ideal for annotating small to medium sized 
#' vcf files.
#' @aliases cbAnnotateVcf
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @export
setMethod("cbAnnotateVcf", "CellBaseR", definition = function(object, file, ...) {
    result <- Annovcf(object=object, file=file, batch_size, num_threads )
    data <- CellBaseResponse(cbData=result)
    return(data)
})
