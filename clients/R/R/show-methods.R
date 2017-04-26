###############################################################################
# The show method for CellBaseR class

#
setMethod("show",signature = "CellBaseR",definition = function(object){
    cat("An object of class ", class(object), "\n", sep = "")
    cat("| it holds the configuration for querying the CellBase databases\n")
    cat("| the current species is ",object@species, "\n")
})

###############################################################################
# The show method for CellBaseParam class
#

setMethod("show",signature = "CellBaseParam",definition = function(object){
  cat("|An object of class ", class(object), "\n", sep = "")
  cat("|use this object to control what results are returned from the CellBaseR
      methods\n")
})


