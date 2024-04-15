package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public interface HpoaError {

    String getDisease();

    String getMessage();

    default String  getMessageWithDisease() {
        return String.format("%s - %s", getDisease(), getMessage());
    }

    default boolean skippable() {
        return false;
    }

}
