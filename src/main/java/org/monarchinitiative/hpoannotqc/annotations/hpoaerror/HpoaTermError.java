package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.Term;

import static org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorCategory.TERM_ERROR;

public class HpoaTermError implements HpoaError {


    private final TermId hpoId;
    private final String message;
    private final HpoaErrorCategory category;

    public HpoaTermError(TermId tid, String msg){
        this.hpoId = tid;
        this.message = msg;
        category = TERM_ERROR;
    }

    public HpoaTermError(String msg, HpoaErrorCategory cat){
        this.hpoId = null;
        this.message = msg;
        category = cat;
    }


    public static HpoaError missingLabel(String msg){
        return new HpoaTermError(msg, HpoaErrorCategory.MISSING_LABEL);
    }


    public static HpoaError invalidFrequencyTerm(Term term) {
        String err = String.format("Term %s (%s) used as frequency but is not a valid frequency term",
                term.getName(), term.id().getValue());
        return new HpoaTermError(err, HpoaErrorCategory.INVALID_FREQUENCY_TERM);
    }

    public static HpoaError emptyDiseaseName(String termIdString) {
        String err = String.format("Disease with id %s had empty or null disease name",
                termIdString);
        return new HpoaTermError(err, HpoaErrorCategory.EMPTY_DISEASE_NAME);
    }



    @Override
    public String getMessage() {
        return message;
    }


    @Override
    public HpoaErrorCategory category() {
        return category;
    }


}
