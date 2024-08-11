package org.monarchinitiative.hpoannotqc.annotations.legacy;


import org.monarchinitiative.hpoannotqc.annotations.AspectIdentifier;
import org.monarchinitiative.hpoannotqc.annotations.DiseaseDatabase;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.*;
import org.monarchinitiative.hpoannotqc.exception.*;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import static org.monarchinitiative.phenol.annotations.formats.hpo.HpoFrequency.EXCLUDED;


/**
 * Created by peter on 1/20/2018.
 * This class represents the contents of a single annotation line.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class HpoAnnotationEntry {
  private final static Logger LOGGER = LoggerFactory.getLogger(HpoAnnotationEntry.class);
  // To match e.g. 10/20
  private static final Pattern RATIO_PATTERN = Pattern.compile("(?<numerator>\\d+)/(?<denominator>\\d+)");
  // To match an int of optionally a float percentage (e.g. 1% or 1.23456789%).
  private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(?<value>\\d+\\.?(\\d+)?)%");
  private static final String EMPTY_STRING = "";
  /**
   * The CURIE of the disease, e.g., OMIM:600201 (Field #0).
   */
  private final String diseaseID;
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

  /** List of any errors encountered while parsing this entry. */
  private final List<HpoaError> errorList;

  private final static String[] expectedFields = {"#diseaseID",
    "diseaseName",
    "phenotypeID",
    "phenotypeName",
    "onsetID",
    "onsetName",
    "frequency",
    "sex",
    "negation",
    "modifier",
    "description",
    "publication",
    "evidence",
    "biocuration"};
  /**
   * Number of tab-separated expectedFields in a valid small file.
   */
  private static final int NUMBER_OF_FIELDS = expectedFields.length;


  private final static Set<String> validDatabases = Arrays.stream(DiseaseDatabase.values())
    .map(DiseaseDatabase::prefix)
    .collect(Collectors.toUnmodifiableSet());
  /**
   * Set of allowable evidence codes.
   */
  private static final Set<String> EVIDENCE_CODES = Set.of("IEA", "TAS", "PCS");

  private static final Set<String> VALID_CITATION_PREFIXES = Set.of("PMID", "OMIM", "http", "https", "DECIPHER",
    "ORPHA", "ISBN", "ISBN-10", "ISBN-13");

  /** Errors encountered in parsing the ORPHANET data --  this requires different handling than than inhouse annots */
  //private final Set<String> orphaErrors;


  public String getDiseaseID() {
    return diseaseID;
  }

  /**
   * The disease ID is a CURIE - DATABASE:identifier.
   *
   * @return the prefix part of the diseaseID.
   */
  public String getDatabasePrefix() {
    return TermId.of(diseaseID).getPrefix();
  }

  /**
   * The disease ID is a CURIE - DATABASE:identifier.
   *
   * @return the identifier part of the diseaseID.
   */
  public String getDatabaseIdentifier() {
    return TermId.of(diseaseID).getId();
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
    return sex != null ? sex : EMPTY_STRING;
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
    return description != null ? modifier : EMPTY_STRING;
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

  HpoAnnotationEntry(String disID,
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
                     String biocuration,
                     List<HpoaError> errors) {
    this.diseaseID = disID;
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
    this.errorList = errors;
  }
  /**
   * This constructor is package-private so that we can use it for merging in
   * {@link HpoAnnotationModel}
   */
  HpoAnnotationEntry(String disID,
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
    this(disID,
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
            biocuration,
            new ArrayList<>());
  }


  /**
   * @return the row that will be written to the V2 file for this entry.
   */
  @Override
  public String toString() {
    return getRow();
  }

  public String getLineNoTabs() {
    return getRow().replaceAll("\\s+", " ");
  }


  public void addError(HpoaError hpoae) {
    errorList.add(hpoae);
  }

  public boolean hasError() {
    return ! errorList.isEmpty();
  }

  public List<HpoaError> getErrorList() {
    return errorList;
  }


  /**
   * Return the row that will be used to write the small files entries to a file. Note that
   * we replace null strings (which are a signal for no data available) with the empty string
   * to avoid the string "null" being written.
   *
   * @return One row of the "big" file corresponding to this entry
   */
  public String getRow() {
    return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
      diseaseID,
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
   * Create an {@link HpoAnnotationEntry} object for a line in an HPO Annotation file. By default, we do not
   * replace obsolete term ids here, this should be done with PhenoteFX in the original files.
   *
   * @param line     A line from an HPO Annotation file (small file)
   * @param ontology reference to HPO ontology
   * @return corresponding {@link HpoAnnotationEntry} object
   * @throws HpoAnnotQcException if there were Q/C problems with the line.
   */
  public static HpoAnnotationEntry fromLine(String line, Ontology ontology) throws HpoAnnotQcException {
    String[] A = line.split("\t");
    if (A.length != NUMBER_OF_FIELDS) {
      throw new HpoAnnotQcException(String.format("We were expecting %d expectedFields but got %d for line %s", NUMBER_OF_FIELDS, A.length, line));
    }
    String diseaseID = A[0];
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

    HpoAnnotationEntry entry = new HpoAnnotationEntry(diseaseID,
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
    // if the following method does not throw an Exception, we are good to go!
    performQualityControl(entry, ontology, diseaseName);
    return entry;
  }




  /**
   * If the frequency of an HPO term is listed in Orphanet as Excluded (0%), then we encode it as
   * a NOT (negated) term.
   *
   * @param diseaseID             Orphanet ID, e.g., ORPHA:99776
   * @param diseaseName           Orphanet disease name, e.g., Moasic trisomy 9
   * @param hpoId                 HPO id (e.g., HP:0001234) as String
   * @param hpoLabel              corresponding HPO term Label
   * @param frequency             Orphanet frequency data as TermId
   * @param ontology              reference to HPO ontology
   * @param biocuration           A String to represent provenance from Orphanet, e.g., ORPHA:orphadata[2019-01-05]
   * @param replaceObsoleteTermId if true, correct obsolete term ids and do not throw an exception.
   * @return corresponding HpoAnnotationEntry object
   */
  public static HpoAnnotationEntry fromOrphaData(String diseaseID,
                                                 String diseaseName,
                                                 String hpoId,
                                                 String hpoLabel,
                                                 TermId frequency,
                                                 Ontology ontology,
                                                 String biocuration,
                                                 boolean replaceObsoleteTermId) {

    if (hpoId == null) {
      throw new PhenolRuntimeException("Null String passed as hpoId for disease " + (diseaseID != null ? diseaseID : "n/a"));
    }
    List<HpoaError> errorList = new ArrayList<>();
    TermId phenotypeId = TermId.of(hpoId);
    // replace the frequency TermId with its string equivalent
    // except if it is Excluded, which we treat as a negative annotation
    String frequencyString = frequency.equals(EXCLUDED.id()) ? EMPTY_STRING : frequency.getValue();
    // Todo, discuss with Orphanet, probably retire the NOT
    String negationString = frequency.equals(EXCLUDED.id()) ? "NOT" : EMPTY_STRING;

    if (replaceObsoleteTermId) {
      TermId currentPhenotypeId = ontology.getPrimaryTermId(phenotypeId);
      if (currentPhenotypeId != null && !currentPhenotypeId.equals(phenotypeId)) {
        String newLabel = ontology.getTermLabel(phenotypeId).orElseThrow();
        String message = String.format("Replacing obsolete TermId \"%s\" with current ID \"%s\" (and obsolete label %s with current label %s)",
          hpoId, currentPhenotypeId.getValue(), hpoLabel, newLabel);
        HpoaError hpoae = new HpoaTermError(diseaseName, currentPhenotypeId, message);
        errorList.add(hpoae);
        phenotypeId = currentPhenotypeId;
        hpoLabel = newLabel;
      }
      // replace label if needed
      if (currentPhenotypeId != null) { // we can only get new name if we got the new id!
        String currentPhenotypeLabel = ontology.getTermLabel(phenotypeId).orElseThrow();
        if (!hpoLabel.equals(currentPhenotypeLabel)) {
          String message = String.format("Replacing obsolete Term label \"%s\" with current label \"%s\"",
                  hpoLabel, currentPhenotypeLabel);
          errorList.add(new HpoaTermError(diseaseName, currentPhenotypeId, message));
          LOGGER.warn("{}: {}", diseaseID, message);
          hpoLabel = currentPhenotypeLabel;
        }
      }
    }

    String DEFAULT_ORPHA_EVIDENCE = "TAS";

    HpoAnnotationEntry entry = new HpoAnnotationEntry(diseaseID,
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
      diseaseID,
      DEFAULT_ORPHA_EVIDENCE,
      biocuration,
      errorList);
    // if the following method does not throw an Exception, we are good to go!
    performQualityControl(entry, ontology, diseaseName);

    return entry;
  }


  /**
   * If the frequency of an HPO term is listed in Orphanet as Excluded (0%), then we encode it as
   * a NOT (negated) term.
   *
   * @param diseaseID        Orphanet ID, e.g., ORPHA:99776
   * @param diseaseName      Orphanet disease name, e.g., Moasic trisomy 9
   * @param hpoInheritanceId HPO id (e.g., HP:0001234) for an inheritance term
   * @param hpoLabel         corresponding HPO term Label
   * @param biocuration      A String to represent provenance from Orphanet, e.g., ORPHA:orphadata[2019-01-05]
   * @return corresponding HpoAnnotationEntry object
   */
  public static HpoAnnotationEntry fromOrphaInheritanceData(String diseaseID,
                                                            String diseaseName,
                                                            TermId hpoInheritanceId,
                                                            String hpoLabel,
                                                            String biocuration) {


    // These items are always empty for inheritance annotations
    String frequencyString = EMPTY_STRING;
    String negationString = EMPTY_STRING;


    return new HpoAnnotationEntry(diseaseID,
      diseaseName,
      hpoInheritanceId,
      hpoLabel,
      EMPTY_STRING,
      EMPTY_STRING,
      frequencyString,
      EMPTY_STRING,
      negationString,
      EMPTY_STRING,
      EMPTY_STRING,
      diseaseID,
      "TAS",
      biocuration);
  }




  // Q/C methods

  /**
   * This method checks all the fields of the HpoAnnotationEntry. If there is an error, then
   * it throws an Exception (upon the first error). If no exception is thrown, then the
   * no errors were found.
   *
   * @param entry    The {@link HpoAnnotationEntry} to be tested.
   * @param ontology A reference to an HpoOntology object (needed for Q/C'ing terms).
   */
  private static void performQualityControl(HpoAnnotationEntry entry, Ontology ontology, String diseaseName)  {
    checkDB(entry, diseaseName);
    checkPhenotypeFields(entry, ontology, diseaseName);
    checkAgeOfOnsetFields(entry, ontology, diseaseName);
    checkFrequency(entry, ontology, diseaseName);
    checkSexEntry(entry, diseaseName);
    checkNegation(entry, diseaseName);
    checkModifier(entry, ontology, diseaseName);
    // description is free text, nothing to check
    checkPublication(entry, diseaseName);
    checkEvidence(entry, diseaseName);
    BiocurationChecker.checkEntry(entry, diseaseName);
  }

  /**
   * Checks if the database string is in the set of valid strings ({@link #validDatabases})
   *
   * @param entry SMallFileEntry to be checked for a database String such as OMIM or ORPHA
   */
  private static void checkDB(HpoAnnotationEntry entry, String diseaseName)  {
    try {
      String db = entry.getDatabasePrefix();
      if (!validDatabases.contains(db)) {
        entry.addError( new HpoAnnotationModelError(diseaseName, String.format("Invalid database symbol: \"%s\"", db)) );
      }
    } catch (PhenolRuntimeException r) {
      String message = "Could not construct database: " + r.getMessage();
      entry.addError( new HpoAnnotationModelError(diseaseName, message));
    }
    String name = entry.getDiseaseName();
    if (name == null || name.isEmpty()) {
      entry.addError( new HpoAnnotationModelError(diseaseName,"Missing disease name"));
    }
  }




  /**
   * Check that the id is not an alt_id (i.e., out of date!)
   *
   * @param entry the {@link HpoAnnotationEntry} to be checked
   */
  private static void checkPhenotypeFields(HpoAnnotationEntry entry, Ontology ontology, String diseaseName) {
    TermId id = entry.getPhenotypeId();
    String termLabel = entry.getPhenotypeLabel();
    if (id == null) {
      entry.addError(new HpoAnnotationModelError(diseaseName,  "Phenotype id was null"));
    } else if (!ontology.containsTerm(id)) {
      String message = String.format("Could not find HPO term id (\"%s\") for \"%s\"", id, termLabel);
      entry.addError(new HpoAnnotationModelError(diseaseName,  message));
    }
    TermId primaryId = ontology.getPrimaryTermId(id);
    if (primaryId == null) {
      String msg = String.format("no primary id found for \"%s\"", id.getValue());
      //entry.addError(new TermIdError(diseaseName,id, id, msg));
      return;
    }
    Optional<String> opt = ontology.getTermLabel(primaryId);
    if (opt.isEmpty()) {
      //entry.addError(new TermIdError(diseaseName,id, primaryId, "no label found"));
    }
    String primaryLabel = opt.orElse("n/a");
    if (!primaryId.equals(id)) {
     // entry.addError(new TermIdError(diseaseName,id, primaryId, primaryLabel));
    }
    // if we get here, the TermId of the HPO Term was OK
    // now check that the label corresponds to the TermId
    if (termLabel == null || termLabel.isEmpty()) {
      String message = String.format("Missing HPO term label for id=%s", id.getValue());
      entry.addError(new HpoAnnotationModelError(diseaseName,  message));
    }
    if (!primaryLabel.equals(termLabel)) {
      String errmsg = String.format("Wrong term label %s instead of %s for %s",
        termLabel, primaryLabel, primaryId.getValue());
      LOGGER.error(errmsg);
      entry.addError(new HpoAnnotationModelError(diseaseName,  errmsg));
    }
  }


  private static void checkAgeOfOnsetFields(HpoAnnotationEntry entry, Ontology ontology, String diseaseName) {
    String onsetId = entry.getAgeOfOnsetId();
    String onsetLabel = entry.getAgeOfOnsetLabel();
    if (onsetId == null || onsetId.isEmpty()) {
      // valid, onset is not required, but let's check that there is not a stray label
      if (onsetLabel != null && !onsetLabel.isEmpty()) {
        entry.addError(new HpoAnnotationModelError(diseaseName,  "Onset ID empty but Onset label present"));
      } else {
        return; // OK!
      }
    }
    TermId tid = TermId.of(onsetId);
    if (!ontology.containsTerm(tid)) {
      String msg = String.format("Onset ID not found: \"%s\"", tid.getValue());
      entry.addError(new HpoAnnotationModelError(diseaseName,  msg));

    }
    TermId primaryId = ontology.getPrimaryTermId(tid);
    // note we do not expect an error getting the label here, and we use orElse to simplify
    // if there is an error we will see if and need to figure out
    // we also check the label further below
    String hpoLabel = ontology.getTermLabel(primaryId).orElseThrow();
    if (!primaryId.equals(tid)) {
      entry.addError(TermIdError.idDoesNotMatchPrimary(tid, primaryId));
    }
    if (! isValidInheritanceTerm(tid, ontology)) {
      String msg = "Invalid ID in onset ID field: \"" + tid + "\"";
      entry.addError(new HpoAnnotationModelError(diseaseName,  msg));
    }
    // if we get here, the Age of onset id was OK
    // now let's check the label
    if (onsetLabel == null || onsetLabel.isEmpty()) {
      entry.addError(new HpoAnnotationModelError(diseaseName,"Missing HPO term label for onset id=" + onsetId));
    }
    if (!hpoLabel.equals(onsetLabel)) {
      String errmsg = String.format("Wrong onset term label %s instead of %s for %s",
        onsetId,
        hpoLabel,
        primaryId.getValue());
      LOGGER.error(errmsg);
      entry.addError(new HpoAnnotationModelError(diseaseName,  errmsg));
    }
  }

  private static void checkEvidence(HpoAnnotationEntry entry, String diseaseName)  {
    String evi = entry.getEvidenceCode();
    if (!EVIDENCE_CODES.contains(evi)) {
      entry.addError(new HpoAnnotationModelError(diseaseName, String.format("Invalid evidence code: \"%s\"", evi)));
    }
  }


  /**
   * There are 3 correct formats for frequency. For example, 4/7, 32% (or 32.6%), or
   * an HPO term from the frequency subontology.
   */
  private static void checkFrequency(HpoAnnotationEntry entry, Ontology ontology, String diseaseName)  {
    String freq = entry.getFrequencyModifier();
    // it is ok not to have frequency data
    if (freq == null || freq.isEmpty()) {
      return;
    }
    Matcher matcher = RATIO_PATTERN.matcher(freq);
    if(matcher.matches()) {
      int numerator = Integer.parseInt(matcher.group("numerator"));
      int denominator = Integer.parseInt(matcher.group("denominator"));
      if (numerator > denominator || denominator == 0) {
        entry.addError(new HpoAnnotationModelError(diseaseName, String.format("Malformed frequency term: \"%s\"", freq)));
      } else {
        return;
      }
    }
    matcher = PERCENTAGE_PATTERN.matcher(freq);
    if(matcher.matches()){
      float percent = Float.parseFloat(matcher.group("value"));
      if (percent > 100f || percent <= 0f) {
        entry.addError(new HpoAnnotationModelError(diseaseName,String.format("Malformed frequency term: \"%s\"", freq)));
      } else {
        return;
      }
    }
    if(!freq.matches("HP:\\d{7}")) {
      // cannot be a valid frequency term
      entry.addError(new HpoAnnotationModelError(diseaseName,String.format("Malformed frequency term: \"%s\"", freq)));
    }
    // if we get here and we can validate that the frequency term comes from the right subontology,
    // then the item is valid
    TermId id;
    try {
      id = TermId.of(freq);
    if (!isValidFrequencyTerm(id, ontology)) {
      LOGGER.error(String.format("Could not get label for %s", id.getValue()));
      entry.addError(new HpoAnnotationModelError(diseaseName,String.format("Usage of incorrect term for frequency: %s [%s]",
        ontology.getTermLabel(id).orElseThrow(),
        id.getValue())));
    }
    } catch (PhenolRuntimeException pre) {
      entry.addError(new HpoAnnotationModelError(diseaseName,String.format("Could not parse frequency term id: \"%s\"", freq)));
    }
  }

  /**
   * The sex entry is used for annotations that are specific to either males or females. It is usually
   * empty. If present, it must be either MALE or FEMALE (for now we do no enforce capitalization).
   *
   */
  private static void checkSexEntry(HpoAnnotationEntry entry, String diseaseName)  {
    String sex = entry.getSex();
    if (sex == null || sex.isEmpty()) return; // OK,  not required
    if (!sex.equalsIgnoreCase("MALE") && !sex.equalsIgnoreCase("FEMALE"))
      entry.addError(new HpoaSkippableError(diseaseName,String.format("Malformed sex entry: \"%s\"", sex)));
  }

  /**
   * The negation string can be null or empty but if it is present it must be "NOT"
   * <p>
   * negation Must be either the empty/null String or "NOT"
   */
  private static void checkNegation(HpoAnnotationEntry entry, String diseaseName)  {
    String negation = entry.getNegation();
    if (negation != null && !negation.isEmpty() && !negation.equals("NOT")) {
      entry.addError(new HpoaSkippableError(diseaseName,String.format("Malformed negation entry: \"%s\"", negation)));
    }
  }

  private static void checkModifier(HpoAnnotationEntry entry,  Ontology ontology, String diseaseName)  {
    String modifierString = entry.getModifier();
    if (modifierString == null || modifierString.isEmpty()) return; // OK,  not required
    // If something is present in this field, it must be in the form of
    // HP:0000001;HP:0000002;...
    String[] A = modifierString.split(";");
    for (String a : A) {
      try {
        TermId tid = TermId.of(a);
        if (!isValidModifier(tid, ontology) && !isValidPaceOfProgressionTerm(tid, ontology) &&
          !isValidTemporalPatternTerm(tid, ontology) && isValidInheritanceModifierTerm(tid, ontology)) {
          String errmsg = String.format("Use of wrong HPO term in modifier field: %s [%s]",
                  ontology.getTermLabel(tid).orElse("n/a"),
                  tid.getValue());
          LOGGER.error(errmsg);
          entry.addError(new HpoaSkippableError(diseaseName,errmsg));
        }
      } catch (PhenolRuntimeException e) {
        entry.addError(new HpoAnnotationModelError(diseaseName,String.format("Malformed modifier term id: \"%s\"", a)));
      }
    }
  }


  private static void checkPublication(HpoAnnotationEntry entry, String diseaseName)  {
    String pub = entry.getPublication();
    if (pub == null || pub.isEmpty()) {
      entry.addError(new  MalformedCitationError(diseaseName,"Empty citation string"));
    }
    int index = pub.indexOf(":");
    if (index <= 0) { // there needs to be a colon in the middle of the string
      entry.addError(new  MalformedCitationError(diseaseName, String.format("Malformed citation id (not a CURIE): \"%s\"", pub)));
    }
    if (pub.contains("::")) { // should only be one colon separating prefix and id
      entry.addError(new  MalformedCitationError(diseaseName, String.format("Malformed citation id (double colon): \"%s\"", pub)));
    }
    if (pub.contains(" ")) {
      entry.addError(new  MalformedCitationError(diseaseName, String.format("Malformed citation id (contains space): \"%s\"", pub)));
    }
    String prefix = pub.substring(0, index);
    if (!VALID_CITATION_PREFIXES.contains(prefix)) {
      entry.addError(new  MalformedCitationError(diseaseName, String.format("Did not recognize publication prefix: \"%s\" ", pub)));
    }
    int len = pub.length();
    if (len - index < 2) {
      entry.addError(new  MalformedCitationError(diseaseName, String.format("Malformed publication string: \"%s\" ", pub)));
    }
  }



  /**
   * Todo -- consider refactor to not create new instance for each entry
   * @param tid TermId of an HPO term
   * @param ontology  HPO
   * @return String such as "P" representing the aspect
   */
  private String getAspect(TermId tid, Ontology ontology)  {
    final AspectIdentifier aspectIdentifier = new AspectIdentifier(ontology);
    return aspectIdentifier.getAspectLetter(tid);
  }


  /**
   * Following quality control of an entry that has been ingested from a small file, and potentially merged,
   * we export the corresponding line for the big file.
   * @param ontology A reference to the HPO ontology
   * @return A line for the phenotype.hpoa file
   */
  public String toBigFileLine(Ontology ontology) {
    String[] elems = {
      getDiseaseID(), //DB_Object_ID
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


  private static boolean isValidInheritanceTerm(TermId tid, Ontology hpo) {
    final TermId ONSET_ROOT = TermId.of("HP:0003674");
    return hpo.graph().existsPath(tid, ONSET_ROOT);
  }


  private static boolean isValidClinicalModifierTerm(TermId tid, Ontology hpo) {
    final TermId CLINICAL_MODIFIER_ROOT = TermId.of("HP:0012823");
    return hpo.graph().existsPath(tid, CLINICAL_MODIFIER_ROOT);
  }

  private static boolean isValidTemporalPatternTerm(TermId tid, Ontology hpo) {
    final TermId TEMPORAL_PATTERN_ROOT = TermId.of("HP:0011008");
    return hpo.graph().existsPath(tid, TEMPORAL_PATTERN_ROOT);
  }

  private static boolean isValidPaceOfProgressionTerm(TermId tid, Ontology hpo) {
    final TermId PACE_OF_PROGRESSION_ROOT = TermId.of("HP:0003679");
    return hpo.graph().existsPath(tid, PACE_OF_PROGRESSION_ROOT);
  }
  private static boolean isValidInheritanceModifierTerm(TermId tid, Ontology hpo) {
    final TermId INHERITANCE_MODIFIER_ROOT = TermId.of("HP:0034335");
    return hpo.graph().existsPath(tid, INHERITANCE_MODIFIER_ROOT);
  }


  private static boolean isValidModifier(TermId tid, Ontology ontology) {
    return isValidTemporalPatternTerm(tid, ontology) ||
            isValidPaceOfProgressionTerm(tid, ontology) ||
            isValidClinicalModifierTerm(tid, ontology) ||
            isValidInheritanceModifierTerm(tid, ontology) ||
            isValidClinicalModifierTerm(tid, ontology);
  }

  /**
   *
   * @param tid TermId of a candidate Frequency term
   * @param hpo Ontology object
   * @return true iif tid is a valid HPO frequency term
   */
  private static boolean isValidFrequencyTerm(TermId tid, Ontology hpo) {
    final TermId FREQUENCY_ROOT =  TermId.of("HP:0040279");
    return hpo.graph().existsPath(tid, FREQUENCY_ROOT);
  }


  public boolean hasSkipabbleError() {
    return errorList.stream().anyMatch(HpoaError::skippable);
  }
}
