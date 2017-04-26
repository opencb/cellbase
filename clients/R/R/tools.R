utils::globalVariables(c("k", "transcripts", "exons"))

# Annovcf
Annovcf <- function(object, file, batch_size, num_threads, BPPARAM=bpparam()){
  num_cores <-2
  register(BPPARAM, default=TRUE)
  registerDoParallel(num_cores) 
  p <- bpparam()
  host <- object@host
  species <- object@species
  version <- object@version
  batch_size <- object@batch_size
  num_threads <- object@num_threads
  ids <- readIds(file, batch_size, num_threads)
  #filter out multiallelic sites
  ids2 <- sapply(ids, function(x)sapply(x, function(y)filterMulti(y)))
  names(ids2) <- NULL
  grls <- list()
  categ <- 'genomic/'
  subcateg <- "variant/"
  resource <- "/annotation"
  # get the IDs
  gcl <- paste0(host,version,species,"/",categ,subcateg,collapse = "")
  for(i in seq_along(ids2)){
    hop <- paste(ids2[[i]],collapse = ",")
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
    resp <- pblapply(grp[[i]], function(x)GET(x, add_headers(`Accept-Encoding`
                                                          = "gzip, deflate")))
    content <- pblapply(resp, function(x) content(x, as="text",
                                                  encoding = "utf-8"))
    js <- bplapply(content, function(x)fromJSON(x),BPPARAM = p)
    res <- bplapply(js, function(x)x$response$result, BPPARAM = p)
    names(res) <- NULL
    ind <- sapply(res, function(x)length(x)!=1)
    res <- res[ind]
    ds <- bplapply(res, function(x)rbind.pages(x), BPPARAM = p)
    container[[i]] <- ds
    i=i+1
  }


  final <-foreach(k=1:length(container),
                  .options.multicore=list(preschedule=TRUE),
                  .combine=function(...)rbind.pages(list(...)),
                  .packages='jsonlite',.multicombine=TRUE) %dopar% {
                  rbind.pages(container[[k]])
                            }

  return(final)

}



#' createGeneModel 
#' 
#' A convience functon to construct a genemodel 
#' @details  This function create a gene model data frame, which can be then 
#' turned into a GeneRegionTrack for visualiaztion
#'  by \code{\link[Gviz]{GeneRegionTrack}}  
#' @param object an object of class CellbaseResponse
#' @param region a character 
#' @return A geneModel
#' @import data.table
#' @examples
#' cb <- CellBaseR()
#' test <- createGeneModel(object = cb, region = "17:1500000-1550000")
#' @seealso  \url{https://github.com/opencb/cellbase/wiki} 
#' and the RESTful API documentation 
#' \url{http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/}
#' @export
createGeneModel <- function(object, region=NULL){
  if(!is.null(region)){
    host <- object@host
    species <- object@species
    version <- object@version
    categ <- "genomic"
    subcateg<- "region"
    ids <- region
    resource <- "gene"
    data <- fetchCellbase(object=object, file=NULL, meta=NULL, categ=categ, 
                          subcateg=subcateg,
                          ids=ids, resource=resource, param=NULL)
    rt4 <- data[,c(1,2,11)]
    rt4 <- as.data.table(rt4)
    #rt4 <- as.data.table(rt4)
    setnames(rt4,  c("id", "name"), c("gene", "symbol"))
    hope <- tidyr::unnest(rt4, transcripts) 
    setnames(hope, c("id", "biotype"), c("transcript","feature"))
    hope <- hope[,c("gene", "symbol","transcript", "exons"), with=FALSE]
    hope <- tidyr::unnest(hope, exons)
    setnames(hope, c("id"), c("exon"))
    
    hope <- as.data.frame(hope)
    hope <- hope[!duplicated(hope),1:9]
    
  }
  hope
}

# Filter multiallelic sites
filterMulti <- function(x){
  res <- strsplit(x, split = ",")[[1]][1]
  res
}

