package org.monarchinitiative.hpoannotqc.exception;

public class MalformedBiocurationEntryException extends HpoAnnotQcException {

    private final String biocurationId;
    private MalformedBiocurationEntryException() { biocurationId = "n/a";}
    public MalformedBiocurationEntryException(String msg) {
        super(msg);
        biocurationId = msg;
    }

    @Override
    public String getMessage() {
        return String.format("Malformed biocuration entry: \"%s\".", biocurationId);
    }

    public String getBiocurationId() {return biocurationId;}
}
