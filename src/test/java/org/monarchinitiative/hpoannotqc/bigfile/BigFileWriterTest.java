package org.monarchinitiative.hpoannotqc.bigfile;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileEntry;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class BigFileWriterTest {



    private static SmallFileEntry entry;
    private static HpoOntology ontology;

    @BeforeAll
    static void init() throws PhenolException, FileNotFoundException {
       // ontology = mock(HpoOntology.class);

        // set up ontology
        ClassLoader classLoader = BigFileWriterTest.class.getClassLoader();
        String hpOboPath =classLoader.getResource("hp_head.obo").getFile();
        Objects.requireNonNull(hpOboPath);
        HpOboParser oboparser = new HpOboParser(new File(hpOboPath));
        ontology = oboparser.parse();
//        // Make a typical entry. All other fields are emtpy.
        String diseaseID="OMIM:123456";
        String diseaseName="MADE-UP SYNDROME";
        TermId hpoId= TermId.of("HP:0000528");
        String hpoName="Anophthalmia";
        String evidenceCode="IEA";
        String pub="OMIM:154700";
        String biocuration="HPO:skoehler[2015-07-26]";
        String onsetModifier="HP:0040283";
        SmallFileEntry.Builder builder=new SmallFileEntry.Builder(diseaseID,diseaseName,hpoId,hpoName,evidenceCode,pub,biocuration).ageOfOnsetId(onsetModifier);
        entry = builder.build();
    }



    /**
     * Test emitting a line of the V2 (2018-?) big file from a V2 small file line.
     */
    @Test
    void testV2line() throws HPOException {
        String [] bigFileFields = {
                "OMIM:123456",//DiseaseID
                "MADE-UP SYNDROME", // Name
                "",//Qualifier
                "HP:0000528", // HPO_ID,
                "OMIM:154700",//DB_Reference
                "IEA", // Evidence_Code
                "HP:0040283",//Onset
                "", // Frequency
                "", // Sex
                "",//Modifier
                "P",// Aspect
                "HPO:skoehler[2015-07-26]", // biocuration

        };
        String expected= String.join("\t", bigFileFields);
        List<SmallFile> emptyList = ImmutableList.of(); // needed for testing.
        V2BigFile v1b = new V2BigFile(ontology, emptyList);
        String line = v1b.transformEntry2BigFileLineV2(entry);
        assertEquals(expected,line);
    }


    @Test
    void testV2Header() {
        String expected="#DatabaseID\tDiseaseName\tQualifier\tHPO_ID\tReference\tEvidence\tOnset\tFrequency\tSex\tModifier\tAspect\tBiocuration";
        assertEquals(expected,V2BigFile.getHeaderV2());
    }

}