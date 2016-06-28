########################################################################################################################
#' The show method for CellBaseR class
#' @aliases show
#' @param object an object of class CellBaseR
#' @export
setMethod("show",signature = "CellBaseR",definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat("| it holds the configuration for querying the Cellbase databases\n")
    cat("| to change the default species from human use CellBaseR(species='')")
})

########################################################################################################################
#' The show method for CellBaseParam class
#' @aliases show
#' @param object an object of class CellBaseParam
#' @export
setMethod("show",signature = "CellBaseParam",definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat("use this object to control what results are returned from the
    CellBaseR methods")
})

#' The show method for CellBaseResponse class
#' @aliases show
#' @param object an object of class CellBaseResponse
#' @export
setMethod("show",signature = "CellBaseResponse", definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat(" containing ", nrow(object@cbData), " rows and ",
      ncol(object@cbData), " columns.\n", sep = "")
  cat(" to get the annotated dataframe use cbData()")
})

