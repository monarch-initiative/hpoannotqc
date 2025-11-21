package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HpoAnnotationFileParser.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class HpoAnnotationFileParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseValidAnnotationFile() throws IOException {
        // Arrange
        File annotFile = createValidAnnotationFile("OMIM-600123.tab");

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertNotNull(model);
        assertEquals("OMIM-600123.tab", model.getBasename());
        assertEquals(2, model.getNumberOfAnnotations());
    }

    @Test
    void testParseFileWithSingleEntry() throws IOException {
        // Arrange
        String header = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);
        String entry = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String content = header + "\n" + entry + "\n";

        File annotFile = createFileWithContent("OMIM-600123.tab", content);

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertNotNull(model);
        assertEquals(1, model.getNumberOfAnnotations());
    }

    @Test
    void testParseFileWithMultipleEntries() throws IOException {
        // Arrange
        String header = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);
        String entry1 = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype 1\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String entry2 = "OMIM:600123\tTest Disease\tHP:0001235\tTest Phenotype 2\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String entry3 = "OMIM:600123\tTest Disease\tHP:0001236\tTest Phenotype 3\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String content = header + "\n" + entry1 + "\n" + entry2 + "\n" + entry3 + "\n";

        File annotFile = createFileWithContent("OMIM-600123.tab", content);

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertEquals(3, model.getNumberOfAnnotations());
    }

    @Test
    void testParseFileWithOnlyHeader() throws IOException {
        // Arrange
        String header = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);
        File annotFile = createFileWithContent("OMIM-600123.tab", header + "\n");

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertEquals(0, model.getNumberOfAnnotations());
    }

    @Test
    void testParseFileWithMalformedHeader() throws IOException {
        // Arrange
        String badHeader = "#diseaseID\tdiseaseName\tphenotypeID"; // Too few fields
        File annotFile = createFileWithContent("OMIM-600123.tab", badHeader + "\n");

        // Act & Assert
        // Parser throws IOException wrapping HpoAnnotQcException
        Exception exception = assertThrows(Exception.class, () -> {
            HpoAnnotationFileParser.parse(annotFile);
        });

        assertTrue(exception.getMessage().contains("Error parsing") ||
                   exception.getMessage().contains("Expecting 14 fields"));
    }

    @Test
    void testParseNonexistentFile() {
        // Arrange
        File nonexistentFile = tempDir.resolve("nonexistent.tab").toFile();

        // Act & Assert
        assertThrows(IOException.class, () -> {
            HpoAnnotationFileParser.parse(nonexistentFile);
        });
    }

    @Test
    void testParseFileExtractsBasename() throws IOException {
        // Arrange
        File annotFile = createValidAnnotationFile("DECIPHER-1.tab");

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertEquals("DECIPHER-1.tab", model.getBasename());
    }

    @Test
    void testParseFileWithDifferentDiseaseIds() throws IOException {
        // Arrange
        String header = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);
        String entry1 = "ORPHA:12345\tRare Disease\tHP:0001234\tTest Phenotype 1\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String entry2 = "ORPHA:12345\tRare Disease\tHP:0001235\tTest Phenotype 2\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String content = header + "\n" + entry1 + "\n" + entry2 + "\n";

        File annotFile = createFileWithContent("ORPHA-12345.tab", content);

        // Act
        HpoAnnotationModel model = HpoAnnotationFileParser.parse(annotFile);

        // Assert
        assertEquals(2, model.getNumberOfAnnotations());
        assertEquals("ORPHA-12345.tab", model.getBasename());
    }

    // Helper methods
    private File createValidAnnotationFile(String name) throws IOException {
        String header = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);
        String entry1 = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String entry2 = "OMIM:600123\tTest Disease\tHP:0001235\tAnother Phenotype\t\t\t\t\t\t\t\t\tTAS\tORPHA:orphadata[2024-01-01]";
        String content = header + "\n" + entry1 + "\n" + entry2 + "\n";

        return createFileWithContent(name, content);
    }

    private File createFileWithContent(String name, String content) throws IOException {
        Path filePath = tempDir.resolve(name);
        Files.writeString(filePath, content);
        return filePath.toFile();
    }
}
