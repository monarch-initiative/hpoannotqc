package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class UpdateLabelTest {

    /*
    OMIM:602535	#602535 MARSHALL-SMITH SYNDROME; MRSHSS					HP:0010759	Premaxillary Prominence			IEA	IEA						OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA	OMIM:602535	HPO:skoehler	03.12.2012
    ->
    OMIM:602535	#602535 MARSHALL-SMITH SYNDROME; MRSHSS	HP:0010759	Prominence of the premaxilla			TAS						OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA	OMIM:602535	HPO:skoehler	2012-12-03
     */

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /*
     private HpoOntology ontology=null;
        private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
        private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;
     */
    @BeforeClass
    public static void init() {
        Path resourceDirectory = Paths.get("src","test","resources","hp.obo");
        String hpOboPath=resourceDirectory.toAbsolutePath().toString();
        try {
            HpoOntologyParser parser = new HpoOntologyParser(hpOboPath);
            HpoOntology ontology = parser.getOntology();
            OldSmallFileEntry.setOntology(ontology);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void writeTmpFile(List<String> annotations, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (String annot : annotations) {
            bw.write(annot + "\n");
        }
        bw.close();
    }
    private OldSmallFile makeOldSmallFile(String filename,
                                          SmallFileBuilder builder) throws IOException {
        File tempFile = testFolder.newFile("tempfile2.tab");
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        String oldSmallFileLine = builder.build();
        annots.add(oldSmallFileLine);
        writeTmpFile(annots, tempFile);
        return new OldSmallFile(tempFile.getAbsolutePath());
    }

    /* The label Premaxillary Prominence should be updated to Prominence of the premaxilla */
    @Test
    public void updateLabelTest() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:602535").
                diseaseName("#602535 MARSHALL-SMITH SYNDROME; MRSHSS").
                hpoId("HP:0010759"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Premaxillary Prominence").
                evidence("IEA").
                pub("OMIM:602535").
                description("OMIM-CS:HEAD AND NECK_FACE > PROMINENT PREMAXILLA");
        OldSmallFile osm = makeOldSmallFile("tempfile42.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:602535", entry.getDiseaseID());
        assertEquals("TAS", entry.getEvidenceID());
        TermId primaryId= ImmutableTermId.constructWithPrefix("HP:0010759");
        assertEquals(primaryId,entry.getPhenotypeId());
        String updatedLabel="Prominence of the premaxilla";
        assertEquals(updatedLabel,entry.getPhenotypeName());

    }

}
