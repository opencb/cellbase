utils::globalVariables(c("k", "transcripts", "exons"))

#' A convience fubction to directly annotate variants from a vcf file
#' 
#' This is a function to annotate variants from a vcf file
#' @param object an object of class CellBaseR
#' @param file Path to a bgzipped and tabix indexed vcf file
#' @param  batch_size intger if multiple queries are raised by a single method 
#' call, e.g. getting annotation info for several genes,
#' queries will be sent to the server in batches. This slot indicates the size
#'  of each batch, e.g. 200
#' @param num_threads integer number of asynchronus batches to be sent to the 
#' server
#' @param ... any extra arguments
#' @return a dataframe
Annovcf <- function(object, file, batch_size, num_threads){
  num_cores <-parallel::detectCores()-2
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
#' @import data.table
# @examples
# cb <- CellBaseR()
# test <- createGeneModel(object = cb, region = "17:1500000-1550000")
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
    data <- fetchCellbase(file=NULL,host=host, version=version, meta=NULL, species=species, categ=categ, subcateg=subcateg,
                          ids=ids, resource=resource, filters=NULL)
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
    chr <- paste0("chr",hope$chromosome[1])
    from <- min(hope$start)-5000
    to <- max(hope$end)+5000
    hope <- Gviz::GeneRegionTrack(hope,from = from, to = to,
                                  transcriptAnnotation='symbol')
    
  }
  hope
}
