package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriterTest;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class V2LineQualityControllerTest {


    private static HpoOntology ontology;
    private static V2LineQualityController qc;


    @BeforeAll
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
    public void testFreq1() throws PhenolException {
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
        V2SmallFileEntry entry = V2SmallFileEntry.fromLine(line,ontology);
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
