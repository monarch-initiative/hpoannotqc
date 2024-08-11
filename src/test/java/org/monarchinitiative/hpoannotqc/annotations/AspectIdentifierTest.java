package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AspectIdentifierTest {


    private static AspectIdentifier aspectIdentifier;

    @BeforeAll
    public static void init() {
        ClassLoader cl = AspectIdentifierTest.class.getClassLoader();
        File file = new File(cl.getResource("hp.json").getFile());
        Ontology hpo = OntologyLoader.loadOntology(file);
        aspectIdentifier = new AspectIdentifier(hpo);
    }

    @Test
    public void testCTOR() {
        assertNotNull(aspectIdentifier);
    }
    @Test
    public void testPhenotypeAspect() {
        // Unsteady gait HP:0002317, is a phenotypic abnormality
        TermId tid = TermId.of("HP:0002317");
        String aspect = aspectIdentifier.getAspectLetter(tid);
        assertEquals("P", aspect);
    }


    @Test
    public void testInheritanceTerm() {
        // X-linked dominant inheritance HP:0001423, and inheritance term
        TermId tid = TermId.of("HP:0001423");
        String aspect = aspectIdentifier.getAspectLetter(tid);
        assertEquals("I", aspect);
    }

    @Test
    public void testClinicalCourseTerm() {
        //Infantile onset HP:0003593, an onset term
        TermId tid = TermId.of("HP:0003593");
        String aspect = aspectIdentifier.getAspectLetter(tid);
        assertEquals("C", aspect);
    }


    @Test
    public void testModifier() {
        // Unilateral HP:0012833, a modifier term
        TermId tid = TermId.of("HP:0012833");
        String aspect = aspectIdentifier.getAspectLetter(tid);
        assertEquals("M", aspect);
    }

    @Test
    public void testPMH() {
        // History of radiation therapy HP:6000181, a past medical history term
        TermId tid = TermId.of("HP:6000181");
        String aspect = aspectIdentifier.getAspectLetter(tid);
        assertEquals("H", aspect);
    }

}
