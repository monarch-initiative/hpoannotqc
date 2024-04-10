package org.monarchinitiative.hpoannotqc.annotations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BiocurationCheckerTest {

    @Test
    public void checkNameCuration() {
        String valid = "HPO:probinson[2023-04-01]";
        assertTrue(BiocurationChecker.check(valid));
        String noColon = "HPOprobinson[2023-04-01]";
        assertFalse(BiocurationChecker.check(noColon));
        String noDate = "HPOprobinson";
        assertFalse(BiocurationChecker.check(noDate));
    }



    @Test
    public void checkName2() {
        String valid = "HPO:skoehler[2010-06-20]";
        assertTrue(BiocurationChecker.check(valid));
    }


    @Test
    public void testIEI(){
        String valid = "HPO:iea[2009-02-17]";
        assertTrue(BiocurationChecker.check(valid));
    }

    @Test
    public void checkOrcidCuration() {
        String valid = "ORCID:0000-0002-5648-2155[2024-04-01]";
        assertTrue(BiocurationChecker.check(valid));
        String noColon = "ORCID0000-0002-5648-2155[2024-04-01]";
        assertFalse(BiocurationChecker.check(noColon));
        String noDate = "ORCID:0000-0002-5648-2155";
        assertFalse(BiocurationChecker.check(noDate));
    }


    @Test
    public void checkOnlyDate() {
        String noItem = "[2024-04-01]";
        assertFalse(BiocurationChecker.check(noItem));
    }

}
