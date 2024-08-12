package org.monarchinitiative.hpoannotqc.annotations.orpha;

import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoAnnotationMerger;
import org.monarchinitiative.hpoannotqc.annotations.legacy.HpoAnnotationEntry;
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
public class OrphaAnnotationModel {
  /**
   * The base name of the HPO Annotation file.
   */
  private final String basename;
  /**
   * List of {@link HpoAnnotationEntry} objects representing the original lines of the small file
   */
  private List<AnnotationEntryI> entryList;

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
   * The constructor creates an immutable copy of the original list of {@link HpoAnnotationEntry} objects
   * provided by the parser
   *
   * @param name    Name of the "small file"
   * @param entries List of {@link HpoAnnotationEntry} objects -- one per line of the small file.
   */
  public OrphaAnnotationModel(String name,
                              List<AnnotationEntryI> entries,
                              HpoAnnotationMerger annotationMerger) {
    basename = name;
    entryList = List.copyOf(entries);
    this.annotationMerger = annotationMerger;
  }

  public OrphaAnnotationModel mergeWithInheritanceAnnotations(Collection<OrphaAnnotationLine> inherit,
                                                              HpoAnnotationMerger annotationMerger) {
    List<AnnotationEntryI> builder = new ArrayList<>();
      builder.addAll(this.entryList);
      builder.addAll(inherit);
    return new OrphaAnnotationModel(this.basename, List.copyOf(builder), annotationMerger);
  }


  /**
   * @return  {@link AnnotationEntryI}
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
    AnnotationEntryI first = entrylist.get(0);
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
        if (annotationMerger.divergentOnset(entrylist)) {
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
    return new OrphaAnnotationModel(this.basename, List.copyOf(builder),annotationMerger);
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


  public void addInheritanceEntryCollection(Collection<OrphaAnnotationLine> entries) {
    List<AnnotationEntryI> builder = new ArrayList<>();
    builder.addAll(this.entryList);
    builder.addAll(entries);
    this.entryList = List.copyOf(builder);
  }


}
