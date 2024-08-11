package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.hpoannotqc.annotations.AspectIdentifierTest;
import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.hpoannotqc.annotations.TestBase;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class HpoProjectFrequencyTest extends TestBase {



    @ParameterizedTest
    @ValueSource(strings = {"4/5",
            "32%",
            "HP:0040281"}) // valid frequency strings, note HP:0040281=Very frequent
    void shouldReturnValidParse(String frequencyString) {
        FrequencyModifier freqMod = HpoProjectFrequency.fromHpoaLine(frequencyString, ontology);
        assertFalse(freqMod.error().isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"4/",
            "4/3",
            "332%",
            "HP:0001166"}) // invalid frequency strings, note HP:0001166=Arachnodactyly
    void shouldReturnFail(String frequencyString) {
        FrequencyModifier freqMod = HpoProjectFrequency.fromHpoaLine(frequencyString, ontology);
        assertTrue(freqMod.error().isPresent());
    }





}
