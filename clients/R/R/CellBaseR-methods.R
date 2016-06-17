### CellbaseR constructor function
#' @aliases CellBaseR
#' @title
#' A constructor function for CellBaseR object
#' @details
#' This class defines the CellBaseR object. It holds the default
#' configuration required by CellBaseR methods to connect to the
#' cellbase web services. By defult it is configured to query human
#' data based on the GRCh37 genome assembly. Please, visit
#' https://github.com/opencb/cellbase/wiki and
#' bioinfodev.hpc.cam.ac.uk/cellbase/webservices/ for more details on
#' following parameters.
#' @param species A character should be     a species supported by cellbase
#' run cbSpeciesClient to see avaiable species and their corresponding data
#' @import methods
#' @param  host A character the default host url for cellbase webservices,
#' e.g. "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @param  version A character the cellbae API version, e.g. "V4"
#' @param  species a character specifying the species to be queried, e.g. "hsapiens"
#' @param  batch_size if multiple queries are raised by a single method call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size of these batches, e.g. 200
#' @return An object of class CellBaseR
#' @examples
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    print(cb)
#' @export
CellBaseR <- function(species=character()){
    if(length(species)>0){
        species<-paste0(species,"/",sep="")
    }else{
        species <-"hsapiens/"
    }
    new("CellBaseR", species=species )
}
