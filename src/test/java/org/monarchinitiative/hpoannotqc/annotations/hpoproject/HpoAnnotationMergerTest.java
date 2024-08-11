package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.hpoannotqc.annotations.TestBase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HpoAnnotationMergerTest extends TestBase  {

    private final static String EMPTY_STRING = "";
    /** items up to onset field that we will not test */
    private final static List<String> baseTemplate = List.of("OMIM:106100",
            "Angioedema, hereditary, 1", "HP:0040078", "Axonal degeneration",EMPTY_STRING, EMPTY_STRING);
    private final static String BIOCURATION = "HPO:probinson[2022-03-31]";
    private final static String BIOCURATION2 = "HPO:skoehler[2010-06-19]";
    private final static String BIOCURATION3 = "ORCID:0000-0002-0736-9199[2024-04-01]";
    private final static String PUBLICATION = "PMID:3001231";


    private static AnnotationEntryI makeEntry(String frequency,
                                              String sex,
                                              String negation,
                                              String modifier,
                                              String description,
                                              String publication,
                                              String evidence,
                                              String biocuration) {
        List<String> fields = new ArrayList<>(baseTemplate);
        fields.add(frequency);
        fields.add(sex); // Sex field
        fields.add(negation);
        fields.add(modifier); // modifier
        fields.add(description); // description
        fields.add(publication);
        fields.add(evidence);
        fields.add(biocuration);
        String line = String.join("\t", fields);
        return HpoProjectAnnotationLine.fromLine(line, validator, ontology);
    }


    private static AnnotationEntryI makeAnnotationEntry(String frequency, String negation, String evidence) {
        return makeEntry(frequency, EMPTY_STRING, negation, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, BIOCURATION);
    }

    private static AnnotationEntryI makeAnnotationEntryWithNegation(String frequency, String evidence) {
        return makeEntry(frequency, EMPTY_STRING, "NOT", EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, BIOCURATION);
    }

    private static AnnotationEntryI makeAnnotationEntryWithBiocuration(String frequency,
                                                                       String negation,
                                                                       String evidence,
                                                                       String biocuration) {
        return makeEntry(frequency, EMPTY_STRING, negation, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, biocuration);
    }

    private static AnnotationEntryI makeAnnotationEntryWithSex(String sex) {
        return makeEntry(EMPTY_STRING, sex, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, "IEA", BIOCURATION2);
    }

    private static AnnotationEntryI makeAnnotationEntryWithOnset(String onsetId, String onsetLabel) {
        final List<String> fields = List.of("OMIM:106100",
                "Angioedema, hereditary, 1", "HP:0040078", "Axonal degeneration",
                onsetId,
                onsetLabel,
                EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,EMPTY_STRING, PUBLICATION, "IEA", BIOCURATION2);
        String line = String.join("\t", fields);
        return HpoProjectAnnotationLine.fromLine(line, validator, ontology);
    }

    private final static AnnotationEntryI entryFreq1 = makeAnnotationEntry("4/5", EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryFreq2 = makeAnnotationEntry("2/5", EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryFreq3 = makeAnnotationEntry("1/10", EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryFreqPerc = makeAnnotationEntry("10%", EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryFreqEmpty = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryIEA1 = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "IEA");
    private final static AnnotationEntryI entryIEA2 = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "IEA");
    private final static AnnotationEntryI entryTAS = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "TAS");
    private final static AnnotationEntryI entryPCS = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "PCS");
    private final static AnnotationEntryI entryBiocurationI = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "PCS");
    private final static AnnotationEntryI entryBiocurationII = makeAnnotationEntryWithBiocuration(EMPTY_STRING, EMPTY_STRING, "PCS",
            BIOCURATION2);
    private final static AnnotationEntryI entryBiocurationIII = makeAnnotationEntryWithBiocuration(EMPTY_STRING, EMPTY_STRING, "PCS",
            BIOCURATION3);
    public final static AnnotationEntryI entryNegatedIEA1 = makeAnnotationEntryWithNegation("1/10",  "IEA");
    public final static AnnotationEntryI entryMALE = makeAnnotationEntryWithSex("MALE");
    public final static AnnotationEntryI entryFEMALE = makeAnnotationEntryWithSex("FEMALE");
    public final static AnnotationEntryI entryNoIndicatedSex = makeAnnotationEntryWithSex(EMPTY_STRING);
    public final static AnnotationEntryI entryInfantileOnset1 = makeAnnotationEntryWithOnset("HP:0003593", "Infantile onset");
    public final static AnnotationEntryI entryInfantileOnset2 = makeAnnotationEntryWithOnset("HP:0003593", "Infantile onset");
    public final static AnnotationEntryI entryChildhoodOnset = makeAnnotationEntryWithOnset("HP:0011463", "Childhood onset");

    private final static HpoAnnotationMerger merger = new HpoAnnotationMerger(ontology, validator);

    @Test
    public void testEvidenceIEAandPCS() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryPCS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("PCS", evidence);
    }

    @Test
    public void testEvidenceIEAandIEA() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryIEA2);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("IEA", evidence);
    }

    @Test
    public void testEvidenceIEAandTAS() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryTAS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("TAS", evidence);
    }

    @Test
    public void testEvidencePCSandTAS() {
        List<AnnotationEntryI> annots = List.of(entryPCS, entryTAS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("PCS", evidence);
    }

    @Test
    public void testMergeIdenticalBiocuration() {
        List<AnnotationEntryI> annots = List.of(entryPCS, entryBiocurationI);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        assertEquals("HPO:probinson[2022-03-31]", biocuration);
    }

    @Test
    public void testMerge2Biocurations() {
        List<AnnotationEntryI> annots = List.of(entryBiocurationI, entryBiocurationII);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        String expected = String.join(";", List.of(BIOCURATION,BIOCURATION2 ));
        assertEquals(expected, biocuration);
    }

    @Test
    public void testMerge3Biocurations() {
        List<AnnotationEntryI> annots = List.of(entryBiocurationI, entryBiocurationII, entryBiocurationIII);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        String expected = String.join(";", List.of(BIOCURATION,BIOCURATION2, BIOCURATION3 ));
        assertEquals(expected, biocuration);
    }

    @Test
    public void testMergeTwoFractionalFrequencies() {
        List<AnnotationEntryI> annots = List.of(entryFreq1, entryFreq2);
        // 4/5 + 2/5 = 6/10
        FrequencyModifier freq = merger.mergeFrequencies(annots);
        assertEquals("6/10", freq.frequencyString());
    }

    @Test
    public void testMergeThreeFractionalFrequencies() {
        List<AnnotationEntryI> annots = List.of(entryFreq1, entryFreq2, entryFreq3);
        // 4/5 + 2/5  + 1/10 = 7/20
        FrequencyModifier freq = merger.mergeFrequencies(annots);
        assertEquals("7/20", freq.frequencyString());
    }

    @Test
    public void testMergeIdenticalBiocurations() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryIEA2);
        AnnotationEntryI merged = merger.mergeEntries(annots);
        assertEquals(entryIEA1.getTsvLine(), merged.getTsvLine());
    }


    @Test
    public void testMergeEntriesWithDivergentNegation() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryNegatedIEA1);
        boolean divergent = merger.divergentNegation(annots);
        assertTrue(divergent);
    }

    @Test
    public void testMergeEntriesWithConfluentNegation() {
        List<AnnotationEntryI> annots = List.of(entryIEA1, entryIEA2);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }
    @Test
    public void testMergeEntriesWithDivergentSex1() {
        List<AnnotationEntryI> annots = List.of(entryMALE, entryFEMALE);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }

    @Test
    public void testMergeEntriesWithDivergentSex2() {
        List<AnnotationEntryI> annots = List.of(entryMALE, entryNoIndicatedSex);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }


    @Test
    public void testMergeEntriesWithDivergentOnset() {
        List<AnnotationEntryI> annots = List.of(entryInfantileOnset1, entryChildhoodOnset);
        boolean divergent = merger.divergentOnset(annots);
        assertTrue(divergent);
    }

    @Test
    public void testMergeEntriesWithConvergentOnset() {
        List<AnnotationEntryI> annots = List.of(entryInfantileOnset1, entryInfantileOnset2);
        boolean divergent = merger.divergentOnset(annots);
        assertFalse(divergent);
    }

}
