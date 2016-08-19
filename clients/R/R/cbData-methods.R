########################################################################################################################
#' An accessor method to get CellBaseResponse cbData slot
#' @aliases cbData
#' @param object an object of class CellBaseResponse
#' @return a dataframe
#' @examples 
#'  cb <- CellBaseR()
#'  res <- cbGeneClient(object=cb, ids="TET1", resource="info")
#'  res <- cbData(res)
#' @export
setMethod("cbData", "CellBaseResponse", definition = function(object) object@cbData)

