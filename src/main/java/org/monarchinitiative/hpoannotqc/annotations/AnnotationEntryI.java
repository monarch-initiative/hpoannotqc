package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;

public interface AnnotationEntryI {

    public String getDiseaseID();

    /**
     * The disease ID is a CURIE - DATABASE:identifier.
     *
     * @return the prefix part of the diseaseID.
     */
    public String getDatabasePrefix();

    /**
     * The disease ID is a CURIE - DATABASE:identifier.
     *
     * @return the identifier part of the diseaseID.
     */
    public String getDatabaseIdentifier();

    /**
     * @return the disease name, e.g., Noonan syndrome.
     */
    public String getDiseaseName();

    /**
     * @return HPO id of this annotation.
     */
    public TermId getPhenotypeId();

    /**
     * @return HPO term label of this annotation.
     */
    public String getPhenotypeLabel();

    /**
     * @return HPO Id of the age of onset, or null.
     */
    public String getAgeOfOnsetId();
    /**
     * @return HPO term label of age of onset or empty string.
     */
    public String getAgeOfOnsetLabel();
    /**
     * @return evidence for this annotation (one of IEA, PCS, TAS).
     */
    public String getEvidenceCode();

    /**
     * @return String representing the frequency modifier.
     */
    public String getFrequencyModifier();
    /**
     * @return String represeting the sex (MALE or FEMALE) or Empty string.
     */
    public String getSex();

    /**
     * @return the String "NOT" or the empty string.
     */
    public String getNegation() ;

    /**
     * @return list of one or more HPO term ids (as a semicolon-separated String), or emtpry string.
     */
    public String getModifier();
    /**
     * @return (optional) free text description.
     */
    public String getDescription();
    /**
     * @return the citation supporting the annotation, e.g., a PubMed ID.
     */
    public String getPublication();

    /**
     * @return a string representing the biocuration history.
     */
    public String getBiocuration();

    public List<HpoaError> getErrors();

    public boolean hasError();
}
