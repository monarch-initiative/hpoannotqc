package org.monarchinitiative.hpoannotqc.mondo;



import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getParentTerms;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.isSubclass;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.termsAreSiblings;


/**
 * This class with serve to experiment with the best way of writing a function that will find the closest
 * match to one term given a set of "other" terms.
 */
public class ClosestMatch {

    private final HpoOntology ontology;
    private final Set<TermId> diseaseterms;
    private int level;

    public ClosestMatch(HpoOntology ontology, Set<TermId> otherterms) {
        this.ontology=ontology;
        this.diseaseterms=otherterms;
    }


    /**
     * Find the set of terms with the shortest path length to the query.
     * @param query the search term
     * @return set of terms that all have path length k to the query term whereby k is the smallest k of any matching term
     */
    public Map<TermId, Integer> closestTerms(TermId query) {
        Map<TermId, Integer> matchingTerms = new HashMap<>();
        if (diseaseterms.contains(query)) {
            matchingTerms.put(query, 0);
            return matchingTerms;
        }
        Stack<TermId> stack=new Stack<>();
        Map<TermId, Integer> seen = new HashMap<>();
        stack.push(query);
        seen.put(query,0);
        while ( ! stack.empty()) {
            TermId tid = stack.pop();
            int level = seen.get(tid) + 1; // parent terms are one level more distant
            Set<TermId> parents = getParentTerms(ontology,tid);
            for (TermId id : parents) {
                if (diseaseterms.contains(id)) {
                    if (! matchingTerms.containsKey(id)) {
                        matchingTerms.put(id,level); // prevent previous seen term from getting wrong level!
                    }
                }
                if (!seen.containsKey(id)) {
                    stack.push(id);
                    seen.put(id,level);
                }
            }
            if (!matchingTerms.isEmpty()) {
                return matchingTerms;
            }
        }
        // if we get here, there was no match and matchingTerms will be empty.
        // look also in opposite direction
        Map<TermId,Integer> opposites = getViceVersa(query);
        for (TermId i : opposites.keySet()) {
            matchingTerms.put(i,opposites.get(i));
        }
        return matchingTerms;
    }


    Map<TermId, Integer> getViceVersa(TermId query) {
        Map<TermId, Integer> opposites = new HashMap<>();
        for (TermId annot : diseaseterms) {
            if (isSubclass(ontology, annot, query)) {
                opposites.put(annot, 3);
            }
        }
            if (opposites.size() > 0)
                return opposites;

            for (TermId annot2 : diseaseterms) {
                if (termsAreSiblings(ontology, annot2, query)) {
                    opposites.put(annot2, 4);
                }
            }
            if (opposites.size() > 0)
                return opposites;
//            for (TermId annot3 : diseaseterms) {
//                if (termsAreRelated(ontology, annot3, query)) {
//                    opposites.put(annot3, 5);
//                }
//
//            }
            return opposites;
        }


}
