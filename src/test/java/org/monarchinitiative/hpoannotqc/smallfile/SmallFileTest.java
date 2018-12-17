package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.io.SmallFileParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

class SmallFileTest {
    private static HpoOntology ontology;
    private static SmallFile v2sf=null;


    @BeforeAll
    static void init() throws PhenolException, FileNotFoundException {
        Path hpOboPath = Paths.get("src","test","resources","hp_head.obo");
        String hpOboFile=hpOboPath.toAbsolutePath().toString();
        HpOboParser oboparser = new HpOboParser(new File(hpOboFile));
        ontology = oboparser.parse();
        Path omim123456path = Paths.get("src","test","resources","smallfiles","OMIM-123456.tab");
        String omim123456file = omim123456path.toAbsolutePath().toString();
        SmallFileParser parser = new SmallFileParser(omim123456file,ontology);
        Optional<SmallFile> v2opt = parser.parse();
        v2opt.ifPresent(v2SmallFile -> v2sf = v2SmallFile);
    }

    @Test
    void testParse() {
        assertNotNull(v2sf);
    }


    /**
     * The raw file has three entries -- one for autosomal dominant and two for aculeiform cataract. The frequencies
     * for the latter are 3/4 and 2/5 and so we expect a total combined frequency of (3+2)/(4+5)=5/9
     */
    @Test
    void testMergeFrequencies() {
        List<SmallFileEntry> entrylist= v2sf.getOriginalEntryList();
        assertEquals(3,entrylist.size());
    }

}
