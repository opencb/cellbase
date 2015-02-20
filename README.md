# Overview
High-Performance scalable NoSQL database and RESTful web services to access to most relevant biological data. CellBase has been designed to support different storage engines, currently MongoDB plugin is fully developed which prtovides and amazing performance and scalability.

Note: this repository comes from a major rewrite and refactoring done from previous versions, old code can still be found at: https://github.com/opencb-cloud. All users must updated to this one.

### Documentation
You can find documentation and tutorials about CellBase at: https://github.com/opencb/cellbase/wiki

### Issues Tracking
You can report bugs or request new features at [GitHub issue tracking](https://github.com/opencb/cellbase/issues)

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

#### Other Contributors
  * Marta Bleda (mb2033@cam.ac.uk)
  * Antonio Rueda (antonior@bioinfomgp.org)

# How to build 
The build process is managed by Apache Maven. CellBase building requires Java 7+ and a set of dependencies which are already uploaded to the [Maven Central Repository](http://search.maven.org/).

## Dependencies
CellBase also depends on the ENSEMBL API version 76, which may be installed following this tutorial:

http://www.ensembl.org/info/docs/api/api_installation.html
  
Build CellBase:
  
    $ mvn clean install -DskipTests

## Cloning
CellBase is open to the community and released in GitHub, so you can download by invoking the following commands:

    $ git clone https://github.com/opencb/cellbase.git
    Cloning into 'cellbase'...
    remote: Counting objects: 6653, done.
    remote: Total 6653 (delta 0), reused 0 (delta 0)
    Receiving objects: 100% (6653/6653), 4.80 MiB | 1.20 MiB/s, done.
    Resolving deltas: 100% (2651/2651), done.

## Build

