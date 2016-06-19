########################################################################################################################
#' This method is a convience method to annotate a vcf files. This methods is ideal for annotating small to medium sized 
#' vcf files.
#' @include AllClasses.R AllGenerics.R
#' @aliases cbAnnotateVcf
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param ... any extra arguments
#' @param  batch_size intger if multiple queries are raised by a single method call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size of each batch, e.g. 200
#' @param num_threads integer number of asynchronus batches to be sent to the server
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @export
setMethod("cbAnnotateVcf", "CellBaseR", definition = function(object, file,
                                                              batch_size,
                                                              num_threads,
                                                              ...){
    result <- Annovcf(object=object, file=file, batch_size, num_threads )
    data <- CellBaseResponse(cbData=result)
    return(data)
})
