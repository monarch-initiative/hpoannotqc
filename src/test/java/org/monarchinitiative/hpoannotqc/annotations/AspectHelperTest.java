package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AspectHelperTest {


    private static AspectHelper aspectHelper;

    @BeforeAll
    private static void init() {
        ClassLoader cl = AspectHelperTest.class.getClassLoader();
        File file = new File(cl.getResource("hp.json").getFile());
        Ontology hpo = OntologyLoader.loadOntology(file);
        aspectHelper = new AspectHelper(hpo);
    }

    @Test
    public void testCTOR() {
        assertNotNull(aspectHelper);
    }
    @Test
    public void testPhenotypeAspect() {
        // Unsteady gait HP:0002317, is a phenotypic abnormality
        TermId tid = TermId.of("HP:0002317");
        assertEquals("P", aspectHelper.parse(tid).toString());
    }


    @Test
    public void testInheritanceTerm() {
        // X-linked dominant inheritance HP:0001423, and inheritance term
        TermId tid = TermId.of("HP:0001423");
        assertEquals("I", aspectHelper.parse(tid).toString());
    }

    @Test
    public void testClinicalCourseTerm() {
        //Infantile onset HP:0003593, an onset term
        TermId tid = TermId.of("HP:0003593");
        assertEquals("C", aspectHelper.parse(tid).toString());
    }


    @Test
    public void testModifier() {
        // Unilateral HP:0012833, a modifier term
        TermId tid = TermId.of("HP:0012833");
        assertEquals("M", aspectHelper.parse(tid).toString());
    }

    @Test
    public void testPMH() {
        // History of radiation therapy HP:6000181, a past medical history term
        TermId tid = TermId.of("HP:6000181");
        assertEquals("H", aspectHelper.parse(tid).toString());
    }

}
