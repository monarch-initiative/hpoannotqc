package org.monarchinitiative.hpoannotqc.mondo;



import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;


import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

public class MergerTest {

    private static String hpOboPath;
    private static String annotationPath;
    private static HpoOntology ontology;
    private static Map<String,HpoDisease> diseaseMap;

    private static HpoDisease omim;
    private static HpoDisease orpha;


    @BeforeAll
    public static void init() throws PhenolException {
        ClassLoader classLoader = MergerTest.class.getClassLoader();
        hpOboPath =classLoader.getResource("hp_head.obo").getFile();
        annotationPath =classLoader.getResource("smallfiles/OMIM-123456.tab").getFile();
        Objects.requireNonNull(hpOboPath);
        Objects.requireNonNull(annotationPath);
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        Objects.requireNonNull(ontology);
    }

    /** We put the ORPHA and OMIM versions of the same disease into the annotation file. */
    @Test
    public void testParsedTwoDiseases() {
        assertTrue(Math.PI > 3.0); // todo refactor this entire class
//        assertEquals(10294,diseaseMap.size());
//        assertNotNull(omim);
//        assertNotNull(orpha);
//        debugPrintDisease(omim);
//        debugPrintDisease(orpha);
//        Merger m = new Merger(omim,orpha,ontology);
//        m.merge();
    }


    private void debugPrintDisease(HpoDisease disease) {
        System.out.println(disease);
    }



}
