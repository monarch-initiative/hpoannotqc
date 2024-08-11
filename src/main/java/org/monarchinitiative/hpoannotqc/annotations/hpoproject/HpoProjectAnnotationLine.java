package org.monarchinitiative.hpoannotqc.annotations.hpoproject;


import org.monarchinitiative.hpoannotqc.Biocuration;
import org.monarchinitiative.hpoannotqc.TermValidator;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.hpoannotqc.annotations.TermValidationResult;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.TermIdError;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Items of this class represent one line of an annotation file for one disease as curated
 * by the HPO project
 * @author Peter Robinson
 */
public class HpoProjectAnnotationLine implements AnnotationEntryI {


    /**
     * The disease, e.g., OMIM:600201
     */
    private final Term diseaseTerm;
    private final Term phenotypeTerm;
    private final Term onsetTerm;

    /**
     * Field #7
     */
    private final String evidenceCode;
    /**
     * Field #8 can be one of N/M, X% or a valid frequency term identifier.
     */
    private final FrequencyModifier frequencyModifier;
    /**
     * Field #9
     */
    private final String sex;
    /**
     * Field #10
     */
    private final String negation;
    /**
     * Field #11
     */
    private final String modifier;
    /**
     * Field #12
     */
    private final String description;
    /**
     * Field #13
     */
    private final String publication;
    /**
     * Field #14
     */
    private final Biocuration biocuration;

    private final List<HpoaError> errorList;

    /**
     * These are the fields of the per-disease annotation files ("small files")
     */
    private final static String[] expectedFields = {
            "#diseaseID",
            "diseaseName",
            "phenotypeID",
            "phenotypeName",
            "onsetID",
            "onsetName",
            "frequency",
            "sex",
            "negation",
            "modifier",
            "description",
            "publication",
            "evidence",
            "biocuration"};
    /**
     * Number of tab-separated expectedFields in a valid small file.
     */
    private static final int NUMBER_OF_FIELDS = expectedFields.length;


    public HpoProjectAnnotationLine(String [] A,
                                    TermValidator validator,
                                    Ontology ontology) {
        errorList = new ArrayList<>();
        TermId diseaseID = TermId.of(A[0]);
        String diseaseName = A[1];
        String phenotypeId  = A[2];
        String phenotypeName = A[3];
        String ageOfOnsetId = A[4];
        String ageOfOnsetName = A[5];
        String frequencyString = A[6];
        String sex = A[7];
        String negation = A[8];
        String modifier = A[9];
        String description = A[10];
        String publication = A[11];
        String evidenceCode = A[12];
        String biocuration = A[13];
        this.diseaseTerm = Term.of(diseaseID, diseaseName);
        TermValidationResult tvalid = validator.checkValidTerm(phenotypeId, phenotypeName);
        if (tvalid.isValid()) {
            this.phenotypeTerm = tvalid.getTerm();
        } else {
            this.phenotypeTerm = null;
            errorList.add(tvalid.getError());
        }
        tvalid = validator.checkOnsetTerm(ageOfOnsetId, ageOfOnsetName);
        if (tvalid.isValid()) {
            this.onsetTerm = tvalid.getTerm();
        } else {
            this.onsetTerm = null;
            errorList.add(tvalid.getError());
        }
        this.evidenceCode = evidenceCode;
        FrequencyModifier freqMod = HpoProjectFrequency.fromHpoaLine(frequencyString, ontology);
        Optional<HpoaError> errorOpt = freqMod.error();
        if (errorOpt.isPresent()) {
            errorList.add(freqMod.error().get());
        }
        this.frequencyModifier = freqMod;

        this.biocuration = new HpoProjectBiocuration(biocuration);
        // TODO CHECK THIS
        this.sex = sex;
        this.negation = negation;
        this.modifier = modifier;
        this.description = description;
        this.publication = publication;

    }




    public static HpoProjectAnnotationLine fromLine(String line,
                                                    TermValidator validator,
                                                    Ontology ontology) {
        String[] A = line.split("\t");

        if (A.length != NUMBER_OF_FIELDS) {
            // Non-recoverable error
            throw new HpoAnnotQcException(String.format("We were expecting %d expectedFields but got %d for line %s", NUMBER_OF_FIELDS, A.length, line));
        }


        return new HpoProjectAnnotationLine(A, validator, ontology);

    }




    @Override
    public String getDiseaseID() {
        return diseaseTerm.id().getValue();
    }

    @Override
    public String getDatabasePrefix() {
        return "";
    }

    @Override
    public String getDatabaseIdentifier() {
        return "";
    }

    @Override
    public String getDiseaseName() {
        return "";
    }

    @Override
    public TermId getPhenotypeId() {
        return null;
    }

    @Override
    public String getPhenotypeLabel() {
        return "";
    }

    @Override
    public String getAgeOfOnsetId() {
        return "";
    }

    @Override
    public String getAgeOfOnsetLabel() {
        return "";
    }

    @Override
    public String getEvidenceCode() {
        return "";
    }

    @Override
    public String getFrequencyModifier() {
        return this.frequencyModifier.frequencyString();
    }

    @Override
    public String getSex() {
        return "";
    }

    @Override
    public String getNegation() {
        return "";
    }

    @Override
    public String getModifier() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getPublication() {
        return "";
    }

    @Override
    public String getBiocuration() {
        return "";
    }
}
