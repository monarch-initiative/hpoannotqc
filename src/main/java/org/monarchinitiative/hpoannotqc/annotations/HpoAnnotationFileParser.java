package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoAnnotationModelError;
import org.monarchinitiative.hpoannotqc.exception.*;
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
 * {@code phenotype.hpoa} file that is created by combining the information from all ca. 7000 small files
 * (and which colloquially we have called the "big-file").
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 2/05/2018.
 */
public class HpoAnnotationFileParser {
  private final static Logger LOGGER = LoggerFactory.getLogger(OrphanetXML2HpoDiseaseModelParser.class);
  /**
   * A reference to the HPO Ontology object.
   */
  private final Ontology ontology;
  /**
   * Path to a file such as "OMIM-600123.tab" containing data about the phenotypes of a disease.
   */
  private final File hpoAnnotationFile;
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
  private List<String> parseErrors;

  private final Map<String, Integer> malformedBiocurationIdMap;

  private final Set<String> obsoleteTermIdSet;

  /** Key - some malformed citation; vlaue-number of times this citation was encountered */
  private final Map<String, Integer> malformedCitationMap;

  private final Set<String> problematicHpoTerms;



  public HpoAnnotationFileParser(File file, Ontology ontology) {
    this.hpoAnnotationFile = file;
    this.ontology = ontology;
    this.malformedBiocurationIdMap = new HashMap<>();
    this.obsoleteTermIdSet = new HashSet<>();
    this.malformedCitationMap = new HashMap<>();
    this.problematicHpoTerms = new HashSet<>();
  }
  /**
   * Set up parser for an individual HPO Annotation file ("small file") with verbosity false.
   * We demand that the entire file parses without error, otherwise, an exception will be thrown with
   * a summary of all the problems of the file. We set a variable called hasErrors to false in the constructor,
   * and set this to true upon the first such error, but continue to parse to the end of the file, accumulating
   * all the errors.
   *
   * @param path     Path to the HPO annotation file
   * @param ontology reference to HPO Ontology object
   */
  public HpoAnnotationFileParser(String path, Ontology ontology) {
    this(new File(path), ontology);
  }


  /**
   * Parse a single HPO Annotation file. If {@code faultTolerant} is set to true, then we will parse as
   * much as we can of an annotation file and return the {@link HpoAnnotationModel} object, even if one or more
   * parse errors occured. Otherwise, an {@link HpoAnnotationModelError} will be thrown
   *
   * @param faultTolerant If true, report errors to STDERR but do not throw an exception
   * @return A {@link HpoAnnotationModel} object corresponding to the data in the HPO Annotation file
   */
  public HpoAnnotationModel parse(boolean faultTolerant) {
    String basename = hpoAnnotationFile.getName();
    List<HpoAnnotationEntry> entryList = new ArrayList<>();
    this.parseErrors = new ArrayList<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(hpoAnnotationFile));
      String line = br.readLine();
      qcHeaderLine(line);
      while ((line = br.readLine()) != null) {
        try {
          HpoAnnotationEntry entry = HpoAnnotationEntry.fromLine(line, ontology);
          entryList.add(entry);
        } catch (HpoAnnotQcException e) {
          parseErrors.add(String.format(e.getMessage()));
        }
      }
      br.close();
      if (!parseErrors.isEmpty()) {
        String errstr = String.join("\n", parseErrors);
        LOGGER.error(String.format("Errors encountered while parsing HPO Annotation file at %s.\n%s",
            hpoAnnotationFile, errstr));
          throw new HpoAnnotQcException(String.format("Errors encountered while parsing HPO Annotation file at %s.\n%s",
            hpoAnnotationFile, errstr));
      }
      return new HpoAnnotationModel(basename, entryList);
    } catch (IOException e) {
      throw new HpoAnnotQcException(String.format("Error parsing %s: %s", hpoAnnotationFile, e.getMessage()));
    }
  }

  /**
   * Parse a single HPO Annotation file with the default setting of no fault-tolerance, i.e. if even a single parse
   * error is encountered, throw an {@link HpoAnnotationModelError}.
   *
   */
  public HpoAnnotationModel parse()  {
    return parse(false);
  }

  /**
   * Can be used with fault-tolerant parsing to determine if parse errors were encountered.
   *
   * @return true if one or more parse errors occured
   */
  public boolean hasErrors() {
    return !parseErrors.isEmpty();
  }

  /**
   * @return A slit of strings describing all parse errors (can be empty but not null)
   */
  public List<String> getParseErrors() {
    return parseErrors;
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

  public boolean hasError() {
    return ! (parseErrors.isEmpty()
            && malformedBiocurationIdMap.isEmpty()
            && malformedCitationMap.isEmpty()
            && obsoleteTermIdSet.isEmpty());
  }

  public List<String> errorList() {
    final String INDENTATION = "\t";
    if (! hasError()) {
      return List.of();
    }
    List<String> errors = new ArrayList<>();
    errors.add(this.hpoAnnotationFile.getName());
    for (var e: malformedBiocurationIdMap.entrySet()) {
      errors.add(String.format("%sMalformed biocuration id: \"%s\": n=%d.",
              INDENTATION, e.getKey(), e.getValue()));
    }
    for (var s: obsoleteTermIdSet) {
      errors.add(INDENTATION + s);
    }
    for (var e: malformedCitationMap.entrySet()) {
      errors.add(String.format("%s\"%s\": n=%d.",
              INDENTATION, e.getKey(), e.getValue()));
    }
    for (var s: problematicHpoTerms) {
      errors.add(INDENTATION + s);
    }
    for (var s: parseErrors) {
      errors.add(INDENTATION + s);
    }
    return errors;
  }


}
