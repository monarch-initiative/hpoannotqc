package org.monarchinitiative.hpoannotqc.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Simple file writer for phenotype.hpoa files.
 * Writes a collection of lines to the output file.
 *
 * This class follows the Single Responsibility Principle by only handling file I/O.
 * All business logic for generating lines should be handled elsewhere.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class PhenotypeDotHpoaFileWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhenotypeDotHpoaFileWriter.class);

    // Private constructor to prevent instantiation
    private PhenotypeDotHpoaFileWriter() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Writes a collection of lines to the specified output file.
     * Each line is written as-is, with a newline character appended.
     *
     * @param lines Collection of lines to write (typically includes headers and data rows)
     * @param outputPath Path to the output file
     * @throws IOException if the file cannot be written
     */
    public static void writeLines(Collection<String> lines, String outputPath) throws IOException {
        LOGGER.info("Writing {} lines to {}", lines.size(), outputPath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String line : lines) {
                writer.write(line);
                writer.write("\n");
            }
        }

        LOGGER.info("Successfully wrote phenotype.hpoa file to {}", outputPath);
    }
}
