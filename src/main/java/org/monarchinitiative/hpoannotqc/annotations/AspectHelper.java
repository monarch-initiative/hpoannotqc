package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.annotations.io.hpo.Aspect;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoClinicalModifierTermIds.CLINICAL_COURSE;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoModeOfInheritanceTermIds.INHERITANCE_ROOT;
import static org.monarchinitiative.phenol.annotations.constants.hpo.HpoSubOntologyRootTermIds.*;


/**
 * Helper class for determining the HPO aspect (category) of phenotype terms.
 *
 * <p>This class provides functionality to determine which HPO aspect (Phenotypic abnormality,
 * Clinical course, Mode of inheritance, etc.) a given HPO term belongs to. This information
 * is used when generating the big file format.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class AspectHelper {

    private final Ontology hpoOntology;

    /**
     * Constructs an AspectHelper with the specified HPO ontology.
     *
     * @param ontology the HPO ontology to use for aspect determination
     */
    public AspectHelper(Ontology ontology) {
        this.hpoOntology = ontology;
    }


    /**
     * Determines the HPO aspect for the given term ID.
     *
     * <p>This method analyzes the hierarchical position of the term in the HPO ontology
     * to determine which aspect (category) it belongs to.</p>
     *
     * @param tid the HPO term ID to analyze
     * @return the HPO aspect that this term belongs to
     * @throws HpoAnnotQcException if the term ID is null or cannot be processed
     */
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
        } else if (Aspect.fromTermId(primaryHpoId).isPresent()) {
            return Aspect.fromTermId(primaryHpoId).get();
        } else {
            throw new HpoAnnotQcException("Could not determine aspect of TermId " + tid.getValue());
        }
    }



}
