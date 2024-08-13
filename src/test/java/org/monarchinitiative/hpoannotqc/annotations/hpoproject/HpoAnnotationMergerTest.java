package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntry;
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


    private static AnnotationEntry makeEntry(String frequency,
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
        return HpoAnnotationLine.fromLine(line, validator, ontology);
    }


    private static AnnotationEntry makeAnnotationEntry(String frequency, String negation, String evidence) {
        return makeEntry(frequency, EMPTY_STRING, negation, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, BIOCURATION);
    }

    private static AnnotationEntry makeAnnotationEntryWithNegation(String frequency, String evidence) {
        return makeEntry(frequency, EMPTY_STRING, "NOT", EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, BIOCURATION);
    }

    private static AnnotationEntry makeAnnotationEntryWithBiocuration(String frequency,
                                                                      String negation,
                                                                      String evidence,
                                                                      String biocuration) {
        return makeEntry(frequency, EMPTY_STRING, negation, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, evidence, biocuration);
    }

    private static AnnotationEntry makeAnnotationEntryWithSex(String sex) {
        return makeEntry(EMPTY_STRING, sex, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, "IEA", BIOCURATION2);
    }

    private static AnnotationEntry makeAnnotationEntryWithOnset(String onsetId, String onsetLabel) {
        final List<String> fields = List.of("OMIM:106100",
                "Angioedema, hereditary, 1", "HP:0040078", "Axonal degeneration",
                onsetId,
                onsetLabel,
                EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,EMPTY_STRING, PUBLICATION, "IEA", BIOCURATION2);
        String line = String.join("\t", fields);
        return HpoAnnotationLine.fromLine(line, validator, ontology);
    }

    private final static AnnotationEntry entryFreq1 = makeAnnotationEntry("4/5", EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryFreq2 = makeAnnotationEntry("2/5", EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryFreq3 = makeAnnotationEntry("1/10", EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryFreqPerc = makeAnnotationEntry("10%", EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryFreqEmpty = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryIEA1 = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "IEA");
    private final static AnnotationEntry entryIEA2 = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "IEA");
    private final static AnnotationEntry entryTAS = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "TAS");
    private final static AnnotationEntry entryPCS = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "PCS");
    private final static AnnotationEntry entryBiocurationI = makeAnnotationEntry(EMPTY_STRING, EMPTY_STRING, "PCS");
    private final static AnnotationEntry entryBiocurationII = makeAnnotationEntryWithBiocuration(EMPTY_STRING, EMPTY_STRING, "PCS",
            BIOCURATION2);
    private final static AnnotationEntry entryBiocurationIII = makeAnnotationEntryWithBiocuration(EMPTY_STRING, EMPTY_STRING, "PCS",
            BIOCURATION3);
    public final static AnnotationEntry entryNegatedIEA1 = makeAnnotationEntryWithNegation("1/10",  "IEA");
    public final static AnnotationEntry entryMALE = makeAnnotationEntryWithSex("MALE");
    public final static AnnotationEntry entryFEMALE = makeAnnotationEntryWithSex("FEMALE");
    public final static AnnotationEntry entryNoIndicatedSex = makeAnnotationEntryWithSex(EMPTY_STRING);
    public final static AnnotationEntry entryInfantileOnset1 = makeAnnotationEntryWithOnset("HP:0003593", "Infantile onset");
    public final static AnnotationEntry entryInfantileOnset2 = makeAnnotationEntryWithOnset("HP:0003593", "Infantile onset");
    public final static AnnotationEntry entryChildhoodOnset = makeAnnotationEntryWithOnset("HP:0011463", "Childhood onset");

    private final static HpoAnnotationMerger merger = new HpoAnnotationMerger(ontology, validator);

    @Test
    public void testEvidenceIEAandPCS() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryPCS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("PCS", evidence);
    }

    @Test
    public void testEvidenceIEAandIEA() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryIEA2);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("IEA", evidence);
    }

    @Test
    public void testEvidenceIEAandTAS() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryTAS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("TAS", evidence);
    }

    @Test
    public void testEvidencePCSandTAS() {
        List<AnnotationEntry> annots = List.of(entryPCS, entryTAS);
        String evidence = HpoAnnotationMerger.getHighestEvidenceCode(annots);
        assertEquals("PCS", evidence);
    }

    @Test
    public void testMergeIdenticalBiocuration() {
        List<AnnotationEntry> annots = List.of(entryPCS, entryBiocurationI);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        assertEquals("HPO:probinson[2022-03-31]", biocuration);
    }

    @Test
    public void testMerge2Biocurations() {
        List<AnnotationEntry> annots = List.of(entryBiocurationI, entryBiocurationII);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        String expected = String.join(";", List.of(BIOCURATION,BIOCURATION2 ));
        assertEquals(expected, biocuration);
    }

    @Test
    public void testMerge3Biocurations() {
        List<AnnotationEntry> annots = List.of(entryBiocurationI, entryBiocurationII, entryBiocurationIII);
        String biocuration = HpoAnnotationMerger.mergeBiocuration(annots);
        String expected = String.join(";", List.of(BIOCURATION,BIOCURATION2, BIOCURATION3 ));
        assertEquals(expected, biocuration);
    }

    @Test
    public void testMergeTwoFractionalFrequencies() {
        List<AnnotationEntry> annots = List.of(entryFreq1, entryFreq2);
        // 4/5 + 2/5 = 6/10
        FrequencyModifier freq = merger.mergeFrequencies(annots);
        assertEquals("6/10", freq.frequencyString());
    }

    @Test
    public void testMergeThreeFractionalFrequencies() {
        List<AnnotationEntry> annots = List.of(entryFreq1, entryFreq2, entryFreq3);
        // 4/5 + 2/5  + 1/10 = 7/20
        FrequencyModifier freq = merger.mergeFrequencies(annots);
        assertEquals("7/20", freq.frequencyString());
    }

    @Test
    public void testMergeIdenticalBiocurations() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryIEA2);
        AnnotationEntry merged = merger.mergeEntries(annots);
        assertEquals(entryIEA1.getTsvLine(), merged.getTsvLine());
    }


    @Test
    public void testMergeEntriesWithDivergentNegation() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryNegatedIEA1);
        boolean divergent = merger.divergentNegation(annots);
        assertTrue(divergent);
    }

    @Test
    public void testMergeEntriesWithConfluentNegation() {
        List<AnnotationEntry> annots = List.of(entryIEA1, entryIEA2);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }
    @Test
    public void testMergeEntriesWithDivergentSex1() {
        List<AnnotationEntry> annots = List.of(entryMALE, entryFEMALE);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }

    @Test
    public void testMergeEntriesWithDivergentSex2() {
        List<AnnotationEntry> annots = List.of(entryMALE, entryNoIndicatedSex);
        boolean divergent = merger.divergentNegation(annots);
        assertFalse(divergent);
    }


    @Test
    public void testMergeEntriesWithDivergentOnset() {
        List<AnnotationEntry> annots = List.of(entryInfantileOnset1, entryChildhoodOnset);
        boolean divergent = merger.divergentOnset(annots);
        assertTrue(divergent);
    }

    @Test
    public void testMergeEntriesWithConvergentOnset() {
        List<AnnotationEntry> annots = List.of(entryInfantileOnset1, entryInfantileOnset2);
        boolean divergent = merger.divergentOnset(annots);
        assertFalse(divergent);
    }

}
