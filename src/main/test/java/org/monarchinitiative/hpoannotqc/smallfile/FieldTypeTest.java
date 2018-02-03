package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.monarchinitiative.hpoannotqc.smallfile.FieldType.ENTITY_ID;

public class FieldTypeTest {

    /** Sanity test. The field type constants also have String names. Note that the built in function
     * {@code name} returns "ENTITY_ID", and our custom functionh getName returns the String constant.
     */
    @Test
    public void testGetName() {
        String expected= "Entity ID";
        assertEquals(expected,ENTITY_ID.getName());
    }

}
