package org.monarchinitiative.hpoannotqc.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class OrphanetXML2HpoDiseaseModelParserTest {

    static private OrphanetXML2HpoDiseaseModelParser parser;

    @BeforeAll
    private static void init() throws PhenolException, FileNotFoundException {
        ClassLoader classLoader = OrphanetXML2HpoDiseaseModelParserTest.class.getClassLoader();
        String orphaXMLpath =Objects.requireNonNull(classLoader.getResource("en_product4_HPO.small.xml")).getFile();
        Path hpOboPath = Paths.get("src","test","resources","hp_head.obo");
        String hpOboFile=hpOboPath.toAbsolutePath().toString();
        HpOboParser oboparser = new HpOboParser(new File(hpOboFile));
        HpoOntology ontology = oboparser.parse();
        parser = new OrphanetXML2HpoDiseaseModelParser(orphaXMLpath,ontology);
    }

    @Test
    void testNotNull() {
        assertNotNull(parser);
    }
}
