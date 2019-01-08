==========
Change Log
==========

This document records all notable changes to `HpoAnnotQc <http://hpo-annotation-qc.readthedocs.io/en/latest/#>`_.

`1.4.2`_(2010-01-09)
--------------------
* refactored to use JCommander
* simplified class structure, now using only two classes to represent content (HpoAnnotationModel and HpoAnnotationEntry)

`1.4.0`_(2010-01-05)
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

