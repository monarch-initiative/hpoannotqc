package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public class MalformedCitationError implements HpoaError {

    private final String disease;

    private final String message;
    public MalformedCitationError(String disease, String message) {
        this.disease = disease;
        this.message = message;
    }

    @Override
    public String getDisease() {
        return disease;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
