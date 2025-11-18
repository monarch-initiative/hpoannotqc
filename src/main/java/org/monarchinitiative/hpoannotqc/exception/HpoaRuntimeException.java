package org.monarchinitiative.hpoannotqc.exception;

/**
 * Base runtime exception for HPOA-related errors that provides common functionality
 * for disease context and skippable behavior.
 */
public abstract class HpoaRuntimeException extends RuntimeException {

    private final String diseaseId;
    private final String diseaseName;

    protected HpoaRuntimeException(String diseaseId, String diseaseName, String message) {
        super(message);
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
    }

    protected HpoaRuntimeException(String diseaseId, String diseaseName, String message, Throwable cause) {
        super(message, cause);
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    /**
     * @deprecated Use getDiseaseName() instead
     */
    @Deprecated
    public String getDisease() {
        return diseaseName;
    }

    public String getMessageWithDisease() {
        return String.format("%s - %s", getDiseaseName(), getMessage());
    }

    public boolean skippable() {
        return false;
    }
}
