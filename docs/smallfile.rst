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
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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



Proposed new small file format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



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
| 7      | frequency       | e.g., HP:0040280 or 3/7 or 24% |
+--------+-----------------+--------------------------------+
| 8      | sex             | Male, Female                   |
+--------+-----------------+--------------------------------+
| 9      | negation        | NOT or not                     |
+--------+-----------------+--------------------------------+
| 10     | modifier        | semicolon sep list HPO terms   |
+--------+-----------------+--------------------------------+
| 11     | description     | free text                      |
+--------+-----------------+--------------------------------+
| 12     | publication     | e.g., PMID:123321              |
+--------+-----------------+--------------------------------+
| 13     | assignedBy      | ORCID or HPO etc               |
+--------+-----------------+--------------------------------+
| 14     | dateCreated     | e.g., 2017-01-15               |
+--------+-----------------+--------------------------------+


1. **diseaseID**. This field is a string that must be one of "OMIM:id", "ORPHA:id", or "DECIPHER:id". The id portion
of the name is the code given by the database, e.g., ``OMIM:157000``. Additional source databases
may be admitted in the future.

2. **diseaseName**. This field is a String that represents the label (name) of the disease in question, e.g.,
``Marfan syndrome``.

3. **phenotypeID**. This must be a valid HP id. It must be the primary id (not an alt_id) for the current version of
the HPO; if not, an error must be generated by the Q/C code; the Q/C code should allow the HPO id's and the
labels of affected annotations to be updated after manual inspection by the user.

4. **phenotypeName**. The label of the HPO term refered to by the phenotypeId field, e.g.,
``Arachnodactyly``.

5. **ageOfOnsetId**. The HPO id of a term from the ``Onset`` subhierarchy of the HPO. This must be the primary id (not the
alt_id). This field can be left empty, in which case, the ageOfOnsetName field must also be empty.

6. **ageOfOnsetName**. The label corresponding to the ageOfOnsetId.
This field can be left empty, in which case, the ageOfOnsetId field must also be empty.

7. **frequency**. This column can be one of three formats: A valid HPO term from the frequency subontology, a fractional
expression m/n (e.g., 4/7 meaning that 4 of 7 individuals in the cited study had the disease and the feature in question,
while the feature was ruled out in the remaining 3 of 7 individuals); or a percentage value such as 47%. This column may be empty.

8. **sex**. This column may be empty or may contain the strings "MALE" or "FEMALE".

9. **negation**. This column may be empty or may contain the string "NOT"

10. **modifier**. This column may be empty of contain HPO term ids for one or more terms from the
Clinical Modifier subontology. Multiple terms are to be separated by semicolons.

11. **description**. Free text. This column must not be used to store modifiers.

12. **publication**. The publication reference for the annotation assertion. Must be present and must be one of
PMID:123, OMIM:123 or ?. Note: pimd:123 is not accepted. The following prefixes are allowed:
- PMID
- OMIM
- http
- ISBN
- DECIPHER

13. **assignedBy**. This field must be filled with a valid reference of the form prefix:id. This can be
ORCID:0000-0000-0000-0123 or a database id followed by a name (usually first initial-lastname) HPO:mmustermann.

14. **dateCreated**. This field contains the date when the term was first created and must have the form yyyy-mm-dd, e.g.,
2016-07-22.




Decisions as to what to do with incomplete/inaccurate data
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1. missing evidence codes. For instance, OMIM:145680. Decision -- add IEA as evidence code.


2. Publications. We do not allow an empty publication field. For OMIM-derived annotations, we can use the dbID field
as the publication reference. There are over 1000 entries with nothing in the publication field, and I just add the
dbID (e.g., OMIM:123000). There are some entries that just have the word "OMIM" in the pub field. I have also corrected
these, e.g.,  ::

    OMIM:306955	HETEROTAXY, VISCERAL, 1, X-LINKED; HTX1					HP:0001419	X-linked recessive inheritance			TAS	TAS							OMIM	HPO:skoehler	30.12.2015

