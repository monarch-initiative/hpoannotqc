package org.monarchinitiative.hpoannotqc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BigFileWriterTest {



    private static V2SmallFileEntry entry;


    @BeforeClass
    public static void init() {
        // Make a typical entry. All other fields are emtpy.
        String diseaseID="OMIM:154700";
        String diseaseName="MARFAN SYNDROME";
        TermId hpoId= ImmutableTermId.constructWithPrefix("HP:0004872");
        String hpoName="Incisional hernia";
        String evidenceCode="IEA";
        String pub="OMIM:154700";
        String assignedBy="HPO:skoehler";
        String dateCreated="2015-07-26";
        V2SmallFileEntry.Builder builder=new V2SmallFileEntry.Builder(diseaseID,diseaseName,hpoId,hpoName,evidenceCode,pub,assignedBy,dateCreated);
        entry = builder.build();
    }


    @Test
    public void testV1line() {
        String [] fields = {
                "OMIM", //DB
                "154700",//DB_Object_ID
                 "MARFAN SYNDROME", // DB_Name
                "",//Qualifier
                "HP:0004872", // HPO ID,
                "OMIM:154700",//DB:Reference
                "IEA", // Evidence code
                "HP:0040283",//onset modifier
                "", // frequency modifier
                "", // with
                "O",// Aspect
                "", //synonyma
                "2015-07-26", // date
                "HPO:skoehler" // assigned by
        };
        String expected= Arrays.stream(fields).collect(Collectors.joining("\t"));
        assertEquals(expected,entry.getRowV1());
    }

}
