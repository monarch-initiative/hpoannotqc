package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnnotationFileCurieConverter.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class AnnotationFileCurieConverterTest {

    @ParameterizedTest
    @CsvSource({
            "OMIM-600123.tab, OMIM:600123",
            "OMIM-100200.tab, OMIM:100200",
            "DECIPHER-1.tab, DECIPHER:1",
            "ORPHA-12345.tab, ORPHA:12345",
            "MONDO-0001234.tab, MONDO:0001234"
    })
    void testPathToCurieWithValidFiles(String fileName, String expectedCurie) {
        // Arrange
        Path path = Paths.get("/some/path", fileName);

        // Act
        String actualCurie = AnnotationFileCurieConverter.pathToCurie(path);

        // Assert
        assertEquals(expectedCurie, actualCurie);
    }

    @Test
    void testPathToCurieWithFullPath() {
        // Arrange
        Path path = Paths.get("/Users/test/data/annotations/OMIM-600123.tab");

        // Act
        String curie = AnnotationFileCurieConverter.pathToCurie(path);

        // Assert
        assertEquals("OMIM:600123", curie);
    }

    @Test
    void testPathToCurieWithRelativePath() {
        // Arrange
        Path path = Paths.get("data/OMIM-100200.tab");

        // Act
        String curie = AnnotationFileCurieConverter.pathToCurie(path);

        // Assert
        assertEquals("OMIM:100200", curie);
    }

    @Test
    void testPathToCurieWithMultipleHyphens() {
        // Arrange
        Path path = Paths.get("MONDO-0001234-extra.tab");

        // Act
        String curie = AnnotationFileCurieConverter.pathToCurie(path);

        // Assert
        assertEquals("MONDO:0001234:extra", curie);
    }

    @Test
    void testPathToCurieWithoutTabExtension() {
        // Arrange
        Path path = Paths.get("OMIM-600123");

        // Act
        String curie = AnnotationFileCurieConverter.pathToCurie(path);

        // Assert
        assertEquals("OMIM:600123", curie);
    }

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Arrange
        var constructor = AnnotationFileCurieConverter.class.getDeclaredConstructor();
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
