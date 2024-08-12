package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationModel;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class represents one disease-entity annotation consisting usually of multiple annotations lines, and using
 * the new format introduced in 2018. Colloquially, these files have been called "small files". This class
 * is meant to be used for parsing the files, and does not perform any kind of analysis. THe main use case
 * is to hold the data from one HPO Annotation file, such as {@code OMIM-100200.tab}, which in turn will be
 * use to create the aggregated file called {@code phenotype.hpoa} (the "big-file").
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 1/20/2018.
 */
public class HpoProjectAnnotationModel implements AnnotationModel {
  /**
   * The base name of the HPO Annotation file.
   */
  private final String basename;
  /**
   * List of {@link AnnotationEntryI} objects representing the original lines of the small file
   */
  private List<AnnotationEntryI> entryList;
  private final List<HpoaError> errorList;



  /**
   * These are the databases currently represented in our data resource.
   */
  private enum Database {
    OMIM, DECIPHER, UNKNOWN
  }

  /**
   * What is the source of the current HpoAnnotationModel?
   */
  private final Database database;

  private static final String EMPTY_STRING = "";

  /**
   * To be used for matching n/m frequencies.
   */
  private final static Pattern n_of_m_pattern = Pattern.compile("^(\\d+)/(\\d+?)");

  private final static Pattern percentage_pattern = Pattern.compile("^(\\d*\\.?\\d+)%");

  private final static Pattern hpoTerm_pattern = Pattern.compile("^HP:\\d{7}$");

  private final HpoAnnotationMerger annotationMerger;

  /**
   * @return The base name of the HPO Annotation file.
   */
  public String getBasename() {
    return basename;
  }


  public boolean hasError() {
    return ! errorList.isEmpty();
  }

  public List<HpoaError> getErrorList() {
    return errorList;
  }

  /**
   * The constructor creates an immutable copy of the original list of {@link AnnotationEntryI} objects
   * provided by the parser
   *
   * @param name    Name of the "small file"
   * @param entries List of {@link AnnotationEntryI} objects -- one per line of the small file.
   */
  public HpoProjectAnnotationModel(String name,
                                   List<AnnotationEntryI> entries,
                                   List<HpoaError> errors,
                                   HpoAnnotationMerger merger) {
    basename = name;
    entryList = List.copyOf(entries);
    errorList = List.copyOf(errors);
    if (basename.contains("OMIM")) this.database = Database.OMIM;
    else if (basename.contains("DECIPHER")) this.database = Database.DECIPHER;
    else this.database = Database.UNKNOWN;
    annotationMerger = merger;
  }

  public Stream<HpoaError> getErrors() {
    return errorList.stream();
  }

  /** Seems a stale function? */
  @Deprecated()
  public HpoProjectAnnotationModel mergeWithInheritanceAnnotations(Collection<AnnotationEntryI> inherit) {
    List<AnnotationEntryI> builder = new ArrayList<>();
      builder.addAll(this.entryList);
      builder.addAll(inherit);
    return new HpoProjectAnnotationModel(this.basename, List.copyOf(builder),
            List.of(),
            annotationMerger);
  }

  /**
   * Private constructor, intended to be used by {@link #getMergedModel()}
   *
   * param base    base name of small file
   * param db      database (OMIM, DECIPHER)
   * param entries list of (merged) entries.

  private HpoProjectAnnotationModel(String base, Database db, List<AnnotationEntryI> entries) {
    this.basename = base;
    this.database = db;
    this.entryList = entries;
    errorList = new ArrayList<>();
  } */


  public boolean isOMIM() {
    return this.database.equals(Database.OMIM);
  }

  public boolean isDECIPHER() {
    return this.database.equals(Database.DECIPHER);
  }


  /**
   * @return the {@link AnnotationEntryI} objects -- one per line of the small file.
   */
  public List<AnnotationEntryI> getEntryList() {
    return entryList;
  }

  public int getNumberOfAnnotations() {
    return entryList.size();
  }


  /**
   * If this method is called, then we have checked that Sex, Negation, AgeOfOnset are the same
   * Merge everything else, concatenating biocuration and PMID and modifier and description
   *
   * @param entrylist List of annotation lines to the same HPO term that we will merge
   * @return a merged entry
   */
  private AnnotationEntryI mergeEntries(List<AnnotationEntryI> entrylist) {
      return annotationMerger.mergeEntries(entrylist);
  }


  public HpoProjectAnnotationModel getMergedModel() {
    Map<String, List<AnnotationEntryI>> termId2AnnotEntryListMap = new HashMap<>();
    for (AnnotationEntryI entry : this.entryList) {
      termId2AnnotEntryListMap.putIfAbsent(entry.getPhenotypeId(), new ArrayList<>());
      termId2AnnotEntryListMap.get(entry.getPhenotypeId()).add(entry);
    }
    List<AnnotationEntryI> builder = new ArrayList<>();
    for (String tid : termId2AnnotEntryListMap.keySet()) {
      List<AnnotationEntryI> entrylist = termId2AnnotEntryListMap.get(tid);
      if (entrylist.size() == 1) { // No duplicate entries for this TermId
        builder.add(entrylist.get(0));
      } else {
        boolean mergable = true;
        // check for things that keep us from merging
        if (annotationMerger.divergentNegation(entrylist)) {
          mergable = false;
        } else if (annotationMerger.divergentSex(entrylist)) {
          mergable = false;
        } else if (annotationMerger.divergentOnset(entrylist)) {
          mergable = false;
        }
        if (mergable) {
          AnnotationEntryI merged = mergeEntries(entrylist);
          builder.add(merged);
        } else {
          builder.addAll(entrylist); // cannot merge, add each separately
        }
      }
    }
    return new HpoProjectAnnotationModel(this.basename,
            List.copyOf(builder),
            getErrorList(),
            annotationMerger);
  }

  /**
   * By construction, the disease ID field of each of the entries in this object must be the same
   * Therefore, we return the first one. Also by construction, there must be at least one entry
   * in ({@link #entryList} for this object to have been created
   * @return The diseaseID of this model
   */
  public TermId getDiseaseId() {
    AnnotationEntryI entry = entryList.iterator().next();
    return TermId.of(entry.getDiseaseID());
  }

  public String getDiseaseName() {
    return entryList
      .stream()
      .map(AnnotationEntryI::getDiseaseName)
      .findAny()
      .orElse("n/a");
  }



}
