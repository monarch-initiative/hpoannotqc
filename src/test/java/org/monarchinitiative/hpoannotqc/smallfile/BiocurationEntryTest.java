package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BiocurationEntryTest {



    @Test
    public void testSingleBiocurationEntry() {
        String field = "HPO:probinson[2013-01-09]";
        List<BiocurationEntry> belist = BiocurationEntry.getBiocurationList(field);
        assertEquals(1,belist.size());
        BiocurationEntry entry = belist.get(0);
        assertEquals("HPO:probinson",entry.getBiocurator());
        assertEquals("2013-01-09",entry.getDate());

    }

    @Test
    public void testDoubleBiocurationEntry() {
        String field = "HPO:probinson[2013-01-09];HPO:skoehler[2017-01-19]";
        List<BiocurationEntry> belist = BiocurationEntry.getBiocurationList(field);
        assertEquals(2,belist.size());
    }

    /* The entry is missing the final bracket. The expected behavior is that we get an empty list. */
    @Test
    public void testMalformedBiocurationEntry() {
        String field = "HPO:probinson[2013-01-09";
        List<BiocurationEntry> belist = BiocurationEntry.getBiocurationList(field);
        assertEquals(0,belist.size());
    }



}
