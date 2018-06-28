package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriterTest;
import org.monarchinitiative.hpoannotqc.io.V2SmallFileParser;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class V2LineQualityControllerTest {


    private static HpoOntology ontology;
    private static V2LineQualityController qc;
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @BeforeClass
    public static void init() throws IOException {
        // set up ontology
        ClassLoader classLoader = BigFileWriterTest.class.getClassLoader();
        String hpOboPath = classLoader.getResource("hp.obo").getFile();
        Objects.requireNonNull(hpOboPath);
        HpoOboParser oboparser = new HpoOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        qc = new V2LineQualityController(ontology);
    }



    @Test
    public void testFreq1() throws IOException {
        String[] fields={
        "OMIM:609939",
                "%609939 SYSTEMIC LUPUS ERYTHEMATOSUS, SUSCEPTIBILITY TO, 6; SLEB6",
                "HP:0002725",
                "Systemic lupus erythematosus",
                "",
                "",
                "76.3%",
                "FEMALE",
                "",
                "",
                "",
                "PMID:9843983",
                "PCS",
                "HPO:probinson",
                "2013-01-09"};
        String line = Arrays.stream(fields).collect(Collectors.joining("\t"));
        File createdFile= folder.newFile("myfile.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(createdFile));
        bw.write(V2SmallFileEntry.getHeaderV2() + "\n");
        bw.write(line);
        bw.close();
        V2SmallFileParser parser = new V2SmallFileParser(createdFile.getAbsolutePath(),ontology);
        Optional<V2SmallFile> opt = parser.parse();
        assertTrue(opt.isPresent());
        V2SmallFile smallFile = opt.get();
        List<V2SmallFileEntry> entries = smallFile.getOriginalEntryList();
        assertEquals(1,entries.size());
        V2SmallFileEntry entry = entries.get(0);
        boolean result = qc.checkV2entry(entry);
        if (!result) {
            List<String> errors = qc.getErrors();
            for (String e: errors) {
                System.err.println(e);
            }
        }
        assertTrue(result);



    }
}
