package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.monarchinitiative.hpoannotqc.TermValidator;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.legacy.HpoAnnotationModel;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoAnnotationModelError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse of a single HPO Annotation File into a {@link HpoAnnotationModel} object. The HPO project uses a single
 * tab-separated file with 14 fields (see {@link #expectedFields}) to store information about individual
 * diseases. Colloquially, we have called these files "small-files" to distinguish them from the
 * {@code phenotype.hpoa} file that is created by combining the information from all ca. 8500 small files
 * (and which colloquially we have called the "big-file").
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 2/05/2018.
 */
public class HpoProjectAnnotationFileParser {
  private final static Logger LOGGER = LoggerFactory.getLogger(HpoProjectAnnotationFileParser.class);
  /**
   * A reference to the HPO Ontology object.
   */
  private final Ontology ontology;

  private final TermValidator termValidator;;

  private final HpoAnnotationMerger annotationMerger;
  /**
   * The column names of the small file.
   */
  private static final String[] expectedFields = {
    "#diseaseID",
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
   * Number of tab-separated fields in a valid small file.
   */
  private static final int NUMBER_OF_FIELDS = expectedFields.length;
  /**
   * A list of all erroneous Small File lines encountered during parsing
   */
  private List<HpoProjectAnnotationModel> diseaseAnnotationModels;



  public HpoProjectAnnotationFileParser( Ontology ontology) {
    this.ontology = ontology;
    this.termValidator = new TermValidator(ontology);
    this.annotationMerger = new HpoAnnotationMerger(ontology, termValidator);
    diseaseAnnotationModels = new ArrayList<>();
  }





  /**
   * Parse a single HPO Annotation file. If {@code faultTolerant} is set to true, then we will parse as
   * much as we can of an annotation file and return the {@link HpoAnnotationModel} object, even if one or more
   * parse errors occured. Otherwise, an {@link HpoAnnotationModelError} will be thrown
   *
   * @param faultTolerant If true, report errors to STDERR but do not throw an exception
   * @return A {@link HpoAnnotationModel} object corresponding to the data in the HPO Annotation file
   */
  public HpoProjectAnnotationModel parse(File hpoAnnotationFile, boolean faultTolerant) {
    String basename = hpoAnnotationFile.getName();
    List<AnnotationEntryI> entryList = new ArrayList<>();
    List<HpoaError> errorList = new ArrayList<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(hpoAnnotationFile));
      String line = br.readLine();
      qcHeaderLine(line);
      while ((line = br.readLine()) != null) {
          AnnotationEntryI entry = HpoProjectAnnotationLine.fromLine(line, termValidator, ontology);
          if (entry.hasError()) {
            entry.getErrors().forEach(errorList::add);
          } else {
            entryList.add(entry);
          }
      }
      br.close();
      return new HpoProjectAnnotationModel(basename, entryList, annotationMerger);
    } catch (IOException e) {
      throw new HpoAnnotQcException(String.format("Error parsing %s: %s", hpoAnnotationFile, e.getMessage()));
    }
  }

  /**
   * Parse a single HPO Annotation file with the default setting of no fault-tolerance, i.e. if even a single parse
   * error is encountered, throw an {@link HpoAnnotationModelError}.
   *
   */
  public HpoProjectAnnotationModel parse(File hpoAnnotationFile)  {
    return parse(hpoAnnotationFile,false);
  }

  /**
   * Can be used with fault-tolerant parsing to determine if parse errors were encountered.
   *
   * @return true if one or more parse errors occured
   */
  public boolean hasErrors() {
    return diseaseAnnotationModels.stream().anyMatch(HpoProjectAnnotationModel::hasError);
  }

  /**
   * This method checks that the nead has the expected number and order of lines.
   * If it doesn't, then a serious error has occured somewhere, and it is better to
   * die and figure out what is wrong than to attempt error correction
   *
   * @param line a header line of a V2 small file
   */
  private void qcHeaderLine(String line)  {
    String[] fields = line.split("\t");
    if (fields.length != NUMBER_OF_FIELDS) {
      String msg = String.format("Malformed header line\n" + line +
        "\nExpecting %d fields but got %d", NUMBER_OF_FIELDS, fields.length);
      throw new HpoAnnotQcException(msg);
    }
    for (int i = 0; i < fields.length; i++) {
      if (!fields[i].equals(expectedFields[i])) {
        throw new HpoAnnotQcException(String.format("Malformed field %d. Expected %s but got %s",
          i, expectedFields[i], fields[i]));
      }
    }
    // if we get here, all is good
  }


  public List<HpoaError> errorList() {
    return diseaseAnnotationModels.stream().flatMap(HpoProjectAnnotationModel::getErrors).toList();
  }


}
