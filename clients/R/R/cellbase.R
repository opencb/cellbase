#' Documentation for the cellbaseR package
#' @title CellbaseR
#' @description  Querying annotation data from the high performance Cellbase web
#' services
#' @author Mohammed OE Abdallah
#' @details This R package makes use of the exhaustive RESTful Web service 
#' API that has been
#' implemented for the Cellabase database. It enables researchers to query and
#' obtain a wealth of biological information from a single database saving a lot 
#' of time. Another benefit is that researchers can easily make  queries about 
#' different biological topics and link all this information together as all 
#' information is integrated.
#' Currently Homo sapiens, Mus musculus and other 20 species are available and 
#' many others will be included soon. Results returned from the cellbase queries
#' are parsed into R data.frames and other common R data strctures so users can 
#' readily get into downstream anaysis.
#' @import methods
#' @import jsonlite
#' @import RCurl
#' @import pbapply
#' @import data.table
"_PACKAGE"