package org.monarchinitiative.hpoannotqc.mondo;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseWithMetadata;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.io.obo.hpo.HpoAnnotation2DiseaseParser;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

public class MergerTest {

    private static String hpOboPath;
    private static String annotationPath;
    private static HpoOntology ontology;
    private static Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology;
    private static Ontology<HpoTerm, HpoTermRelation> phenotypeSubontology;
    private static Ontology<HpoTerm, HpoTermRelation> onsetSubontology;
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
        inheritanceSubontology = ontology.subOntology(inheritId);
        Objects.requireNonNull(inheritanceSubontology);

        HpoAnnotation2DiseaseParser parser = new HpoAnnotation2DiseaseParser(annotationPath,
                ontology.getPhenotypicAbnormalitySubOntology(),inheritanceSubontology);

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
