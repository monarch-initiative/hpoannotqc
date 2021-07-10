package org.monarchinitiative.hpoannotqc.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniqueAnnotationTest {

    @Test
    public void testLineEligibleForDuplicate() {
        String line = "OMIM:610978\tCHOREOATHETOSIS, HYPOTHYROIDISM, AND NEONATAL RESPIRATORY DISTRESSCHOREOATHETOSIS AND CONGENITAL HYPOTHYROIDISM, INCLUDED\t\tHP:0000851\tOMIM:610978\tIEA\t\t\tt\t\t\t\tP\tHPO:iea[2009-02-17]";
        UniqueAnnotation ua = new UniqueAnnotation(line);
        assertTrue(ua.isDuplicateRemovalCandidate());
    }

    @Test
    public void testLineNotEligibleForDuplicate1() {
        // False because not iea or skoehler
        String line = "OMIM:610978\tCHOREOATHETOSIS, HYPOTHYROIDISM, AND NEONATAL RESPIRATORY DISTRESSCHOREOATHETOSIS AND CONGENITAL HYPOTHYROIDISM, INCLUDED\t\tHP:0000851\tOMIM:610978\tIEA\t\t\tt\t\t\t\tP\tHPO:sdoelken[2009-02-17]";
        UniqueAnnotation ua = new UniqueAnnotation(line);
        assertFalse(ua.isDuplicateRemovalCandidate());
    }

    @Test
    public void testLineNotEligibleForDuplicate2() {
        // False because evidence not IEA
        String line = "OMIM:610978\tCHOREOATHETOSIS, HYPOTHYROIDISM, AND NEONATAL RESPIRATORY DISTRESSCHOREOATHETOSIS AND CONGENITAL HYPOTHYROIDISM, INCLUDED\t\tHP:0000851\tOMIM:610978\tTAS\t\t\tt\t\t\t\tP\tHPO:sdoelken[2009-02-17]";
        UniqueAnnotation ua = new UniqueAnnotation(line);
        assertFalse(ua.isDuplicateRemovalCandidate());
    }
}
