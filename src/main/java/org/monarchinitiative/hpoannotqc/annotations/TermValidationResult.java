package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.phenol.ontology.data.Term;

import java.util.List;

public class TermValidationResult {



    private final Term term;

    private final HpoaError error;

    public TermValidationResult(Term term, HpoaError  err) {
        this.term = term;
        this.error = err;
    }


    public static TermValidationResult of(HpoaError error) {
        return new TermValidationResult(null, error);
    }

    public static TermValidationResult of(Term term) {
        return new TermValidationResult(term, null);
    }

    public boolean isValid() {
        return error == null;
    }

    public HpoaError getError() {
        return error;
    }

    public Term getTerm() {
        return term;
    }

}
