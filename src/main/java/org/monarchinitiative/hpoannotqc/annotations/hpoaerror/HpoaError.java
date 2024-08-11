package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public interface HpoaError {

    HpoaErrorCategory category();

    String getMessage();

    default boolean skippable() {
        return false;
    }

    default String getCategoryAndError() {
        return String.format("%s: %s", category().name(), getMessage());
    }

}
