# Overview
CellBase is a scalable and high-performance NoSQL database that integrates relevant biological information from well-known data sources such as Ensembl, Uniprot, IntAct or ClinVar among others. All this data can be queried through a comprehensive RESTful web services API or using the command line interface. Also, a built-in variant annotator has been developed and can be used to annotate files containing variants in Variant Call Format (VCF).

CellBase not only consumes multiple data sources, but has also been designed to run on different database engines. So far, a MongoDB plugin has been fully developed, providing amazing performance and scalability.

CellBase constitutes the knowledge-base component of [OpenCB](http://www.opencb.org/) initiative. It is used by other OpenCB projects such as [OpenCGA](https://github.com/opencb/opencga) as well as other external applications such as [EMBL-EBI EVA](http://www.ebi.ac.uk/eva/) or [Babelomics](http://www.babelomics.org/).

Note: This repository is a major refactoring of https://github.com/opencb-cloud. All users, please update to this one.

### Documentation
You can find CellBase documentation and tutorials at: https://github.com/opencb/cellbase/wiki.

For documenting RESTful web services [Swagger](http://swagger.io/) has been set-up and is available at http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/.  

### Issues Tracking
You can report bugs or request new features at [GitHub issue tracking](https://github.com/opencb/cellbase/issues).


### Release Notes and Roadmap
Releases notes are available at [GitHub releases](https://github.com/opencb/cellbase/releases).

Roadmap is available at [GitHub milestones](https://github.com/opencb/cellbase/milestones). You can report bugs or request new features at [GitHub issue tracking](https://github.com/opencb/cellbase/issues).

### Versioning
CellBase is versioned following the rules from [Semantic versioning](http://semver.org/).

### Maintainers
We recommend to contact CellBase developers by writing to OpenCB mailing list opencb@googlegroups.com. The main developers and maintainers are:
* Ignacio Medina (im411@cam.ac.uk) (_Founder and Project Leader_)
* Javier Lopez (javier.lopez@genomicsengland.co.uk)
* Pablo Arce (pablo.arce@bioinfomgp.org)

##### Other Contributors
* Marta Bleda (mb2033@cam.ac.uk)
* Antonio Rueda (antonio.rueda-martin@genomicsengland.co.uk)

##### Contributing
CellBase is an open-source and collaborative project. We appreciate any help and feeback from users, you can contribute in many different ways such as simple bug reporting and feature request. Dependending on your skills you are more than welcome to develop client tools, new features or even fixing bugs.


# How to build 
CellBase is mainly developed in Java and it uses [Apache Maven](http://maven.apache.org/) as building tool. CellBase requires Java 8+ and others OpenCB Java dependencies that can be found in [Maven Central Repository](http://search.maven.org/).

Stable releases are merged and tagged at **_master_** branch, you are encourage to use latest stable release for production. Current active development is carried out at **_develop_** branch, only compilation is guaranteed and bugs are expected, use this branch for development or for testing new functionalities. Only dependencies of **_master_** branch are ensured to be deployed at [Maven Central Repository](http://search.maven.org/), **_develop_** branch may require users to download and install other active OpenCB repositories:
* _java-common-libs_: https://github.com/opencb/java-common-libs (branch 'develop')
* _biodata_: https://github.com/opencb/biodata (branch 'develop')
* _datastore_: https://github.com/opencb/datastore (branch 'develop')

### Cloning
CellBase is an open-source and free project, you can download **_develop_** branch by executing:

    imedina@ivory:~$ git clone https://github.com/opencb/cellbase.git
    Cloning into 'cellbase'...
    remote: Counting objects: 13804, done.
    remote: Compressing objects: 100% (619/619), done.
    remote: Total 13804 (delta 324), reused 240 (delta 77)
    Receiving objects: 100% (13804/13804), 24.32 MiB | 723.00 KiB/s, done.
    Resolving deltas: 100% (5049/5049), done.


Latest stable release at **_master_** branch can be downloaded executing:

    imedina@ivory:~$ git clone -b master https://github.com/opencb/cellbase.git
    Cloning into 'cellbase'...
    remote: Counting objects: 13804, done.
    remote: Compressing objects: 100% (619/619), done.
    remote: Total 13804 (delta 324), reused 240 (delta 77)
    Receiving objects: 100% (13804/13804), 24.32 MiB | 939.00 KiB/s, done.
    Resolving deltas: 100% (5049/5049), done.


### Build
In case the CellBase installation requires to access a self-maintained installation of the CellBase database, database connection credentials must be set. We use maven to inject the credential values, you need to add this maven configuration to the profiles section at your _~.m2/settings.xml_, you may need to change these values according to your particular installation:

    <profile>
     <id>localhost</id>
     <activation>
       <activeByDefault>true</activeByDefault>
     </activation>
     <properties>
       <CELLBASE.DB.HOST>localhost:27017</CELLBASE.DB.HOST>
       <CELLBASE.DB.PORT>27017</CELLBASE.DB.PORT>
       <CELLBASE.DB.USER></CELLBASE.DB.USER>
       <CELLBASE.DB.PASSWORD></CELLBASE.DB.PASSWORD>
       <CELLBASE.DB.MONGODB.AUTHENTICATIONDATABASE>admin</CELLBASE.DB.MONGODB.AUTHENTICATIONDATABASE> 
       <CELLBASE.DB.MONGODB.READPREFERENCE>nearest</CELLBASE.DB.MONGODB.READPREFERENCE> 
       <CELLBASE.VERSION>v3</CELLBASE.VERSION> 
       <CELLBASE.ENSEMBL.LIBS>/home/imedina/apis/ensembl/api_79</CELLBASE.ENSEMBL.LIBS> 
       <CELLBASE.DB.MONGODB.REPLICASET></CELLBASE.DB.MONGODB.REPLICASET>
     </properties>
     </profile>

Now you can build CellBase by executing the following command from the root of the cloned repository:
  
    $ mvn clean install -DskipTests
    
Remember that **_develop_** branch dependencies are not ensured to be deployed at Maven Central, you may need to clone and install **_develop_** branches from OpenCB _biodata_ and _datastore_ repositories. After this you should have this file structure in **_cellbase-app/build_**:

    cellbase/build/
    ├── bin
    ├── cellbase.war
    ├── configuration.json
    ├── example
    ├── libs
    ├── LICENSE
    ├── mongodb-scripts
    └── README.md

You can copy the content of the _build_ folder into any directory such as _/opt/cellbase_.

### Testing
You can run the unit tests using Maven or your favorite IDE. Just notice that some tests may require of certain database back-ends such as MongoDB and may fail if they are not available.

### Command Line Interface (CLI)
If the build process has gone well you should get an integrated help by executing:

    ./bin/cellbase.sh --help

As you can see there are four commands implemented, each of them with different options:
 * **_download_**: this command downloads the data form different sources such as Ensembl or Uniprot to build CellBase
 * **_build_**: with this command you can run the pipelines to create the data models to be loaded into the database
 * **_load_**: this command reads the data models and convert and load them into the database
 * **_query_**: with this command you can execute queries to CellBase regardless the database used and also you can perform a variant annotation

You can find more detailed documentation and tutorials at: https://github.com/opencb/cellbase/wiki.

### Other Dependencies
We try to improve the user experience by making the installation and build as simple as possible. Unfortunately, for some CellBase commands such as _build_ and _load_ other dependencies are required.

##### Building data
We have developed fast and efficient algorithms to build almost all data models from Ensembl raw data or dumps. But CellBase still depends on Ensembl Perl API for building some specific data models, this can be installed following this tutorial:

    http://www.ensembl.org/info/docs/api/api_installation.html

##### Loading data
At this moment the only fully developed storage engine plugin is [MongoDB](https://www.mongodb.org/). MongoDB is free and open-source and can be downloaded from [here](https://www.mongodb.org/downloads).

# Supporters
JetBrains is supporting this open source project with:

[![Intellij IDEA](https://www.jetbrains.com/idea/docs/logo_intellij_idea.png)]
(http://www.jetbrains.com/idea/)
