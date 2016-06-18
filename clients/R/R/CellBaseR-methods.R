### CellbaseR constructor function
#' @aliases CellBaseR
#' @title  A constructor function for CellBaseR object
#' 
#' @description  This is a constructor function for CellBaseR object
#' @details
#' This class defines the CellBaseR object. It holds the default
#' configuration required by CellBaseR methods to connect to the
#' cellbase web services. By defult it is configured to query human
#' data based on the GRCh37 genome assembly. Please, visit
#' https://github.com/opencb/cellbase/wiki and
#' bioinfodev.hpc.cam.ac.uk/cellbase/webservices/ for more details on
#' following parameters.
#' run cbSpeciesClient to see avaiable species and their corresponding data
#' @import methods
#' @param  host A character the default host url for cellbase webservices,
#' e.g. "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @param  version A character the cellbae API version, e.g. "V4"
#' @param  species a character specifying the species to be queried, e.g. "hsapiens"
#' @param  batch_size intger if multiple queries are raised by a single method call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size of each batch, e.g. 200
#' @param num_threads integer number of asynchronus batches to be sent to the server
#' @return An object of class CellBaseR
#' @examples
#' library(cellbaseR)
#'    cb <- CellBaseR()
#'    print(cb)
#' @export
CellBaseR <- function(host=NULL, version=NULL, species=NULL, 
                      batch_size=NULL, num_threads=NULL ){
    if(!is.null(host)){
      host <- paste0(host, "/")
    }else {
      host <-"http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/"
    }
  
    if(!is.null(species)){
        species<-paste0(species,"/")
    }else{
        species <-"hsapiens/"
    }  
  
    if(!is.null(version)){
      version <- paste0(version,"/")
    }else{
      version <- "v4/"
    }
  
   if(!is.null(batch_size)){
     batch_size <- batch_size
   }else{
     batch_size <- 200L
   }
  
   if(!is.null(num_threads)){
     num_threads <- num_threads
   }else{
     num_threads <- 8L
   }
  
    new("CellBaseR", host=host, version=version,species=species,
        batch_size=batch_size, num_threads=num_threads )
}
