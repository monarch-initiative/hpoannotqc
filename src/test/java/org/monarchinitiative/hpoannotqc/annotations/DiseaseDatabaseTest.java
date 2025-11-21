package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DiseaseDatabase enum.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class DiseaseDatabaseTest {

    @Test
    void testAllEnumValues() {
        // Act
        DiseaseDatabase[] values = DiseaseDatabase.values();

        // Assert
        assertEquals(5, values.length);
        assertArrayEquals(
                new DiseaseDatabase[]{
                        DiseaseDatabase.MONDO,
                        DiseaseDatabase.OMIM,
                        DiseaseDatabase.ORPHANET,
                        DiseaseDatabase.DECIPHER,
                        DiseaseDatabase.UNKNOWN
                },
                values
        );
    }

    @ParameterizedTest
    @CsvSource({
            "MONDO, MONDO",
            "OMIM, OMIM",
            "ORPHANET, ORPHA",
            "DECIPHER, DECIPHER",
            "UNKNOWN, UNKNOWN"
    })
    void testPrefix(String enumName, String expectedPrefix) {
        // Arrange
        DiseaseDatabase db = DiseaseDatabase.valueOf(enumName);

        // Act
        String actualPrefix = db.prefix();

        // Assert
        assertEquals(expectedPrefix, actualPrefix);
    }

    @ParameterizedTest
    @CsvSource({
            "MONDO, MONDO",
            "mondo, MONDO",
            "MoNdO, MONDO",
            "OMIM, OMIM",
            "omim, OMIM",
            "ORPHA, ORPHANET",
            "orpha, ORPHANET",
            "ORPHANET, ORPHANET",
            "orphanet, ORPHANET",
            "DECIPHER, DECIPHER",
            "decipher, DECIPHER"
    })
    void testFromStringValid(String input, String expectedEnumName) {
        // Act
        DiseaseDatabase db = DiseaseDatabase.fromString(input);

        // Assert
        assertEquals(expectedEnumName, db.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "invalid", "TEST", "", "   ", "NCBI", "PUBMED"})
    void testFromStringInvalid(String input) {
        // Act
        DiseaseDatabase db = DiseaseDatabase.fromString(input);

        // Assert
        assertEquals(DiseaseDatabase.UNKNOWN, db);
    }

    @Test
    void testValidDiseaseDatabases() {
        // Act
        Set<DiseaseDatabase> validDbs = DiseaseDatabase.validDiseaseDatabases();

        // Assert
        assertEquals(4, validDbs.size());
        assertTrue(validDbs.contains(DiseaseDatabase.MONDO));
        assertTrue(validDbs.contains(DiseaseDatabase.OMIM));
        assertTrue(validDbs.contains(DiseaseDatabase.ORPHANET));
        assertTrue(validDbs.contains(DiseaseDatabase.DECIPHER));
        assertFalse(validDbs.contains(DiseaseDatabase.UNKNOWN));
    }

    @Test
    void testValidDiseaseDatabasesIsEnumSet() {
        // Act
        Set<DiseaseDatabase> validDbs = DiseaseDatabase.validDiseaseDatabases();

        // Assert - EnumSet is more efficient than HashSet for enums
        // RegularEnumSet is a private subclass of EnumSet, so check if it's an instance
        assertTrue(java.util.EnumSet.class.isAssignableFrom(validDbs.getClass()),
                "validDiseaseDatabases should return an EnumSet");
    }

    @Test
    void testFromStringCaseInsensitive() {
        // Assert all case variations work
        assertEquals(DiseaseDatabase.OMIM, DiseaseDatabase.fromString("OMIM"));
        assertEquals(DiseaseDatabase.OMIM, DiseaseDatabase.fromString("omim"));
        assertEquals(DiseaseDatabase.OMIM, DiseaseDatabase.fromString("Omim"));
        assertEquals(DiseaseDatabase.OMIM, DiseaseDatabase.fromString("oMiM"));
    }

    @Test
    void testOrphanetAliases() {
        // Both "ORPHA" and "ORPHANET" should map to ORPHANET enum
        assertEquals(DiseaseDatabase.ORPHANET, DiseaseDatabase.fromString("ORPHA"));
        assertEquals(DiseaseDatabase.ORPHANET, DiseaseDatabase.fromString("ORPHANET"));
        assertEquals(DiseaseDatabase.ORPHANET, DiseaseDatabase.fromString("orpha"));
        assertEquals(DiseaseDatabase.ORPHANET, DiseaseDatabase.fromString("orphanet"));
    }
}
