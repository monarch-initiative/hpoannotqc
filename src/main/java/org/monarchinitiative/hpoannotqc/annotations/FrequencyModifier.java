package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;

import java.util.Optional;

public interface FrequencyModifier {

    String frequencyString();

    Optional<HpoaError> error();

}
