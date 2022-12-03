package org.monarchinitiative.hpoannotqc.analysis;

import org.junit.jupiter.api.Test;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SmallFileCleanerTest {

    private static final Path path = Paths.get("src", "test", "resources","OMIM-with-duplicates.tab");
    private static final File smallFile = path.toFile();
    private static final SmallFileCleaner cleaner = new SmallFileCleaner(smallFile.getAbsolutePath());
    private static final String label = cleaner.getPreferredLabel();
    private static final List<String> cleanLines = cleaner.getNewLines();



    private static final String [] fieldsForTesting = {"OMIM:120330", "Papillorenal syndrome",
            "HP:0000608", "Macular degeneration","","",	"","","","HP:0040283",
            "OMIM-CS:HEAD AND NECK_EYES > MACULAR DEGENERATION (RARE)",	"OMIM:120330",	"TAS", "HPO:skoehler[2012-11-16]"
    };
    private static final String originalLine = String.join("\t", fieldsForTesting);

    private static String lineWithEvidenceCode(String evidence) {
        int newLength = fieldsForTesting.length;
        String[] copiedArray = Arrays.copyOf(fieldsForTesting, newLength);
        copiedArray[12] = evidence;
        return String.join("\t", copiedArray);
    }

    private static String lineWithChangeAtIndex(String change, int i) {
        int newLength = fieldsForTesting.length;
        String[] copiedArray = Arrays.copyOf(fieldsForTesting, newLength);
        copiedArray[i] = change;
        return String.join("\t", copiedArray);
    }


    @Test
    public void testNumberOfLines() {
        // the OMIM-with-duplicates file has 8 data lines but two duplications, so we expect 6 data lines (7 lines, includes header)
        assertNotNull(cleaner);
        assertEquals(7, cleanLines.size());
    }


    @Test
    public void testDifferentEvidenceCode() {
       List<String> lines = new ArrayList<>();
       lines.add(originalLine);
        String pcsLine = lineWithEvidenceCode("PCS");
       lines.add(pcsLine);
        // the temp file now has two lines in it -- they are not duplicate because the evidence code is different
        SmallFileCleaner myCleaner = new SmallFileCleaner(lines, "dummyHeader");
        List<String> cleanLines = myCleaner.getNewLines(); // includes header, so we expect 3 lines
        assertEquals(3, cleanLines.size());
    }

    @Test
    public void testDifferentFrequency() {
        List<String> lines = new ArrayList<>();
        lines.add(originalLine);
        String lineWithDifferentFrequency = lineWithChangeAtIndex("1/2", 6);
        lines.add(lineWithDifferentFrequency);
        // the temp file now has two lines in it -- they are not duplicate because the evidence code is different
        SmallFileCleaner myCleaner = new SmallFileCleaner(lines, "dummyHeader");
        List<String> cleanLines = myCleaner.getNewLines();// includes header, so we expect 3 lines
        assertEquals(3, cleanLines.size());
    }

    @Test
    public void testDifferentDescription() {
        List<String> lines = new ArrayList<>();
        lines.add(originalLine);
        String lineWithDifferentDescription = lineWithChangeAtIndex("new description", 10);
        lines.add(lineWithDifferentDescription);
        // the temp file now has two lines in it -- they are  duplicate because we do not count description
        SmallFileCleaner myCleaner = new SmallFileCleaner(lines, "dummyHeader");
        List<String> cleanLines = myCleaner.getNewLines();// includes header, so we expect 2 lines
        assertEquals(2, cleanLines.size());
    }


    @Test
    public void test209770() {
        Path path = Paths.get("src", "test", "resources","OMIM-209770.tab");
        File smallFile = path.toFile();
        SmallFileCleaner cleaner = new SmallFileCleaner(smallFile.getAbsolutePath());
        List<String> cleanLines = cleaner.getNewLines();
        String expected = "Aural atresia, multiple congenital anomalies, and mental retardation";
        assertEquals(expected, cleaner.getPreferredLabel());
        assertEquals(7, cleanLines.size()); // no duplicates
    }







}
