########################################################################################################################
#' An accessor method to get CellBaseResponse cbData slot
#' @aliases cbData
#' @param object an object of class CellBaseResponse
#' @return a dataframe
#' @examples 
#'  cb <- CellBaseR()
#'  res <- cbSnpClient(object=cb, ids="rs6025", resource="info")
#'  res <- cbData(res)
#' @export
setMethod("cbData", "CellBaseResponse", definition = function(object) object@cbData)

################################################################################
#' A substter method for CellBaseResponse class objects
#' 
#' This method is a substter method for CellBaseResponse class objects
#' @param x object from which to extract element(s)
#' @param i,j, indices specifying elements to extract or replace. Indices are 
#' numeric or character vectors or empty (missing)
#' @param drop if TRUE the result is coerced to the lowest possible dimension
setMethod("[","CellBaseResponse",definition = function(x,i,j,drop="missing")
{
    .cbData <- x@cbData[i, j]
    CellBaseResponse(cbData = .cbData)
})
