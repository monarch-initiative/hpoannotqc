package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.hpoannotqc.annotations.Biocuration;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorCategory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HpoProjectBiocurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"HPO:probinson[2021-06-21]",
    "HPO:lccarmody[2018-10-03]",
    "HPO:lccarmody[2018-10-03];HPO:lccarmody[2018-10-03];HPO:probinson[2024-03-14]",
    "HPO:skoehler[2015-04-05]",
    "HPO:probinson[2023-03-09];ORCID:0000-0002-0736-9199[2024-04-01]"}) // valid strings
    void shouldReturnValidParse(String curation) {
        HpoProjectBiocuration biocuration = new HpoProjectBiocuration(curation);
        assertEquals(curation, biocuration.curation());
        assertTrue(biocuration.errors().isEmpty());
    }


@Test
public void testValidOrcid() {
    String curationString = "ORCID:0000-0002-0736-9199[2024-04-01]";
    Biocuration bc = new HpoProjectBiocuration(curationString);
    List<HpoaError> errors = bc.errors();
    assertTrue(errors.isEmpty());
}

    @Test
    public void lacksClosingBracket() {
        String curationString = "ORCID:0000-0002-0736-9199[2024-04-01";
        Biocuration bc = new HpoProjectBiocuration(curationString);
        List<HpoaError> errors = bc.errors();
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        HpoaError error = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY, error.category());
        String errString = String.format("Malformed biocuration entry: \"%s\"", curationString);
        assertEquals(errString, error.getMessage());
    }

    @Test
    public void noColon() {
        String curationString = "HPOprobinson[2024-04-01]";
        Biocuration bc = new HpoProjectBiocuration(curationString);
        List<HpoaError> errors = bc.errors();
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        HpoaError error = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY, error.category());
        String errString = String.format("Malformed biocuration entry: \"%s\"", curationString);
        assertEquals(errString, error.getMessage());
    }

    @Test
    public void noDate() {
        String curationString = "HPO:probinson";
        Biocuration bc = new HpoProjectBiocuration(curationString);
        List<HpoaError> errors = bc.errors();
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        HpoaError error = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY, error.category());
        String errString = String.format("Malformed biocuration entry: \"%s\"", curationString);
        assertEquals(errString, error.getMessage());
    }

    @Test
    public void onlyDate() {
        String curationString = "[2024-04-01]";
        Biocuration bc = new HpoProjectBiocuration(curationString);
        List<HpoaError> errors = bc.errors();
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        HpoaError error = errors.get(0);
        assertEquals(HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY, error.category());
        String errString = String.format("Malformed biocuration entry: \"%s\"", curationString);
        assertEquals(errString, error.getMessage());
    }





}
