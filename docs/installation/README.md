# Installation

You do not need to install CellBase to run queries. See Using CellBase for more information on how to search using CellBase.

If you do need a local installation of CellBase, please make sure that the server\(s\) have all dependencies installed then you can configure and complete the CellBase build.

Building a CellBase instance has three stages:

| Stage | Description |
| :--- | :--- |
| Download | Downloads the data files for the specified data sets |
| Build \*\* | Parses the downloaded data files, generates JSON objects, e.g. gene.json |
| Load | Loads the generated JSON objects into the Mongo database |

First, you will download a set of raw files from several data sources. These raw files shall contain the core data that will populate the Cellbase knowledgebase. Then, you will build the JSON documents that should be loaded into the Cellbase knowledgebase. These three stages are described in detail below.

\*\* We have already downloaded and processed these data, and the resulting JSON documents are available through our FTP server. For those users who wish to skip these two sections, directly download json documents from [http://bioinfo.hpc.cam.ac.uk/downloads/cellbase/v4/homo\_sapiens\_grch37/mongodb/](http://bioinfo.hpc.cam.ac.uk/downloads/cellbase/v4/homo_sapiens_grch37/mongodb/) and jump to the Load Data section.

