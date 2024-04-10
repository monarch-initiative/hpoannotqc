package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public class MalformedBiocurationEntryError implements HpoaError {

    private final String disease;

    private final String message;

    public MalformedBiocurationEntryError(String disease, String msg) {
        this.disease = disease;
        this.message = msg;
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
