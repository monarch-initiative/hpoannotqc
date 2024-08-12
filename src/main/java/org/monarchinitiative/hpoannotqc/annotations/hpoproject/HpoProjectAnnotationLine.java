package org.monarchinitiative.hpoannotqc.annotations.hpoproject;


import org.monarchinitiative.hpoannotqc.Biocuration;
import org.monarchinitiative.hpoannotqc.TermValidator;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.hpoannotqc.annotations.TermValidationResult;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaMetadataError;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Items of this class represent one line of an annotation file for one disease as curated
 * by the HPO project
 *
 * @author Peter Robinson
 */
public record HpoProjectAnnotationLine(
        Term diseaseTerm,
        Term phenotypeTerm,
        Term onsetTerm,
        FrequencyModifier frequencyModifier,
        String sex,
        String negation,
        String modifier,
        String description,
        String publication,
        String evidenceCode,
        Biocuration biocuration,
        List<HpoaError> errorList
) implements AnnotationEntryI {
    private final static Logger LOGGER = LoggerFactory.getLogger(HpoProjectAnnotationLine.class);

    private final static String EMPTY_STRING = "";

    /**
     * These are the fields of the per-disease annotation files ("small files")
     */
    private final static int DISEASE_ID = 0;
    private final static int DISEASE_NAME = 1;
    private final static int PHENOTYPE_ID = 2;
    private final static int PHENOTYPE_NAME = 3;
    private final static int ONSET_ID = 4;
    private final static int ONSET_NAME = 5;
    private final static int FREQUENCY = 6;
    private final static int SEX = 7;
    private final static int NEGATION = 8;
    private final static int MODIFIER = 9;
    private final static int DESCRIPTION = 10;
    private final static int PUBLICATION = 11;
    private final static int EVIDENCE = 12;
    private final static int BIOCURATION = 13;
    /**
     * Number of tab-separated expectedFields in a valid small file.
     */
    private static final int NUMBER_OF_FIELDS = 14;


    private String checkModifiers(String modifier, TermValidator validator) {
        String[] modifiers = modifier.split(";");
        for (String m : modifiers) {
            TermValidationResult tvalid = validator.checkModifier(m);
            if (!tvalid.isValid()) {
                errorList.add(tvalid.getError());
            }
        }
        return modifier;
    }

    private static Optional<String> checkSexString(String sex) {
        if (sex == null || sex.isEmpty()) {
            return Optional.of(EMPTY_STRING);
        } else if (sex.equals("MALE") || sex.equals("FEMALE")) {
            return Optional.of(sex);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String> checkNegation(String negation) {
        if (negation == null || negation.isEmpty()) {
            // fine
            return Optional.of(EMPTY_STRING);
        } else if (!negation.equals("NOT")) {
            return Optional.empty();
        } else { // well-formed, i.e., "NOT"
            return Optional.of(negation);
        }
    }


    public static AnnotationEntryI fromLine(String line,
                                            TermValidator validator,
                                            Ontology ontology) {
        String[] A = line.split("\t");
        if (A.length != NUMBER_OF_FIELDS) {
            // Non-recoverable error
            throw new HpoAnnotQcException(String.format("We were expecting %d expectedFields but got %d for line %s", NUMBER_OF_FIELDS, A.length, line));
        }
        List<HpoaError> errorList = new ArrayList<>();
        TermValidationResult tvalid = validator.validateDiseaseTerm(A[DISEASE_ID], A[DISEASE_NAME]);
        Term disease = tvalid.validate(errorList);

        tvalid = validator.checkValidTerm(A[PHENOTYPE_ID], A[PHENOTYPE_NAME]);
        Term phenotype = tvalid.validate(errorList);
        tvalid = validator.checkOnsetTerm(A[4], A[5]);
        Term onsetTerm = tvalid.validate(errorList);
        FrequencyModifier freqMod = HpoProjectFrequency.fromHpoaLine(A[FREQUENCY], ontology);
        Optional<HpoaError> errorOpt = freqMod.error();
        errorOpt.ifPresent(errorList::add);
        Optional<String> sexOpt = checkSexString(A[SEX]);
        String sexString;
        if (sexOpt.isPresent()) {
            sexString = sexOpt.get();
        } else {
            sexString = null;
            errorList.add(HpoaMetadataError.sexStringError(A[SEX]));
        }
        String negationString;
        Optional<String> negOpt = checkNegation(A[NEGATION]);
        if (negOpt.isPresent()) {
            negationString = negOpt.get();
        } else {
            negationString = null;
            errorList.add(HpoaMetadataError.malformedNegation(A[NEGATION]));
        }
        String modString = A[MODIFIER];
        if (!modString.isEmpty()) {
            // fine to nbe empty, but if something is present it must be valid
            tvalid = validator.checkModifier(modString);
            if (!tvalid.isValid()) {
                errorList.add(tvalid.getError());
            }
        }
        String description = A[DESCRIPTION] != null ? A[DESCRIPTION] : EMPTY_STRING;
        String publication = A[PUBLICATION] != null ? A[PUBLICATION] : EMPTY_STRING;
        String evidenceCode = A[12];
        Biocuration biocuration = new HpoProjectBiocuration(A[BIOCURATION]);
        if (!biocuration.errors().isEmpty()) {
            errorList.addAll(biocuration.errors());
        }
        return new HpoProjectAnnotationLine(disease,
                phenotype,
                onsetTerm,
                freqMod,
                sexString,
                negationString,
                modString,
                description,
                publication,
                evidenceCode,
                biocuration,
                errorList);
    }


    @Override
    public String getDiseaseID() {
        return diseaseTerm.id().getValue();
    }

    @Override
    public String getDatabasePrefix() {
        return diseaseTerm.id().getPrefix();
    }

    @Override
    public String getDatabaseIdentifier() {
        return diseaseTerm.id().getId();
    }

    @Override
    public String getDiseaseName() {
        return diseaseTerm.getName();
    }

    @Override
    public String getPhenotypeId() {
        return phenotypeTerm.id().getValue();
    }

    @Override
    public TermId getPhenotypeTermId() {
        return phenotypeTerm.id();
    }

    @Override
    public String getPhenotypeLabel() {
        return phenotypeTerm.getName();
    }

    @Override
    public String getAgeOfOnsetId() {
        return onsetTerm == null ? EMPTY_STRING : onsetTerm.id().getValue();
    }

    @Override
    public String getAgeOfOnsetLabel() {
        return onsetTerm == null ? EMPTY_STRING : onsetTerm.getName();
    }

    @Override
    public String getEvidenceCode() {
        return evidenceCode;
    }

    @Override
    public String getFrequencyModifier() {
        return this.frequencyModifier.frequencyString();
    }

    @Override
    public String getSex() {
        return sex == null ? EMPTY_STRING : sex;
    }

    @Override
    public String getNegation() {
        return negation == null ? EMPTY_STRING : negation;
    }

    @Override
    public String getModifier() {
        return modifier != null ? modifier : EMPTY_STRING;
    }

    @Override
    public String getDescription() {
        return description != null ? description : EMPTY_STRING;
    }

    @Override
    public String getPublication() {
        return publication;
    }

    @Override
    public String getBiocuration() {
        return biocuration.curation();
    }

    @Override
    public List<HpoaError> getErrors() {
        return errorList;
    }

    @Override
    public boolean hasError() {
        return !errorList.isEmpty();
    }

    @Override
    public String getTsvLine() {
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
