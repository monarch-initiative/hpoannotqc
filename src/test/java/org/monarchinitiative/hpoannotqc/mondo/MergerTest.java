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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class MergerTest {

    private static String hpOboPath;
    private static String pachyonychiaAnnotationPath;
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
        pachyonychiaAnnotationPath=classLoader.getResource("pheno_annotation/pachyonychia_pc2.tab").getFile();
        HpoOboParser oboparser = new HpoOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
        TermPrefix pref = new ImmutableTermPrefix("HP");
        TermId inheritId = new ImmutableTermId(pref,"0000005");
        inheritanceSubontology = ontology.subOntology(inheritId);
//        TermId frequencyId = new ImmutableTermId(pref,"0040279");
//        frequencySubontology = ontology.subOntology(frequencyId);

        HpoAnnotation2DiseaseParser parser = new HpoAnnotation2DiseaseParser(pachyonychiaAnnotationPath,
                ontology.getPhenotypicAbnormalitySubOntology(),inheritanceSubontology);
        diseaseMap=parser.getDiseaseMap();
        omim=diseaseMap.get("167210");
        orpha=diseaseMap.get("2309");
    }

    /** We put the ORPHA and OMIM versions of the same disease into the annotation file. */
    @Test
    public void testParsedTwoDiseases() {
        assertEquals(2,diseaseMap.size());
        assertNotNull(omim);
        assertNotNull(orpha);
        debugPrintDisease(omim);
        debugPrintDisease(orpha);
        Merger m = new Merger(omim,orpha);
        m.merge();
    }


    private void debugPrintDisease(HpoDiseaseWithMetadata disease) {
        System.out.println(disease);
    }



}
