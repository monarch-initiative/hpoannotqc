package org.monarchinitiative.hpoannotqc.exception;

/**
 * Abstract base runtime exception for HPOA-related errors.
 *
 * <p>This exception provides common functionality for disease context and skippable behavior.
 * It serves as the base class for all HPO annotation processing exceptions that need to
 * maintain disease information context.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public abstract class HpoaRuntimeException extends RuntimeException {

    private final String diseaseId;
    private final String diseaseName;

    /**
     * Constructs a new HPOA runtime exception with disease context and message.
     *
     * @param diseaseId   the disease identifier (e.g., OMIM:600123)
     * @param diseaseName the human-readable disease name
     * @param message     the detail message
     */
    protected HpoaRuntimeException(String diseaseId, String diseaseName, String message) {
        super(message);
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
    }

    /**
     * Constructs a new HPOA runtime exception with disease context, message, and cause.
     *
     * @param diseaseId   the disease identifier (e.g., OMIM:600123)
     * @param diseaseName the human-readable disease name
     * @param message     the detail message
     * @param cause       the underlying cause of this exception
     */
    protected HpoaRuntimeException(String diseaseId, String diseaseName, String message, Throwable cause) {
        super(message, cause);
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
    }

    /**
     * Gets the disease identifier associated with this exception.
     *
     * @return the disease identifier (e.g., OMIM:600123)
     */
    public String getDiseaseId() {
        return diseaseId;
    }

    /**
     * Gets the disease name associated with this exception.
     *
     * @return the human-readable disease name
     */
    public String getDiseaseName() {
        return diseaseName;
    }

    /**
     * Gets the disease name associated with this exception.
     *
     * @deprecated Use {@link #getDiseaseName()} instead
     * @return the human-readable disease name
     */
    @Deprecated
    public String getDisease() {
        return diseaseName;
    }

    /**
     * Gets a formatted message that includes the disease name and the original message.
     *
     * @return formatted message with disease context
     */
    public String getMessageWithDisease() {
        return String.format("%s - %s", getDiseaseName(), getMessage());
    }

    /**
     * Determines if this exception can be skipped during processing.
     *
     * <p>Subclasses can override this method to indicate whether processing can continue
     * despite this exception.</p>
     *
     * @return true if this exception can be skipped, false otherwise (default)
     */
    public boolean skippable() {
        return false;
    }
}
