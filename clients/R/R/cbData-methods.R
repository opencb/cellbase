########################################################################################################################
#' An accessor method to get CellBaseResponse cbData slot
#' @aliases cbData
#' @param object an object of class CellBaseResponse
#' @return a dataframe
#' @export
setMethod("cbData", "CellBaseResponse", definition = function(object) object@cbData)

##
setMethod("[","CellBaseResponse",definition = function(x,i,j,drop="missing")
{
    .cbData <- x@cbData[i, j]
    CellBaseResponse(cbData = .cbData)
})
