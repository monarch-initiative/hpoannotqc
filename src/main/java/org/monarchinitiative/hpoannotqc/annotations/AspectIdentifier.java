package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoAspect;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.HashMap;
import java.util.Map;

import static org.monarchinitiative.hpoannotqc.annotations.hpo.HpoModeOfInheritanceTermIds.INHERITANCE_ROOT;
import static org.monarchinitiative.hpoannotqc.annotations.hpo.HpoSubOntologyRootTermIds.*;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoClinicalModifierTermIds.CLINICAL_COURSE;




/**
 *
 */
public class AspectIdentifier {


    private final Ontology hpoOntology;

    /** Some data sources annotate to the roots of these subhierarchies. There is no
     * path between a node and itself in our graph, and so we create this map to
     * categorize these terms.
     */
    private final static Map<TermId, HpoAspect> SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP;

    static {
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP = new HashMap<>();
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.put(PHENOTYPIC_ABNORMALITY, HpoAspect.PHENOTYPIC_ABNORMALITY_ASPECT);
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.put(INHERITANCE_ROOT, HpoAspect.INHERITANCE_ASPECT);
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.put(CLINICAL_COURSE, HpoAspect.CLINICAL_COURSE_ASPECT);
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.put(CLINICAL_MODIFIER, HpoAspect.CLINICAL_MODIFIER_ASPECT);
        SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.put(PAST_MEDICAL_HISTORY, HpoAspect.PAST_MEDICAL_HISTORY_ASPECT);
    }

    public AspectIdentifier(Ontology ontology) {
        this.hpoOntology = ontology;
    }


    public HpoAspect getAspect(TermId tid) throws HpoAnnotQcException {
        TermId primaryHpoId = hpoOntology.getPrimaryTermId(tid);
        if (primaryHpoId == null) {
            throw new HpoAnnotQcException("Cannot compute Aspect of NULL term");
        }
        if (! primaryHpoId.equals(tid)) {
            throw new HpoAnnotQcException(String.format("TermId %s did not match primary id %s", tid.getValue(), primaryHpoId.getValue()));
        }
        if (hpoOntology.graph().existsPath(primaryHpoId, PHENOTYPIC_ABNORMALITY) ) {
            return HpoAspect.PHENOTYPIC_ABNORMALITY_ASPECT;
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return HpoAspect.INHERITANCE_ASPECT;
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_COURSE)) {
            return HpoAspect.CLINICAL_COURSE_ASPECT;
        } else if (hpoOntology.graph().existsPath(primaryHpoId,  CLINICAL_MODIFIER)) {
            return HpoAspect.CLINICAL_MODIFIER_ASPECT;
        } else if (hpoOntology.graph().existsPath(primaryHpoId, PAST_MEDICAL_HISTORY)) {
            return HpoAspect.PAST_MEDICAL_HISTORY_ASPECT; // the Orphanet annotations include some entries to the phenotype root
        } else if (hpoOntology.graph().existsPath(primaryHpoId, INHERITANCE_ROOT)) {
            return HpoAspect.INHERITANCE_ASPECT; // the Orphanet annotations include some entries to the root
        } else if (SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.containsKey(primaryHpoId)) {
            return SUBHIERARCHY_ROOT_TO_IDENTIFIER_MAP.get(primaryHpoId);
        } else {
            throw new HpoAnnotQcException("Could not determine aspect of TermId " + tid.getValue());
        }
    }


    public String getAspectLetter(TermId tid) throws HpoAnnotQcException {
       HpoAspect aspect = getAspect(tid);
       return aspect.letter();
    }



}
