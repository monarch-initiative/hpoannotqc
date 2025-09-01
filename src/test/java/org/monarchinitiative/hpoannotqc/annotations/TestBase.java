package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoProjectFrequencyTest;
import org.monarchinitiative.hpoannotqc.annotations.util.TermValidator;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

/**
 * Supply a reference to the HPO ontology to subclasses.
 */
public class TestBase {
    private static final ClassLoader cl = HpoProjectFrequencyTest.class.getClassLoader();
    private static final File file = new File(cl.getResource("hp.json").getFile());
    protected static final Ontology ontology = OntologyLoader.loadOntology(file);

    protected static final TermValidator validator = new TermValidator(ontology);

    protected final static String EMPTY_STRING = "";

}
