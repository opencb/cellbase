cb <- CellbaseQuery()
cb
# what argument is object
library(RCurl)
t2 <- fetchCellbase(host = t1@config$host,version = t1@config$version,species = t1@config$species,categ="feature",subcateg="gene",ids=c("TMEM27"),resource="snp")
t3 <- getCellbase(object = t1,categ="feature",subcateg="gene",ids=c("BRCA1","BRCA2"),resource="snp")
t4 <- getSnpsByGene(object = t1,ids=c("TMEM27"))
#ids=c("BRCA1","BRCA2","BCL2")
b1 <- createURL(host = t1@config$host,version = t1@config$version,species = t1@config$species,categ = "feature",subcateg = "gene",ids = c("TMEM27","TET1"),resource ="snp",filter = NULL)
b1
t3
cellbaseData(t3[1:5,1:5])

fs <- system.file("extdata", "chr7-sub.vcf.gz",  package="VariantAnnotation")
fl <- system.file("extdata", "chr22.vcf.gz", package="VariantAnnotation")
system.time({
  y1 <- annotateVcf(object = cb, file =fl )
}) 
elav <- cbGene(object=cb, ids=c("ELAVL1","TET1","TP73"),resource="clinical")
elav
elv <- cellbaseData(elav)

cb@host
cb@species

A1 <- list(1:8,1:8)
A2 <- list(1:8,1:8)
A3 <- list(1:8,1:8)
A4 <- list(1:8,1:8)
A5 <- list(1:8,1:4)
test <- list(A1,A2,A3,A4,A5)
test
require(foreach)
ts <-foreach(k=1:length(test))%do%{
  foreach(j=1:length(test[[k]]))%do%{
    test[[k]][[j]]
  }
}
tss <- unlist(ts, recursive = FALSE)
return(ids)