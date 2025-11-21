package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteAspectError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteTermError;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HpoAnnotationEntryValidator.
 * Tests all validation methods including database, evidence, sex, negation, publication,
 * frequency, phenotype, and modifier validations.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class HpoAnnotationEntryValidatorTest {

    private static Ontology ontology;

    @BeforeAll
    static void setUp() {
        // Load the test HPO ontology using ClassLoader
        ClassLoader cl = HpoAnnotationEntryValidatorTest.class.getClassLoader();
        File file = new File(cl.getResource("hp.json").getFile());
        ontology = OntologyLoader.loadOntology(file);
    }

    // ============================================================================
    // Tests for checkDB() - Database validation
    // ============================================================================

    @Test
    void testCheckDbWithValidOmim() throws Exception {
        HpoAnnotationEntry entry = createValidEntry("OMIM:600123");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckDbWithValidOrpha() throws Exception {
        HpoAnnotationEntry entry = createValidEntry("ORPHA:12345");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckDbWithValidDecipher() throws Exception {
        HpoAnnotationEntry entry = createValidEntry("DECIPHER:1");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckDbWithInvalidDatabase() {
        HpoAnnotationEntry entry = createEntryWithCustomFields(
                "INVALID:123",
                "Test Disease",
                "HP:0001166", // Valid phenotype
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Invalid database symbol") ||
                   exception.getMessage().contains("not recognized"));
    }

    @Test
    void testCheckDbWithEmptyDiseaseName() {
        HpoAnnotationEntry entry = createEntryWithCustomFields(
                "OMIM:600123",
                "",  // Empty disease name
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Missing disease name"));
    }

    @Test
    void testCheckDbWithNullDiseaseName() {
        HpoAnnotationEntry entry = createEntryWithCustomFields(
                "OMIM:600123",
                null,  // Null disease name
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Missing disease name"));
    }

    // ============================================================================
    // Tests for checkEvidence() - Evidence code validation
    // ============================================================================

    @ParameterizedTest
    @ValueSource(strings = {"TAS", "IEA", "PCS"})
    void testCheckEvidenceWithValidCodes(String evidenceCode) throws Exception {
        HpoAnnotationEntry entry = createEntryWithEvidence(evidenceCode);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckEvidenceWithInvalidCode() {
        HpoAnnotationEntry entry = createEntryWithEvidence("INVALID");

        assertThrows(Exception.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });
    }

    // ============================================================================
    // Tests for checkSexEntry() - Sex validation
    // ============================================================================

    @Test
    void testCheckSexWithMale() throws Exception {
        HpoAnnotationEntry entry = createEntryWithSex("MALE");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckSexWithFemale() throws Exception {
        HpoAnnotationEntry entry = createEntryWithSex("FEMALE");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckSexWithEmpty() throws Exception {
        HpoAnnotationEntry entry = createEntryWithSex("");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckSexWithInvalidValue() throws Exception {
        // Invalid sex values are treated as "not specified" and are acceptable
        HpoAnnotationEntry entry = createEntryWithSex("INVALID");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    // ============================================================================
    // Tests for checkNegation() - Negation validation
    // ============================================================================

    @Test
    void testCheckNegationWithNot() throws Exception {
        HpoAnnotationEntry entry = createEntryWithNegation("NOT");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckNegationWithEmpty() throws Exception {
        HpoAnnotationEntry entry = createEntryWithNegation("");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckNegationWithNull() throws Exception {
        HpoAnnotationEntry entry = createEntryWithNegation(null);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckNegationWithInvalidValue() {
        HpoAnnotationEntry entry = createEntryWithNegation("INVALID");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed negation entry"));
    }

    // ============================================================================
    // Tests for checkPublication() - Publication/citation validation
    // ============================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PMID:12345678",
            "OMIM:600123",
            "http://example.com/paper",
            "https://example.com/paper",
            "DECIPHER:1",
            "ORPHA:12345",
            "ISBN:1234567890",
            "ISBN-10:1234567890",
            "ISBN-13:1234567890123"
    })
    void testCheckPublicationWithValidPrefixes(String publication) throws Exception {
        HpoAnnotationEntry entry = createEntryWithPublication(publication);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckPublicationWithEmptyString() {
        HpoAnnotationEntry entry = createEntryWithPublication("");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Empty citation string"));
    }

    @Test
    void testCheckPublicationWithNullString() {
        HpoAnnotationEntry entry = createEntryWithPublication(null);

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Empty citation string"));
    }

    @Test
    void testCheckPublicationWithNoColon() {
        HpoAnnotationEntry entry = createEntryWithPublication("PMID12345678");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("not a CURIE"));
    }

    @Test
    void testCheckPublicationWithDoubleColon() {
        HpoAnnotationEntry entry = createEntryWithPublication("PMID::12345678");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("double colon"));
    }

    @Test
    void testCheckPublicationWithSpace() {
        HpoAnnotationEntry entry = createEntryWithPublication("PMID: 12345678");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("contains space"));
    }

    @Test
    void testCheckPublicationWithInvalidPrefix() {
        HpoAnnotationEntry entry = createEntryWithPublication("INVALID:12345");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Did not recognize publication prefix"));
    }

    @Test
    void testCheckPublicationWithColonOnly() {
        HpoAnnotationEntry entry = createEntryWithPublication("PMID:");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed publication string"));
    }

    // ============================================================================
    // Tests for checkFrequency() - Frequency validation (non-ontology parts)
    // ============================================================================

    @Test
    void testCheckFrequencyWithEmptyString() throws Exception {
        HpoAnnotationEntry entry = createEntryWithFrequency("");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckFrequencyWithNullString() throws Exception {
        HpoAnnotationEntry entry = createEntryWithFrequency(null);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1/10", "5/100", "1/1", "0/10"})
    void testCheckFrequencyWithValidRatios(String frequency) throws Exception {
        HpoAnnotationEntry entry = createEntryWithFrequency(frequency);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckFrequencyWithInvalidRatioNumeratorTooLarge() {
        HpoAnnotationEntry entry = createEntryWithFrequency("10/5");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed frequency"));
    }

    @Test
    void testCheckFrequencyWithInvalidRatioZeroDenominator() {
        HpoAnnotationEntry entry = createEntryWithFrequency("1/0");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed frequency"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"50%", "10.5%", "0.1%", "99.9%", "100%"})
    void testCheckFrequencyWithValidPercentages(String frequency) throws Exception {
        HpoAnnotationEntry entry = createEntryWithFrequency(frequency);
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    @Test
    void testCheckFrequencyWithInvalidPercentageTooLarge() {
        HpoAnnotationEntry entry = createEntryWithFrequency("150%");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed frequency"));
    }

    @Test
    void testCheckFrequencyWithInvalidPercentageZero() {
        HpoAnnotationEntry entry = createEntryWithFrequency("0%");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed frequency"));
    }

    @Test
    void testCheckFrequencyWithMalformedString() {
        HpoAnnotationEntry entry = createEntryWithFrequency("invalid");

        HpoaEntryError exception = assertThrows(HpoaEntryError.class, () -> {
            HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
        });

        assertTrue(exception.getMessage().contains("Malformed frequency"));
    }

    // TODO: Add tests for frequency as HPO term (requires ontology validation)
    // Example: HP:0040283 (Occasional), HP:0040284 (Very frequent), etc.

    // ============================================================================
    // Tests for checkPhenotypeFields() - Phenotype validation
    // TODO: User will implement ontology-related phenotype validation tests
    // ============================================================================

    // TODO: Test with null phenotype ID
    // TODO: Test with phenotype ID not in ontology
    // TODO: Test with obsolete phenotype ID (should throw ObsoleteTermError)
    // TODO: Test with phenotype label mismatch (should throw ObsoleteTermError)
    // TODO: Test with valid phenotype ID and matching label

    // ============================================================================
    // Tests for checkAgeOfOnsetFields() - Age of onset validation
    // TODO: User will implement ontology-related onset validation tests
    // ============================================================================

    // TODO: Test with empty onset ID and empty onset label (should pass)
    // TODO: Test with empty onset ID but present onset label (should fail)
    // TODO: Test with obsolete onset ID (should throw ObsoleteAspectError)
    // TODO: Test with onset ID that is not an inheritance/onset term
    // TODO: Test with onset label mismatch
    // TODO: Test with valid onset ID and matching label

    // ============================================================================
    // Tests for checkModifier() - Modifier validation
    // TODO: User will implement ontology-related modifier validation tests
    // ============================================================================

    // TODO: Test with null/empty modifier (should pass)
    // TODO: Test with valid single modifier term
    // TODO: Test with valid multiple modifier terms (semicolon-separated)
    // TODO: Test with invalid modifier term (not in valid modifier hierarchy)
    // TODO: Test with malformed modifier term ID

    // ============================================================================
    // Tests for performQualityControl() - Full validation
    // ============================================================================

    @Test
    void testPerformQualityControlWithCompletelyValidEntry() throws Exception {
        HpoAnnotationEntry entry = createValidEntry("OMIM:600123");
        assertDoesNotThrow(() -> HpoAnnotationEntryValidator.performQualityControl(entry, ontology));
    }

    // TODO: Add more integration tests that test multiple validation failures

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a valid HpoAnnotationEntry for testing.
     * Uses a real HPO term that should exist in the test ontology.
     */
    private HpoAnnotationEntry createValidEntry(String diseaseId) {
        return new HpoAnnotationEntry(
                TermId.of(diseaseId),
                "Test Disease",
                TermId.of("HP:0001166"), // Arachnodactyly - should be in test ontology
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates an entry with custom field values for specific testing.
     */
    private HpoAnnotationEntry createEntryWithCustomFields(String diseaseId,
                                                           String diseaseName,
                                                           String phenotypeId,
                                                           String phenotypeName,
                                                           String onsetId,
                                                           String onsetName,
                                                           String frequency,
                                                           String sex,
                                                           String negation,
                                                           String modifier,
                                                           String description,
                                                           String publication,
                                                           String evidence,
                                                           String biocuration) {
        return new HpoAnnotationEntry(
                TermId.of(diseaseId),
                diseaseName,
                TermId.of(phenotypeId),
                phenotypeName,
                onsetId,
                onsetName,
                frequency,
                sex,
                negation,
                modifier,
                description,
                publication,
                evidence,
                biocuration
        );
    }

    /**
     * Creates an entry with a specific evidence code.
     */
    private HpoAnnotationEntry createEntryWithEvidence(String evidenceCode) {
        return createEntryWithCustomFields(
                "OMIM:600123",
                "Test Disease",
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "PMID:12345678",
                evidenceCode,
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates an entry with a specific sex value.
     */
    private HpoAnnotationEntry createEntryWithSex(String sex) {
        return createEntryWithCustomFields(
                "OMIM:600123",
                "Test Disease",
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                sex,
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates an entry with a specific negation value.
     */
    private HpoAnnotationEntry createEntryWithNegation(String negation) {
        return createEntryWithCustomFields(
                "OMIM:600123",
                "Test Disease",
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                negation,
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates an entry with a specific publication value.
     */
    private HpoAnnotationEntry createEntryWithPublication(String publication) {
        return createEntryWithCustomFields(
                "OMIM:600123",
                "Test Disease",
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                publication,
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates an entry with a specific frequency value.
     */
    private HpoAnnotationEntry createEntryWithFrequency(String frequency) {
        return createEntryWithCustomFields(
                "OMIM:600123",
                "Test Disease",
                "HP:0001166",
                "Arachnodactyly",
                "",
                "",
                frequency,
                "",
                "",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }
}
