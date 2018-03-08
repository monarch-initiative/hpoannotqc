HPO Annotation File Formats
===========================


The HPO annotations files are created by editing one file per disease entries (which we will call "small" files here for brevity).
These files are merged into a single file that has been called phenotype_annotation.tab.


Previous big file format
~~~~~~~~~~~~~~~~~~~~~~~~
THe HPO project has used the file format described `Here <http://human-phenotype-ontology.github.io/documentation.html#annot>_`
for annotation data. Owing to the development of the project, it was decided to update the format. We will be transitioning
to the new format in 2018.



Proposed new big file format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~


+----+-------------------+---------+--------------------------------+
| Nr |   Content         | Required| Example                        |
+====+===================+==========================================+
| 1  | DB                |     Yes |MIM, ORPHA, DECIPHER            |
+----+-------------------+------------------------------------------+
| 2  | DB_Object_ID      |     Yes |154700                          |
+----+-------------------+------------------------------------------+
| 3  | DB_Name           |     Yes |Achondrogenesis, type IB        |
+----+-------------------+------------------------------------------+
| 4  | Qualifier         |     No  |NOT                             |
+----+-------------------+------------------------------------------+
| 5  | HPO_ID            |    Yes  |HP:0002487                      |
+----+-------------------+------------------------------------------+
| 6  | DB_Reference      |    Yes  |OMIM:154700 or PMID:15517394    |
+----+-------------------+------------------------------------------+
| 7  | Evidence_Code     |    Yes  | IEA                            |
+----+-------------------+------------------------------------------+
| 8  | Onset             |    No   | HP:0003577                     |
+----+-------------------+------------------------------------------+
| 9  |Frequency          |    No   | HP:0003577 or 12/45 or 22%     |
+----+-------------------+------------------------------------------+
| 10 |Sex                |    No   | MALE or FEMALE                 |
+----+-------------------+------------------------------------------+
| 11 |Modifier           |    No   | HP:0025257 (";"-separated list)|
+----+-------------------+------------------------------------------+
| 12 |Aspect             |    Yes  | "P" or "C" or "I"              |
+----+-------------------+------------------------------------------+
| 13 |Date_Created       |    Yes  | YYYY-MM-DD                     |
+----+-------------------+------------------------------------------+
| 14 |Assigned_By        |    Yes  | HPO:skoehler                   |
+----+-------------------+------------------------------------------+

**Explanations**

1. **DB**: This field refers to the database from which the identifier in DB_Object_ID (column 2) is drawn. At present,
annotations from the OMIM, ORHPANET, DECIPHER, and the HPO team are available.

2. **DB_Object_ID**: This is the identified of the annotated disease within the database indicated in column 1.
Note that for OMIM identifiers, the symbol preceding the MIM number is omitted (*,#,+,%).

3. **DB_Name**: This is the name of the disease associated with the DB_Object_ID in the database.
Only the accepted name should be used, synonyms should not be listed here.

4. **Qualifier**: This optional field can be used to qualify the annotation shown in field 5. The field can only be used to record "NOT" or is empty. A value
of NOT indicates that the disease in question is not characterized by the indicated HPO term. This is used to record phenotypic features that can be of
special differential diagnostic utility.

5. **HPO_ID**: This field is for the HPO identifier for the term attributed to the DB_Object_ID.
This field is mandatory, cardinality 1.

6. **DB_Reference**: This required field indicates the source of the information used for the annotation.
This may be the clinical experience of the annotator or may be taken from an article as indicated by a pubmed id. Each collaborating center of the Human Phenotype Ontology consortium is assigned a HPO:Ref id. In addition, if appropriate, a pubmed id for an article describing the clinical abnormality may be used.

7. **Evidence_Code**: This required field indicates the level of evidence supporting the annotation.
Annotations  extracted by parsing the Clinical Features sections of the Online Mendelian Inheritance in Man resource
are assigned the evidence code “IEA” (inferred from electronic annotation). Other codes include “PCS” for published clinical study.
This should be used for information extracted from articles in the medical literature. Generally, annotations of this type will
include the pubmed id of the published study in the ``DB_Reference`` field. Finally, “ICE” can be used for annotations based on
individual clinical experience. This may be appropriate for disorders with a limited amount of published data.
This must be accompanied by an entry in the DB:Reference field denoting the individual or center performing the annotation
together with an identifier. For instance, GH:007 might be used to refer to the seventh such annotation made by a specialist
from Gotham Hospital. (assuming the prefix GH has been registered with the HPO).

8. **Onset**: A term-id from the HPO-sub-ontology below the term
“Age of onset” (HP:0003674). Note that if an HPO onset term is used in this field, it refers to the onset of the
feature specified in field 5 in the disease being annotated. On the other hand, if an HPO onset term is used
in field 5, then it refers to the overall onset of the disease. In this case, no additional onset term should be
used in field 8.

9. **Frequency**: There are three allowed options for this field.
**(A)** A term-id from the HPO-sub-ontology below the term “Frequency” (HP:0040279).
(since December 2016 ; before was a mixture of values). The terms for frequency are in alignment with Orphanet.
* **(B)** A count of patients affected within a cohort. For instance, 7/13 would indicate that 7 of the 13 patients with the
specified disease were found to have the phenotypic abnormality referred to by the HPO term in question in the study
refered to by the DB_Reference; **(C)** A percentage value such as 17%.

10. **Sex**: This field contains the strings MALE or FEMALE if the annotation in question is limited to
males or females. This field refers to the phenotypic (and not the chromosomal) sex, and does not intend to capture
the further complexities of sex determination. If a phenotype is limited to one or the other sex, then the corresponding
term for the "Clinical modifier" subontology should also be used in the Modifier field. TODO

11. **Modifier**: A term-id from the HPO-sub-ontology below the
term "Clinical modifier".


12. **Aspect**: one of P (Phenotypic abnormality), I (inheritance), C (onset and clinical course).
This field is mandatory; cardinality 1. Terms with the ``P`` aspect are located in the Phenotypic abnormality
subontology. Terms with the ``I`` aspect are from the Inheritance subontology. Terms with the ``C`` aspect
are located in the Clinical course subontology, which includes onset, mortality, and other terms related to the
temporal aspects of disease.


13. **Date_Created**: Date on which the annotation was made; format is YYYY.MM.DD this field is mandatory,
cardinality 1

14. **Assigned By**: This refers to the biocurator who made the
annotation.

