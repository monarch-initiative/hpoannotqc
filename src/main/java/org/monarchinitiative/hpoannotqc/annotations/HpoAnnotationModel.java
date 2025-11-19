package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoFrequency;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a complete HPO annotation model for a single disease entity.
 *
 * <p>This class holds all annotation entries from one HPO annotation "small file"
 * (e.g., {@code OMIM-100200.tab}) and provides functionality for merging duplicate
 * entries and transforming the data for inclusion in the aggregated "big file"
 * ({@code phenotype.hpoa}).</p>
 *
 * <p>The class supports merging entries that annotate the same HPO term for a disease,
 * combining frequencies, publications, and other metadata according to specific rules.
 * Entries can only be merged if they have compatible negation, sex, and onset qualifiers.</p>
 *
 * <p>Key operations include:</p>
 * <ul>
 *   <li>Parsing and storing annotation entries from small files</li>
 *   <li>Merging duplicate phenotype annotations</li>
 *   <li>Adding inheritance annotations from Orphanet</li>
 *   <li>Identifying the source database (OMIM, DECIPHER, etc.)</li>
 * </ul>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class HpoAnnotationModel {
  /**
   * The base name of the HPO Annotation file.
   */
  private final String basename;
  /**
   * List of {@link HpoAnnotationEntry} objects representing the original lines of
   * the small file
   */
  private List<HpoAnnotationEntry> entryList;

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

  /**
   * @return The base name of the HPO Annotation file.
   */
  public String getBasename() {
    return basename;
  }

  /**
   * Constructs a new HPO annotation model for a disease entity.
   *
   * <p>Creates an immutable copy of the provided annotation entries and determines
   * the source database based on the filename.</p>
   *
   * @param name    base name of the small file (e.g., "OMIM-100200.tab")
   * @param entries list of annotation entries parsed from the small file
   */
  public HpoAnnotationModel(String name, List<HpoAnnotationEntry> entries) {
    basename = name;
    entryList = List.copyOf(entries);
    if (basename.contains("OMIM"))
      this.database = Database.OMIM;
    else if (basename.contains("DECIPHER"))
      this.database = Database.DECIPHER;
    else
      this.database = Database.UNKNOWN;
  }

  /**
   * Creates a new model that includes inheritance annotations in addition to the existing entries.
   *
   * @param inherit collection of inheritance annotation entries to add
   * @return new HPO annotation model with inheritance annotations included
   */
  public HpoAnnotationModel mergeWithInheritanceAnnotations(Collection<HpoAnnotationEntry> inherit) {
    List<HpoAnnotationEntry> builder = new ArrayList<>();
    builder.addAll(this.entryList);
    builder.addAll(inherit);
    return new HpoAnnotationModel(this.basename, List.copyOf(builder));
  }

  /**
   * Checks if this model represents OMIM data.
   *
   * @return true if the source is OMIM, false otherwise
   */
  public boolean isOMIM() {
    return this.database.equals(Database.OMIM);
  }

  /**
   * Checks if this model represents DECIPHER data.
   *
   * @return true if the source is DECIPHER, false otherwise
   */
  public boolean isDECIPHER() {
    return this.database.equals(Database.DECIPHER);
  }

  /**
   * Gets the list of annotation entries in this model.
   *
   * @return immutable list of HPO annotation entries
   */
  public List<HpoAnnotationEntry> getEntryList() {
    return entryList;
  }

  /**
   * Gets the number of annotation entries in this model.
   *
   * @return count of annotation entries
   */
  public int getNumberOfAnnotations() {
    return entryList.size();
  }

  private boolean divergentNegation(List<HpoAnnotationEntry> entrylist) {
    String firstItemNegated = entrylist.get(0).getNegation();
    if (firstItemNegated == null)
      firstItemNegated = EMPTY_STRING;
    for (int i = 1; i < entrylist.size(); ++i) {
      if (!firstItemNegated.equals(entrylist.get(i).getNegation())) {
        return true;
      }
    }
    return false; // if we get here we can still merge. Items are not divergent
  }

  private boolean divergentSex(List<HpoAnnotationEntry> entrylist) {
    String firstItemSex = entrylist.get(0).getSex();
    if (firstItemSex == null)
      firstItemSex = EMPTY_STRING;
    for (int i = 1; i < entrylist.size(); ++i) {
      if (!firstItemSex.equals(entrylist.get(i).getSex())) {
        return true;
      }
    }
    return false; // if we get here we can still merge. Items are not divergent
  }

  private boolean divergentOnset(List<HpoAnnotationEntry> entrylist) {
    String firstItemOnsetId = entrylist.get(0).getAgeOfOnsetId();
    if (firstItemOnsetId == null)
      firstItemOnsetId = EMPTY_STRING;
    for (int i = 1; i < entrylist.size(); ++i) {
      if (!firstItemOnsetId.equals(entrylist.get(i).getAgeOfOnsetId())) {
        return true;
      }
    }
    return false; // if we get here we can still merge. Items are not divergent
  }

  /**
   * We want to merge entries with different n-of-m frequencies. For instance, if
   * we have 2/3 and 5/7 then we would merge this to 7/10. If one of the entries
   * is not n-of-m, then we will transform it as if the percentage or ontology
   * term
   * represents 10 observations. If the field is empty, then we will assume it is
   * 100%, i.e., 10/10
   *
   * @param entrylist List of frequency strings
   * @return merged frequency string
   */
  private String mergeFrequencies(final List<HpoAnnotationEntry> entrylist) {
    int sum_of_numerators = 0;
    int sum_of_denominators = 0;
    final int DEFAULT_NUMBER_OF_OBSERVATIONS = 10;

    for (HpoAnnotationEntry e : entrylist) {
      String q = e.getFrequencyModifier();
      Matcher matcher = n_of_m_pattern.matcher(q);
      Matcher percentageMatcher = percentage_pattern.matcher(q);
      Matcher termMatcher = hpoTerm_pattern.matcher(q);
      if (q.isEmpty()) {
        // 1) No frequency entry available. Assume 100%, i.e., 10/10
        sum_of_numerators += DEFAULT_NUMBER_OF_OBSERVATIONS;
        sum_of_denominators += DEFAULT_NUMBER_OF_OBSERVATIONS;
      } else if (matcher.matches()) {
        // 2) The frequency string is of the form n/m
        String n_str = matcher.group(1);
        String m_str = matcher.group(2);
        // if we match the regex, the following "must" work.
        int n = Integer.parseInt(n_str);
        int m = Integer.parseInt(m_str);
        sum_of_numerators += n;
        sum_of_denominators += m;
      } else if (percentageMatcher.matches()) {
        String p_str = percentageMatcher.group(1);
        // If we match the regex, the following "must" work
        double d = Double.parseDouble(p_str);
        int n = (int) Math.round(d / 10.0);
        sum_of_numerators += n;
        sum_of_denominators += DEFAULT_NUMBER_OF_OBSERVATIONS;
      } else if (termMatcher.matches()) {
        TermId freqid = TermId.of(q);
        HpoFrequency hpofreq = HpoFrequency.fromTermId(freqid);
        double proportion = hpofreq.mean();
        int n = (int) Math.round(proportion * 10.0);
        sum_of_numerators += n;
        sum_of_denominators += DEFAULT_NUMBER_OF_OBSERVATIONS;
      } else {
        // should never happen but if it does we want to know right away
        throw new PhenolRuntimeException("Could not parse frequency entry: \"" + q + "\"");
      }
    }
    return String.format("%d/%d", sum_of_numerators, sum_of_denominators);
  }

  private String mergeModifiers(final List<HpoAnnotationEntry> entrylist) {
    List<String> modifiers = new ArrayList<>();
    for (HpoAnnotationEntry entry : entrylist) {
      String mod = entry.getModifier();
      if (mod != null && !mod.isEmpty()) {
        modifiers.add(mod);
      }
    }
    if (modifiers.isEmpty()) {
      return ""; // no modifiers, return empty string
    } else {
      return String.join(";", modifiers);
    }
  }

  private String mergeDescriptions(final List<HpoAnnotationEntry> entrylist) {
    List<String> descriptions = new ArrayList<>();
    for (HpoAnnotationEntry entry : entrylist) {
      String mod = entry.getDescription();
      if (mod != null && mod.isEmpty()) {
        descriptions.add(mod);
      }
    }
    if (descriptions.isEmpty()) {
      return ""; // no modifiers, return empty string
    } else {
      return String.join(";", descriptions);
    }
  }

  private String mergePublications(final List<HpoAnnotationEntry> entrylist) {
    Set<String> pubs = new HashSet<>();
    for (HpoAnnotationEntry entry : entrylist) {
      pubs.add(entry.getPublication());
    }
    return String.join(";", pubs);
  }

  private String getHighestEvidenceCode(final List<HpoAnnotationEntry> entrylist) {
    String evi = "IEA";// default
    for (HpoAnnotationEntry entry : entrylist) {
      if (entry.getEvidenceCode().equals("PCS")) {
        return "PCS";
      } else if (entry.getEvidenceCode().equals("TAS")) {
        evi = "TAS"; // better than IEA
      }
    }
    return evi;
  }

  private String mergeBiocuration(final List<HpoAnnotationEntry> entrylist) {
    Set<String> biocuration = new HashSet<>();
    for (HpoAnnotationEntry entry : entrylist) {
      biocuration.add(entry.getBiocuration());
    }
    return String.join(";", biocuration);
  }

  /**
   * If this method is called, then we have checked that Sex, Negation, AgeOfOnset
   * are the same
   * Merge everything else, concatenating biocuration and PMID and modifier and
   * description
   *
   * @param entrylist List of annotation lines to the same HPO term that we will
   *                  merge
   * @return a merged entry
   */
  private HpoAnnotationEntry mergeEntries(List<HpoAnnotationEntry> entrylist) {
    HpoAnnotationEntry first = entrylist.get(0);
    TermId diseaseId = first.getDiseaseTermId();
    String diseaseName = first.getDiseaseName();
    TermId phenoId = first.getPhenotypeId();
    String phenoName = first.getPhenotypeLabel();
    String onsetId = first.getAgeOfOnsetId();
    String onsetName = first.getAgeOfOnsetLabel();
    String mergedFrequency = mergeFrequencies(entrylist);
    String sex = first.getSex();
    String negation = first.getNegation();
    String mergedModifiers = mergeModifiers(entrylist);
    String mergedDescriptions = mergeDescriptions(entrylist);
    String mergedPublications = mergePublications(entrylist);
    String evidence = getHighestEvidenceCode(entrylist);
    String mergedBiocuration = mergeBiocuration(entrylist);
    return new HpoAnnotationEntry(diseaseId,
        diseaseName,
        phenoId,
        phenoName,
        onsetId,
        onsetName,
        mergedFrequency,
        sex,
        negation,
        mergedModifiers,
        mergedDescriptions,
        mergedPublications,
        evidence,
        mergedBiocuration);
  }

  /**
   * Creates a new model with duplicate phenotype annotations merged.
   *
   * <p>This method identifies entries that annotate the same HPO term for this disease
   * and attempts to merge them if they have compatible qualifiers (same negation, sex,
   * and onset). Frequencies are combined using n/m arithmetic, publications are concatenated,
   * and the highest evidence code is selected.</p>
   *
   * <p>Entries cannot be merged if they have conflicting:</p>
   * <ul>
   *   <li>Negation qualifiers (NOT vs empty)</li>
   *   <li>Sex qualifiers (MALE vs FEMALE)</li>
   *   <li>Age of onset terms</li>
   * </ul>
   *
   * @return new HPO annotation model with merged duplicate entries
   */
  public HpoAnnotationModel getMergedModel() {
    Map<TermId, List<HpoAnnotationEntry>> termId2AnnotEntryListMap = new HashMap<>();
    for (HpoAnnotationEntry entry : this.entryList) {
      termId2AnnotEntryListMap.putIfAbsent(entry.getPhenotypeId(), new ArrayList<>());
      termId2AnnotEntryListMap.get(entry.getPhenotypeId()).add(entry);
    }
    List<HpoAnnotationEntry> builder = new ArrayList<>();
    for (TermId tid : termId2AnnotEntryListMap.keySet()) {
      List<HpoAnnotationEntry> entrylist = termId2AnnotEntryListMap.get(tid);
      if (entrylist.size() == 1) { // No duplicate entries for this TermId
        builder.add(entrylist.get(0));
      } else {
        boolean mergable = true;
        // check for things that keep us from merging
        if (divergentNegation(entrylist)) {
          mergable = false;
        } else if (divergentSex(entrylist)) {
          mergable = false;
        } else if (divergentOnset(entrylist)) {
          mergable = false;
        }
        if (mergable) {
          HpoAnnotationEntry merged = mergeEntries(entrylist);
          builder.add(merged);
        } else {
          builder.addAll(entrylist); // cannot merge, add each separately
        }
      }
    }
    return new HpoAnnotationModel(this.basename, List.copyOf(builder));
  }

  /**
   * By construction, the disease ID field of each of the entries in this object
   * must be the same
   * Therefore, we return the first one. Also by construction, there must be at
   * least one entry
   * in ({@link #entryList} for this object to have been created
   * 
   * @return The diseaseID of this model
   */
  public TermId getDiseaseId() {
    HpoAnnotationEntry entry = entryList.iterator().next();
    return TermId.of(entry.getDiseaseId());
  }

  public String getDiseaseName() {
    return entryList
        .stream()
        .map(HpoAnnotationEntry::getDiseaseName)
        .findAny()
        .orElse("n/a");
  }

  public void addInheritanceEntryCollection(Collection<HpoAnnotationEntry> entries) {
    List<HpoAnnotationEntry> builder = new ArrayList<>();
    builder.addAll(this.entryList);
    builder.addAll(entries);
    this.entryList = List.copyOf(builder);
  }

}
