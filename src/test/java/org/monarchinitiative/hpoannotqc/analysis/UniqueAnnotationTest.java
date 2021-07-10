package org.monarchinitiative.hpoannotqc.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniqueAnnotationTest {

    @Test
    public void testLineEligibleForDuplicate() {
        String line = "OMIM:610978\tCHOREOATHETOSIS, HYPOTHYROIDISM, AND NEONATAL RESPIRATORY DISTRESSCHOREOATHETOSIS AND CONGENITAL HYPOTHYROIDISM, INCLUDED\t\tHP:0000851\tOMIM:610978\tIEA\t\t\tt\t\t\t\tP\tHPO:iea[2009-02-17]";
        UniqueAnnotation ua = new UniqueAnnotation(line);
        assertTrue(ua.isDuplicateRemovalCandidate());
    }
}
