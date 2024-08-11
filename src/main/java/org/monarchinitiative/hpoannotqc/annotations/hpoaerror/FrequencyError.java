package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class FrequencyError implements HpoaError  {


    private final String message;
    private final HpoaErrorCategory category;

    public FrequencyError(String msg, HpoaErrorCategory cat) {
        message = msg;
        category = cat;
    }

    @Override
    public HpoaErrorCategory category() {
        return null;
    }

    @Override
    public String getMessage() {
        return "";
    }

    public static FrequencyError invalidFraction(String freqField) {
        String err = String.format("Invalid fraction \"%s\".", freqField);
        return new FrequencyError(err, HpoaErrorCategory.INVALID_FRACTION);
    }

    public static FrequencyError invalidPercentage(String freqField) {
        String err = String.format("Invalid percentage \"%s\".", freqField);
        return new FrequencyError(err, HpoaErrorCategory.INVALID_PERCENTAGE);
    }

    public static FrequencyError malformed(String freqField) {
        String err = String.format("Malformed frequency entry \"%s\".", freqField);
        return new FrequencyError(err, HpoaErrorCategory.MALFORMED_FREQUENCY_STRING);
    }

    public static FrequencyError invalidSubontology(String freqField) {
        String err = String.format("Use of HPO term outside of Frequency subontology for frequency \"%s\".", freqField);
        return new FrequencyError(err, HpoaErrorCategory.INVALID_SUBONTOLOGY);
    }


}
