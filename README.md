# Overview
An integrative, scalable and high-performance NoSQL database with a Java RESTful web services API to query the most relevant biological information. Different data sources are integrated and laoded into CellBase such as Ensembl, Uniprot, IntAct, ClinVar among others. Also, a variant annotation tool has been implemented. CellBase has been designed to support different storage engines, currently MongoDB plugin is fully developed which provides an amazing performance and scalability.

CellBase constitutes the knowledge-base component of [OpenCB](http://www.opencb.org/) initiative, it is used by other OpenCB projects such as [OpenCGA](https://github.com/opencb/opencga) as well as other external applications such as [EBI EVA](www.ebi.ac.uk/eva/) or [Babelomics](http://www.babelomics.org/).

Note: this repository is a major refactoring of: https://github.com/opencb-cloud. All users must update to this one.

### Documentation
You can find documentation and tutorials about CellBase at: https://github.com/opencb/cellbase/wiki

### Issues Tracking
You can report bugs or request new features at [GitHub issue tracking](https://github.com/opencb/cellbase/issues).

### Release Notes and Roadmap
Releases notes are available at [GitHub releases](https://github.com/opencb/cellbase/releases).

Roadmap are available at [GitHub milestones](https://github.com/opencb/cellbase/milestones). You can request features at [GitHub issue tracking](https://github.com/opencb/cellbase/issues).

### Versioning
CellBase is versioned following the rules from [Semantic versioning](http://semver.org/).

### Maintainers
 You can contact any of the following main developers:
  * Ignacio Medina (im411@cam.ac.uk)
  * Javier Lopez (fjlopez@ebi.ac.uk)
  * Pablo Arce (pablo.arce@bioinfomgp.org)

##### Other Contributors
  * Marta Bleda (mb2033@cam.ac.uk)
  * Antonio Rueda (antonior@bioinfomgp.org)

##### Contributing
CellBase is an open-source and collaborative project. We appreciate any help and collaboraton, you can contribute in many different ways such as bug reporting, feature request or developing client tools or even bug fixes or new features dependending on your skills.


# How to build 
CellBase is mainly developed in Java and it uses [Apache Maven](http://maven.apache.org/) as build tool. CellBase building requires Java 7+ and a set of other OpenCB Java dependencies that can be found to [Maven Central Repository](http://search.maven.org/).

### Cloning
CellBase is open to the community and released in GitHub, so you can download by invoking the following commands:

    $ git clone https://github.com/opencb/cellbase.git
    Cloning into 'cellbase'...
    remote: Counting objects: 6653, done.
    remote: Total 6653 (delta 0), reused 0 (delta 0)
    Receiving objects: 100% (6653/6653), 4.80 MiB | 1.20 MiB/s, done.
    Resolving deltas: 100% (2651/2651), done.

### Build
You can build CellBase by executing the following command from the root of the cloned repository:
  
    $ mvn clean install -DskipTests

After this you should have this file structure in _cellbase-app/build_:

    cellbase-app/build/
    ├── bin
    ├── example
    ├── libs
    ├── mongodb-scripts

You can copy the content of the _build_ folder into any directory such as _/opt/cellbase_.

### Command Line Interface (CLI)
If the build process has gone well you should get an integrated help by executing:

    ./bin/cellbase.sh --help

As you can see there are four commands implemented, each of them with different options:
 * _download_: this command downloads the data form different sources such as Ensembl or Uniprot to build CellBase
 * _build_: with this command you can run the pipelines to create the data models to be loaded into the database
 * _load_: this command reads the data models and convert and load them into the database
 * _query_: with this command you can execute queries to CellBase regardless the database used and also you can perform a variant annotation

We try to improve the user experience by making the installation and building as simple as possible. In order to this we have developed efficient algorithms to generate all data models. Unfortunately, for some specific Ensembl data users still need to install Ensembl Perl API, in general raw data or dumps are downloaded to generate data models instead of using Perl API that is too slow. Other general dependencies of CellBase include MongoDB to load the data.

##### Building some Ensembl data
CellBase also depends on the Ensembl Perl API, which may be installed following this tutorial:

    http://www.ensembl.org/info/docs/api/api_installation.html

##### Loading data into MongoDB
The only fully developed storage engine plugin at this moment is [MongoDB](https://www.mongodb.org/). MongoDB is free and open-source and can be downloaded from [here](https://www.mongodb.org/downloads).
