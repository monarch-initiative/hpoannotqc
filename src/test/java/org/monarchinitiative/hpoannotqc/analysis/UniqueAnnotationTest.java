package org.monarchinitiative.hpoannotqc.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniqueAnnotationTest {


    private static final String [] fields = {"OMIM:610978",
    "CHOREOATHETOSIS, HYPOTHYROIDISM, AND NEONATAL RESPIRATORY DISTRESSCHOREOATHETOSIS AND CONGENITAL HYPOTHYROIDISM, INCLUDED",
    "HP:0000851",
    "Congenital hypothyroidism",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "OMIM:610978",
    "IEA",
    "HPO:iea[2009-02-17]"};

    @Test
    public void testLineEligibleForDuplicate() {
        String line = String.join("\t", fields);
        UniqueAnnotation ua = new UniqueAnnotation(line);
        assertTrue(ua.isDuplicateRemovalCandidate());
    }

    @Test
    public void testLineNotEligibleForDuplicate1() {
        // False because not iea or skoehler
        String [] annot = new String[fields.length];
        System.arraycopy(fields, 0, annot, 0, fields.length);
        annot[13] = "HPO:sdoelken[2009-02-17]";
        String annotLine = String.join("\t", annot);
        UniqueAnnotation ua = new UniqueAnnotation(annotLine);
        assertFalse(ua.isDuplicateRemovalCandidate());
    }

    @Test
    public void testLineNotEligibleForDuplicate2() {
        // False because evidence not IEA
        String [] annot = new String[fields.length];
        System.arraycopy(fields, 0, annot, 0, fields.length);
        annot[12] = "TAS";
        String annotLine = String.join("\t", annot);
        UniqueAnnotation ua = new UniqueAnnotation(annotLine);
        assertFalse(ua.isDuplicateRemovalCandidate());
    }

    @Test
    public void testLineNotEligibleForDuplicate3() {
        // False because evidence not IEA
        String [] annot = new String[fields.length];
        System.arraycopy(fields, 0, annot, 0, fields.length);
        annot[12] = "PCS";
        String annotLine = String.join("\t", annot);
        UniqueAnnotation ua = new UniqueAnnotation(annotLine);
        assertFalse(ua.isDuplicateRemovalCandidate());
    }
}
