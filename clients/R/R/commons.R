utils::globalVariables(c("name", "j"))
################################################################################
# we need to adjust the output for the protein and Genomesequence methods
#
fetchCellbase <- function(file=NULL,host=host, version=version, meta=meta, 
    species=species, categ, subcateg,ids, resource,filters=NULL, 
    batch_size=NULL,num_threads=NULL,...){
  # Get the parametrs
  if(species=="hsapiens/"){
    batch_size <- batch_size
  }else{
    batch_size <- 50
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
                      ids=ids, resource=resource,...)
    cat("\ngetting the data....\n")
    content <- callREST(grls = grls,async=TRUE,num_threads)
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
                          species=species, categ=categ, subcateg=subcateg, ids=ids, 
        resource=resource,filters=filters,skip = skip)
        skip=skip+1000
        content <- callREST(grls = grls)
        res_list <- parseResponse(content=content)
        num_results <- res_list$num_results
        cell <- res_list$result
        container[[i]] <- cell
        i=i+1
    }
    if(class(container[[1]])=="data.frame"){
      ds <- rbind.pages(container)
    }else{
      ds <- as.data.frame(container[[1]], stringsAsFactors=FALSE, names="result")
      
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
    resource=resource, filters=filters,skip=0)
    {

    if(is.null(file)){
    skip=paste0("?","skip=",skip)
    filters <- paste(skip,filters, sep = "&")
    grls <- paste0(host,version, meta, species, categ, subcateg, ids, 
        resource,filters,collapse = "")

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
## A function to make the API calls
callREST <- function(grls,async=FALSE,num_threads=num_threads)
    {
    content <- list()
    if(is.null(file)){
    content <- getURI(grls)
    } else{
          if (requireNamespace("pbapply", quietly = TRUE)){
            content <- pbapply::pbsapply(grls, function(x)getURI(x))
            
          }

    }

  return(content)
}
## A function to parse the json data into R dataframes
parseResponse <- function(content,parallel=FALSE,num_threads=num_threads){
        if(parallel==TRUE){
    num_cores <-detectCores()/2
    registerDoMC(num_cores)
    
    # 
    # ## Extracting the content in parallel
    js <- mclapply(content, function(x)fromJSON(x), mc.cores=num_cores)
    res <- mclapply(js, function(x)x$response$result, mc.cores=num_cores)
    names(res) <- NULL
    ind <- sapply(res, function(x)length(x)!=1)
    res <- res[ind]
    ds <- mclapply(res, function(x)rbind.pages(x), mc.cores=num_cores)
    if(requireNamespace("pbapply", quietly = TRUE)){
      ds <- pbapply::pblapply(res, function(x)rbind.pages(x))
      
    }
    ## Important to get correct merging of dataframe
    names(ds) <- NULL
    ds <- rbind.pages(ds)
    nums <- NULL
     }else{
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
# consider seperating into two functions
### Docs
#' A function to get help about cellbase queries
#' 
#' This is a convience function to get help on cellbase methods
#' @param object a cellBase class object
#' @param category a character the category to be queried
#' @param subcategory a character the subcategory to be queried
#' @param  resource A charcter when specified will get all the parametrs for
#' that specific resource
#' @return documentation about avaiable resources or required parameters
#' @examples 
#' cb <- CellBaseR()
#' cbHelp(cb, category="feature", subcategory="gene")
#' @export
cbHelp <- function(object, category, subcategory, resource=NULL){
  host <- object@host
  cbDocsUrl <- paste0(host, "swagger.json")
  Data <- fromJSON(cbDocsUrl)
  tags <- Data$tags
  paths <- Data$paths
  getList <- lapply(paths, function(x)x$get)
  ## filtered
  parts <- Filter(Negate(function(x) is.null(unlist(x))), getList)
  cbGetParams <- lapply(parts, function(x)x$parameters)
  catsub <- paste(category,subcategory, sep = "/")
  index <- grep(catsub, names(cbGetParams))
  narrowed <- names(parts)[index]
  patt1 <- paste0(catsub,"/", ".*?/","(.*)" )
  resMatch <- regexec(patt1,narrowed)
  m <- regmatches(narrowed, resMatch)
  if(is.null(resource)){
    res <- sapply(m, function(x)x[2])
    res <- res[!is.na(res)]
  }else{
    patt2 <- paste(catsub,"/", ".*?/", resource, sep="")
    index <- grep(patt2, names(parts))
    res <- parts[[index]]
    res <- res$parameters
    res <- subset(res,!(name %in% c("version", "species")), select=c("name", "description","required", "type"))
  }
  res
}
#