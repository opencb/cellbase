utils::globalVariables(c("name", "j", "registerDoMC"))
###############################################################################
# we need to adjust the output for the protein and Genomesequence methods
#
fetchCellbase <- function(object=object, file=NULL, meta=meta, 
    species=species, categ, subcateg,ids, resource,param=NULL, 
    batch_size=NULL, num_threads=NULL){
    host <- object@host
    species <- object@species

  # Get the parametrs
  if(species=="hsapiens"){
    batch_size <- batch_size
    version <- object@version
    
  }else{
    batch_size <- 50
    version <- "latest/"
  }
    
  num_threads <- num_threads
  
  if(is.null(categ)){
    categ <- ""
  }else{
    categ <- paste0(categ,"/",sep="")
  }
  
  if(is.null(subcateg)){
    subcateg <- ""
  }else{
    subcateg <- paste0(subcateg,"/",sep="")
  }
  # How to read the ids from the function parameter
  if(is.null(file)){
    if(is.null(ids)){
      ids <- ""
    }else{
      ids <- paste0(ids,collapse = ",")
      ids <- paste0(ids,"/",collapse = "")
    }
  # or from a file
  }else{
    cat("\nreading the file....\n")
    ids <- readIds(file,batch_size = batch_size,num_threads = num_threads)
  }

 
# in case a vcf file has been specified
  if(!is.null(file)){
    container=list()
    grls <- createURL(file=file, host=host, version=version, species=species,
                      categ=categ, subcateg=subcateg, 
                      ids=ids, resource=resource)
    cat("\ngetting the data....\n")
    content <- callREST2(grls = grls,async=FALSE,num_threads)
    cat("\nparsing the data....\n")
    res_list <- parseResponse(content=content,parallel=TRUE, 
    num_threads=num_threads)
    ds <- res_list$result
    cat("Done!")
  
# in case of all other methods except for annotateVcf
  }else{
    i=1
    server_limit=1000
    skip=0
    num_results=1000
    container=list()
        while(is.null(file)&all(unlist(num_results)==server_limit)){
        grls <- createURL(file=NULL, host=host, version=version, meta=meta, 
                          species=species, categ=categ, subcateg=subcateg,
                          ids=ids, resource=resource,param=param,
                          skip = skip)
        skip=skip+1000
        content <- callREST2(grls = grls)
        res_list <- parseResponse(content=content)
        num_results <- res_list$num_results
        cell <- res_list$result
        container[[i]] <- cell
        i=i+1
    }
    if(class(container[[1]])=="data.frame"){
      ds <- rbind.pages(container)
    }else{
      ds <- as.data.frame(container[[1]], stringsAsFactors=FALSE,names="result")
      
    }
    
  }


  return(ds)
}
## all working functions
## a function to read the varinats from a vcf file
readIds <- function(file=file,batch_size,num_threads)
    {
  
    ids<- list()
    num_iter<- ceiling(countLines(file)[[1]]/(batch_size*num_threads))
    #batchSize * numThreads
    demo <- TabixFile(file,yieldSize = batch_size*num_threads)
    tbx <- open(demo)
    i <- 1
    while (i <=num_iter) {
    inter <- scanTabix(tbx)[[1]]
    if(length(inter)==0)break
    whim <- lapply(inter, function(x){
        strsplit(x[1],split = "\t")[[1]][c(1,2,4,5)]})
    whish <- sapply(whim, function(x){paste(x,collapse =":")})
    hope <- split(whish, ceiling(seq_along(whish)/batch_size))
    ids[[i]] <- hope
    i <- i+1
    }
    ids <-foreach(k=1:length(ids))%do%{
        foreach(j=1:length(ids[[k]]))%do%{
        ids[[k]][[j]]
        }
    }
    ids <- unlist(ids, recursive = FALSE)
    return(ids)
}

## A function to create URLs
## create a list of character vectors of urls
createURL <- function(file=NULL, host=host, version=version, meta=meta, 
    species=species, categ=categ, subcateg=subcateg, ids=ids, 
    resource=resource, param=param,skip=0)
    {

    if(is.null(file)){
    skip=paste0("?","skip=",skip)
    param <- paste(skip,param, sep = "&")
       if(nchar(species)>1){
      grls <- paste0(host,version, meta, species,"/", categ, subcateg, ids, 
                     resource,param,collapse = "")
      }else{
      grls <- paste0(host,version, meta, species, categ, subcateg, ids, 
                     resource,param,collapse = "")
    }
    }else{
       grls <- list()
       gcl <- paste0(host,version,species,categ,subcateg,collapse = "")

    for(i in seq_along(ids)){
       hop <- paste(ids[[i]],collapse = ",")
       tmp <- paste0(gcl,hop,resource,collapse = ",")
       grls[[i]] <- gsub("chr","",tmp)
        }
    }
  return(grls)
}


