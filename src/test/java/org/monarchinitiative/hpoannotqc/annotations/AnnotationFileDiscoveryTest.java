package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnnotationFileDiscovery.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class AnnotationFileDiscoveryTest {

    @TempDir
    Path tempDir;

    @Test
    void testDiscoverFilesWithValidTabFiles() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("OMIM-100200.tab");
        createTabFile("DECIPHER-1.tab");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), Set.of());

        // Assert
        assertEquals(3, files.size());
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("OMIM-600123.tab")));
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("OMIM-100200.tab")));
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("DECIPHER-1.tab")));
    }

    @Test
    void testDiscoverFilesIgnoresNonTabFiles() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createFile("OMIM-600124.txt");
        createFile("README.md");
        createFile("data.json");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), Set.of());

        // Assert
        assertEquals(1, files.size());
        assertEquals("OMIM-600123.tab", files.get(0).getName());
    }

    @Test
    void testDiscoverFilesWithOmitList() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("OMIM-100200.tab");
        createTabFile("OMIM-600001.tab");
        Set<String> omitList = Set.of("OMIM:100200", "OMIM:600001");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), omitList);

        // Assert
        assertEquals(1, files.size());
        assertEquals("OMIM-600123.tab", files.get(0).getName());
    }

    @Test
    void testDiscoverFilesWithEmptyOmitList() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("OMIM-100200.tab");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), Set.of());

        // Assert
        assertEquals(2, files.size());
    }

    @Test
    void testDiscoverFilesWithAllFilesOmitted() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("OMIM-100200.tab");
        Set<String> omitList = Set.of("OMIM:600123", "OMIM:100200");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), omitList);

        // Assert
        assertEquals(0, files.size());
    }

    @Test
    void testDiscoverFilesWithEmptyDirectory() {
        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), Set.of());

        // Assert
        assertNotNull(files);
        assertEquals(0, files.size());
    }

    @Test
    void testDiscoverFilesWithNonexistentDirectory() {
        // Arrange
        String nonexistentPath = tempDir.resolve("nonexistent").toString();

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(nonexistentPath, Set.of());

        // Assert
        assertNotNull(files);
        assertEquals(0, files.size());
    }

    @Test
    void testDiscoverFilesWithDifferentDatabases() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("ORPHA-12345.tab");
        createTabFile("DECIPHER-1.tab");
        createTabFile("MONDO-0001234.tab");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), Set.of());

        // Assert
        assertEquals(4, files.size());
    }

    @Test
    void testDiscoverFilesWithPartialOmitList() throws IOException {
        // Arrange
        createTabFile("OMIM-600123.tab");
        createTabFile("OMIM-100200.tab");
        createTabFile("OMIM-600001.tab");
        createTabFile("ORPHA-12345.tab");
        Set<String> omitList = Set.of("OMIM:100200");

        // Act
        List<File> files = AnnotationFileDiscovery.discoverFiles(tempDir.toString(), omitList);

        // Assert
        assertEquals(3, files.size());
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("OMIM-600123.tab")));
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("OMIM-600001.tab")));
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("ORPHA-12345.tab")));
        assertFalse(files.stream().anyMatch(f -> f.getName().equals("OMIM-100200.tab")));
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Arrange
        var constructor = AnnotationFileDiscovery.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            constructor.newInstance();
        });

        assertTrue(exception.getCause() instanceof AssertionError);
        assertEquals("Utility class should not be instantiated", exception.getCause().getMessage());
    }

    // Helper methods
    private void createTabFile(String name) throws IOException {
        Files.createFile(tempDir.resolve(name));
    }

    private void createFile(String name) throws IOException {
        Files.createFile(tempDir.resolve(name));
    }
}
