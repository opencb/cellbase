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
#' @title  A substter method for CellBaseResponse class objects
#' A substter method for CellBaseResponse class objects
#' @param object from which to extract element(s).
#' @param i,j, indices specifying elements to extract or replace. Indices are numeric
#'  or character vectors or empty (missing) 
setMethod("[","CellBaseResponse",definition = function(x,i,j,drop="missing")
{
    .cbData <- x@cbData[i, j]
    CellBaseResponse(cbData = .cbData)
})
