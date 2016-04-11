fetchCellbase <- function(file=NULL,host=host, version=version, species=species, categ, subcateg,ids,resource,filter=NULL,...){
  #ids <- ids
  categ <- paste0(categ,"/",sep="")
  subcateg <- paste0(subcateg,"/",sep="")
  if(is.null(file)){
    ids <- paste0(ids,collapse = ",")
    ids <- paste0(ids,"/",collapse = "")
  }else{
    ids <- readIds(file)
  }
  
  grls <- createURL(host=host, version=version, species=species, categ=categ,subcateg=subcateg,ids=ids,resource=resource,filter=NULL) 
  content <- callREST(grls = grls)
  cell <- parseResponse(content=content)
  return(cell)
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
  while (i <=num_iter)
    {
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
createURL <- function(file=NULL,host=host,version=version,species=species,categ=categ,subcateg=subcateg,ids=ids,resource=resource,filter=NULL)
  {
  if(is.null(file)){
    grls <- paste0(host,version,species,categ,subcateg,ids,resource,filter,collapse = "")
    
  }else{
    grls <- list()
    gcl <- paste0(host,version,species,categ,subcateg,collapse = "")
    
    for(i in seq_along(ids)){
      hop <- paste(ids[[i]],collapse = ",")
      tmp <- paste0(gcl,hop,resource,collapse = ",")
      grls[[i]] <- gsub("chr","",tmp)
    }
  }
  # host <- "http://bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0/webservices/rest/"
  # #host <- paste0(host,"/",sep="")
  # version <- "v3"
  # version <- paste0(version,"/",sep="")
  # species <- "hsapiens"
  # species <- paste0(species,"/",sep="")
  # categ <- "genomic"
  # categ <- paste0(categ,"/",sep="")
  # # subcateg <- "variant"
  # subcateg <- paste0(subcateg,"/",sep="")
  # ids <- paste0(ids,collapse = ",")
  # ids <- paste0(ids,"/",collapse = "")
  # resource <- "/full_annotation"
  # filter <- list()
  
  #grls <- paste0(host,version,species,categ,subcateg,ids,resource,filter,collapse = "")
  #gbase <- "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/"

  return(grls)
}
callREST <- function(grls,async=FALSE){
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
parseResponse <- function(content,parallel=TRUE){
  require(jsonlite)
  if(parallel==TRUE){
    library(parallel)
    library(doMC)
    numcores <- 4
    registerDoMC(numcores)
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
  ds <- pblapply(ares,function(x)rbind.pages(x))
  ### Important to get correct merging of dataframe
  names(ds) <- NULL
  ds <- rbind.pages(ds)
  }
  return(ds)
  
}




