CellBase
========

High-Performance NoSQL database and RESTful web services to access to most relevant biological data.


This repository comes from a major rewrite and refactoring done from previous versions, old code can be found at: https://github.com/opencb-cloud



CONTACT
------- 
  You can contact any of the following developers:

    * Ignacio Medina (imedina@ebi.ac.uk)
    * Javier Lopez (fjavier@bioinfomgp.org)


DOWNLOAD and BUILDING
---------------------

  CellBase is open to the community and released in GitHub, so you can download by invoking the following commands:

    $ git clone https://github.com/opencb/cellbase.git
    Cloning into 'cellbase'...
    remote: Counting objects: 6653, done.
    remote: Total 6653 (delta 0), reused 0 (delta 0)
    Receiving objects: 100% (6653/6653), 4.80 MiB | 1.20 MiB/s, done.
    Resolving deltas: 100% (2651/2651), done.

  For the most recent HPG Aligner version, choose the 'master' Git branch (both for the hpg-libs and the HPG Aligner):

    $ cd lib/hpg-libs
    $ git checkout master
    $ cd ../..
    $ git checkout master


  Before you can build HPG Aligner, you must install on your system:

    * the GSL (GNU Scientific Library), http://www.gnu.org/software/gsl/
    * the Check library, http://check.sourceforge.net/

  Finally, use Scons to build the HPG Aligner application:

    $ scons


RUNNING
-------

  For command line options invoke:

    $ ./bin/hpg-aligner -h



  In order to map DNA sequences:

    1) Create the HPG Aligner index based on suffix arrays by invoking:

      $ ./bin/hpg-aligner build-sa-index -g <ref-genome-fasta-file> -i <index-directory>

      Example:

      $./bin/hpg-aligner build-sa-index -g /hpg/genome/human/GRCH_37_ens73.fa -i /tmp/sa-index-human73/ 

    2) Map by invoking:

      $./bin/hpg-aligner dna -i <index-directory> -f <fastq-file> -o <output-directory>

      Example:

      $./bin/hpg-aligner dna -i /tmp/sa-index-human73/ -f /hpg/fastq/simulated/human/DNA/GRCh37_ens73/4M_100nt_r0.01.bwa.read1.fastq 
      ----------------------------------------------
      Loading SA tables...
      End of loading SA tables in 1.03 min. Done!!
      ----------------------------------------------
      Starting mapping...
      End of mapping in 1.40 min. Done!!
      ----------------------------------------------
      Output file        : hpg_aligner_output/out.sam
      Num. reads         : 4000000
      Num. mapped reads  : 3994693 (99.87 %)
      Num. unmapped reads: 5307 (0.13 %)
      ----------------------------------------------


  In order to map RNA sequences:

    A) Map Based on Burrows-Wheeler Transform method for slow memory consumption
       1) Create the BWT HPG Aligner index

         $ ./bin/hpg-aligner build-bwt-index -g <ref-genome-fasta-file> -i <index-directory> -r <compression-ratio>
	 
	 Example:

	 $ ./bin/hpg-aligner build-bwt-index -g /home/user/Homo_sapiens.fa -i /home/user/INDEX/  -r 8

       2) Map by invoking:

         $ ./bin/hpg-aligner rna -i <index-directory> -f <fastq-file> -o <output-directory>

         Example:

         $ ./bin/hpg-aligner rna -i /home/user/INDEX/ -f reads.fq
         +===============================================================+
         |                           RNA MODE                            |
         +===============================================================+
         |      ___  ___  ___                                            |
         |    \/ H \/ P \/ G \/                                          |
      	 |    /\___/\___/\___/\                                          |
         |      ___  ___  ___  ___  ___  ___  ___                        |
         |    \/ A \/ L \/ I \/ G \/ N \/ E \/ R \/                      |
         |    /\___/\___/\___/\___/\___/\___/\___/\                      |
         |                                                               |
         +===============================================================+

         WORKFLOW 1
         [|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||]  100%

         WORKFLOW 2
         [|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||]  100%

         WORKFLOW 3
         [|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||]  100%

         +===============================================================+
         |        H P G - A L I G N E R    S T A T I S T I C S           |
         +===============================================================+
         |              T I M E    S T A T I S T I C S                   |
         +---------------------------------------------------------------+
          Loading time                            :  5.69 (s)
          Alignment time                          :  438.09 (s)
          Total time                              :  443.79 (s)
         +---------------------------------------------------------------+
         |            M A P P I N G    S T A T I S T I C S               |
         +---------------------------------------------------------------+
          Total reads process                     :  10000000
          Total reads mapped                      :  9977505 (99.78%)
          Total reads unmapped                    :  22495 (0.22%)
          Reads with a single mapping             :  9532383 (95.32%)
          Reads with multi mappings               :  445122 (4.45%)
          Reads mapped with BWT phase             :  6441691 (64.42%)
          Reads mapped in workflow 1              :  9737937 (97.38%)
          Reads mapped in workflow 2              :  149381 (1.49%)
          Reads mapped in workflow 3              :  90187 (0.90%)
         +---------------------------------------------------------------+
         |    S P L I C E    J U N C T I O N S    S T A T I S T I C S    |
         +---------------------------------------------------------------+
          Total splice junctions                  :  2556653
          Total cannonical splice junctions       :  2535847 (99.19%)
          Total semi-cannonical splice junctions  :  20806 (0.81%)
         +---------------------------------------------------------------+

    B) Map Based on SA method for more speed

       1) Create the SA HPG Aligner index

         $ ./bin/hpg-aligner build-sa-index -g <ref-genome-fasta-file> -i <index-directory>
	 
	 Example:

	 $./bin/hpg-aligner build-sa-index  -g /home/user/Homo_sapiens.fa -i /home/user/INDEX/

       2) Map by invoking:

         $ ./bin/hpg-aligner rna -i <index-directory> -f <fastq-file> -o <output-directory> --sa-mode

         Example:

         $ ./bin/hpg-aligner rna  -i /home/user/INDEX/ -f reads.fq -o /home/user/sa_output --sa-mode
    

DOCUMENTATION
-------------


  You can find more info about HPG project at:

  http://wiki.opencb.org/projects/hpg/doku.php
