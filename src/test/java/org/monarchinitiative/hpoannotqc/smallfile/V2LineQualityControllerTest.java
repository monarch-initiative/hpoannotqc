package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriterTest;
import org.monarchinitiative.hpoannotqc.io.V2SmallFileParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.*;
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
    public static void init() throws PhenolException, FileNotFoundException {
        // set up ontology
        ClassLoader classLoader = BigFileWriterTest.class.getClassLoader();
        String hpOboPath = Objects.requireNonNull(classLoader.getResource("hp_head.obo")).getFile();
        Objects.requireNonNull(hpOboPath);
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        qc = new V2LineQualityController(ontology);
    }



    @Test
    public void testFreq1() throws IOException {
        String[] fields={
        "OMIM:123456",
                "MADE-UP SYNDROME",
                "HP:0000528",
                "Anophthalmia",
                "",
                "",
                "76.3%",
                "FEMALE",
                "",
                "",
                "",
                "PMID:9843983",
                "PCS",
                "HPO:probinson[2013-01-09]"};
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
