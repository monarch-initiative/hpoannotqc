package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;
import org.monarchinitiative.phenol.ontology.data.TermId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiocurationChecker.
 * Tests both the string-based check() method and the entry-based checkEntry() method.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class BiocurationCheckerTest {

    // ============================================================================
    // Tests for check(String) method - existing tests plus additional coverage
    // ============================================================================

    @Test
    public void checkNameCuration() {
        String valid = "HPO:probinson[2023-04-01]";
        assertTrue(BiocurationChecker.check(valid));
        String noColon = "HPOprobinson[2023-04-01]";
        assertFalse(BiocurationChecker.check(noColon));
        String noDate = "HPOprobinson";
        assertFalse(BiocurationChecker.check(noDate));
    }

    @Test
    public void checkName2() {
        String valid = "HPO:skoehler[2010-06-20]";
        assertTrue(BiocurationChecker.check(valid));
    }

    @Test
    public void testIEI() {
        String valid = "HPO:iea[2009-02-17]";
        assertTrue(BiocurationChecker.check(valid));
    }

    @Test
    public void checkOrcidCuration() {
        String valid = "ORCID:0000-0002-5648-2155[2024-04-01]";
        assertTrue(BiocurationChecker.check(valid));
        String noColon = "ORCID0000-0002-5648-2155[2024-04-01]";
        assertFalse(BiocurationChecker.check(noColon));
        String noDate = "ORCID:0000-0002-5648-2155";
        assertFalse(BiocurationChecker.check(noDate));
    }

    @Test
    public void checkOnlyDate() {
        String noItem = "[2024-04-01]";
        assertFalse(BiocurationChecker.check(noItem));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HPO:probinson[2023-04-01]",
            "HPO:iea[2009-02-17]",
            "ORPHA:orphadata[2024-01-01]",
            "DECIPHER:team[2022-12-25]",
            "ORCID:0000-0002-5648-2155[2024-04-01]",
            "ORCID:1234-5678-9012-3456[2020-01-01]"
    })
    public void testValidBiocurationStrings(String validString) {
        assertTrue(BiocurationChecker.check(validString),
                "Should accept valid biocuration: " + validString);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HPO:probinson",                    // Missing date
            "probinson[2023-04-01]",           // Missing prefix
            "[2023-04-01]",                     // Only date
            "HPO-probinson[2023-04-01]",       // Wrong separator
            "HPO:probinson[2023/04/01]",       // Wrong date format
            "HPO:probinson[23-04-01]",         // Wrong year format
            "HPO:probinson[2023-4-1]",         // Missing leading zeros
            "ORCID:0000-0002-5648-2155",       // Missing date
            "ORCID:0000-2155[2024-04-01]",     // Wrong ORCID format
            "",                                 // Empty string
            "   "                               // Whitespace only
    })
    public void testInvalidBiocurationStrings(String invalidString) {
        assertFalse(BiocurationChecker.check(invalidString),
                "Should reject invalid biocuration: " + invalidString);
    }

    // ============================================================================
    // Tests for checkEntry(HpoAnnotationEntry) method - NEW comprehensive tests
    // ============================================================================

    @Test
    public void testCheckEntryWithValidSingleBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("HPO:probinson[2023-04-01]");

        // Act & Assert
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry),
                "Should accept entry with valid biocuration");
    }

    @Test
    public void testCheckEntryWithValidOrcidBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("ORCID:0000-0002-5648-2155[2024-04-01]");

        // Act & Assert
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry),
                "Should accept entry with valid ORCID biocuration");
    }

    @Test
    public void testCheckEntryWithMultipleBiocurations() {
        // Arrange - Multiple biocuration entries separated by semicolons
        HpoAnnotationEntry entry = createDummyEntry(
                "HPO:probinson[2023-04-01];ORPHA:orphadata[2024-01-01];HPO:skoehler[2010-06-20]"
        );

        // Act & Assert
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry),
                "Should accept entry with multiple valid biocurations");
    }

    @Test
    public void testCheckEntryWithEmptyBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("");

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Empty biocuration entry"),
                "Error message should indicate empty biocuration");
    }

    @Test
    public void testCheckEntryWithNullBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry(null);

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Empty biocuration entry"),
                "Error message should indicate empty biocuration");
    }

    @Test
    public void testCheckEntryWithMalformedBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("HPO:probinson");  // Missing date

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"),
                "Error message should indicate malformed biocuration");
        assertTrue(exception.getMessage().contains("HPO:probinson"),
                "Error message should include the malformed entry");
    }

    @Test
    public void testCheckEntryWithMultipleBiocurationsOneInvalid() {
        // Arrange - Mix of valid and invalid biocurations
        HpoAnnotationEntry entry = createDummyEntry(
                "HPO:probinson[2023-04-01];INVALID_ENTRY;HPO:skoehler[2010-06-20]"
        );

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"),
                "Should detect invalid entry in list");
        assertTrue(exception.getMessage().contains("INVALID_ENTRY"),
                "Error message should identify the invalid entry");
    }

    @Test
    public void testCheckEntryWithMissingDateBrackets() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("HPO:probinson2023-04-01");

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"));
    }

    @Test
    public void testCheckEntryWithWrongDateFormat() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("HPO:probinson[2023/04/01]");

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"));
    }

    @Test
    public void testCheckEntryWithInvalidOrcidFormat() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("ORCID:0000-2155[2024-04-01]");  // Wrong ORCID format

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"));
    }

    @Test
    public void testCheckEntryWithVariousDatabasePrefixes() {
        // Arrange - Test different valid database prefixes
        HpoAnnotationEntry entry1 = createDummyEntry("ORPHA:orphadata[2024-01-01]");
        HpoAnnotationEntry entry2 = createDummyEntry("DECIPHER:team[2022-12-25]");
        HpoAnnotationEntry entry3 = createDummyEntry("OMIM:curator[2021-06-15]");

        // Act & Assert
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry1));
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry2));
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry3));
    }

    @Test
    public void testCheckEntryWithWhitespaceInBiocuration() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("HPO:probinson [2023-04-01]");  // Space before date

        // Act & Assert
        HpoaEntryError exception = assertThrows(HpoaEntryError.class,
                () -> BiocurationChecker.checkEntry(entry));

        assertTrue(exception.getMessage().contains("Malformed biocuration entry"));
    }

    @Test
    public void testCheckEntryWithLeadingTrailingSpaces() {
        // Arrange
        HpoAnnotationEntry entry = createDummyEntry("  HPO:probinson[2023-04-01]  ");

        // Act & Assert - The regex uses find() so it will find the pattern within the string
        // even with leading/trailing spaces
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry),
                "The regex find() method will match the valid pattern even with surrounding spaces");
    }

    @Test
    public void testCheckEntryWithComplexMultipleEntries() {
        // Arrange - Mix of different valid formats
        HpoAnnotationEntry entry = createDummyEntry(
                "HPO:probinson[2023-04-01];" +
                "ORCID:0000-0002-5648-2155[2024-04-01];" +
                "ORPHA:orphadata[2024-01-01];" +
                "HPO:iea[2009-02-17]"
        );

        // Act & Assert
        assertDoesNotThrow(() -> BiocurationChecker.checkEntry(entry),
                "Should accept multiple valid biocurations of different formats");
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a dummy HpoAnnotationEntry with the specified biocuration string.
     * Uses minimal valid data for other required fields.
     */
    private HpoAnnotationEntry createDummyEntry(String biocuration) {
        return HpoAnnotationEntry.fromOrphaData(
                "OMIM:600123",
                "Dummy Disease for Testing",
                "HP:0001234",
                "Dummy Phenotype",
                TermId.of("HP:0040283"),  // Occasional frequency
                biocuration
        );
    }
}
