########################################################################################################################
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
    grls <- createURL(file=file, host=host, version=version, species=species, categ=categ, subcateg=subcateg, 
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
    require(Rsamtools)
    #require(pbapply)
    ids<- list()
    num_iter<- ceiling(R.utils::countLines(file)[[1]]/(batch_size*num_threads))
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
    #ids <- pbsapply(ids, function(x)lapply(x, function(x)x))
    require(foreach)
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
    filters <- paste(skip,filters,sep = "&")
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
    require(RCurl)
    if(is.null(file)){
    content <- getURI(grls)
    }else{
    require(pbapply)
    if(async==TRUE){
        prp <- split(grls,ceiling(seq_along(grls)/num_threads))
        cat("Preparing The Asynchronus call.............")
        gs <- pblapply(prp, function(x)unlist(x))
        cat("Getting the Data...............")
        content <- pblapply(gs,function(x)getURIAsynchronous(x,perform = Inf))
        content <- unlist(content)

    }else{
      content <- pbsapply(grls, function(x)getURI(x))

    }
  }


  return(content)
}
## A function to parse the json data into R dataframes
parseResponse <- function(content,parallel=FALSE,num_threads=num_threads){
    require(BiocParallel)
    require(jsonlite)
    if(parallel==TRUE){
    require(parallel)
    require(doMC)
    num_cores <-detectCores()/2
    registerDoMC(num_cores)
    # 
    # ## Extracting the content in parallel
    js <- mclapply(content, function(x)fromJSON(x, flatten=TRUE), mc.cores=num_cores)
    res <- mclapply(js, function(x)x$response$result, mc.cores=num_cores)
    names(res) <- NULL
    ind <- sapply(res, function(x)length(x)!=1)
    res <- res[ind]
    ds <- mclapply(res, function(x)rbind.pages(x), mc.cores=num_cores)
    ds <- pblapply(res, function(x)rbind.pages(x))
    ## Important to get correct merging of dataframe
    names(ds) <- NULL
    ds <- rbind.pages(ds)
    nums <- NULL
     }else{
    js <- lapply(content, function(x)fromJSON(x))
    ares <- lapply(js, function(x)x$response$result)
    nums <- lapply(js, function(x)x$response$numResults)
    
    if (class(ares[[1]][[1]])=="data.frame"){
      ds <- pblapply(ares,function(x)rbind.pages(x))
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
#' A convience fubction to directly annotate variants from a vcf file
#' @importFrom RCurl getURIAsynchronous
#' @importFrom foreach foreach
#' @importFrom jsonlite fromJSON
#' @importFrom doParallel registerDoParallel
#' @importFrom  parallel detectCores
#' @import BiocParallel
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param  batch_size intger if multiple queries are raised by a single method 
#' call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size
#'  of each batch, e.g. 200
#' @param num_threads integer number of asynchronus batches to be sent to the 
#' server
#' @param ... any extra arguments

Annovcf <- function(object, file, batch_size, num_threads){
  num_cores <-detectCores()-2
  registerDoParallel(num_cores) 
  p <- DoparParam()
  host <- object@host
  species <- object@species
  version <- object@version
  batch_size <- object@batch_size
  num_threads <- object@num_threads
  ids <- readIds(file, batch_size, num_threads)
  grls <- list()
  categ <- 'genomic/'
  subcateg <- "variant/"
  resource <- "/annotation"
  # get the IDs
  gcl <- paste0(host,version,species,categ,subcateg,collapse = "")
  for(i in seq_along(ids)){
    hop <- paste(ids[[i]],collapse = ",")
    tmp <- paste0(gcl,hop,resource,collapse = ",")
    grls[[i]] <- gsub("chr","",tmp)
  }
  prp <- split(grls,ceiling(seq_along(grls)/num_threads))
  grp <- foreach(i=1:length(prp))%do%{
    paste(prp[[i]])
  }

  # get the data and parse in chuncks
  num <- length(prp)
  i <- 1
  container <- list()
  while(i<=num){
    content <- getURIAsynchronous(grp[[i]],perform = Inf)#  alist of responses
    js <- bplapply(content, function(x)fromJSON(x),BPPARAM = p)
    res <- bplapply(js, function(x)x$response$result, BPPARAM = p)
    names(res) <- NULL
    ind <- sapply(res, function(x)length(x)!=1)
    res <- res[ind]
    ds <- bplapply(res, function(x)rbind.pages(x), BPPARAM = p)
    container[[i]] <- ds 
    i=i+1
  }
  
 
    final <- foreach(k=1:length(container),.options.multicore=list(preschedule=TRUE),
                     .combine=function(...)rbind.pages(list(...)),
                     .packages='jsonlite',.multicombine=TRUE) %dopar% {
                       rbind.pages(container[[k]])
                     }
  
  return(final)
  
}

# create GeneModel
#' A convience functon to construct a genemodel
#' 
#' @details  This function takes cbResponse object and returns a geneRegionTrack
#' model to be plotted by Gviz
#' @param object an object of class CellbaseResponse
#' @param region a character 
#' @return A geneModel
#' @examples 
#' library(cellbaseR)
#' cb <- CellBaseR()
#' test <- createGeneModel(object = cb, region = "17:1500000-1550000")
#' @export
createGeneModel <- function(object, region=NULL){
  require(data.table)
  require(tidyr)
  if(!is.null(region)){
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "region"
    ids <- region
    resource <- "gene"
    data <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ, subcateg=subcateg,
                            ids=ids, resource=resource, filters=NULL)
    rt4 <- as.data.table(data)
    rt4 <- rt4[,c("id", "name", "transcripts"), with=FALSE]
    #rt4 <- as.data.table(rt4)
    setnames(rt4,  c("id", "name"), c("gene", "symbol"))
    hope <- unnest(rt4, transcripts) 
    setnames(hope, c("id", "biotype"), c("transcript","feature"))
    hope <- hope[,c("gene", "feature","transcript", "exons", "symbol")]
    hope <- unnest(hope, exons)
    hope <- subset(hope, feature=="protein_coding")
    setnames(hope, c("id"), c("exon"))
    
    hope <- as.data.frame(hope)
    hope <- hope[!duplicated(hope),1:9]
  }
  return(hope)
}
### Docs
#' A function to get help about cellbase queries
#' 
#' This is a convience function to get help on cellbase methods
#' @param object a cellBase class object
#' @param category a character the category to be queried
#' @param subcategory a character the subcategory to be queried
#' @param  resource A charcter when specified will get all the parametrs for
#' that specific resource
#' @examples 
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
