package org.monarchinitiative.hpoannotqc.annotations;

import java.nio.file.Path;

/**
 * Utility class for converting annotation file paths to disease CURIEs.
 * Transforms file names like "OMIM-600123.tab" to CURIEs like "OMIM:600123".
 *
 * @author <a href="mailto:Michael.Gargano@jax.org">Michael Gargano</a>
 */
public final class AnnotationFileCurieConverter {

    // Private constructor to prevent instantiation
    private AnnotationFileCurieConverter() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Converts an annotation file path to its corresponding disease CURIE.
     * Extracts the file name, replaces hyphens with colons, and removes the .tab extension.
     *
     * @param path Path to an annotation file (e.g., /path/to/OMIM-600123.tab)
     * @return The corresponding CURIE (e.g., OMIM:600123)
     */
    public static String pathToCurie(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.replace('-', ':').replace(".tab", "");
    }
}
