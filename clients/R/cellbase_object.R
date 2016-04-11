library(methods)
cellbase <- setClass("cellbase",slots = c(cellbase="data.frame",config="list"),prototype = prototype(
  cellbase=data.frame(),
  config=list(host="http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/",version = "v3/")
))
list(host="http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/",version = "v3/")
cellbase()
