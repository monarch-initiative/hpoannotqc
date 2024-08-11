package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;

import java.util.List;

public interface AnnotationEntryI {

    String getDiseaseID();

    /**
     * The disease ID is a CURIE - DATABASE:identifier.
     *
     * @return the prefix part of the diseaseID.
     */
    String getDatabasePrefix();

    /**
     * The disease ID is a CURIE - DATABASE:identifier.
     *
     * @return the identifier part of the diseaseID.
     */
    String getDatabaseIdentifier();

    /**
     * @return the disease name, e.g., Noonan syndrome.
     */
    String getDiseaseName();

    /**
     * @return HPO id of this annotation.
     */
    String getPhenotypeId();

    /**
     * @return HPO term label of this annotation.
     */
    String getPhenotypeLabel();

    /**
     * @return HPO Id of the age of onset, or null.
     */
    String getAgeOfOnsetId();
    /**
     * @return HPO term label of age of onset or empty string.
     */
    String getAgeOfOnsetLabel();
    /**
     * @return evidence for this annotation (one of IEA, PCS, TAS).
     */
    String getEvidenceCode();

    /**
     * @return String representing the frequency modifier.
     */
    String getFrequencyModifier();
    /**
     * @return String represeting the sex (MALE or FEMALE) or Empty string.
     */
    String getSex();

    /**
     * @return the String "NOT" or the empty string.
     */
    String getNegation() ;

    /**
     * @return list of one or more HPO term ids (as a semicolon-separated String), or emtpry string.
     */
    String getModifier();
    /**
     * @return (optional) free text description.
     */
    String getDescription();
    /**
     * @return the citation supporting the annotation, e.g., a PubMed ID.
     */
    String getPublication();

    /**
     * @return a string representing the biocuration history.
     */
    String getBiocuration();

    List<HpoaError> getErrors();

    boolean hasError();

    default String getTsvLine() {
            List<String> items = List.of(
                    getDiseaseID(),
                    getDiseaseName(),
                    getPhenotypeId(),
                    getPhenotypeLabel(),
                    getAgeOfOnsetId(),
                    getAgeOfOnsetLabel(),
                    getFrequencyModifier(),
                    getSex(),
                    getNegation(),
                    getModifier(),
                    getDescription(),
                    getPublication(),
                    getEvidenceCode(),
                    getBiocuration());
            return String.join("\t", items);
    }
}
