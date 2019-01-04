package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriterTest;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.*;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.BAD_DATABASE_NAME;
import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.MISSING_DISEASE_NAME;
import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.PHENOTYPE_ID_NOT_FOUND;


class SmallFileEntryQCTest {


    private static HpoOntology ontology;
    private static SmallFileEntryQC qc;


    @BeforeAll
    static void init() throws PhenolException, FileNotFoundException {
        // set up ontology
        ClassLoader classLoader = BigFileWriterTest.class.getClassLoader();
        String hpOboPath = Objects.requireNonNull(classLoader.getResource("hp_head.obo")).getFile();
        Objects.requireNonNull(hpOboPath);
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        qc = new SmallFileEntryQC(ontology);
    }



    @Test
    void testFreq1() throws PhenolException {
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
        String line = String.join("\t",fields);
        SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        boolean result = qc.checkSmallFileEntry(entry);
        if (!result) {
            List<String> errors = qc.getErrors();
            for (String e: errors) {
                System.err.println(e);
            }
        }
        assertTrue(result);
    }

    /** The first entry should be OMIM:123456 and not just 123456.*/
    @Test
    void testRecognizeBadDatabaseId() throws PhenolException {
        String[] fields={
                "123456",
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
        String line = String.join("\t",fields);
        SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        qc.checkSmallFileEntry(entry);
        assertTrue(qc.hasErrors());
        List<SmallFileQCCode> errorcodes = qc.getErrorCodes();
        assertEquals(1,errorcodes.size());
        assertEquals(BAD_DATABASE_NAME,errorcodes.get(0));
    }

    @Test
    void testMissingDiseaseName() throws PhenolException {
        String[] fields={
                "OMIM:123456",
                "",
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
        String line = String.join("\t",fields);
        SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        boolean result = qc.checkSmallFileEntry(entry);
        qc.checkSmallFileEntry(entry);
        assertTrue(qc.hasErrors());
        List<SmallFileQCCode> errorcodes = qc.getErrorCodes();
        assertEquals(1,errorcodes.size());
        assertEquals(MISSING_DISEASE_NAME,errorcodes.get(0));
    }


    /** Test that an exception is thrown because HP:1230528 does not exist.*/
    @Test
    void testRecognizeBadHPOId() throws PhenolException {
        String[] fields={
                "123456",
                "MADE-UP SYNDROME",
                "HP:1230528",
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
        String line = String.join("\t",fields);
        Assertions.assertThrows(PhenolException.class, () -> {
            SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        });
    }

    /** for some reason, 96.7% was not being recongized. */
    @Test
    void testFrequency() throws PhenolException {
        String[] fields={
                "OMIM:123456",
                "MADE-UP SYNDROME",
                "HP:0000528",
                "Anophthalmia",
                "",
                "",
                "96.7%",
                "FEMALE",
                "",
                "",
                "",
                "PMID:9843983",
                "PCS",
                "HPO:probinson[2013-01-09]"};
        String line = String.join("\t",fields);
        SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        boolean result = qc.checkSmallFileEntry(entry);
        qc.checkSmallFileEntry(entry);
        assertFalse(qc.hasErrors());
    }


}