package org.monarchinitiative.hpoannotqc;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;

import java.util.List;

public interface Biocuration {

    String curation();
    List<HpoaError> errors();
}
