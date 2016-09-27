
# CellBaseR methods

########################################################################################################################
#' The generic method for getCellbase. This method allows the user to query the cellbase web services without any 
#' predefined categories, subcategries, or resources. Please, for details on possible values for the parameters and 
#' additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases getCellbase
#' @param object an object of class CellBaseR
#' @param category character to specify the category to be queried.
#' @param subcategory character to specify the subcategory to be queried
#' @param ids a character vector of the ids to be queried
#' @param resource a character to specify the resource to be queried
#' @param filters an object of class CellBaseParam specifying additional filters for the CellBaseR
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR
#'    res <- getCellbase(object=cb,categ="feature",subcateg="gene",ids="TET1",
#'    resource="info")
#' @seealso for more information about the cellbase webservices see
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
########################################################################################################################
setGeneric("getCellbase", function(object, categ, subcateg,ids, resource,filters=NULL, ...) 
standardGeneric("getCellbase"))

########################################################################################################################
#' A method to query Clinical data from Cellbase web services. Please, for details on possible values for the parameters 
#' and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API 
#' documentation  http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbClinicalClient
#' @param object an object of class CellBaseR
#' @param filters a object of class CellBaseParam specifying the parameters limiting the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    cbParam <- CellBaseParam(gene=c("TP73","TET1"))
#'    res <- cbClinicalClient(object=cb,filters=cbParam)
########################################################################################################################
setGeneric("cbClinicalClient", function(object, filters,...)
standardGeneric("cbClinicalClient"))

########################################################################################################################
#' A method to query gene data from Cellbase web services. Please, for details on possible values for the parameters 
#' and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful API 
#' documentation  http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbGeneClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of gene ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbGeneClient(object=cb, ids=c("TP73","TET1"), resource="clinical")
########################################################################################################################
setGeneric("cbGeneClient", function(object,ids,resource,filter, ...)
standardGeneric("cbGeneClient"))

########################################################################################################################
#' A method to query features within a genomic region from Cellbase web services. Please, for details on possible values
#' for the parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the 
#' RESTful API documentation  http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbRegionClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the regions to be queried, for example, "1:1000000-1200000' should always be in the
#' form 'chr:start-end'
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbRegionClient(object=cb, ids="17:1000000-1200000", resource="gene")
########################################################################################################################
setGeneric("cbRegionClient", function(object,ids,resource,filters, ...)
standardGeneric("cbRegionClient"))

########################################################################################################################
#' A method to query known genomic variation data from Cellbase web services from Cellbase web services. Please, 
#' for details on possible values for the parameters  and  additional filters of this function refer to
#' https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbSnpClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid rsid,
#' for example 'rs6025'
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbSnpClient(object=cb, ids="rs6025", resource="info")
########################################################################################################################
setGeneric("cbSnpClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbSnpClient"))

########################################################################################################################
#' A method to query variant annotation data from Cellbase web services from Cellbase web services. Please, 
#' for details on possible values for the parameters  and  additional filters of this function refer to
#' https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbVariantClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be in the following format 'chr:start:ref:alt', for 
#' example, '1:128546:A:T'
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbVariantClient(object=cb, ids="19:45411941:T:C", resource="annotation")
########################################################################################################################
setGeneric("cbVariantClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbVariantClient"))

########################################################################################################################
#' A method to query transcription factors binding sites data from Cellbase web services. Please, 
#' for details on possible values for the parameters  and  additional filters of this function refer to
#' https://github.com/opencb/cellbase/wiki and the RESTful API documentation 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbTfbsClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, must be a valid transcription factor name, for example, 
#' Pdr1, and Oaf1
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbTfbsClient(object=cb, ids="PAX1", resource="gene")
########################################################################################################################
setGeneric("cbTfbsClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTfbsClient"))

########################################################################################################################
#' A method to query transcript data from Cellbase web services. Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbTranscriptClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the transcript ids to be queried, for example, ensemble transccript ID
#' like ENST00000380152
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbTranscriptClient(object=cb, ids="ENST00000373644", resource="info")
########################################################################################################################
setGeneric("cbTranscriptClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbTranscriptClient"))

########################################################################################################################
#' A method to query cross reference data from Cellbase web services. Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbXrefClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of the ids to be queried, any crossrefereable ID, gene names, transcript ids, 
#' uniprot ids,etc.
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbXrefClient(object=cb, ids="ENST00000373644", resource="xref")
########################################################################################################################
setGeneric("cbXrefClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbXrefClient"))

########################################################################################################################
#' A method to query protein data from Cellbase web services. Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbProteinClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of uniprot ids to be queried, should be one or more of uniprot ids, for example O15350.
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbProteinClient(object=cb, ids="O15350", resource="sequence")
########################################################################################################################
setGeneric("cbProteinClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbProteinClient"))

########################################################################################################################
#' A method to query sequence data from Cellbase web services. Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbChromosomeInfoClient
#' @param object an object of class CellBaseR
#' @param ids a character vector of chromosome ids to be queried
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbChromosomeInfoClient(object=cb, ids="22", resource="info")
########################################################################################################################
setGeneric("cbChromosomeInfoClient", function(object,ids,resource,filters=NULL, ...)
standardGeneric("cbChromosomeInfoClient"))

########################################################################################################################
#' A method for getting the avaiable species from the cellbase web services. Please, for details on possible values for the 
#' parameters  and  additional filters of this function refer to https://github.com/opencb/cellbase/wiki and the RESTful 
#' http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/
#' @aliases cbSpeciesClient
#' @param object an object of class CellBaseR
#' @param resource a character vector to specify the resource to be queried
#' @param filters a object of class CellBaseParam specifying additional filters for the query
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @examples
#'    library(cellbaseR)
#'    cb <- CellBaseR()
#'    res <- cbSpeciesClient(object=cb)
########################################################################################################################
setGeneric("cbSpeciesClient", function(object, ...) 
standardGeneric("cbSpeciesClient"))

########################################################################################################################
#' This method is a convience method to annotate a vcf files. This methods is ideal for annotating small to medium sized 
#' vcf files.
#' @aliases cbAnnotateVcf
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param ... any extra arguments
#' @return an object of class CellBaseResponse which holds a dataframe with the results of the query
#' @export
setGeneric("cbAnnotateVcf", function(object,file, ...)
standardGeneric("cbAnnotateVcf"))


# CellBaseResponse methods

########################################################################################################################
#' An accessor method to get CellBaseResponse cbData slot
#' @aliases cbData
#' @param object an object of class CellBaseResponse
#' @return a dataframe
#' @export
setGeneric("cbData", function(object, ...)
standardGeneric("cbData"))

