package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.util.AspectIdentifier;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;

public interface AnnotationEntry {

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

    TermId getPhenotypeTermId();

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

    /**
     * Following quality control of an entry that has been ingested from a small file, and potentially merged,
     * we export the corresponding line for the big file.
     * @param aspectIdentifier An object to decide what "aspect" this line is
     * @return A line for the phenotype.hpoa file
     */
    default String toBigFileLine(AspectIdentifier aspectIdentifier) {
        String[] elems = {
                getDiseaseID(), //DB_Object_ID
                getDiseaseName(), // DB_Name
                getNegation(), // Qualifier
                getPhenotypeId(), // HPO_ID
                getPublication(), // DB_Reference
                getEvidenceCode(), // Evidence_Code
                getAgeOfOnsetId(), // Onset
                getFrequencyModifier(), // Frequency
                getSex(), // Sex
                getModifier(), // Modifier
                aspectIdentifier.getAspectLetter(getPhenotypeTermId()), // Aspect
                getBiocuration() // Biocuration
        };
        return String.join("\t", elems);
    }


}
