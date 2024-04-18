package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.annotations.io.hpo.Aspect;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoClinicalModifierTermIds.CLINICAL_COURSE;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoModeOfInheritanceTermIds.INHERITANCE_ROOT;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoSubOntologyRootTermIds.*;


/**
 *
 */
public class AspectHelper {


    private final Ontology hpoOntology;

    public AspectHelper(Ontology ontology) {
        this.hpoOntology = ontology;
    }


    public Aspect parse(TermId tid) throws HpoAnnotQcException {
        TermId primaryHpoId = hpoOntology.getPrimaryTermId(tid);
        if (primaryHpoId == null) {
            throw new HpoAnnotQcException("Cannot compute Aspect of NULL term");
        }

        if (! primaryHpoId.equals(tid)) {
            throw new HpoAnnotQcException(String.format("TermId %s did not match primary id %s", tid.getValue(), primaryHpoId.getValue()));
        }
        if (hpoOntology.graph().existsPath(primaryHpoId, PHENOTYPIC_ABNORMALITY) ) {
            return Aspect.P; // organ/phenotype abnormality
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return Aspect.I;
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_COURSE)) {
            return Aspect.C;
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_MODIFIER)) {
            return Aspect.M;
        } else if (hpoOntology.graph().existsPath(primaryHpoId, PAST_MEDICAL_HISTORY)) {
            return Aspect.H; // the Orphanet annotations include some entries to the phenotype root
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return Aspect.I; // the Orphanet annotations include some entries to the root
        } else if (Aspect.fromTermId(primaryHpoId).isPresent()) {
            return Aspect.fromTermId(primaryHpoId).get();
        } else {
            throw new HpoAnnotQcException("Could not determine aspect of TermId " + tid.getValue());
        }
    }



}
