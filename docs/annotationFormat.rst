HPO Annotation File Formats
===========================


The HPO annotations files are created by editing one file per disease entries (which we will call "small" files here for brevity).
These files are merged into a single file that has been called phenotype_annotation.tab.

The previous format for the small files is shown on the small files page.


Proposed new small file format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



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
| 8      | frequencyName   | e.g., Occasional               |
+--------+-----------------+--------------------------------+
| 9      | frequencyString | e.g., 5/13 (optional)          |
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

