package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotationModelException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import static org.monarchinitiative.hpoannotqc.annotations.hpo.HpoModeOfInheritanceTermIds.INHERITANCE_ROOT;
import static org.monarchinitiative.hpoannotqc.annotations.hpo.HpoSubOntologyRootTermIds.*;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoClinicalModifierTermIds.CLINICAL_COURSE;




/**
 *
 */
public class AspectIdentifier {


    private final Ontology hpoOntology;
    public AspectIdentifier(Ontology ontology) {
        this.hpoOntology = ontology;
    }


    public String getAspect(TermId tid) throws HpoAnnotationModelException {
        TermId primaryHpoId = hpoOntology.getPrimaryTermId(tid);
        if (primaryHpoId == null) {
            throw new HpoAnnotationModelException("Cannot compute Aspect of NULL term");
        }

        if (! primaryHpoId.equals(tid)) {
            throw new HpoAnnotationModelException(String.format("TermId %s did not match primary id %s", tid.getValue(), primaryHpoId.getValue()));
        }
        if (hpoOntology.graph().existsPath(primaryHpoId, PHENOTYPIC_ABNORMALITY)) {
            return "P"; // organ/phenotype abnormality
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return "I";
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_COURSE)) {
            return "C";
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_MODIFIER)) {
            return "M";
        } else if (hpoOntology.graph().existsPath(primaryHpoId, PAST_MEDICAL_HISTORY)) {
            return "H"; // the Orphanet annotations include some entries to the phenotype root
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return "I"; // the Orphanet annotations include some entries to the root
        } else {
            throw new HpoAnnotationModelException("Could not determine aspect of TermId " + tid.getValue());
        }
    }



}
