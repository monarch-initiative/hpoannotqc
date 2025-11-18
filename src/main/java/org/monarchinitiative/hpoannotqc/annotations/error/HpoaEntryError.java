package org.monarchinitiative.hpoannotqc.annotations.error;

import org.monarchinitiative.hpoannotqc.annotations.HpoAnnotationEntry;
import org.monarchinitiative.phenol.ontology.data.TermId;

public class HpoaEntryError extends HpoaRuntimeException {

    private final TermId hpoId;

    public HpoaEntryError(HpoAnnotationEntry entry, String msg) {
        super(entry.getDiseaseId(), entry.getDiseaseName(), String.format("%s: %s", entry.getPhenotypeId().getValue(), msg));
        this.hpoId = entry.getPhenotypeId();
    }

    public TermId getHpoId() {
        return hpoId;
    }
}
