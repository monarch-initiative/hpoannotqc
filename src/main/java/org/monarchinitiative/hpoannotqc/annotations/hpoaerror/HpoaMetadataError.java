package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public class HpoaMetadataError implements HpoaError {
    private final String message;
    private final HpoaErrorCategory category;

    private HpoaMetadataError(String message, HpoaErrorCategory category) {
        this.message = message;
        this.category = category;
    }



    @Override
    public HpoaErrorCategory category() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static HpoaMetadataError sexStringError(String sexString) {
        String err = String.format("Malformed sex string: \"%s\"", sexString);
        return new HpoaMetadataError(err, HpoaErrorCategory.MALFORMED_SEX_STRING);
    }


    public static HpoaError malformedNegation(String negation) {
        String err = String.format("Malformed negation entry: \"%s\"", negation);
        return new HpoaMetadataError(err, HpoaErrorCategory.MALFORMED_NEGATION);
    }
}
