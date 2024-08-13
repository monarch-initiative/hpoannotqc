package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public class MalformedCitationError implements HpoaError {


    private final String message;
    public MalformedCitationError(String message) {
        this.message = message;
    }

    @Override
    public HpoaErrorCategory category() {
        return HpoaErrorCategory.CITATION_ERROR;
    }



    @Override
    public String getMessage() {
        return message;
    }
}
