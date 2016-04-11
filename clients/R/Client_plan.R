getCellbase <- function(host, version, species, category, subcategory,ids,resource,filter=NULL,...){
  ids <- readIds(ids = ids)
  grls <- createURL(host=NULL, version=NULL, species=NULL, categ=NULL,subcateg=NULL,ids,resource,filter=NULL) 
  content <- callREST(grls = grls)
  cell <- parseResponse(content)
  return(cell)
}
# cellVarAnnotate <- function(ids,...){
#   
#   
# }
# cellbase_gene <- function(ids){
#   
#   
# }
readIds <- function(file=NULL,ids=NULL)
  {
  require(Rsamtools)
  require(pbapply)
  ids<- list()
  num_iter<- ceiling(R.utils::countLines(file)[[1]]/800)
  #batchSize * numThreads
  demo <- Rsamtools::TabixFile(file,yieldSize = 800)
  tbx <- open(demo)
  i <- 1
  while (i <=num_iter)
    {
    inter <- scanTabix(tbx)[[1]]
    if(length(inter)==0)break
    whim <- lapply(inter, function(x){strsplit(x[1],split = "\t")[[1]][c(1,2,4,5)]})
    whish <- sapply(whim, function(x){paste(x,collapse =":")})
    #hope <- paste(whish,collapse = ",")
    hope <- split(whish, ceiling(seq_along(whish)/200))
    ids[[i]] <- hope
    i <- i+1
  }
  ids <- pbsapply(ids, function(x)lapply(x, function(x)x))
  return(ids)
}
  
  #create a list of character vectors of urls
createURL <- function(host=host,version=version,species=species,categ=categ,subcateg=subcate,ids,resource=resource,filter=list)
  {
  host <- "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest"
  host <- paste0(host,"/",sep="")
  version <- "v3"
  version <- paste0(version,"/",sep="")
  species <- "hsapiens"
  species <- paste0(species,"/",sep="")
  categ <- "genomic"
  categ <- paste0(categ,"/",sep="")
  subcateg <- "variant"
  subcateg <- paste0(subcateg,"/",sep="")
  resource <- "/full_annotation"
  filter <- list()
  gcell <- paste0(host,version,species,categ,subcateg,collapse = "")
  #gbase <- "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/"
  grls <- list()
  for(i in seq_along(ids)){
    hop <- paste(ids[[i]],collapse = ",")
    tmp <- paste0(gcell,hop,resource,collapse = ",")
    grls[[i]] <- gsub("chr","",tmp)
  }
  return(grls)
}
callREST <- function(grls,config=NULL,async=TRUE){
  require(RCurl)
  if(async==TRUE){
    prp <- split(grls,ceiling(seq_along(grls)/4))
    cat("Preparing The Asynchronus call.............")
    gs <- pblapply(prp, function(x)unlist(x))
    cat("Getting the Data...............")
    content <- pblapply(gs,function(x)getURIAsynchronous(x,perform = Inf))
    
  }else{
    content <- pbsapply(grls, function(x)getURI(x))
    
  }
  return(unlist(content))
}
parseResponse <- function(content,parallel=TRUE){
  require(jsonlite)
  if(parallel==TRUE){
    library(parallel)
    library(doMC)
    numcores <- 4
    registerDoMC(numcores)
    ### Extracting the content in parallel
    js <- mclapply(content, function(x)fromJSON(x),mc.cores=numcores)
    res <- mclapply(js, function(x)x$response$result,mc.cores=numcores)
    ds <- mclapply(res, function(x)rbind.pages(x),mc.cores=numcores)
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



