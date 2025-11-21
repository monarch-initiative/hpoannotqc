package org.monarchinitiative.hpoannotqc.annotations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse of a single HPO Annotation File into a {@link HpoAnnotationModel} object. The HPO project uses a single
 * tab-separated file to store information about individual diseases.
 * Colloquially, we have called these files "small-files" to distinguish them from the
 * {@code phenotype.hpoa} file that is created by combining the information from all ca. 7000 small files
 * (and which colloquially we have called the "big-file").
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 2/05/2018.
 */
public class HpoAnnotationFileParser {

  /**
   * Parse a single HPO Annotation file. This checks that the file looks like one we expect, then returns a new
   * entry. This entry is not validated and should be validated after if necessary.
   *
   * @return A {@link HpoAnnotationModel} object corresponding to the data in the HPO Annotation file
   * @throws IOException if there is an error reading the file
   */
  public static HpoAnnotationModel parse(File hpoAnnotationFile) throws IOException {
    String basename = hpoAnnotationFile.getName();
    List<HpoAnnotationEntry> entryList = new ArrayList<>();

    try {
      BufferedReader br = new BufferedReader(new FileReader(hpoAnnotationFile));
      String line = br.readLine();
      HpoAnnotationFileValidator.qcHeaderLine(line);

      while ((line = br.readLine()) != null) {
          HpoAnnotationEntry entry = HpoAnnotationEntry.fromLine(line);
          entryList.add(entry);
      }

      br.close();
      return new HpoAnnotationModel(basename, entryList);
    } catch (IOException e) {
      throw new IOException(String.format("Error parsing %s: %s", hpoAnnotationFile, e.getMessage()));
    }
  }
}
