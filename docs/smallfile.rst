HPO Annotation Small Files
==========================

We are currently in the process of updating the ontologuy


Converting old "small files" to new format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See Google doc that was sent around the phenotype list for background.
The HPO project is updating the rare disease annotation files to add some new features. This document is intended
to explain the process, but we note it is intended for internal use and will be deleted after the conversion has been
carried out.


Running HPO Annot QC to perform the conversion
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
To run the demo program, enter the following command. ::

    $ java -jar target/HpoAnnotQc.jar convert -h <path to hp.obo> -d <path to /hpo-annotation-data/rare-diseases/annotated>

Here, <path to /hpo-annotation-datrare-diseases/annotated> is the path to the ``annotated`` directory containing the original small files.

This command will create a new directory called "v2files" and write one "new" small file for each "old" small file.

To turn this into a "big" file, run the following command. ::

    $ java -jar target/HpoAnnotQc.jar big-file -h <path to hp.obo>

This will create a new file called ``phenotype_annotation2.tab``.


Note that if you first run the command to download the latest hp.obo file, the file will be placed in the default location
``data/hp.obo`` and does not need to be passed via the command line. ::

    $ java -jar target/HpoAnnotQc.jar download



Current small file format
~~~~~~~~~~~~~~~~~~~~~~~~~



The current output format for the new small files is as follows. The (proposed new format is on the annotationFormat page.




+--------+-----------------+--------------------------------+
| Column |    Item         | Comment                        |
+========+=================+================================+
| 1      | diseaseID       | OMIM, ORPHA, DECIPHER          |
+--------+-----------------+--------------------------------+
| 2      | diseaseName     | e.g., Neurofibromatosis type 1 |
+--------+-----------------+--------------------------------+
| 3      | phenotypeId     | e.g., HP:0000123               |
+--------+-----------------+--------------------------------+
| 4      | phenotypeName   | e.g., Scoliosis                |
+--------+-----------------+--------------------------------+
| 5      | ageOfOnsetId    | e.g., HP:0003581               |
+--------+-----------------+--------------------------------+
| 6      | ageOfOnsetName  | e.g., Adult onset              |
+--------+-----------------+--------------------------------+
| 7      | frequencyId     | e.g., HP:0040280               |
+--------+-----------------+--------------------------------+
| 8      | frequencyString | e.g., 5/13                     |
+--------+-----------------+--------------------------------+
| 9      | sex             | Male, Female                   |
+--------+-----------------+--------------------------------+
| 10     | negation        | NOT or not                     |
+--------+-----------------+--------------------------------+
| 11     | modifier        | semicolon sep list HPO terms   |
+--------+-----------------+--------------------------------+
| 12     | description     | free text                      |
+--------+-----------------+--------------------------------+
| 13     | publication     | e.g., PMID:123321              |
+--------+-----------------+--------------------------------+
| 14     | assignedBy      | ORCID or HPO etc               |
+--------+-----------------+--------------------------------+
| 15     | dateCreated     | e.g., 2017-01-15               |
+--------+-----------------+--------------------------------+




Decisions as to what to do with incomplete/inaccurate data
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1. missing evidence codes. For instance, OMIM:145680. Decision -- add IEA as evidence code.