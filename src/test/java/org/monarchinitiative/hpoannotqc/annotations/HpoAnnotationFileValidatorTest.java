package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HpoAnnotationFileValidator.
 *
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
class HpoAnnotationFileValidatorTest {

    @Test
    void testExpectedFieldsArrayHasCorrectLength() {
        // Assert
        assertEquals(14, HpoAnnotationFileValidator.EXPECTED_FIELDS.length);
    }

    @Test
    void testExpectedFieldsHasCorrectValues() {
        // Assert
        String[] expected = {
                "#diseaseID",
                "diseaseName",
                "phenotypeID",
                "phenotypeName",
                "onsetID",
                "onsetName",
                "frequency",
                "sex",
                "negation",
                "modifier",
                "description",
                "publication",
                "evidence",
                "biocuration"
        };
        assertArrayEquals(expected, HpoAnnotationFileValidator.EXPECTED_FIELDS);
    }

    @Test
    void testQcHeaderLineWithValidHeader() {
        // Arrange
        String validHeader = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS);

        // Act & Assert
        assertDoesNotThrow(() -> HpoAnnotationFileValidator.qcHeaderLine(validHeader));
    }

    @Test
    void testQcHeaderLineWithTooFewFields() {
        // Arrange
        String invalidHeader = "#diseaseID\tdiseaseName\tphenotypeID";

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(invalidHeader);
        });

        assertTrue(exception.getMessage().contains("Expecting 14 fields but got 3"));
    }

    @Test
    void testQcHeaderLineWithTooManyFields() {
        // Arrange
        String invalidHeader = String.join("\t", HpoAnnotationFileValidator.EXPECTED_FIELDS) + "\textraField";

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(invalidHeader);
        });

        assertTrue(exception.getMessage().contains("Expecting 14 fields but got 15"));
    }

    @Test
    void testQcHeaderLineWithWrongFieldName() {
        // Arrange
        String[] wrongFields = HpoAnnotationFileValidator.EXPECTED_FIELDS.clone();
        wrongFields[0] = "diseaseID"; // Missing the # prefix
        String invalidHeader = String.join("\t", wrongFields);

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(invalidHeader);
        });

        assertTrue(exception.getMessage().contains("Malformed field 0"));
        assertTrue(exception.getMessage().contains("Expected #diseaseID but got diseaseID"));
    }

    @Test
    void testQcHeaderLineWithWrongFieldOrder() {
        // Arrange
        String invalidHeader = "diseaseName\t#diseaseID\tphenotypeID\tphenotypeName\tonsetID\tonsetName\tfrequency\tsex\tnegation\tmodifier\tdescription\tpublication\tevidence\tbiocuration";

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(invalidHeader);
        });

        assertTrue(exception.getMessage().contains("Malformed field 0"));
    }

    @Test
    void testQcHeaderLineWithMisspelledField() {
        // Arrange
        String[] wrongFields = HpoAnnotationFileValidator.EXPECTED_FIELDS.clone();
        wrongFields[2] = "phenotypeId"; // Wrong capitalization
        String invalidHeader = String.join("\t", wrongFields);

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(invalidHeader);
        });

        assertTrue(exception.getMessage().contains("Malformed field 2"));
        assertTrue(exception.getMessage().contains("Expected phenotypeID but got phenotypeId"));
    }

    @Test
    void testQcHeaderLineWithEmptyString() {
        // Arrange
        String emptyHeader = "";

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(emptyHeader);
        });

        assertTrue(exception.getMessage().contains("Expecting 14 fields but got 1"));
    }

    @Test
    void testQcHeaderLineWithOnlyTabs() {
        // Arrange
        String tabHeader = "\t\t\t\t\t\t\t\t\t\t\t\t\t";

        // Act & Assert
        HpoAnnotQcException exception = assertThrows(HpoAnnotQcException.class, () -> {
            HpoAnnotationFileValidator.qcHeaderLine(tabHeader);
        });

        // With 14 tabs, we get 14 fields (all empty strings)
        // The exception should contain info about malformed field or empty fields
        assertTrue(exception.getMessage().contains("Malformed") ||
                   exception.getMessage().contains("Expected"));
    }
}
