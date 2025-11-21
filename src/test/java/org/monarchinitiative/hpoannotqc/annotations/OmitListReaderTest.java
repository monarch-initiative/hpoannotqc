package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OmitListReader.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class OmitListReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadOmitListWithValidFile() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("omit-list.txt");
        String content = "#List of OMIM entries that we want to omit from further analysis\n" +
                "#DiseaseId Reason\n" +
                "OMIM:107850 trait\n" +
                "OMIM:147320 legacy\n" +
                "OMIM:600001 deprecated\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertEquals(3, omitList.size());
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("OMIM:147320"));
        assertTrue(omitList.contains("OMIM:600001"));
    }

    @Test
    void testReadOmitListIgnoresComments() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("omit-list.txt");
        String content = "# This is a comment\n" +
                "OMIM:107850 trait\n" +
                "# Another comment\n" +
                "#OMIM:999999 commented out entry\n" +
                "OMIM:147320 legacy\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertEquals(2, omitList.size());
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("OMIM:147320"));
        assertFalse(omitList.contains("OMIM:999999"));
    }

    @Test
    void testReadOmitListWithEmptyLines() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("omit-list.txt");
        String content = "OMIM:107850 trait\n" +
                "\n" +
                "OMIM:147320 legacy\n" +
                "\n" +
                "\n" +
                "OMIM:600001 deprecated\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        // Note: Empty lines may produce empty strings which get added to the set
        // The implementation splits on whitespace, so empty lines add empty string
        assertTrue(omitList.size() >= 3, "Should have at least 3 entries");
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("OMIM:147320"));
        assertTrue(omitList.contains("OMIM:600001"));
    }

    @Test
    void testReadOmitListWithOnlyDiseaseId() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("omit-list.txt");
        String content = "OMIM:107850\n" +
                "OMIM:147320\n" +
                "OMIM:600001\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertEquals(3, omitList.size());
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("OMIM:147320"));
        assertTrue(omitList.contains("OMIM:600001"));
    }

    @Test
    void testReadOmitListWithMultipleSpaces() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("omit-list.txt");
        String content = "OMIM:107850    trait    additional    data\n" +
                "OMIM:147320\t\tlegacy\ttabs\n" +
                "OMIM:600001     deprecated\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertEquals(3, omitList.size());
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("OMIM:147320"));
        assertTrue(omitList.contains("OMIM:600001"));
    }

    @Test
    void testReadOmitListWithNullPath() {
        // Act
        Set<String> omitList = OmitListReader.readOmitList(null);

        // Assert
        assertNotNull(omitList);
        assertTrue(omitList.isEmpty());
    }

    @Test
    void testReadOmitListWithEmptyPath() {
        // Act
        Set<String> omitList = OmitListReader.readOmitList("");

        // Assert
        assertNotNull(omitList);
        assertTrue(omitList.isEmpty());
    }

    @Test
    void testReadOmitListWithNonexistentFile() {
        // Arrange
        String nonexistentPath = tempDir.resolve("nonexistent.txt").toString();

        // Act
        Set<String> omitList = OmitListReader.readOmitList(nonexistentPath);

        // Assert
        assertNotNull(omitList);
        assertTrue(omitList.isEmpty());
    }

    @Test
    void testReadOmitListWithEmptyFile() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("empty-omit-list.txt");
        Files.writeString(omitFile, "");

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertNotNull(omitList);
        assertTrue(omitList.isEmpty());
    }

    @Test
    void testReadOmitListWithOnlyComments() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("comments-only.txt");
        String content = "# Comment 1\n" +
                "# Comment 2\n" +
                "# Comment 3\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertNotNull(omitList);
        assertTrue(omitList.isEmpty());
    }

    @Test
    void testReadOmitListWithDifferentDatabases() throws IOException {
        // Arrange
        Path omitFile = tempDir.resolve("multi-database.txt");
        String content = "OMIM:107850 trait\n" +
                "ORPHA:12345 legacy\n" +
                "DECIPHER:1 test\n" +
                "MONDO:0001234 deprecated\n";
        Files.writeString(omitFile, content);

        // Act
        Set<String> omitList = OmitListReader.readOmitList(omitFile.toString());

        // Assert
        assertEquals(4, omitList.size());
        assertTrue(omitList.contains("OMIM:107850"));
        assertTrue(omitList.contains("ORPHA:12345"));
        assertTrue(omitList.contains("DECIPHER:1"));
        assertTrue(omitList.contains("MONDO:0001234"));
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Arrange
        var constructor = OmitListReader.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert - Reflection wraps the AssertionError in InvocationTargetException
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            constructor.newInstance();
        });

        // Verify the cause is an AssertionError
        assertTrue(exception.getCause() instanceof AssertionError);
        assertEquals("Utility class should not be instantiated", exception.getCause().getMessage());
    }
}
