package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class HpoTermException extends HpoAnnotQcException {


    private String message;
    private TermId problematicId;

    private HpoTermException() { }


    public HpoTermException(String msg, TermId tid) {
        super(msg);
        this.message = msg;
        this.problematicId = tid;
    }


    public TermId getProblematicId() {
        return problematicId;
    }
    @Override
    public String getMessage() {
        return String.format("%s: %s", problematicId.getValue(), message);
    }
}
