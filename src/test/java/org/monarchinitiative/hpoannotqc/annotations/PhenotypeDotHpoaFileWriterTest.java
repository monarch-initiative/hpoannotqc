package org.monarchinitiative.hpoannotqc.annotations;


import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.annotations.util.HpoBigfileUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PhenotypeDotHpoaFileWriterTest extends TestBase{

    /** Note that the header should have a #
     */
    @Test
    public void testHpoaHeader() {
        String expected="database_id\tdisease_name\tqualifier\thpo_id\treference\tevidence\tonset\tfrequency\tsex\tmodifier\taspect\tbiocuration";
        HpoBigfileUtil bfutil = new HpoBigfileUtil(ontology);
        assertEquals(expected, bfutil.getHeaderLine());
        assertEquals(12, bfutil.getHeaderLine().split("\t").length);
    }

}
