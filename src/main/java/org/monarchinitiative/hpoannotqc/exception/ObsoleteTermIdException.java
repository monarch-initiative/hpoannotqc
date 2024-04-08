package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class ObsoleteTermIdException extends HpoAnnotQcException {
  private final TermId obsoleteId;
  private final TermId primaryId;

  private String termLabel;

    public ObsoleteTermIdException(TermId obsolete, TermId primary, String primaryLabel) {
      super(obsolete.getValue());
      obsoleteId = obsolete;
      this.primaryId = primary;
      this.termLabel = primaryLabel;
    }


    @Override
  public String getMessage() {
      return String.format("Usage of (obsolete) alt_id %s for %s (%s)",
              obsoleteId.getValue(), primaryId.getValue(), termLabel);
    }
}


