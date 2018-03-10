package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.REMOVED_FREQUENCY_WHITESPACE;

public class FrequencyTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void init() {
        Path resourceDirectory = Paths.get("src","test","resources","hp.obo");
        String hpOboPath=resourceDirectory.toAbsolutePath().toString();
        try {
            HpoOboParser oboparser = new HpoOboParser(new File(hpOboPath));
            HpoOntology ontology = oboparser.parse();
            OldSmallFileEntry.setOntology(ontology);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void writeTmpFile(List<String> annotations, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (String annot : annotations) {
            bw.write(annot + "\n");
        }
        bw.close();
    }
    private OldSmallFile makeOldSmallFile(String filename,
                                          SmallFileBuilder builder) throws IOException {
        File tempFile = testFolder.newFile(filename);
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        String oldSmallFileLine = builder.build();
        annots.add(oldSmallFileLine);
        writeTmpFile(annots, tempFile);
        return new OldSmallFile(tempFile.getAbsolutePath());
    }

    /* Test that the String "37 %" (i.e., with extra white space) gets converted to "37%". */
    @Test
    public void frequencyTest1() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:602535").
                diseaseName("#602535 MARSHALL-SMITH SYNDROME; MRSHSS").
                hpoId("HP:0010759"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Premaxillary Prominence").
                evidence("IEA").
                pub("OMIM:602535").
                frequency("37 %").
                description("OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA");
        OldSmallFile osm = makeOldSmallFile("tempfile42.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("37%",entry.getFrequencyString());
    }


    /* Test that the String "2 of 4" gets converted to "2/4". */
    @Test
    public void frequencyTest2() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:602535").
                diseaseName("#602535 MARSHALL-SMITH SYNDROME; MRSHSS").
                hpoId("HP:0010759"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Premaxillary Prominence").
                evidence("IEA").
                pub("OMIM:602535").
                frequency("2 of 4").
                description("OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA");
        OldSmallFile osm = makeOldSmallFile("tempfile43.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("2/4",entry.getFrequencyString());
    }

    /* Test that the String "81.0811 %" gets flagged as a quality issue. Also the extra whitespace should be removed */
    @Test
    public void frequencyTest3() throws IOException, HPOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:602535").
                diseaseName("#602535 MARSHALL-SMITH SYNDROME; MRSHSS").
                hpoId("HP:0010759"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Premaxillary Prominence").
                evidence("IEA").
                pub("OMIM:602535").
                frequency("81.0811 %").
                description("OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA");
        OldSmallFile osm = makeOldSmallFile("tempfile44.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("81.0811%",entry.getFrequencyString());
        Set<SmallFileQCCode> qcItemList = entry.doQCcheck();
        assertTrue(qcItemList.contains(REMOVED_FREQUENCY_WHITESPACE));
    }

}
