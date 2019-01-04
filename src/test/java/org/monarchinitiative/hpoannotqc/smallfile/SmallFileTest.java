package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.io.SmallFileParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.*;

class SmallFileTest {
    private static HpoOntology ontology;
    private static SmallFile v2sf=null;


    @BeforeAll
    static void init() throws PhenolException, FileNotFoundException {
        Path hpOboPath = Paths.get("src","test","resources","hp_head.obo");
        String hpOboFile=hpOboPath.toAbsolutePath().toString();
        HpOboParser oboparser = new HpOboParser(new File(hpOboFile));
        ontology = oboparser.parse();
        Path omim123456path = Paths.get("src","test","resources","smallfiles","OMIM-123456.tab");
        String omim123456file = omim123456path.toAbsolutePath().toString();
        SmallFileParser parser = new SmallFileParser(omim123456file,ontology);
        Optional<SmallFile> v2opt = parser.parse();
        v2opt.ifPresent(v2SmallFile -> v2sf = v2SmallFile);
    }

    @Test
    void testParse() {
        assertNotNull(v2sf);
    }

    @Test
    void basenameTest() {
        assertEquals("OMIM-123456.tab", v2sf.getBasename());
    }

    @Test
    void isOmimTest() {
        assertTrue(v2sf.isOMIM());
        assertFalse(v2sf.isDECIPHER());
    }

    /** Our test file has three annotation lines. */
    @Test
    void numberOfAnnotationsTest() {
        assertEquals(3,v2sf.getNumberOfAnnotations());
    }



}
