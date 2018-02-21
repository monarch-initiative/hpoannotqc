package org.monarchinitiative.hpoannotqc;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetXML2HpoDiseaseModelParser;

import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;

/**
 * This class is here just for convenience to develop the Orphanet XML parser but we will get rid of or adapt it
 * once everything is working.
 */
public class TestOrphanet {

    String xmlpath;

    @Before
    public void init() {
        Path resourceDirectory = Paths.get("data","en_product4_HPO.xml");
        this.xmlpath=resourceDirectory.toAbsolutePath().toString();
    }


    @Test
    public void testPath() {
        assertNotNull(xmlpath);
        System.out.println(xmlpath);
    }

    @Test
    public void parseXML() throws Exception {
        OrphanetXML2HpoDiseaseModelParser parser = new OrphanetXML2HpoDiseaseModelParser(xmlpath);
    }


}