callREST2 <- function(grls,async=FALSE,num_threads=num_threads)
{
  content <- list()
  if(is.null(file)){
    resp <- GET(grls, add_headers(`Accept-Encoding` = "gzip, deflate"), 
                timeout(2))
    content <- content(resp, as="text", encoding = "utf-8")
  }else{
    resp <- GET(grls, add_headers(`Accept-Encoding` = "gzip, deflate"))
    content <- content(resp, as="text", encoding = "utf-8")
  }
  return(content)
}
## A function to parse the json data into R dataframes
parseResponse <- function(content, parallel=FALSE, num_threads=num_threads){
        if(parallel==TRUE){

     } else{
    js <- lapply(content, function(x)fromJSON(x))
    ares <- lapply(js, function(x)x$response$result)
    
    nums <- lapply(js, function(x)x$response$numResults)
    
    if (class(ares[[1]][[1]])=="data.frame"){
      if(requireNamespace("pbapply", quietly = TRUE)){
        ds <- pbapply::pblapply(ares,function(x)rbind.pages(x))
        }
      ### Important to get correct vertical binding of dataframes
      names(ds) <- NULL
      ds <- rbind.pages(ds)
    }else{
      ds <-ares
      names(ds) <- NULL
      
    }
    
    }
  
    return(list(result=ds,num_results=nums))
}
### Docs
#' getCellBaseResourceHelp
#' 
#' A function to get help about available cellbase resources
#' @details This function retrieves available resources for each generic method
#' like getGene, getRegion, getprotein, etc. It help the user see all possible 
#' resources to use with the getGeneric methods 
#' @param object a cellBase class object
#' @param subcategory a character the subcategory to be queried
#' @return character vector of the available resources to that particular 
#' subcategory 
#' @examples 
#' cb <- CellBaseR()
#' # Get help about what resources are available to the getGene method
#' getCellBaseResourceHelp(cb, subcategory="gene")
#' # Get help about what resources are available to the getRegion method
#' getCellBaseResourceHelp(cb, subcategory="region")
#' # Get help about what resources are available to the getXref method
#' getCellBaseResourceHelp(cb, subcategory="id")
#' @export
getCellBaseResourceHelp <- function(object, subcategory){
  host <- object@host
  if(exists('.api', .GlobalEnv)&exists('.tags', .GlobalEnv)){
    getList <- get('.api',envir = .GlobalEnv)
    tags <- get('.tags',envir = .GlobalEnv) 
  }else {
    cbDocsUrl <- paste0(host, "swagger.json")
    Datp <- jsonlite::fromJSON(cbDocsUrl)
    tags <- Datp$tags
    paths <- Datp$paths 
    getList<- lapply(paths, function(x)x$get)
    assign('.api', getList, .GlobalEnv)
    assign('.tags', tags, .GlobalEnv)
  }
  category <- switch (subcategory,
                      gene= "feature",
                      protein= "feature",
                      tf="regulation",
                      variation="feature",
                      variant="genomic",
                      clinical="feature",
                      transcript="feature",
                      id="feature")
  
  ## filtered
  SUBCATEGORIES <- c('gene', 'protein', 'tf', 'variation', 'variant',
                     'clinical', 'transcript', 'id', 'region')
  if(!(subcategory %in% SUBCATEGORIES)){
    cat("Please use one of CellBase Subcategories\n")
    cat(SUBCATEGORIES,'\n')
    stop("Error unknown subcategory")}
  parts <- Filter(Negate(function(x) is.null(unlist(x))), getList)
  cbGetParams <- lapply(parts, function(x)x$parameters)
  catsub <- paste(category,subcategory, sep = "/")
  index <- grep(catsub, names(cbGetParams))
  narrowed <- names(parts)[index]
  patt1 <- paste0(catsub,"/", ".*?/","(.*)" )
  resMatch <- regexec(patt1,narrowed)
  m <- regmatches(narrowed, resMatch)
  res <- sapply(m, function(x)x[2])
  res <- res[!is.na(res)]

  res
}
#
# cbCheck <- function(object, category, subcategory, resource){
#   CATEGORIES <- c("feature", "genomic", "regulation")
#   if(!(category %in% CATEGORIES)){
#     stop("Error Unknown category")
#   } 
#   SUBCATEGORIES <- tolower(unlist(tags[[1]]))
#   if(!(subcategory %in% SUBCATEGORIES)){
#     stop("Error Unknown subcategory")
#   } 
#   RESOURCES <- getCellBaseResourceHelp(object, subcategory)
#   if(!(resource %in% RESOURCES)){
#     stop("Error Unknown resource")
#   } 
# }