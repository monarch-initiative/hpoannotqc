package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntry;
import org.monarchinitiative.hpoannotqc.annotations.TestBase;
import org.monarchinitiative.hpoannotqc.annotations.util.TermValidator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AggregateFrequenciesTest extends TestBase {

    private final static List<String> line1 = List.of(
            "OMIM:191100",
            "Tuberous sclerosis-1",
            "HP:0012798",
            "Pulmonary lymphangiomyomatosis",
            EMPTY_STRING,
            EMPTY_STRING,
            "20/78",
            "FEMALE",
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            "PMID:10852420",
            "PCS",
            "HPO:probinson[2019-11-28]");


    private final static List<String> line2 = List.of(
            "OMIM:191100",
            "Tuberous sclerosis-1",
            "HP:0012798",
            "Pulmonary lymphangiomyomatosis",
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            "MALE",
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            "PMID:10852420",
            "PCS",
            "HPO:probinson[2019-11-28]");

    private final static List<String> line3 = List.of(
            "OMIM:191100",
            "Tuberous sclerosis-1",
            "HP:0012798",
            "Pulmonary lymphangiomyomatosis",
            EMPTY_STRING,
            EMPTY_STRING,
            "0/5",
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            EMPTY_STRING,
            "PMID:10852420",
            "PCS",
            "HPO:probinson[2019-11-28]");

    private static final AnnotationEntry entry1 = HpoAnnotationLine.fromLine(String.join("\t", AggregateFrequenciesTest.line1), validator, ontology);
    private static final AnnotationEntry entry2 = HpoAnnotationLine.fromLine(String.join("\t", AggregateFrequenciesTest.line2), validator, ontology);
    private static final AnnotationEntry entry3 = HpoAnnotationLine.fromLine(String.join("\t", AggregateFrequenciesTest.line3), validator, ontology);


    @Test
    public void testTemplateBaseCase() {
        assertFalse(entry1.hasError(), "Could not parse line 1");
        assertFalse(entry2.hasError(), "Could not parse line 2");
        assertFalse(entry3.hasError(), "Could not parse line 2");
    }

    /**
     * Here we do not merge the three entries, because the SEX fields are different
     */
    @Test
    public void testAggregateFrequencies() {
        TermValidator termValidator = new TermValidator(ontology);
        HpoAnnotationMerger annotationMerger = new HpoAnnotationMerger(ontology, termValidator);
        List<AnnotationEntry> annotationEntries = List.of(entry1, entry2, entry3);
        String basename = "OMIM-191100";
        HpoAnnotationModel model = new HpoAnnotationModel(basename, annotationEntries, List.of(), annotationMerger);
        HpoAnnotationModel mergedModel = model.getMergedModel();
        assertEquals(3, model.getNumberOfAnnotations());
        assertEquals(3, mergedModel.getNumberOfAnnotations());
    }

    /**
     * If we remove the SEX fields, we expect to merge the lines.
     * We also test that we correctly merge the frequency entries "20/78" and "0/5"
     */
    @Test
    public void testAggregateFrequenciesUnisex() {
        TermValidator termValidator = new TermValidator(ontology);
        HpoAnnotationMerger annotationMerger = new HpoAnnotationMerger(ontology, termValidator);
        List<String> line1NoSexFIELD = new ArrayList<>(line1);
        line1NoSexFIELD.set(7, EMPTY_STRING);
        AnnotationEntry entry1 = HpoAnnotationLine.fromLine(String.join("\t", line1NoSexFIELD), validator, ontology);
        List<String> line2NoSexFIELD = new ArrayList<>(line2);
        line2NoSexFIELD.set(7, EMPTY_STRING);
        AnnotationEntry entry2 = HpoAnnotationLine.fromLine(String.join("\t", line2NoSexFIELD), validator, ontology);
        List<AnnotationEntry> annotationEntries = List.of(entry1, entry2, entry3);
        String basename = "OMIM-191100";
        HpoAnnotationModel model = new HpoAnnotationModel(basename, annotationEntries, List.of(), annotationMerger);
        HpoAnnotationModel mergedModel = model.getMergedModel();
        assertEquals(3, model.getNumberOfAnnotations());
        assertEquals(1, mergedModel.getNumberOfAnnotations());
        List<AnnotationEntry> entryList = mergedModel.getEntryList();
        assertEquals(1,entryList.size());
        AnnotationEntry e1 = entryList.get(0);
        // frequencies are  "20/78" and "0/5" and no indication
        // the expected logic is that we only consider the n/m forms (since these are the most precise)
        // thus we sum numerator and denominator
        String expectedFreq = "20/83";
        assertEquals(expectedFreq, e1.getFrequencyModifier());
    }

}