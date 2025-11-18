package org.monarchinitiative.hpoannotqc.annotations.error;

import org.monarchinitiative.hpoannotqc.annotations.HpoAnnotationEntry;
import org.monarchinitiative.phenol.ontology.data.TermId;

public class ObsoleteAspectError extends HpoaRuntimeException {

  private final TermId primaryId;
  private final String primaryLabel;

  public ObsoleteAspectError(HpoAnnotationEntry entry, TermId primary, String primaryLabel) {
    super(entry.getDiseaseId(), entry.getDiseaseName(), String.format("Usage of (obsolete) aspect id %s for %s (%s)",
        entry.getPhenotypeId().getValue(), primary.getValue(), primaryLabel));

    this.primaryId = primary;
    this.primaryLabel = primaryLabel;
  }

  public TermId getPrimaryId() {
    return primaryId;
  }

  public String getTermLabel() {
    return primaryLabel;
  }
}