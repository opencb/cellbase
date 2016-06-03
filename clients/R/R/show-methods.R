##### Methods for CellBaseR objects
#    The show method for CellBaseR class
setMethod("show",signature = "CellBaseR",definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat("| it holds the configuration for querying the Cellbase databases\n")
    cat("| to change the default species from human use CellBaseR(species='')")
})

#    The show method for CellBaseResponse class
setMethod("show",signature = "CellBaseResponse", definition = function(object){
  cat("An object of class ", class(object), "\n", sep = "")
  cat(" containing ", nrow(object@cbData), " rows and ",
      ncol(object@cbData), " columns.\n", sep = "")
  cat(" to get the annotated dataframe use cbData()")
})

##### Methods for CellBaseParam objects
# Show method
setMethod("show",signature = "CellBaseParam",definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat("use this object to control what results are returned from the
    CellBaseR methods")
})
