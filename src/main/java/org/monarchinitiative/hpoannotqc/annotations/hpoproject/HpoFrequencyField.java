package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.FrequencyError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpoFrequencyField implements FrequencyModifier {
    private final static Logger LOGGER = LoggerFactory.getLogger(HpoFrequencyField.class);
    // To match e.g. 10/20
    private static final Pattern RATIO_PATTERN = Pattern.compile("(?<numerator>\\d+)/(?<denominator>\\d+)");
    // To match an int of optionally a float percentage (e.g. 1% or 1.23456789%).
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(?<value>\\d+\\.?(\\d+)?)%");
    private static final String EMPTY_STRING = "";


    private String freq;
    private HpoaError error;

    private HpoFrequencyField() {
        freq = EMPTY_STRING;
        error = null;
    }


    private HpoFrequencyField(String frequencyField, Ontology ontology) {
        if (frequencyField == null || frequencyField.isEmpty()) {
            freq = EMPTY_STRING; // OK, frequency is not required
            return;
        }
        Matcher matcher = RATIO_PATTERN.matcher(frequencyField);
        if (matcher.matches()) {
            int numerator = Integer.parseInt(matcher.group("numerator"));
            int denominator = Integer.parseInt(matcher.group("denominator"));
            if (numerator > denominator || denominator == 0) {
                error = FrequencyError.invalidFraction(frequencyField);
                freq = EMPTY_STRING;
                return;
            } else {
                freq = frequencyField;
                return;
            }
        }
        matcher = PERCENTAGE_PATTERN.matcher(frequencyField);
        if (matcher.matches()) {
            float percent = Float.parseFloat(matcher.group("value"));
            if (percent > 100f || percent <= 0f) {
                error = FrequencyError.invalidPercentage(frequencyField);
                freq = EMPTY_STRING;
                return;
            } else {
                freq = frequencyField;
                return;
            }
        }
        // If we get here, the only remaining valid term is an HPO frequency term.
        // We first check if the entry looks like an HPO term at all
        if (!frequencyField.matches("HP:\\d{7}")) {
            // cannot be a valid frequency term
            error = FrequencyError.malformed(frequencyField);
            freq = EMPTY_STRING;
            return;
        }
        String [] fields = frequencyField.split(":");
        if (fields.length != 2) {
            error = FrequencyError.malformed(frequencyField);
            freq = EMPTY_STRING;
            return;
        }
        // if we get here and we can validate that the frequency term comes from the right subontology,
        // then the item is valid
        TermId tid = TermId.of(frequencyField);
        final TermId FREQUENCY_ROOT = TermId.of("HP:0040279");
        boolean valid = ontology.graph().existsPath(tid, FREQUENCY_ROOT);
        if (!valid) {
            error = FrequencyError.invalidSubontology(frequencyField);
            freq = EMPTY_STRING;
        } else {
            freq = frequencyField;
            error = null;
        }
    }


    public static FrequencyModifier fromHpoaLine(String freq, Ontology ontology) {
        return new HpoFrequencyField(freq, ontology);
    }


    @Override
    public String frequencyString() {
        return freq;
    }

    @Override
    public Optional<HpoaError> error() {
        return Optional.ofNullable(error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(freq);
    }

    public static FrequencyModifier empty() {
        return new HpoFrequencyField();
    }
}
