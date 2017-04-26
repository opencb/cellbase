#' CellbaseR constructor function
#' @aliases CellBaseR
#' @title  CellBaseR
#' 
#' @description  This is a constructor function for the CellBaseR object
#' @details
#' This class defines the CellBaseR object. It holds the default
#' configuration required by CellBaseR methods to connect to the
#' cellbase web services. By defult it is configured to query human
#' data based on the GRCh37 genome assembly.
#' @import methods
#' @param  host A character the default host url for cellbase webservices,
#' e.g. "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
#' @param  version A character the cellbae API version, e.g. "V4"
#' @param  species a character specifying the species to be queried, e.g. 
#' "hsapiens"
#' @param  batch_size intger if multiple queries are raised by a single method 
#' call, e.g. getting annotation info for several genes, queries will be sent 
#' to the server in batches.This slot indicates the size of each batch,e.g. 200
#' @param num_threads integer number of  batches to be sent to the server
#' @return An object of class CellBaseR
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @examples
#'    cb <- CellBaseR()
#'    print(cb)
#' @export
CellBaseR <- function(host="http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/"
                      , version='v4', species='hsapiens', 
                      batch_size=200L, num_threads=8L){
   
  available_species=c("hsapiens","mmusculus","drerio","rnorvegicus",
                      "ptroglodytes","ggorilla","pabelii","mmulatta",
                      "csabaeus","sscrofa","cfamiliaris","ecaballus",
                      "ocuniculus","ggallus","btaurus","fcatus",
                      "cintestinalis","oaries","olatipes","ttruncatus",
                      "lafricana","cjacchus","nleucogenys","aplatyrhynchos",
                      "falbicollis","celegans","dmelanogaster","dsimulans",
                      "dyakuba","agambiae","adarlingi","nvectensis",
                      "spurpuratus","bmori","aaegypti","apisum","scerevisiae"
                      ,"spombe","afumigatus","aniger","anidulans","aoryzae",
                      "foxysporum","pgraminis","ptriticina","moryzae",
                      "umaydis","ssclerotiorum","cneoformans","ztritici",
                      "pfalciparum","lmajor","ddiscoideum","glamblia",
                      "pultimum","alaibachii","athaliana","alyrata",
                      "bdistachyon","osativa","gmax","vvinifera","zmays",
                      "hvulgare","macuminata","sbicolor","sitalica",
                      "taestivum","brapa","ptrichocarpa","slycopersicum",
                      "stuberosum","smoellendorffii","creinhardtii",
                      "cmerolae")
  if(species%in% available_species){
    species<-species
  } else{
    stop("please enter a valid species name\n
           see ?getMeta examples to see the aviable species")
  } 
  
  version <- paste0(version,"/")

  # Get the API list
  cbDocsUrl <- paste0(host, "swagger.json")
  Datp <- fromJSON(cbDocsUrl)
  tags <- Datp$tags
  paths <- Datp$paths
  api <- lapply(paths, function(x)x$get)
  
    new("CellBaseR", host=host, version=version,species=species,
        batch_size=batch_size, num_threads=num_threads )
}
