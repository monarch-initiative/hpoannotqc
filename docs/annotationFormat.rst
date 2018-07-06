HPO Annotation File Formats
===========================


The HPO annotations files are created by editing one file per disease entries (which we will call "small" files here for brevity).
These files are merged into a single file that has been called ``phenotype_annotation.tab``.


New and old big file formats
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
THe HPO project has used the file format described `here <http://human-phenotype-ontology.github.io/documentation.html#annot>`_
for annotation data. The original format is showing its age, and so we will be transitioning
to the new format in 2018. The new file will be called ``phenotype.hpoa``. During a transition time
whose duration will be determined by the needs of our users, we will continue to offer the
``phenotype_annotation.tab`` file.

The following table explains the format of the ``phenotype.hpoa`` file.


The first few lines present metadata (comments) preceeded by hash signs (#) at the beginning of the lines. ::

  #description: HPO phenotype annotations for rare diseases
  #date: YYYY-MM-DD
  #version: YYYY-MM-DD
  #source_date: Human Phenotype Ontology (hp.obo) version from XXX
  #tracker: https://github.com/obophenotype/human-phenotype-ontology
  #contributor: list of biocurator IDs, and ORCIDs




+----+-------------------+----------+--------------------------------+
| Nr |   Content         | Required | Example                        |
+====+===================+===========================================+
| 1  | DatabaseId        |     Yes  | MIM:154700                     |
+----+-------------------+----------+--------------------------------+
| 2  | DB_Name           |     Yes  | Achondrogenesis, type IB       |
+----+-------------------+----------+--------------------------------+
| 3  | Qualifier         |     No   | NOT                            |
+----+-------------------+----------+--------------------------------+
| 4  | HPO_ID            |    Yes   | HP:0002487                     |
+----+-------------------+----------+--------------------------------+
| 5  | DB_Reference      |    Yes   | OMIM:154700 or PMID:15517394   |
+----+-------------------+----------+--------------------------------+
| 6  | Evidence          |    Yes   | IEA                            |
+----+-------------------+----------+--------------------------------+
| 7  | Onset             |    No    | HP:0003577                     |
+----+-------------------+----------+--------------------------------+
| 8  |Frequency          |    No    | HP:0003577 or 12/45 or 22%     |
+----+-------------------+----------+--------------------------------+
| 9  |Sex                |    No    | MALE or FEMALE                 |
+----+-------------------+----------+--------------------------------+
| 10 |Modifier           |    No    | HP:0025257 (";"-separated list)|
+----+-------------------+----------+--------------------------------+
| 11 |Aspect             |    Yes   | "P" or "C" or "I" or "M"       |
+----+-------------------+----------+--------------------------------+
| 12 |BiocurationBy      |    Yes   | HPO:skoehler[YYYY-MM-DD]       |
+----+-------------------+----------+--------------------------------+


**Explanations**

1. **DatabaseId**: This field refers to the database from which the identifier in DB_Object_ID (column 2) is drawn. At present,
annotations from the OMIM, ORHPANET, DECIPHER, and the HPO team are available. This field must be formated as a
valid CURIE, e.g., OMIM:1547800,DECIPHER:22, ORPHANET:5431


2. **DB_Name**: This is the name of the disease associated with the DB_Object_ID in the database.
Only the accepted name should be used, synonyms should not be listed here.

3. **Qualifier**: This optional field can be used to qualify the annotation shown in field 5. The field can only be used to record "NOT" or is empty. A value
of NOT indicates that the disease in question is not characterized by the indicated HPO term. This is used to record phenotypic features that can be of
special differential diagnostic utility.

4. **HPO_ID**: This field is for the HPO identifier for the term attributed to the DB_Object_ID.
This field is mandatory, cardinality 1.

5. **DB_Reference**: This required field indicates the source of the information used for the annotation.
This may be the clinical experience of the annotator or may be taken from an article as indicated by a pubmed id. Each collaborating center of the Human Phenotype Ontology consortium is assigned a HPO:Ref id. In addition, if appropriate, a pubmed id for an article describing the clinical abnormality may be used.

6. **Evidence**: This required field indicates the level of evidence supporting the annotation. The HPO project currently
uses three evidence codes.

* **IEA** (inferred from electronic annotation): Annotations  extracted by parsing the Clinical Features sections of the Online Mendelian Inheritance in Man resource are assigned the evidence code “IEA”.
* **PCS** (published clinical study) is used for used for information extracted from articles in the medical literature. Generally, annotations of this type will include the pubmed id of the published study in the ``DB_Reference`` field.
* **TAS** (traceable author statement) is used for information gleaned from knowledge bases such as OMIM or Orphanet that have derived the information frm a published source..

7. **Onset**: A term-id from the HPO-sub-ontology below the term
“Age of onset” (HP:0003674). Note that if an HPO onset term is used in this field, it refers to the onset of the
feature specified in field 5 in the disease being annotated. On the other hand, if an HPO onset term is used
in field 5, then it refers to the overall onset of the disease. In this case, no additional onset term should be
used in field 8.

8. **Frequency**: There are three allowed options for this field.
**(A)** A term-id from the HPO-sub-ontology below the term “Frequency” (HP:0040279).
(since December 2016 ; before was a mixture of values). The terms for frequency are in alignment with Orphanet.
* **(B)** A count of patients affected within a cohort. For instance, 7/13 would indicate that 7 of the 13 patients with the
specified disease were found to have the phenotypic abnormality referred to by the HPO term in question in the study
refered to by the DB_Reference; **(C)** A percentage value such as 17%.

9. **Sex**: This field contains the strings MALE or FEMALE if the annotation in question is limited to
males or females. This field refers to the phenotypic (and not the chromosomal) sex, and does not intend to capture
the further complexities of sex determination. If a phenotype is limited to one or the other sex, then the corresponding
term for the "Clinical modifier" subontology should also be used in the Modifier field.

10. **Modifier**: A term-id from the HPO-sub-ontology below the
term "Clinical modifier".


11. **Aspect**: one of P (Phenotypic abnormality), I (inheritance), C (onset and clinical course), M (clinical modifier).
This field is mandatory; cardinality 1.

* Terms with the ``P`` aspect are located in the Phenotypic abnormality subontology.
* Terms with the ``I`` aspect are from the Inheritance subontology.
* Terms with the ``C`` aspect are located in the Clinical course subontology, which includes onset, mortality, and other terms related to the temporal aspects of disease.
* Terms with the ``M`` aspect are located in the Clinical Modifier subontology.


12. **BiocurationBy**: This refers to the biocurator who made the
annotation and the date on which the annotation was made; the date format is ``YYYY-MM-DD``.
The first entry in this field refers to the creation date. Any additional biocuration is recorded
following a semicolon. So, if Joseph curated on July 5, 2012, and Suzanna curated on December 7, 2015, one might
have a field like this: ``HPO:Joseph[2012-07-05];HPO:Suzanna[2015-12-07]``. It is acceptable to use ORCID ids.
This field is mandatory,
cardinality 1

13. **Assigned By**:

