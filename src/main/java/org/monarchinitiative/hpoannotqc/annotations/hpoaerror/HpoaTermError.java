package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class HpoaTermError implements HpoaError {

    private final String disease;
    private final TermId hpoId;
    private final String message;

    public HpoaTermError(String disease, TermId tid, String msg){
        this.disease = disease;
        this.hpoId = tid;
        this.message = msg;
    }



    @Override
    public String getMessage() {
        return String.format("%s: %s", hpoId.getValue(), message);
    }



    @Override
    public String getDisease() {
        return this.disease;
    }
}
