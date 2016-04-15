fetchCellbase <- function(file=NULL,host=host, version=version, meta=meta,species=species, categ, subcateg,ids,resource,filter=NULL,...){
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
  #categ <- paste0(categ,"/",sep="")
  #subcateg <- paste0(subcateg,"/",sep="")
  if(is.null(file)){
    if(is.null(ids)){
      ids <- ""
    }else{
      ids <- paste0(ids,collapse = ",")
      ids <- paste0(ids,"/",collapse = "")
    }
   
  }else{
    ids <- readIds(file)
  }
  i=1
  server_limit=1000
  skip=0
  num_results=1000
  container=list()
  while(is.null(file)&all(unlist(num_results)==server_limit)){
    grls <- createURL(file=NULL,host=host, version=version, meta=meta, species=species, categ=categ,subcateg=subcateg,ids=ids,resource=resource,filter=filter,skip = skip)
    skip=skip+1000
    content <- callREST(grls = grls)
    res_list <- parseResponse(content=content)
    num_results <- res_list$num_results
    cell <- res_list$result
    container[[i]] <- cell
    i=i+1
  }
  return(rbind.pages(container))
}

readIds <- function(file=file)
  {
  require(Rsamtools)
  require(pbapply)
  ids<- list()
  num_iter<- ceiling(R.utils::countLines(file)[[1]]/(batch_size*num_threads))
  #batchSize * numThreads
  demo <- Rsamtools::TabixFile(file,yieldSize = batch_size*num_threads)
  tbx <- open(demo)
  i <- 1
  while (i <=num_iter) {
    inter <- scanTabix(tbx)[[1]]
    if(length(inter)==0)break
    whim <- lapply(inter, function(x){strsplit(x[1],split = "\t")[[1]][c(1,2,4,5)]})
    whish <- sapply(whim, function(x){paste(x,collapse =":")})
    #hope <- paste(whish,collapse = ",")
    hope <- split(whish, ceiling(seq_along(whish)/batch_size))
    ids[[i]] <- hope
    i <- i+1
  }
  ids <- pbsapply(ids, function(x)lapply(x, function(x)x))
  return(ids)
}
  
  #create a list of character vectors of urls
createURL <- function(file=NULL,host=host,version=version,meta=meta,species=species,categ=categ,subcateg=subcateg,ids=ids,resource=resource,filter=filter,skip=0)
  {
  skip=paste0("?","skip=",skip)
  filter <- paste(skip,filter,sep = "&")
  if(is.null(file)){
    grls <- paste0(host,version,meta,species,categ,subcateg,ids,resource,filter,collapse = "")
    
  }else{
    grls <- list()
    gcl <- paste0(host,version,meta,species,categ,subcateg,collapse = "")
    
    for(i in seq_along(ids)){
      hop <- paste(ids[[i]],collapse = ",")
      tmp <- paste0(gcl,hop,resource,collapse = ",")
      grls[[i]] <- gsub("chr","",tmp)
    }
  }

  
  #grls <- paste0(host,version,species,categ,subcateg,ids,resource,filter,collapse = "")
  #gbase <- "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/"

  return(grls)
}
callREST <- function(grls,async=FALSE){
  content <- list()
  
  require(RCurl)
  if(is.null(file)){
    content <- getURI(grls)
  }else{
    require(pbapply)
    if(async==TRUE){
      prp <- split(grls,ceiling(seq_along(grls)/4))
      cat("Preparing The Asynchronus call.............")
      gs <- pblapply(prp, function(x)unlist(x))
      cat("Getting the Data...............")
      content <- pblapply(gs,function(x)getURIAsynchronous(x,perform = Inf))
      
    }else{
      content <- pbsapply(grls, function(x)getURI(x))
      
    }
  }
 
  
  return(content)
}
parseResponse <- function(content,parallel=FALSE){
 
  require(jsonlite)
  if(parallel==TRUE){
    library(parallel)
    library(doMC)
    num_threads <- 4
    registerDoMC(num_threads)
    ### Extracting the content in parallel
    js <- mclapply(content, function(x)fromJSON(x),mc.cores=num_threads)
    res <- mclapply(js, function(x)x$response$result,mc.cores=num_threads)
    ds <- mclapply(res, function(x)rbind.pages(x),mc.cores=num_threads)
    ### Important to get correct merging of dataframe
    names(ds) <- NULL
    ds <- rbind.pages(ds)
    # js <- lapply(content, function(x)fromJSON(x))
    # ares <- lapply(js, function(x)x$response$result)
    # ds <- pblapply(ares,function(x)rbind.pages(x))
  }else{
  js <- lapply(content, function(x)fromJSON(x))
  ares <- lapply(js, function(x)x$response$result)
  nums <- lapply(js, function(x)x$response$numResults)
  ds <- pblapply(ares,function(x)rbind.pages(x))
  ### Important to get correct vertical binding of dataframes
  names(ds) <- NULL
  ds <- rbind.pages(ds)
  }
  return(list(result=ds,num_results=nums))
}
