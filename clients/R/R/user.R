#' getClinicalByGene
#' 
#' A convienice method to fetch clinical variants for specific gene/s
#' @param object an object of CellBaseR class
#' @param id a charcter vector of HUGO symbol (gene names)
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getClinicalByGene(cb, "TET1")
#' @export
getClinicalByGene <- function(object, id, param=NULL){
 
 res <- getGene(object = object, ids = id, resource = "clinical",
                param = param)
 res
}
##############################################################################
#' getTranscriptByGene
#' 
#' A convienice method to fetch transcripts for specific gene/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of HUGO symbol (gene names)
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getTranscriptByGene(cb, "TET1")
#' @export
getTranscriptByGene <- function(object, id, param=NULL){
  
  res <- getGene(object = object, ids = id, resource = "transcript",
                 param = param)
  res
}
##############################################################################
#' getGeneInfo
#' 
#' A convienice method to fetch gene annotations specific gene/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of HUGO symbol (gene names)
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getGeneInfo(cb, "TET1")
#' @export
getGeneInfo <- function(object, id, param=NULL){

  res <- getGene(object = object, ids = id, resource = "info",
                 param = param)
  res
}
##############################################################################
#' getSnpByGene
#' 
#' A convienice method to fetch known variants (snps) for specific gene/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of HUGO symbol (gene names)
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' param <- CellBaseParam(limit = 10)
#' res <- getSnpByGene(cb, "TET1", param = param)
#' @export
getSnpByGene <- function(object, id, param=NULL){
  
  res <- getGene(object = object, ids = id, resource = "snp", param = param)
  res
}
##############################################################################
#' getProteinInfo
#' 
#' A convienice method to fetch annotations for specific protein/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of Uniprot Ids 
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getProteinInfo(cb, "O15350")
#' @export
getProteinInfo <- function(object, id, param=NULL){
  
  res <- getProtein(object = object, ids = id, resource = "info",
                    param = param)
  res
}
##############################################################################
#' getClinicalByRegion
#' 
#' A convienice method to fetch clinical variants for specific region/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of genomic regions, eg 17:1000000-1100000
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getClinicalByRegion(cb, "17:1000000-1189811")
#' @export
getClinicalByRegion <- function(object, id, param=NULL){
 
  res <- getRegion(object = object, ids = id, resource = "clinical",
                   param = param)
  res
}
##############################################################################
#' getConservationByRegion
#' 
#' A convienice method to fetch conservation data for specific region/s
#' @param id a charcter vector of genomic regions, eg 17:1000000-1100000
#' @param object an object of class CellBaseR
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getConservationByRegion(cb, "17:1000000-1189811")
#' @export
getConservationByRegion <- function(object, id, param=NULL){

  res <- getRegion(object = object, ids = id, resource = "conservation",
                   param = param)
  res
}
##############################################################################
#' getRegulatoryByRegion
#' 
#' A convienice method to fetch regulatory data for specific region/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of genomic regions, eg 17:1000000-1100000
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getRegulatoryByRegion(cb, "17:1000000-1189811")
#' @export
getRegulatoryByRegion <- function(object, id, param=NULL){

  res <- getRegion(object = object, ids = id, resource = "regulatory",
                   param = param)
  res
}
##############################################################################
#' getTfbsByRegion
#' 
#' A convienice method to fetch Transcription facrots data for specific region/s
#' @param id a charcter vector of genomic regions, eg 17:1000000-1100000
#' @param object an object of class CellBaseR
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getTfbsByRegion(cb, "17:1000000-1189811")
#' @export
getTfbsByRegion <- function(object, id, param=NULL){

  res <- getRegion(object = object, ids = id, resource = "tfbs",
                   param = param)
  res
}
##############################################################################
#' getCaddScores
#' 
#' A convienice method to fetch Cadd scores for specific variant/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of genomic variants, eg 19:45411941:T:C
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getCaddScores(cb, "19:45411941:T:C")
#' @export
getCaddScores <- function(object, id, param=NULL){
  
  res <- getVariant(object = object, ids = id, resource = "cadd",
                    param = param)
  res
}
##############################################################################
#' getVariantAnnotation
#' 
#' A convienice method to fetch variant annotation for specific variant/s
#' @param object an object of class CellBaseR
#' @param id a charcter vector of length < 200 of genomic variants,
#'  eg 19:45411941:T:C
#' @param param an object of class CellBaseParam
#' @return a dataframe of the query result
#' @examples 
#' cb <- CellBaseR()
#' res <- getVariantAnnotation(cb, "19:45411941:T:C")
#' @export
getVariantAnnotation <- function(object, id, param=NULL){
  
  res <- getVariant(object = object, ids = id, resource = "annotation",
                    param = param)
  res
}