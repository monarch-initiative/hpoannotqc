package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class coordinates the input of all the HPO Annotation files ("small files"). If an
 * {@code omit-list.txt} is provided by the user, then these files are
 * omitted. The output of this class is a list of {@link HpoAnnotationModel} objects
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class HpoAnnotationFileIngestor {
  private static final Logger LOGGER = LoggerFactory.getLogger(HpoAnnotationFileIngestor.class);
  /**
   * Reference to the HPO object.
   */
  private final Ontology ontology;
  /**
   * The paths to all the small files, e.g., OMIM-600301.tab.
   */
  private final List<File> smallFilePaths;
  /**
   * List of all of the {@link HpoAnnotationModel} objects, which represent annotated diseases.
   */
  private final List<HpoAnnotationModel> hpoaFileList = new ArrayList<>();
  /**
   * Names of entries (small files) that we will omit because they do not represent diseases.
   */
  private final Set<String> omitEntries;
  /**
   * Total number of annotations of all the annotation files.
   */
  private int n_total_annotation_lines = 0;

  private int n_total_omitted_entries = 0;
  /**
   * Merge entries with the same phenotype-disease association but different metadata for the big file.
   */
  private boolean mergeEntries = false;

  private final List<String> errors = new ArrayList<>();

  List<HpoAnnotationModel> getHpoaFileEntries() {
    return hpoaFileList;
  }



  public HpoAnnotationFileIngestor(String directoryPath, Ontology ontology, boolean merge_fr) {
    this(directoryPath,
      String.format("%s%s%s", directoryPath, File.separator, "omit-list.txt"),
      ontology,
      merge_fr);
  }

  /**
   * @param directoryPath path to the directory with HPO annotation "small files"
   * @param omitFile      path to the {@code omit-list.txt} file with non-disease entries to be omitted
   * @param ontology      reference to HPO ontologt object
   * @param merge         Should we merge small file lines with the same HPO but different metadata?
   */
  public HpoAnnotationFileIngestor(String directoryPath, String omitFile, Ontology ontology, boolean merge) {
    omitEntries = getOmitEntries(omitFile);
    this.mergeEntries = merge;
    smallFilePaths = getListOfSmallFiles(directoryPath);
    this.ontology = ontology;
    inputHpoAnnotationFiles();
  }

  private void inputHpoAnnotationFiles() {
    int i = 0;
    for (File file : smallFilePaths) {
      HpoAnnotationFileParser parser = new HpoAnnotationFileParser(file.getAbsolutePath(), ontology);

      HpoAnnotationModel smallFile = parser.parse(true);
      if (mergeEntries) {
        smallFile = smallFile.getMergedModel();
      }
      if (parser.hasError()) {
        this.errors.addAll(parser.errorList());
      }
      n_total_annotation_lines += smallFile.getNumberOfAnnotations();
      hpoaFileList.add(smallFile);
    }
    if (! this.errors.isEmpty()) {
      for (var e:errors){
        System.err.println(e);
      }
      String ERROR_FILE = "HPOA-errors.txt";
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(ERROR_FILE))) {
        for (var line: errors) {
          bw.write(line + "\n");
          LOGGER.error(line);
        }
      } catch (IOException e){
        LOGGER.error(e.getMessage());
      }
      throw new PhenolRuntimeException("Parse errors encountered with HPOA file generation!");
    }
  }

  /**
   * This is the format of the omit-list.txt file.
   * Thus, we need to extract only the first field.
   * <pre></pre>
   * #List of OMIM entries that we want to omit from further analysis
   * #DiseaseId    Reason
   * OMIM:107850   trait
   * OMIM:147320   legacy
   * </pre>
   * If this file is not passed (is null or empty), then we return the empty set.
   *
   * @param path the path to {@code omit-list.txt}
   * @return Set of entries (encoded as strings like "OMIM:600123") that should be omitted
   */
  private Set<String> getOmitEntries(String path) {
    if (path == null || path.isEmpty())
      return Set.of();

    Set<String> entrylist = new HashSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) continue; // skip comment
        String[] A = line.split("\\s+");
        String id = A[0]; // the first field has items such as OMIM:500123
        entrylist.add(id);
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      errors.add(e.getMessage());
    }
    return entrylist;
  }

  int get_omitted_entry_count() {
    return this.n_total_omitted_entries;
  }

  int get_valid_smallfile_count() {
    return this.smallFilePaths.size();
  }

  /**
   * Get the entry Curie for a certain path
   *
   * @param path e.g., /.../rare-diseases/annotated/OMIM-600123.tab
   * @return the corresinding Curie, e.g., OMIM:600123
   */
  private String baseName(Path path) {
    String bname = path.getFileName().toString();
    bname = bname.replace('-', ':').replace(".tab", "");
    return bname;
  }

  /**
   *
   * @param smallFileDirectory location of our curation files
   * @return list of small files (such as OMIM-600123.tab)
   */
  private List<File> getListOfSmallFiles(String smallFileDirectory) {
    List<File> fileNames = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(smallFileDirectory))) {
      for (Path path : directoryStream) {
        if (path.toString().endsWith(".tab")) {
          String basename = baseName(path);
          if (omitEntries.contains(basename)) {
            n_total_omitted_entries++;
            continue; // skip omit entries!
          }
          fileNames.add(new File(path.toString()));
        }
      }
    } catch (IOException ex) {
      errors.add(String.format("Could not get list of small smallFilePaths from %s [%s]. Terminating...",
        smallFileDirectory, ex));
    }
    return fileNames;
  }

}
