package org.monarchinitiative.hpoannotqc.smallfile;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertEquals;

class SmallFileEntryTest {

    private static HpoOntology ontology;


    @BeforeAll
    static void init() throws PhenolException, FileNotFoundException  {
        Path resourceDirectory = Paths.get("src","test","resources","hp_head.obo");
        String hpOboPath=resourceDirectory.toAbsolutePath().toString();
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
    }

    @Test
    void testEvidenceCodeNotEmpty() {
       SmallFileEntryQC qc = new SmallFileEntryQC(ontology);
        String diseaseId="OMIM:216300";
        String diseasename="CLEFT PALATE, DEAFNESS, AND OLIGODONTIA";
        TermId phenoID= TermId.of("HP:0000007");
        String phenoName="Autosomal recessive inheritance";
        String evidence="IEA";
        String pub="OMIM:216300";
        String ab="HPO:IEA[2009-02-17]";

        SmallFileEntry.Builder builder = new SmallFileEntry.Builder( diseaseId,
                 diseasename,
                 phenoID,
                 phenoName,
                 evidence,
                 pub,
                 ab
                 );
        SmallFileEntry entry=builder.build();
        qc.checkSmallFileEntry(entry);
        assertEquals("IEA",entry.getEvidenceCode());
    }

    @Test
    void testEvidenceCodeValid() {
        String[] fields={
                "OMIM:123456",
                "MADE-UP SYNDROME",
                "HP:0000528",
                "Anophthalmia",
                "",
                "",
                "76.3%",
                "FEMALE",
                "",
                "",
                "",
                "PMID:9843983",
                "PC",
                "HPO:probinson[2013-01-09]"};
        String line = String.join("\t",fields);//Arrays.stream(fields).collect(Collectors.joining("\t"));
        Assertions.assertThrows(PhenolException.class, () -> {
            SmallFileEntry entry = SmallFileEntry.fromLine(line,ontology);
        });
    }



}
