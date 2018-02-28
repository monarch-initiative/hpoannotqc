package org.monarchinitiative.hpoannotqc.mondo;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.ontology.data.TermId;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.github.phenomics.ontolib.ontology.algo.OntologyAlgorithm.getParentTerms;

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
     * @param query
     * @return set of terms that all have path length k to the query term whereby k is the smallest k of any matching term
     */
    public Set<TermId> closestTerms(TermId query) {
        Set<TermId> matchingTerms = new HashSet<>();
        if (diseaseterms.contains(query)) {
            matchingTerms.add(query);
            return matchingTerms;
        }
        this.level=0;
        Stack<TermId> stack=new Stack();
        Set<TermId> seen = new HashSet<>();
        stack.push(query);
        seen.add(query);
        while ( ! stack.empty()) {
            TermId tid = stack.pop();
            Set<TermId> parents = getParentTerms(ontology,tid);
            for (TermId id : parents) {
                if (diseaseterms.contains(id)) {
                    matchingTerms.add(id);
                }
                if (!seen.contains(id)) {
                    stack.push(id);
                    seen.add(id);
                }
            }
            if (!matchingTerms.isEmpty()) {
                return matchingTerms;
            }
        }
        // if we get here, there was no match and matchingTerms will be empty.
        return matchingTerms;

    }
}
