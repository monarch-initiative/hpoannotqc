package org.monarchinitiative.hpoannotqc.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for discovering HPO annotation files (.tab files) in a directory.
 * Can filter files based on an omit list of disease IDs.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class AnnotationFileDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationFileDiscovery.class);

    // Private constructor to prevent instantiation
    private AnnotationFileDiscovery() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Discovers all .tab annotation files in the specified directory,
     * excluding any files whose disease ID appears in the omit list.
     *
     * @param directoryPath Path to directory containing annotation files
     * @param omitList Set of disease IDs (CURIEs) to exclude
     * @return List of File objects for valid annotation files
     */
    public static List<File> discoverFiles(String directoryPath, Set<String> omitList) {
        List<File> validFiles = new ArrayList<>();
        int omittedCount = 0;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    String curie = AnnotationFileCurieConverter.pathToCurie(path);

                    if (omitList.contains(curie)) {
                        omittedCount++;
                        LOGGER.debug("Omitting file: {} (CURIE: {})", path.getFileName(), curie);
                        continue;
                    }
                    validFiles.add(new File(path.toString()));
                }
            }
        } catch (IOException ex) {
            String errorMsg = String.format(
                "Could not get list of annotation files from %s [%s]",
                directoryPath,
                ex.getMessage()
            );
            LOGGER.error(errorMsg);
        }

        LOGGER.info("Discovered {} valid annotation files, omitted {} files", validFiles.size(), omittedCount);
        return validFiles;
    }
}
