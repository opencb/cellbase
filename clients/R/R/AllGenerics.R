########################################################################################################################
# CellBaseR methods
########################################################################################################################

#' The generic method for getCellbase
#'
#' This method allows the user to query the cellbase web services without
#' any predefined categories, subcategries, or resources
setGeneric("getCellbase", function(object, file=NULL,categ, subcateg,ids,
resource,filters=NULL, ...) standardGeneric("getCellbase"))

##############
##############
#############
# cbClinicalClientClient
setGeneric("cbClinicalClient", function(object, filters,...)
standardGeneric("cbClinicalClient"))

###
setGeneric("cbGeneClient", function(object,ids,resource,filter, ...)
standardGeneric("cbGeneClient"))

###
setGeneric("cbRegionClient", function(object,ids,resource,filters, ...)
standardGeneric("cbRegionClient"))

###
setGeneric("cbSnpClient", function(object,ids,resource,filters, ...)
standardGeneric("cbSnpClient"))

###
setGeneric("cbVariantClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbVariantClient"))

###
setGeneric("cbTfbsClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTfbsClient"))

###
setGeneric("cbTranscriptClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTranscriptClient"))

###
setGeneric("cbXrefClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbXrefClient"))

###
###
setGeneric("cbProteinClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbProteinClient"))

###
setGeneric("cbChromosomeInfoClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbChromosomeInfoClient"))

###
setGeneric("cbSpeciesClient", function(object, ...) standardGeneric("cbSpeciesClient"))

##############################################################################
setGeneric("cbAnnotateVcf", function(object,file, ...)
standardGeneric("cbAnnotateVcf"))


########################################################################################################################
# CellBaseResponse methods
########################################################################################################################

# An accessor method to get CellBaseResponse cbData slot cbData
setGeneric("cbData", function(object, ...)
standardGeneric("cbData"))

