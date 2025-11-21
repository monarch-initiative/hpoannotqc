package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HpoAnnotationFileWriter.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class HpoAnnotationFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteModelToFile() throws IOException {
        // Arrange
        HpoAnnotationModel model = createTestModel(2);
        File outputFile = tempDir.resolve("test-output.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertTrue(lines.size() >= 3); // Header + 2 entries

        // Check header
        assertEquals(String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS), lines.get(0));
    }

    @Test
    void testWriteModelWithSingleEntry() throws IOException {
        // Arrange
        HpoAnnotationModel model = createTestModel(1);
        File outputFile = tempDir.resolve("single-entry.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(2, lines.size()); // Header + 1 entry
    }

    @Test
    void testWriteModelWithMultipleEntries() throws IOException {
        // Arrange
        HpoAnnotationModel model = createTestModel(5);
        File outputFile = tempDir.resolve("multiple-entries.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(6, lines.size()); // Header + 5 entries
    }

    @Test
    void testWriteModelWithEmptyEntries() throws IOException {
        // Arrange
        HpoAnnotationModel model = new HpoAnnotationModel("OMIM-600123.tab", new ArrayList<>());
        File outputFile = tempDir.resolve("empty.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        assertTrue(outputFile.exists());
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size()); // Only header
    }

    @Test
    void testWriteOverwritesExistingFile() throws IOException {
        // Arrange
        File outputFile = tempDir.resolve("overwrite.tab").toFile();
        Files.writeString(outputFile.toPath(), "old content");
        HpoAnnotationModel model = createTestModel(1);

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertFalse(String.join("", lines).contains("old content"));
        assertEquals(2, lines.size());
    }

    @Test
    void testWriteToNonexistentDirectory() {
        // Arrange
        File outputFile = tempDir.resolve("nonexistent/test.tab").toFile();
        HpoAnnotationModel model = createTestModel(1);

        // Act & Assert
        assertThrows(IOException.class, () -> {
            HpoAnnotationFileWriter.write(model, outputFile);
        });
    }

    @Test
    void testWritePreservesEntryData() throws IOException {
        // Arrange
        List<HpoAnnotationEntry> entries = new ArrayList<>();
        entries.add(HpoAnnotationEntry.fromOrphaData(
                "OMIM:600123",
                "Test Disease",
                "HP:0001234",
                "Test Phenotype",
                TermId.of("HP:0040283"),
                "ORPHA:orphadata[2024-01-01]"
        ));
        HpoAnnotationModel model = new HpoAnnotationModel("OMIM-600123.tab", entries);
        File outputFile = tempDir.resolve("preserve.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(model, outputFile);

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(2, lines.size());

        // Parse back and verify
        HpoAnnotationModel parsedModel = HpoAnnotationFileParser.parse(outputFile);
        assertEquals(1, parsedModel.getNumberOfAnnotations());
    }

    @Test
    void testWriteAndReadRoundTrip() throws IOException {
        // Arrange
        HpoAnnotationModel originalModel = createTestModel(3);
        File outputFile = tempDir.resolve("roundtrip.tab").toFile();

        // Act
        HpoAnnotationFileWriter.write(originalModel, outputFile);
        HpoAnnotationModel parsedModel = HpoAnnotationFileParser.parse(outputFile);

        // Assert
        assertEquals(originalModel.getNumberOfAnnotations(), parsedModel.getNumberOfAnnotations());
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Arrange
        var constructor = HpoAnnotationFileWriter.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            constructor.newInstance();
        });

        assertTrue(exception.getCause() instanceof AssertionError);
        assertEquals("Utility class should not be instantiated", exception.getCause().getMessage());
    }

    // Helper method
    private HpoAnnotationModel createTestModel(int numEntries) {
        List<HpoAnnotationEntry> entries = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            entries.add(HpoAnnotationEntry.fromOrphaData(
                    "OMIM:600123",
                    "Test Disease",
                    "HP:" + String.format("%07d", 1234 + i),
                    "Test Phenotype " + i,
                    TermId.of("HP:0040283"),
                    "ORPHA:orphadata[2024-01-01]"
            ));
        }
        return new HpoAnnotationModel("OMIM-600123.tab", entries);
    }
}
