package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.hpoannotqc.annotations.util.TermValidator;
import org.monarchinitiative.hpoannotqc.annotations.TermValidationResult;
import org.monarchinitiative.hpoannotqc.annotations.TestBase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnsetTermValidationTest extends TestBase {


    private final static TermValidator validator = new TermValidator(ontology);

    @ParameterizedTest
    @ValueSource(strings = {"Adult onset;HP:0003581",
            "Late onset;HP:0003584",
            "Congenital onset;HP:0003577",
                "Onset;HP:0003674"}) // valid onset terms
    void shouldReturnValidParse(String onsetString) {
        String [] fields = onsetString.split(";");
        if (fields.length != 2) {
            throw new AssertionError("OnsetTermValidation should return 2 fields");
        }
        String termId = fields[1];
        String termLabel = fields[0];
        TermValidationResult tvalid = validator.checkOnsetTerm(termId, termLabel);
        assertTrue(tvalid.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Adult onset;HP:00081",
            "Late ons;HP:0003584",
            "Arachnoid cyst;HP:0100702",
            "Hepatomegaly;HP:0002240"}) // invalid onset terms (two malformed and two from wrong subhierarchy)
    void shouldReturnInValidParse(String onsetString) {
        String [] fields = onsetString.split(";");
        if (fields.length != 2) {
            throw new AssertionError("OnsetTermValidation should return 2 fields");
        }
        String termId = fields[1];
        String termLabel = fields[0];
        TermValidationResult tvalid = validator.checkOnsetTerm(termId, termLabel);
        assertFalse(tvalid.isValid());
    }
}
