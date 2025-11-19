package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.hpoannotqc.annotations.HpoAnnotationEntry;
import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Exception thrown when an error is detected in an HPO annotation entry.
 *
 * <p>This exception extends {@link HpoaRuntimeException} and provides additional
 * context about the specific HPO term that caused the error.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class HpoaEntryError extends HpoaRuntimeException {

    private final TermId hpoId;

    /**
     * Constructs a new HPO annotation entry error with details from the problematic entry.
     *
     * @param entry the HPO annotation entry that caused the error
     * @param msg   detailed error message describing the problem
     */
    public HpoaEntryError(HpoAnnotationEntry entry, String msg) {
        super(entry.getDiseaseId(), entry.getDiseaseName(), String.format("%s: %s", entry.getPhenotypeId().getValue(), msg));
        this.hpoId = entry.getPhenotypeId();
    }

    /**
     * Gets the HPO term ID associated with this error.
     *
     * @return the HPO term ID that caused the error
     */
    public TermId getHpoId() {
        return hpoId;
    }
}
