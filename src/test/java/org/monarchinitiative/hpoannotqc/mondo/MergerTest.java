package org.monarchinitiative.hpoannotqc.mondo;


import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.hpoannotqc.bigfile.HpoAnnotation2DiseaseParser;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseWithMetadata;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;

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
    private static Map<String,HpoDiseaseWithMetadata> diseaseMap;

    private static HpoDiseaseWithMetadata omim;
    private static HpoDiseaseWithMetadata orpha;


    @BeforeClass
    public static void init() throws IOException {
        ClassLoader classLoader = MergerTest.class.getClassLoader();
        hpOboPath =classLoader.getResource("hp.obo").getFile();
        annotationPath =classLoader.getResource("phenotype_annotation.tab").getFile();
        Objects.requireNonNull(hpOboPath);
        Objects.requireNonNull(annotationPath);
        HpoOboParser oboparser = new HpoOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        Objects.requireNonNull(ontology);
        TermPrefix pref = new ImmutableTermPrefix("HP");
        TermId inheritId = new ImmutableTermId(pref,"0000005");



        HpoAnnotation2DiseaseParser parser = new HpoAnnotation2DiseaseParser(annotationPath,
                ontology);

        Objects.requireNonNull(parser);
        diseaseMap=parser.getDiseaseMap();

        omim=diseaseMap.get("167210");
        orpha=diseaseMap.get("2309");
    }

    /** We put the ORPHA and OMIM versions of the same disease into the annotation file. */
    @Test
    public void testParsedTwoDiseases() {
        assertTrue(true);
        assertEquals(10294,diseaseMap.size());
        assertNotNull(omim);
        assertNotNull(orpha);
        debugPrintDisease(omim);
        debugPrintDisease(orpha);
        Merger m = new Merger(omim,orpha,ontology);
        m.merge();
    }


    private void debugPrintDisease(HpoDiseaseWithMetadata disease) {
        System.out.println(disease);
    }



}
