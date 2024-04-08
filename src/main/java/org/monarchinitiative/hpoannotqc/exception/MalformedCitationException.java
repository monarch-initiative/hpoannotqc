package org.monarchinitiative.hpoannotqc.exception;

public class MalformedCitationException extends HpoAnnotQcException {


    private MalformedCitationException() { }
    public MalformedCitationException(String msg) {
        super(msg);
    }

}
