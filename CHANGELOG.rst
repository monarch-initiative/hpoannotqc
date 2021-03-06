==========
Change Log
==========

This document records all notable changes to `HpoAnnotQc <http://hpo-annotation-qc.readthedocs.io/en/latest/#>`_.

`1.8.2`_(2020-08-09)
--------------------
* update to picocli
* update to phenol 1.6.1 for new Orphanet XML structure

`1.8.1`_(2020-06-02)
--------------------
* changed path for product4_HPO.xml due to change at Orphanet
* updating XML parser in phenol (currently using phenol-1.6.1-SNAPSHOT)

`1.8.0`_(2020-06-02)
--------------------
* Add a '#' to the start of the header line
* phenol 1.6.0
* Reduce number of warnings to shell (Set logging level back to trace to see everything, including obsolete labels from ORPHA)

`1.7.0`_(2019-10-10)
--------------------
Set default to error tolerant parsing (minor errors such as out of date hp ids are now corrected automatically).
Previously, some small files were being omitted erroneously.

`1.6.1`_(2019-06-20)
--------------------
The --merge option allows users to generate a big-file in which frequency data for the same disease/HPO are merged

`1.6.0`_(2019-06-20)
--------------------
* Creates a version of the phenotype_to_genes file.


`1.5.0`_(2019-06-20)
--------------------
* Adding support for Orphanet inheritance annotations

`1.4.3`_(2019-01-09)
--------------------
* fixing logging error
* Update for phenol 1.3.3


`1.4.2`_(2019-01-09)
--------------------
* refactored to use JCommander
* simplified class structure, now using only two classes to represent content (HpoAnnotationModel and HpoAnnotationEntry)
* Update/refactor for phenol 1.3.2

`1.4.0`_(2019-01-05)
--------------------
* fixed bug with '#' in header, added test.
* refactored and simplifed classes.
* changed name of classes from "SmallFile-*" to "HpoAnnotationFile-*".
* Added multiple new Q/C features (this part of HpoAnnotQc will be transferred to phenol).
* Unifying Orphanet ingest with small file ingest by creating HpoAnnotationFileEntry from XML data

`1.2.4`_(2018-12-11)
--------------------
* Update/refactor for phenol 1.3.1

`1.2.3`_(2018-11-10)
--------------------
* Update to JUnit 5.
* Add # to column headers in output file

`1.2.1`_(2018-07-10)
--------------------
* Refactor for new biocuration format.

`1.1.2`_(2018-07-01)
--------------------
* Refactor to enable merging of duplicated entries with different frequency data
* Update documentation

`1.0.0`_ (2018-05-19)
---------------------
* Refactor to use phenol 1.0.0
* Removal of code related to old-smallfile conversion. From now on, HpoAnnotQC will concentrate on the Q/C and
conversion of V2 small files to V2 bigfile.



`0.2.1`_ (2018-03-13)
---------------------

* Conversion of old-format small files complete.
* Conversion of new-format small files to bigfile ``phenotype.hpoa`` with Q/C

