package org.monarchinitiative.hpoannotqc.annotations.orpha;

import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntry;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationModel;
import org.monarchinitiative.hpoannotqc.annotations.DiseaseDatabase;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoAnnotationMerger;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;

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
public class OrphaAnnotationModel implements AnnotationModel {
  /**
   * The base name of the HPO Annotation file.
   */
  private final String basename;
  /**
   * List of {@link AnnotationEntry} objects representing the original lines of the small file
   */
  private List<AnnotationEntry> entryList;

  private List<HpoaError> errorList;


  private static final String EMPTY_STRING = "";


  private final HpoAnnotationMerger annotationMerger;



  /**
   * @return The base name of the HPO Annotation file.
   */
  public String getBasename() {
    return basename;
  }

  public List<HpoaError> getErrorList() {
    return errorList;
  }



  /**
   * The constructor creates an immutable copy of the original list of {@link AnnotationEntry} objects
   * provided by the parser
   *
   * @param name    Name of the "small file"
   * @param entries List of {@link AnnotationEntry} objects -- one per line of the small file.
   */
  public OrphaAnnotationModel(String name,
                              List<AnnotationEntry> entries,
                              HpoAnnotationMerger annotationMerger) {
    basename = name;
    entryList = List.copyOf(entries);
    this.annotationMerger = annotationMerger;
  }

  public OrphaAnnotationModel mergeWithInheritanceAnnotations(Collection<AnnotationEntry> inherit,
                                                              HpoAnnotationMerger annotationMerger) {
    List<AnnotationEntry> builder = new ArrayList<>();
      builder.addAll(this.entryList);
      builder.addAll(inherit);
    return new OrphaAnnotationModel(this.basename, List.copyOf(builder), annotationMerger);
  }


  /**
   * @return  {@link AnnotationEntry}
   */
  public List<AnnotationEntry> getEntryList() {
    return entryList;
  }

  @Override
  public List<HpoaError> getErrors() {
    return errorList;
  }

  @Override
  public String getTitle() {
      if (entryList.isEmpty()) return "n/a"; // should never happen
      AnnotationEntry entry = entryList.get(0);
      return String.format("%s - %s (%s)", getBasename(), entry.getDiseaseName(), entry.getDiseaseID());
  }

  @Override
  public DiseaseDatabase getDatabase() {
    return DiseaseDatabase.ORPHANET;
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
  private AnnotationEntry mergeEntries(List<AnnotationEntry> entrylist) {
    AnnotationEntry first = entrylist.get(0);
    String diseaseId=first.getDiseaseID();
    String diseaseName=first.getDiseaseName();
    String phenoId=first.getPhenotypeId();
    String phenoName=first.getPhenotypeLabel();
    String mergedFrequency = annotationMerger.mergeFrequencies(entrylist).frequencyString();
    String mergedBiocuration = HpoAnnotationMerger.mergeBiocuration(entrylist);
    return new OrphaAnnotationLine(diseaseId,
      diseaseName,
      phenoId,
      phenoName, mergedFrequency,
      mergedBiocuration,
      List.of());
  }




  public OrphaAnnotationModel getMergedModel() {
    Map<String, List<AnnotationEntry>> termId2AnnotEntryListMap = new HashMap<>();
    for (AnnotationEntry entry : this.entryList) {
      termId2AnnotEntryListMap.putIfAbsent(entry.getPhenotypeId(), new ArrayList<>());
      termId2AnnotEntryListMap.get(entry.getPhenotypeId()).add(entry);
    }
    List<AnnotationEntry> builder = new ArrayList<>();
    for (String tid : termId2AnnotEntryListMap.keySet()) {
      List<AnnotationEntry> entrylist = termId2AnnotEntryListMap.get(tid);
      if (entrylist.size() == 1) { // No duplicate entries for this TermId
        builder.add(entrylist.get(0));
      } else {
        boolean mergable = true;
        // check for things that keep us from merging
        if (annotationMerger.divergentOnset(entrylist)) {
          mergable = false;
        }
        if (mergable) {
          AnnotationEntry merged = mergeEntries(entrylist);
          builder.add(merged);
        } else {
          builder.addAll(entrylist); // cannot merge, add each separately
        }
      }
    }
    return new OrphaAnnotationModel(this.basename, List.copyOf(builder),annotationMerger);
  }

  /**
   * By construction, the disease ID field of each of the entries in this object must be the same
   * Therefore, we return the first one. Also by construction, there must be at least one entry
   * in ({@link #entryList} for this object to have been created
   * @return The diseaseID of this model
   */
  public TermId getDiseaseId() {
    AnnotationEntry entry = entryList.iterator().next();
    return TermId.of(entry.getDiseaseID());
  }

  public String getDiseaseName() {
    return entryList
      .stream()
      .map(AnnotationEntry::getDiseaseName)
      .findAny()
      .orElse("n/a");
  }


  public void addInheritanceEntryCollection(Collection<OrphaAnnotationLine> entries) {
    List<AnnotationEntry> builder = new ArrayList<>();
    builder.addAll(this.entryList);
    builder.addAll(entries);
    this.entryList = List.copyOf(builder);
  }


}
