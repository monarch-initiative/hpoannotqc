package org.monarchinitiative.hpoannotqc.smallfile;

import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertEquals;

public class V2SmallFileEntryTest {

    private static HpoOntology ontology;


    @BeforeClass
    public static void init() throws PhenolException{
        Path resourceDirectory = Paths.get("src","test","resources","hp.obo");
        String hpOboPath=resourceDirectory.toAbsolutePath().toString();
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
    }

    @Test
    public void testEvidenceCodeNotEmpty() {
       V2LineQualityController qc = new V2LineQualityController(ontology);
        String diseaseId="OMIM:216300";
        String diseasename="CLEFT PALATE, DEAFNESS, AND OLIGODONTIA";
        TermId phenoID= TermId.constructWithPrefix("HP:0000007");
        String phenoName="Autosomal recessive inheritance";
        String evidence="IEA";
        String pub="OMIM:216300";
        String ab="HPO:IEA[2009-02-17]";

        V2SmallFileEntry.Builder builder = new V2SmallFileEntry.Builder( diseaseId,
                 diseasename,
                 phenoID,
                 phenoName,
                 evidence,
                 pub,
                 ab
                 );
        V2SmallFileEntry entry=builder.build();
        qc.checkV2entry(entry);
        assertEquals("IEA",entry.getEvidenceCode());
    }
}
