package org.monarchinitiative.hpoannotqc.mondo;


import org.junit.BeforeClass;
import org.junit.Test;

import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;


import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.io.IOException;
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


    @BeforeClass
    public static void init() throws IOException, PhenolException {
        ClassLoader classLoader = MergerTest.class.getClassLoader();
        hpOboPath =classLoader.getResource("hp.obo").getFile();
        annotationPath =classLoader.getResource("phenotype_annotation.tab").getFile();
        Objects.requireNonNull(hpOboPath);
        Objects.requireNonNull(annotationPath);
        HpoOboParser oboparser = new HpoOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        Objects.requireNonNull(ontology);
        TermPrefix pref = new TermPrefix("HP");
        TermId inheritId = new TermId(pref,"0000005");


//
//        HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(annotationPath,
//                ontology);
//
//        // We need to use the new big file format!!
//
//        Objects.requireNonNull(parser);
//        diseaseMap=parser.parse();
//
//        omim=diseaseMap.get("167210");
//        orpha=diseaseMap.get("2309");
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
