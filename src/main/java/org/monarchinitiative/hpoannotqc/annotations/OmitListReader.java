package org.monarchinitiative.hpoannotqc.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for reading and parsing the omit-list.txt file.
 * This file contains disease IDs that should be excluded from processing.
 *
 * File format:
 * <pre>
 * #List of OMIM entries that we want to omit from further analysis
 * #DiseaseId Reason
 * OMIM:107850 trait
 * OMIM:147320 legacy
 * </pre>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class OmitListReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(OmitListReader.class);

    // Private constructor to prevent instantiation
    private OmitListReader() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Reads the omit-list.txt file and extracts disease IDs to be omitted.
     * Lines starting with '#' are treated as comments and ignored.
     * Only the first whitespace-separated field is extracted as the disease ID.
     *
     * @param path Path to the omit-list.txt file
     * @return Set of disease IDs (e.g., "OMIM:600123") to be omitted.
     *         Returns empty set if path is null, empty, or file cannot be read.
     */
    public static Set<String> readOmitList(String path) {
        if (path == null || path.isEmpty()) {
            return Set.of();
        }

        Set<String> omitEntries = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // skip comment
                }
                String[] fields = line.split("\\s+");
                if (fields.length > 0) {
                    String id = fields[0]; // the first field has items such as OMIM:500123
                    omitEntries.add(id);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error reading omit list file: {}", e.getMessage());
            // Return empty set rather than partial results
            return Set.of();
        }

        LOGGER.debug("Loaded {} entries from omit list", omitEntries.size());
        return omitEntries;
    }
}
