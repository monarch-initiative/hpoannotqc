package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;

import java.util.List;

public class TestBase {

    public sealed interface TestOutcome {
        record Ok(String value) implements TestOutcome {}
        record Error(List<HpoaError> errorList) implements TestOutcome {}
    }

}
