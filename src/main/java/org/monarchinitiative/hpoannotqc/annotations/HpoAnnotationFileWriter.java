package org.monarchinitiative.hpoannotqc.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes an {@link HpoAnnotationModel} back to a small annotation file.
 * This is used when updating obsolete term IDs or labels in small files.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public final class HpoAnnotationFileWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoAnnotationFileWriter.class);

    // Private constructor to prevent instantiation
    private HpoAnnotationFileWriter() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Writes an HpoAnnotationModel to a file.
     * The file will contain the standard header followed by all entries from the model.
     *
     * @param model The model to write
     * @param outputFile The file to write to (will be overwritten if it exists)
     * @throws IOException if the file cannot be written
     */
    public static void write(HpoAnnotationModel model, File outputFile) throws IOException {
        LOGGER.info("Writing {} entries to {}", model.getNumberOfAnnotations(), outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write header
            writer.write(String.join("\t",HpoAnnotationFileValidator.EXPECTED_FIELDS));
            writer.write("\n");

            for (HpoAnnotationEntry entry : model.getEntryList()) {
                writer.write(entry.getRow());
            }
        }

        LOGGER.info("Successfully wrote annotation file to {}", outputFile.getAbsolutePath());
    }
}
