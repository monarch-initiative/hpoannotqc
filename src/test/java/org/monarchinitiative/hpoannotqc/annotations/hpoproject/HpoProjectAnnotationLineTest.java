package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntry;
import org.monarchinitiative.hpoannotqc.annotations.TestBase;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorCategory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HpoProjectAnnotationLineTest extends TestBase {

    private final static String EMPTY_STRING = "";

    private final static List<String> template = List.of(
            "OMIM:106100",
                    "Angioedema, hereditary, 1",
                    "HP:0040078",
                    "Axonal degeneration",
            EMPTY_STRING,
            EMPTY_STRING,
            "2/2",
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            "PMID:16813612",
            "PCS",
            "HPO:probinson[2022-03-31]");


    @Test
    public void testemplateFieldNumber() {
        int expectedFields = 14;
        assertEquals(expectedFields, template.size());
    }

    @Test
    public void testTemplateBaseCase() {
        String line = String.join("\t", template);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertFalse(entry.hasError());
    }

    @Test
    public void testInvalidDiseaseId() {
        List<String> fields = new ArrayList<>(template);
        fields.set(0,"OM:123456");
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_TERM_ID, error1.category());
    }

    @Test
    public void testEmptyDiseaseName() {
        List<String> fields = new ArrayList<>(template);
        fields.set(1,EMPTY_STRING);
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.EMPTY_DISEASE_NAME, error1.category());
    }

    @Test
    public void testInvalidHpoId() {
        List<String> fields = new ArrayList<>(template);
        fields.set(2,"HP:9999999");
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.TERM_ID_NOT_IN_ONTOLOGY, error1.category());
    }

    @Test
    public void testObsoleteHpoId() {
        List<String> fields = new ArrayList<>(template);
        fields.set(2,"HP:0004715");
        fields.set(3,"Multicystic kidney dysplasia");
        // HP:0004715 alt id for Multicystic kidney dysplasia
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.OBSOLETE_TERM_ID, error1.category());
    }

    @Test
    public void testBadFrequencyInvalidFraction() {
        List<String> fields = new ArrayList<>(template);
        fields.set(6,"4/3");
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.INVALID_FRACTION, error1.category());
    }

    @Test
    public void testInvalidSexString() {
        List<String> fields = new ArrayList<>(template);
        fields.set(7,"4/7"); // field 7 is for sex, maybe we mixed up the index
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_SEX_STRING, error1.category());
    }

    @Test
    public void testInvalidNegationString() {
        List<String> fields = new ArrayList<>(template);
        fields.set(8,"NONE"); // only empty or NOT is correct
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_NEGATION, error1.category());
    }


    @Test
    public void testMalformedModifier() {
        List<String> fields = new ArrayList<>(template);
        fields.set(9,"HP:0002240"); // Hepatomegaly HP:0002240, not a modifier!
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.INVALID_MODIFIER_TERMID, error1.category());
    }

    @Test
    public void testMalformedBiocuration() {
        List<String> fields = new ArrayList<>(template);
        fields.set(13,"HPO:probinson[2021-06-21"); // Missing closing bracket
        String line = String.join("\t", fields);
        AnnotationEntry entry = HpoAnnotationLine.fromLine(line, validator, ontology);
        assertTrue(entry.hasError());
        List<HpoaError> errors = entry.getErrors();
        assertEquals(1, errors.size());
        HpoaError error1 = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY, error1.category());
    }
}
