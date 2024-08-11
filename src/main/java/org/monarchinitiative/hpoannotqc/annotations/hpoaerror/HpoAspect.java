package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public enum HpoAspect {


    PHENOTYPIC_ABNORMALITY_ASPECT("P"),
    INHERITANCE_ASPECT("I"),
    CLINICAL_COURSE_ASPECT("C"),
    CLINICAL_MODIFIER_ASPECT("M"),
    PAST_MEDICAL_HISTORY_ASPECT("H");

    private final String value;

    HpoAspect(String aspect) {
        this.value = aspect;
    }

    public String letter() {
        return this.value;
    }


}
