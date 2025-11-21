package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HpoAnnotationEntry.
 * Tests all getter methods, factory methods, and transformation methods.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class HpoAnnotationEntryTest {

    private static Ontology ontology;

    @BeforeAll
    static void setUp() {
        // Load the test HPO ontology using ClassLoader
        ClassLoader cl = HpoAnnotationEntryTest.class.getClassLoader();
        File file = new File(cl.getResource("hp.json").getFile());
        ontology = OntologyLoader.loadOntology(file);
    }

    // ============================================================================
    // Tests for getter methods with complete entry
    // ============================================================================

    @Test
    void testGetDiseaseId() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("OMIM:600123", entry.getDiseaseId());
    }

    @Test
    void testGetDiseaseTermId() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals(TermId.of("OMIM:600123"), entry.getDiseaseTermId());
    }

    @Test
    void testGetDatabasePrefix() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("OMIM", entry.getDatabasePrefix());
    }

    @Test
    void testGetDatabaseIdentifier() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("600123", entry.getDatabaseIdentifier());
    }

    @Test
    void testGetDiseaseName() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("Test Disease", entry.getDiseaseName());
    }

    @Test
    void testGetPhenotypeId() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals(TermId.of("HP:0001234"), entry.getPhenotypeId());
    }

    @Test
    void testGetPhenotypeLabel() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("Test Phenotype", entry.getPhenotypeLabel());
    }

    @Test
    void testGetAgeOfOnsetId() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("HP:0003577", entry.getAgeOfOnsetId());
    }

    @Test
    void testGetAgeOfOnsetLabel() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("Congenital onset", entry.getAgeOfOnsetLabel());
    }

    @Test
    void testGetEvidenceCode() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("TAS", entry.getEvidenceCode());
    }

    @Test
    void testGetFrequencyModifier() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("HP:0040283", entry.getFrequencyModifier());
    }

    @Test
    void testGetSex() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("MALE", entry.getSex());
    }

    @Test
    void testGetNegation() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("", entry.getNegation());
    }

    @Test
    void testGetModifier() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("HP:0012828", entry.getModifier());
    }

    @Test
    void testGetDescription() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("Test description", entry.getDescription());
    }

    @Test
    void testGetPublication() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("PMID:12345678", entry.getPublication());
    }

    @Test
    void testGetBiocuration() {
        HpoAnnotationEntry entry = createFullEntry();
        assertEquals("HPO:probinson[2023-04-01]", entry.getBiocuration());
    }

    // ============================================================================
    // Tests for getter methods with null/empty values
    // ============================================================================

    @Test
    void testGetAgeOfOnsetLabelWithNull() {
        HpoAnnotationEntry entry = createMinimalEntry();
        assertEquals("", entry.getAgeOfOnsetLabel());
    }

    @Test
    void testGetFrequencyModifierWithNull() {
        HpoAnnotationEntry entry = createMinimalEntry();
        assertEquals("", entry.getFrequencyModifier());
    }

    @Test
    void testGetNegationWithNull() {
        HpoAnnotationEntry entry = createMinimalEntry();
        assertEquals("", entry.getNegation());
    }

    @Test
    void testGetModifierWithNull() {
        HpoAnnotationEntry entry = createMinimalEntry();
        assertEquals("", entry.getModifier());
    }

    @Test
    void testGetDescriptionWithNull() {
        HpoAnnotationEntry entry = createMinimalEntry();
        assertEquals("", entry.getDescription());
    }

    // ============================================================================
    // Tests for different disease database prefixes
    // ============================================================================

    @ParameterizedTest
    @CsvSource({
            "OMIM:600123, OMIM, 600123",
            "ORPHA:12345, ORPHA, 12345",
            "DECIPHER:1, DECIPHER, 1",
            "MONDO:0007972, MONDO, 0007972"
    })
    void testDifferentDiseasePrefixes(String diseaseId, String expectedPrefix, String expectedId) {
        HpoAnnotationEntry entry = HpoAnnotationEntry.fromOrphaData(
                diseaseId,
                "Test Disease",
                "HP:0001234",
                "Test Phenotype",
                TermId.of("HP:0040283"),
                "ORPHA:orphadata[2024-01-01]"
        );

        assertEquals(expectedPrefix, entry.getDatabasePrefix());
        assertEquals(expectedId, entry.getDatabaseIdentifier());
    }

    // ============================================================================
    // Tests for toString() and getRow() methods
    // ============================================================================

    @Test
    void testToString() {
        HpoAnnotationEntry entry = createMinimalEntry();
        String result = entry.toString();

        assertNotNull(result);
        assertTrue(result.contains("OMIM:600123"));
        assertTrue(result.contains("HP:0001234"));
        assertTrue(result.contains("Test Disease"));
    }

    @Test
    void testGetRow() {
        HpoAnnotationEntry entry = createMinimalEntry();
        String row = entry.getRow();

        assertNotNull(row);
        assertTrue(row.contains("OMIM:600123"));
        assertTrue(row.contains("Test Disease"));
        assertTrue(row.contains("HP:0001234"));
        assertTrue(row.contains("Test Phenotype"));
        assertTrue(row.endsWith("\n"));
    }

    @Test
    void testGetRowWithNullFields() {
        HpoAnnotationEntry entry = createMinimalEntry();
        String row = entry.getRow();

        // Row should not contain the string "null" for empty fields
        assertFalse(row.contains("null"));
    }

    // ============================================================================
    // Tests for toBigFileLine() method
    // ============================================================================

    @Test
    void testToBigFileLine() {
        HpoAnnotationEntry entry = createFullEntry();
        String bigFileLine = entry.toBigFileLine(ontology);

        assertNotNull(bigFileLine);
        assertTrue(bigFileLine.contains("OMIM:600123"));
        assertTrue(bigFileLine.contains("Test Disease"));
        assertTrue(bigFileLine.contains("HP:0001234"));
        assertTrue(bigFileLine.contains("PMID:12345678"));
        assertTrue(bigFileLine.contains("TAS"));
    }

    @Test
    void testToBigFileLineWithNegation() {
        HpoAnnotationEntry entry = new HpoAnnotationEntry(
                TermId.of("OMIM:600123"),
                "Test Disease",
                TermId.of("HP:0001234"),
                "Test Phenotype",
                "",
                "",
                "",
                "",
                "NOT",
                "",
                "",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );

        String bigFileLine = entry.toBigFileLine(ontology);
        assertTrue(bigFileLine.contains("NOT"));
    }

    // ============================================================================
    // Tests for fromLine() factory method
    // ============================================================================

    @Test
    void testFromLineWithValidData() throws HpoAnnotQcException {
        String line = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype\t" +
                "HP:0003577\tCongenital onset\tHP:0040283\tMALE\t\t" +
                "HP:0012828\tTest description\tPMID:12345678\tTAS\tHPO:probinson[2023-04-01]";

        HpoAnnotationEntry entry = HpoAnnotationEntry.fromLine(line);

        assertNotNull(entry);
        assertEquals("OMIM:600123", entry.getDiseaseId());
        assertEquals("Test Disease", entry.getDiseaseName());
        assertEquals(TermId.of("HP:0001234"), entry.getPhenotypeId());
        assertEquals("Test Phenotype", entry.getPhenotypeLabel());
    }

    @Test
    void testFromLineWithMinimalData() throws HpoAnnotQcException {
        String line = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype\t" +
                "\t\t\t\t\t\t\t\tTAS\tHPO:probinson[2023-04-01]";

        HpoAnnotationEntry entry = HpoAnnotationEntry.fromLine(line);

        assertNotNull(entry);
        assertEquals("OMIM:600123", entry.getDiseaseId());
        assertEquals("", entry.getAgeOfOnsetLabel());
        assertEquals("", entry.getFrequencyModifier());
    }

    @Test
    void testFromLineWithTooFewFields() {
        String line = "OMIM:600123\tTest Disease\tHP:0001234";

        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationEntry.fromLine(line);
        });

        assertTrue(exception.getMessage().contains("expecting 14"));
    }

    @Test
    void testFromLineWithTooManyFields() {
        String line = "OMIM:600123\tTest Disease\tHP:0001234\tTest Phenotype\t" +
                "\t\t\t\t\t\t\t\tTAS\tHPO:probinson[2023-04-01]\tExtraField";

        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationEntry.fromLine(line);
        });

        assertTrue(exception.getMessage().contains("expecting 14"));
    }

    // ============================================================================
    // Tests for fromOrphaData() factory method
    // ============================================================================

    @Test
    void testFromOrphaDataWithNormalFrequency() {
        HpoAnnotationEntry entry = HpoAnnotationEntry.fromOrphaData(
                "ORPHA:12345",
                "Rare Disease",
                "HP:0001234",
                "Test Phenotype",
                TermId.of("HP:0040283"), // Occasional frequency
                "ORPHA:orphadata[2024-01-01]"
        );

        assertNotNull(entry);
        assertEquals("ORPHA:12345", entry.getDiseaseId());
        assertEquals("Rare Disease", entry.getDiseaseName());
        assertEquals("HP:0040283", entry.getFrequencyModifier());
        assertEquals("", entry.getNegation());
        assertEquals("TAS", entry.getEvidenceCode());
    }

    @Test
    void testFromOrphaDataWithExcludedFrequency() {
        HpoAnnotationEntry entry = HpoAnnotationEntry.fromOrphaData(
                "ORPHA:12345",
                "Rare Disease",
                "HP:0001234",
                "Test Phenotype",
                TermId.of("HP:0040285"), // Excluded frequency (0%)
                "ORPHA:orphadata[2024-01-01]"
        );

        assertNotNull(entry);
        assertEquals("", entry.getFrequencyModifier());
        assertEquals("NOT", entry.getNegation());
    }

    @Test
    void testFromOrphaDataWithNullHpoId() {
        PhenolRuntimeException exception = assertThrows(PhenolRuntimeException.class, () -> {
            HpoAnnotationEntry.fromOrphaData(
                    "ORPHA:12345",
                    "Rare Disease",
                    null,
                    "Test Phenotype",
                    TermId.of("HP:0040283"),
                    "ORPHA:orphadata[2024-01-01]"
            );
        });

        assertTrue(exception.getMessage().contains("Null String passed as hpoId"));
    }

    // ============================================================================
    // Tests for fromOrphaInheritanceData() factory method
    // ============================================================================

    @Test
    void testFromOrphaInheritanceData() {
        HpoAnnotationEntry entry = HpoAnnotationEntry.fromOrphaInheritanceData(
                "ORPHA:12345",
                "Rare Disease",
                TermId.of("HP:0000006"), // Autosomal dominant
                "Autosomal dominant inheritance",
                "ORPHA:orphadata[2024-01-01]"
        );

        assertNotNull(entry);
        assertEquals("ORPHA:12345", entry.getDiseaseId());
        assertEquals("Rare Disease", entry.getDiseaseName());
        assertEquals(TermId.of("HP:0000006"), entry.getPhenotypeId());
        assertEquals("Autosomal dominant inheritance", entry.getPhenotypeLabel());
        assertEquals("TAS", entry.getEvidenceCode());
        assertEquals("", entry.getFrequencyModifier());
    }

    // ============================================================================
    // Tests for withUpdatedPhenotype() method
    // ============================================================================

    @Test
    void testWithUpdatedPhenotype() {
        HpoAnnotationEntry original = createFullEntry();

        TermId newPhenotypeId = TermId.of("HP:0009999");
        String newPhenotypeLabel = "Updated Phenotype";

        HpoAnnotationEntry updated = original.withUpdatedPhenotype(newPhenotypeId, newPhenotypeLabel);

        // Check updated fields
        assertEquals(newPhenotypeId, updated.getPhenotypeId());
        assertEquals(newPhenotypeLabel, updated.getPhenotypeLabel());

        // Check that other fields remain unchanged
        assertEquals(original.getDiseaseId(), updated.getDiseaseId());
        assertEquals(original.getDiseaseName(), updated.getDiseaseName());
        assertEquals(original.getAgeOfOnsetId(), updated.getAgeOfOnsetId());
        assertEquals(original.getBiocuration(), updated.getBiocuration());
    }

    // ============================================================================
    // Tests for withUpdatedOnset() method
    // ============================================================================

    @Test
    void testWithUpdatedOnset() {
        HpoAnnotationEntry original = createFullEntry();

        String newOnsetId = "HP:0003584";
        String newOnsetLabel = "Late onset";

        HpoAnnotationEntry updated = original.withUpdatedOnset(newOnsetId, newOnsetLabel);

        // Check updated fields
        assertEquals(newOnsetId, updated.getAgeOfOnsetId());
        assertEquals(newOnsetLabel, updated.getAgeOfOnsetLabel());

        // Check that other fields remain unchanged
        assertEquals(original.getDiseaseId(), updated.getDiseaseId());
        assertEquals(original.getPhenotypeId(), updated.getPhenotypeId());
        assertEquals(original.getPhenotypeLabel(), updated.getPhenotypeLabel());
        assertEquals(original.getBiocuration(), updated.getBiocuration());
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a fully populated HpoAnnotationEntry for testing all fields.
     */
    private HpoAnnotationEntry createFullEntry() {
        return new HpoAnnotationEntry(
                TermId.of("OMIM:600123"),
                "Test Disease",
                TermId.of("HP:0001234"),
                "Test Phenotype",
                "HP:0003577",
                "Congenital onset",
                "HP:0040283",
                "MALE",
                "",
                "HP:0012828",
                "Test description",
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }

    /**
     * Creates a minimal HpoAnnotationEntry with only required fields and nulls for optional fields.
     */
    private HpoAnnotationEntry createMinimalEntry() {
        return new HpoAnnotationEntry(
                TermId.of("OMIM:600123"),
                "Test Disease",
                TermId.of("HP:0001234"),
                "Test Phenotype",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "PMID:12345678",
                "TAS",
                "HPO:probinson[2023-04-01]"
        );
    }
}
