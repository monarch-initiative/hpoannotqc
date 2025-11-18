package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.*;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import static org.monarchinitiative.phenol.annotations.formats.hpo.HpoFrequency.EXCLUDED;

/**
 * Created by peter on 1/20/2018.
 * This class represents the contents of a single annotation line.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class HpoAnnotationEntry {
  private static final String EMPTY_STRING = "";
  // ...existing code...
  /**
   * The CURIE of the disease, e.g., OMIM:600201 (Field #0).
   */
  private final TermId diseaseId;
  /**
   * Field #2
   */
  private final String diseaseName;
  /**
   * Field #3
   */
  private final TermId phenotypeId;
  /**
   * Field #4
   */
  private final String phenotypeName;
  /**
   * Field #5
   */
  private final String ageOfOnsetId;
  /**
   * Field #6
   */
  private final String ageOfOnsetName;
  /**
   * Field #7
   */
  private final String evidenceCode;
  /**
   * Field #8 can be one of N/M, X% or a valid frequency term identifier.
   */
  private final String frequencyModifier;
  /**
   * Field #9
   */
  private final String sex;
  /**
   * Field #10
   */
  private final String negation;
  /**
   * Field #11
   */
  private final String modifier;
  /**
   * Field #12
   */
  private final String description;
  /**
   * Field #13
   */
  private final String publication;
  /**
   * Field #14
   */
  private final String biocuration;

  public String getDiseaseId() {
    return diseaseId.getValue();
  }

  public TermId getDiseaseTermId(){
    return diseaseId;
  }

  /**
   * The disease ID is a CURIE - DATABASE:identifier.
   *
   * @return the prefix part of the diseaseID.
   */
  public String getDatabasePrefix() {
    return diseaseId.getPrefix();
  }

  /**
   * The disease ID is a CURIE - DATABASE:identifier.
   *
   * @return the identifier part of the diseaseID.
   */
  public String getDatabaseIdentifier() {
    return diseaseId.getId();
  }

  /**
   * @return the disease name, e.g., Noonan syndrome.
   */
  public String getDiseaseName() {
    return diseaseName;
  }

  /**
   * @return HPO id of this annotation.
   */
  public TermId getPhenotypeId() {
    return phenotypeId;
  }

  /**
   * @return HPO term label of this annotation.
   */
  public String getPhenotypeLabel() {
    return phenotypeName;
  }

  /**
   * @return HPO Id of the age of onset, or null.
   */
  public String getAgeOfOnsetId() {
    return ageOfOnsetId;
  }

  /**
   * @return HPO term label of age of onset or empty string.
   */
  public String getAgeOfOnsetLabel() {
  return ageOfOnsetName != null ? ageOfOnsetName : EMPTY_STRING;
  }

  /**
   * @return evidence for this annotation (one of IEA, PCS, TAS).
   */
  public String getEvidenceCode() {
    return evidenceCode;
  }

  /**
   * @return String representing the frequency modifier.
   */
  public String getFrequencyModifier() {
  return frequencyModifier != null ? frequencyModifier : EMPTY_STRING;
  }

  /**
   * @return String represeting the sex (MALE or FEMALE) or Empty string.
   */
  public String getSex() {
    return sex;
  }

  /**
   * @return the String "NOT" or the empty string.
   */
  public String getNegation() {
  return negation != null ? negation : EMPTY_STRING;
  }

  /**
   * @return list of one or more HPO term ids (as a semicolon-separated String), or emtpry string.
   */
  public String getModifier() {
  return modifier != null ? modifier : EMPTY_STRING;
  }

  /**
   * @return (optional) free text description.
   */
  public String getDescription() {
    return description != null ? description : EMPTY_STRING;
  }

  /**
   * @return the citation supporting the annotation, e.g., a PubMed ID.
   */
  public String getPublication() {
    return publication;
  }

  /**
   * @return a string representing the biocuration history.
   */
  public String getBiocuration() {
    return biocuration;
  }

  HpoAnnotationEntry(TermId diseaseId,
                     String diseaseName,
                     TermId phenotypeId,
                     String phenotypeName,
                     String ageOfOnsetId,
                     String ageOfOnsetName,
                     String frequencyString,
                     String sex,
                     String negation,
                     String modifier,
                     String description,
                     String publication,
                     String evidenceCode,
                     String biocuration) {
    this.diseaseId = diseaseId;
    this.diseaseName = diseaseName;
    this.phenotypeId = phenotypeId;
    this.phenotypeName = phenotypeName;
    this.ageOfOnsetId = ageOfOnsetId;
    this.ageOfOnsetName = ageOfOnsetName;
    this.frequencyModifier = frequencyString;
    this.sex = sex;
    this.negation = negation;
    this.modifier = modifier;
    this.description = description;
    this.publication = publication;
    this.evidenceCode = evidenceCode;
    this.biocuration = biocuration;
  }

  /**
   * @return the row that will be written to the V2 file for this entry.
   */
  @Override
  public String toString() {
    return getRow();
  }


  /**
   * Return the row that will be used to write the small files entries to a big file. Note that
   * we replace null strings (which are a signal for no data available) with the empty string
   * to avoid the string "null" being written.
   *
   * @return One row of the "big" file corresponding to this entry
   */
  public String getRow() {
    return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
      diseaseId,
      diseaseName,
      phenotypeId.getValue(),
      phenotypeName,
      ageOfOnsetId != null ? ageOfOnsetId : EMPTY_STRING,
      ageOfOnsetName != null ? ageOfOnsetName : EMPTY_STRING,
      frequencyModifier != null ? frequencyModifier : EMPTY_STRING,
      sex != null ? sex : EMPTY_STRING,
      negation != null ? negation : EMPTY_STRING,
      modifier != null ? modifier : EMPTY_STRING,
      description != null ? description : EMPTY_STRING,
      publication != null ? publication : EMPTY_STRING,
      evidenceCode != null ? evidenceCode : "",
      biocuration != null ? biocuration : EMPTY_STRING);
  }

  /**
   * Following quality control of an entry that has been ingested from a small file, and potentially merged,
   * we export the corresponding line for the big file.
   * @param ontology A reference to the HPO ontology
   * @return A line for the phenotype.hpoa file
   */
  public String toBigFileLine(Ontology ontology) {
    String[] elems = {
            getDiseaseId(), //DB_Object_ID
            getDiseaseName(), // DB_Name
            getNegation(), // Qualifier
            getPhenotypeId().getValue(), // HPO_ID
            getPublication(), // DB_Reference
            getEvidenceCode(), // Evidence_Code
            getAgeOfOnsetId() != null ? getAgeOfOnsetId() : EMPTY_STRING, // Onset
            getFrequencyModifier() != null ? getFrequencyModifier() : EMPTY_STRING, // Frequency
            getSex(), // Sex
            getModifier(), // Modifier
            getAspect(getPhenotypeId(), ontology), // Aspect
            getBiocuration() // Biocuration
    };
    return String.join("\t", elems);
  }

  private String getAspect(TermId tid, Ontology ontology)  {
    final AspectHelper aspectHelper = new AspectHelper(ontology);
    return aspectHelper.parse(tid).toString();
  }


 /**
  * Create an {@link HpoAnnotationEntry} object for a line in an HPO Annotation file.
  *
  * @param line A line from an HPO Annotation file (small file)
  * @return corresponding {@link HpoAnnotationEntry} object.
  */
  public static HpoAnnotationEntry fromLine(String line) throws HpoAnnotQcException {
    String[] A = line.split("\t");
    if (A.length != 14) {
      throw new HpoAnnotQcException(String.format("We were expecting %d expectedFields but got %d for line %s", 14, A.length, line));
    }
    TermId diseaseId = TermId.of(A[0]);
    String diseaseName = A[1];
    TermId phenotypeId = TermId.of(A[2]);
    String phenotypeName = A[3];
    String ageOfOnsetId = A[4];
    String ageOfOnsetName = A[5];
    String frequencyString = A[6];
    String sex = A[7];
    String negation = A[8];
    String modifier = A[9];
    String description = A[10];
    String publication = A[11];
    String evidenceCode = A[12];
    String biocuration = A[13];
    
    try {
		return new HpoAnnotationEntry(diseaseId,
		  diseaseName,
		  phenotypeId,
		  phenotypeName,
		  ageOfOnsetId,
		  ageOfOnsetName,
		  frequencyString,
		  sex,
		  negation,
		  modifier,
		  description,
		  publication,
		  evidenceCode,
		  biocuration);
    } catch (Exception e) {
      throw new HpoAnnotQcException("Error creating HpoAnnotationEntry: " + line);
    }
  }

  /**
   * If the frequency of an HPO term is listed in Orphanet as Excluded (0%), then we encode it as
   * a NOT (negated) term.
   *
   * @param diseaseId             Orphanet ID, e.g., ORPHA:99776
   * @param diseaseName           Orphanet disease name, e.g., Moasic trisomy 9
   * @param hpoId                 HPO id (e.g., HP:0001234) as String
   * @param hpoLabel              corresponding HPO term Label
   * @param frequency             Orphanet frequency data as TermId
   * @param biocuration           A String to represent provenance from Orphanet, e.g., ORPHA:orphadata[2019-01-05]
   * @return corresponding HpoAnnotationEntry object
   */
  public static HpoAnnotationEntry fromOrphaData(String diseaseId,
                                                 String diseaseName,
                                                 String hpoId,
                                                 String hpoLabel,
                                                 TermId frequency,
                                                 String biocuration) {

    if (hpoId == null) {
      throw new PhenolRuntimeException("Null String passed as hpoId for disease " + (diseaseId != null ? diseaseId : "n/a"));
    }

    TermId diseaseTermId = TermId.of(diseaseId);
    TermId phenotypeId = TermId.of(hpoId);
    // replace the frequency TermId with its string equivalent
    // except if it is Excluded, which we treat as a negative annotation
    String frequencyString = frequency.equals(EXCLUDED.id()) ? EMPTY_STRING : frequency.getValue();
    // NOTE: The NOT negation for Excluded frequency is legacy; consider retiring if Orphanet agrees.
    String negationString = frequency.equals(EXCLUDED.id()) ? "NOT" : EMPTY_STRING;

    String DEFAULT_ORPHA_EVIDENCE = "TAS";

    final HpoAnnotationEntry entry = new HpoAnnotationEntry(diseaseTermId,
      diseaseName,
      phenotypeId,
      hpoLabel,
      EMPTY_STRING,
      EMPTY_STRING,
      frequencyString,
      EMPTY_STRING,
      negationString,
      EMPTY_STRING,
      EMPTY_STRING,
      diseaseTermId.getValue(),
      DEFAULT_ORPHA_EVIDENCE,
      biocuration);
      return entry;
  }

  /**
   * Create a new HpoAnnotationEntry with updated phenotype ID and label.
   * Used when the current phenotype ID or label is obsolete.
   *
   * @param newPhenotypeId The updated primary phenotype ID
   * @param newPhenotypeLabel The updated primary phenotype label
   * @return A new HpoAnnotationEntry with updated phenotype information
   */
  public HpoAnnotationEntry withUpdatedPhenotype(TermId newPhenotypeId, String newPhenotypeLabel) {
    return new HpoAnnotationEntry(
      this.diseaseId,
      this.diseaseName,
      newPhenotypeId,
      newPhenotypeLabel,
      this.ageOfOnsetId,
      this.ageOfOnsetName,
      this.frequencyModifier,
      this.sex,
      this.negation,
      this.modifier,
      this.description,
      this.publication,
      this.evidenceCode,
      this.biocuration
    );
  }

  /**
   * Create a new HpoAnnotationEntry with updated age of onset ID and label.
   * Used when the current onset ID or label is obsolete.
   *
   * @param newOnsetId The updated primary onset ID
   * @param newOnsetLabel The updated primary onset label
   * @return A new HpoAnnotationEntry with updated onset information
   */
  public HpoAnnotationEntry withUpdatedOnset(String newOnsetId, String newOnsetLabel) {
    return new HpoAnnotationEntry(
      this.diseaseId,
      this.diseaseName,
      this.phenotypeId,
      this.phenotypeName,
      newOnsetId,
      newOnsetLabel,
      this.frequencyModifier,
      this.sex,
      this.negation,
      this.modifier,
      this.description,
      this.publication,
      this.evidenceCode,
      this.biocuration
    );
  }
}
