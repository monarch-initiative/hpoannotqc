package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class ObsoleteTermIdError implements HpoaError {
  private final TermId obsoleteId;
  private final TermId primaryId;

  private final String termLabel;

  private final String diseaseLabel;

    public ObsoleteTermIdError(String diseaseName, TermId obsolete, TermId primary, String primaryLabel) {
      obsoleteId = obsolete;
      this.primaryId = primary;
      this.termLabel = primaryLabel;
      this.diseaseLabel = diseaseName;
    }


  @Override
  public String getDisease() {
    return diseaseLabel;
  }

  @Override
  public String getMessage() {
      return String.format("Usage of (obsolete) alt_id %s for %s (%s)",
              obsoleteId.getValue(), primaryId.getValue(), termLabel);
    }
}


