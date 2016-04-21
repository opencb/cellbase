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