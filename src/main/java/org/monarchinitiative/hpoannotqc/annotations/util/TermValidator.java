package org.monarchinitiative.hpoannotqc.annotations.util;

import org.monarchinitiative.hpoannotqc.annotations.TermValidationResult;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaTermError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.TermIdError;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;


import java.util.Optional;
import java.util.Set;

public class TermValidator {

    private final Ontology ontology;
    /**
     * Identifier of the HPO term "Onset", which is the root of all of the onset terms.
     */
    private final TermId ONSET_ROOT_ID = TermId.of("HP:0003674");
    /**
     * Identifier of the HPO term "Clinical modifier HP:0012823",
     */
    private final TermId MODIFIER_ROOT_ID = TermId.of("HP:0012823");

    private final Set<String> VALID_DB_PREFIXES = Set.of("OMIM", "DECIPHER", "ORPHA");

    public TermValidator(Ontology hpoOntology) {
        this.ontology = hpoOntology;
    }



    private boolean isMalformedCurie(String termId, String expectedPrefix) {
        String [] fields = termId.split(":");
        if (fields.length != 2) {
            return true;
        } else if (!fields[0].equals(expectedPrefix)) {
            return true;
        } else {
            return false;
        }
    }


    public TermValidationResult validateDiseaseTerm(String termId, String termName) {
        String [] fields = termId.split(":");
        if (fields.length != 2) {
            return TermValidationResult.of(TermIdError.malformed(termId));
        }
        String candidatePrefix = fields[0];
        if (!VALID_DB_PREFIXES.contains(candidatePrefix)) {
            return TermValidationResult.of(TermIdError.malformed(candidatePrefix));
        }
        if (termName == null || termName.isEmpty()) {
            return TermValidationResult.of(HpoaTermError.emptyDiseaseName(termId));
        }
        Term validDbTerm = Term.builder(TermId.of(termId)).name(termName).build();
        return TermValidationResult.of(validDbTerm);
    }

    public boolean isValidCurie(String candidateCurie) {
        String [] fields = candidateCurie.split(":");
        return fields.length == 2;
    }

    public TermValidationResult checkValidTerm(String hpoId,
                                                String hpoLabel) {
        if (isMalformedCurie(hpoId, "HP")) {
            return TermValidationResult.of(TermIdError.malformed(hpoId));
        }
        TermId termId = TermId.of(hpoId);
        if (!ontology.containsTerm(termId)) {
            return TermValidationResult.of(TermIdError.termIdNotInOntology(termId));
        } else {
            TermId primaryId = ontology.getPrimaryTermId(termId);
            if (!primaryId.equals(termId)) {
                return TermValidationResult.of(TermIdError.idDoesNotMatchPrimary(termId, primaryId));
            } else {
                Optional<Term> termOpt = ontology.termForTermId(primaryId);
                if (termOpt.isEmpty()) {
                    // this should never happen, placeholder
                    return TermValidationResult.of(TermIdError.termIdNotInOntology(termId));
                } else {
                    Term term = termOpt.get();
                    String primaryLabel = term.getName();
                    if (!primaryLabel.equals(hpoLabel)) {
                       return TermValidationResult.of(TermIdError.labelDoesNotMatchPrimary(hpoLabel, primaryLabel));
                    } else {
                        return TermValidationResult.of(term);
                    }
                }
            }
        }
    }

    /**
     * It is allowable for the onset term to be empty.
     * This method checks that, if onset data is present, it is also valid.
     * Side effect -- add errors to list if term is invalid
     * @param onsetId id of the HPO onset term
     * @param onsetName label of the HPO onset term
     * @return Optional -- present if the term is valid.
     *
     */
    public TermValidationResult checkOnsetTerm(String onsetId, String onsetName) {
        if (onsetId == null || onsetId.isEmpty()) {
            Term inexistentOnsetTerm = null;
            return TermValidationResult.of(inexistentOnsetTerm);
        }
        TermValidationResult checkResult = checkValidTerm(onsetId, onsetName);
        if (!checkResult.isValid()) {
            return checkResult;
        } else {
            Term candidateTerm = checkResult.getTerm();
            if (ontology.graph().isDescendantOf(candidateTerm.id(), ONSET_ROOT_ID) ||
                    candidateTerm.id().equals(ONSET_ROOT_ID)) {
                return checkResult;
            } else {
                // if we get here, we have a valid HPO term that is not in the Onset subhierarchy
                return TermValidationResult.of(HpoaTermError.invalidFrequencyTerm(candidateTerm));
            }
        }
    }

    public TermValidationResult checkModifier(String hpoModifierId) {
        if ( isMalformedCurie(hpoModifierId, "HP")) {
            return TermValidationResult.of(TermIdError.malformed(hpoModifierId));
        }
        TermId termId = TermId.of(hpoModifierId);
        if (!ontology.containsTermId(termId)) {
            return TermValidationResult.of(TermIdError.termIdNotInOntology(termId));
        } else {
            Optional<Term> termOpt = ontology.termForTermId(termId);
            if (termOpt.isEmpty()) {
                return TermValidationResult.of(TermIdError.termIdNotInOntology(termId));
            }
            Term candidateTerm = termOpt.get();
            if (ontology.graph().isDescendantOf(candidateTerm.id(), MODIFIER_ROOT_ID) ||
                    candidateTerm.id().equals(ONSET_ROOT_ID)) {
                return TermValidationResult.of(candidateTerm);
            } else {
                // if we get here, we have a valid HPO term that is not in the Onset subhierarchy
                return TermValidationResult.of(TermIdError.invalidModifierId(termId.getValue()));
            }
        }
    }
}
