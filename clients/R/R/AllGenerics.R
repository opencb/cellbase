# CellBaseR methods
#'@include commons.R
###############################################################################
###############################################################################
setGeneric("getClinical", function(object, param)
standardGeneric("getClinical"))


setGeneric("getGene", function(object,ids,resource,param=NULL)
standardGeneric("getGene"))

###############################################################################

###############################################################################
setGeneric("getRegion", function(object,ids,resource,param=NULL)
standardGeneric("getRegion"))

###############################################################################

###############################################################################
setGeneric("getSnp", function(object,ids,resource,param=NULL)
standardGeneric("getSnp"))

###############################################################################

###############################################################################
setGeneric("getVariant", function(object,ids,resource,param=NULL)
standardGeneric("getVariant"))

###############################################################################

setGeneric("getCellBase", function(object, category, subcategory, ids, resource,
                                   param=NULL)
standardGeneric("getCellBase"))

###############################################################################

###############################################################################
setGeneric("getTf", function(object,ids,resource,param=NULL)
standardGeneric("getTf"))

###############################################################################

###############################################################################
setGeneric("getTranscript", function(object,ids,resource,param=NULL)
standardGeneric("getTranscript"))

##############################################################################

##############################################################################
setGeneric("getXref", function(object,ids,resource,param=NULL)
standardGeneric("getXref"))

##############################################################################

##############################################################################
setGeneric("getProtein", function(object,ids,resource,param=NULL)
standardGeneric("getProtein"))

###############################################################################
###############################################################################
setGeneric("getChromosomeInfo", function(object,ids,resource,param=NULL)
standardGeneric("getChromosomeInfo"))

###############################################################################

setGeneric("getMeta", function(object, resource) 
standardGeneric("getMeta"))

###############################################################################
setGeneric("AnnotateVcf", function(object, file, batch_size,
                                   num_threads,
                                   BPPARAM=bpparam())
standardGeneric("AnnotateVcf"))




