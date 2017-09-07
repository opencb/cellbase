# CellBaseR methods
#'@include commons.R
################################################################################
################################################################################
setGeneric("cbClinicalClient", function(object, filters,...)
standardGeneric("cbClinicalClient"))


setGeneric("cbGeneClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbGeneClient"))

################################################################################

################################################################################
setGeneric("cbRegionClient", function(object,ids,resource,filters, ...)
standardGeneric("cbRegionClient"))

################################################################################

################################################################################
setGeneric("cbSnpClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbSnpClient"))

################################################################################

########################################################################################################################
setGeneric("cbVariantClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbVariantClient"))

########################################################################################################################

setGeneric("cbGet", function(object, category, subcategory, ids, resource, filters=NULL,...)
standardGeneric("cbGet"))

########################################################################################################################

########################################################################################################################
setGeneric("cbTfbsClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTfbsClient"))

########################################################################################################################

########################################################################################################################
setGeneric("cbTranscriptClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTranscriptClient"))

########################################################################################################################

########################################################################################################################
setGeneric("cbXrefClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbXrefClient"))

########################################################################################################################

########################################################################################################################
setGeneric("cbProteinClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbProteinClient"))

########################################################################################################################
################################################################################
setGeneric("cbChromosomeInfoClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbChromosomeInfoClient"))

################################################################################
#' A method for getting the avaiable species from the cellbase web services
#'
#' This method is for getting species data from the cellbase web services.
#' @details Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to 
#' https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @param object an object of class CellBaseR
#' @return an object of class CellBaseResponse which holds a dataframe with the
#'  results of the query
#' @examples
#'    cb <- CellBaseR()
#'    res <- cbSpeciesClient(object=cb)
setGeneric("cbSpeciesClient", function(object) 
standardGeneric("cbSpeciesClient"))

################################################################################
setGeneric("cbAnnotateVcf", function(object,file, ...)
standardGeneric("cbAnnotateVcf"))


# CellBaseResponse methods

################################################################################
setGeneric("cbData", function(object)
standardGeneric("cbData"))

