package org.monarchinitiative.hpoannotqc.annotations.orpha;

import org.monarchinitiative.hpoannotqc.annotations.Biocuration;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrphaBiocuration implements Biocuration {

private final String curation;

    /**
     * Use today's date created value for the Orphanet annotations.
     */
    public OrphaBiocuration() {
        Date date = new Date();
        String todaysDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        curation = String.format("ORPHA:orphadata[%s]", todaysDate);
    }


    @Override
    public String curation() {
        return curation;
    }

    @Override
    public List<HpoaError> errors() {
        return List.of();
    }
}
