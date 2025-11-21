package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.hpoannotqc.annotations.HpoAnnotationEntry;
import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Exception thrown when an annotation uses an obsolete HPO aspect term.
 *
 * <p>This exception is raised when an annotation entry contains an HPO term ID that
 * refers to an obsolete aspect (category) in the ontology. The exception provides
 * information about the current primary aspect term that should be used instead.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class ObsoleteAspectError extends HpoaRuntimeException {

  private final TermId primaryId;
  private final String primaryLabel;

  /**
   * Constructs a new obsolete aspect error with information about the primary replacement term.
   *
   * @param entry        the annotation entry containing the obsolete aspect term
   * @param primary      the current primary aspect term ID that should be used
   * @param primaryLabel the label of the current primary aspect term
   */
  public ObsoleteAspectError(HpoAnnotationEntry entry, TermId primary, String primaryLabel) {
    super(entry.getDiseaseId(), entry.getDiseaseName(), String.format("Usage of (obsolete) aspect id %s for %s (%s)",
        entry.getPhenotypeId().getValue(), primary.getValue(), primaryLabel));

    this.primaryId = primary;
    this.primaryLabel = primaryLabel;
  }

  /**
   * Gets the current primary aspect term ID that should replace the obsolete term.
   *
   * @return the primary aspect term ID
   */
  public TermId getPrimaryId() {
    return primaryId;
  }

  /**
   * Gets the label of the current primary aspect term.
   *
   * @return the primary aspect term label
   */
  public String getTermLabel() {
    return primaryLabel;
  }
}
