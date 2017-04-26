################################################################################
#' AnnotateVcf
#' 
#' This method is a convience method to annotate bgzipped tabix-indexed vcf 
#' files. It should be ideal for annotating small to medium sized 
#' vcf files.
#' @include AllClasses.R AllGenerics.R
#' @aliases AnnotateVcf
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param  batch_size intger if multiple queries are raised by a single method 
#' call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size 
#' of each batch, e.g. 200
#' @param num_threads number of asynchronus batches to be sent to the server
#' @param BPPARAM a BiocParallel class object 
#' @return a dataframe with the results of the query
#' @examples 
#' cb <- CellBaseR()
#' fl <- system.file("extdata", "hapmap_exome_chr22_500.vcf.gz",
#'                   package = "cellbaseR" )
#' res <- AnnotateVcf(object=cb, file=fl, BPPARAM = bpparam(workers=2))
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
setMethod("AnnotateVcf", "CellBaseR", definition = function(object, file,
                                                            batch_size,
                                                            num_threads,
                                                            BPPARAM=bpparam()){
    result <- Annovcf(object=object, file=file, batch_size, num_threads, 
                      BPPARAM=bpparam())
    return(result)
})
