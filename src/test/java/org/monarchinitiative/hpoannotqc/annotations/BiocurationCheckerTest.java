package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.exception.MalformedBiocurationEntryException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BiocurationCheckerTest {

    @Test
    public void checkNameCuration() {
        String valid = "HPO:probinson[2023-04-01]";
        assertDoesNotThrow(() -> BiocurationChecker.check(valid));
        String noColon = "HPOprobinson[2023-04-01]";
        MalformedBiocurationEntryException thrown = assertThrows(
                MalformedBiocurationEntryException.class,
                () -> BiocurationChecker.check(noColon),
                "Expected BiocurationChecker.check() to throw, but it didn't"
        );
        String noDate = "HPOprobinson";
        thrown = assertThrows(
                MalformedBiocurationEntryException.class,
                () -> BiocurationChecker.check(noDate),
                "Expected BiocurationChecker.check() to throw, but it didn't"
        );
    }



    @Test
    public void checkName2() {
        String valid = "HPO:skoehler[2010-06-20]";
        assertDoesNotThrow(() -> BiocurationChecker.check(valid));
    }


    @Test
    public void testIEI(){
        String valid = "HPO:iea[2009-02-17]";
        assertDoesNotThrow(() -> BiocurationChecker.check(valid));
    }

    @Test
    public void checkOrcidCuration() {
        String valid = "ORCID:0000-0002-5648-2155[2024-04-01]";
        assertDoesNotThrow(() -> BiocurationChecker.check(valid));
        String noColon = "ORCID0000-0002-5648-2155[2024-04-01]";
        MalformedBiocurationEntryException thrown = assertThrows(
                MalformedBiocurationEntryException.class,
                () -> BiocurationChecker.check(noColon),
                "Expected BiocurationChecker.check() to throw, but it didn't"
        );
        String noDate = "ORCID:0000-0002-5648-2155";
        thrown = assertThrows(
                MalformedBiocurationEntryException.class,
                () -> BiocurationChecker.check(noDate),
                "Expected BiocurationChecker.check() to throw, but it didn't"
        );
    }


    @Test
    public void checkOnlyDate() {
        String noDate = "[2024-04-01]";
        MalformedBiocurationEntryException thrown = assertThrows(
                MalformedBiocurationEntryException.class,
                () -> BiocurationChecker.check(noDate),
                "Expected BiocurationChecker.check() to throw, but it didn't"
        );
    }

}
