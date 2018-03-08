package org.monarchinitiative.hpoannotqc.smallfile;


import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.phenol.formats.hpo.HpoFrequencyTermIds;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.monarchinitiative.hpoannotqc.smallfile.DateUtil.convertToCanonicalDateFormat;


public class OldSmallFileEntryTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /*
     private HpoOntology ontology=null;
        private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
        private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;
     */
    @BeforeClass
    public static void init() {
        //ClassLoader classLoader = OldSmallFileEntryTest.class.getClassLoader();
        //String hpOboPath = classLoader.getResource("hp.obo").getFile();
        //File file = ResourceUtils.getFile(OldSmallFileEntryTest.getResource("/some_file.txt"));
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

    private void printTempAnnotationFileToShell(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        System.out.println("#############  Annotation file ###############");
        System.out.println(path);
        while ((line = br.readLine()) != null) {
            System.out.println("\"" + line + "\"");
        }
        br.close();
        System.out.println("#############  End ###############");
    }


    @Test
    public void testDiseaseName() throws IOException {
        File tempFile = testFolder.newFile("tempfile.tab");
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:220220").
                diseaseName("220220 DANDY-WALKER MALFORMATION WITH POSTAXIAL POLYDACTYLY;;DWM WITH POSTAXIAL POLYDACTYLY;;PIERQUIN SYNDROME");
        //System.out.println("BB" + builder.build());
        annots.add(builder.build());
        writeTmpFile(annots, tempFile);
        //printTempAnnotationFileToShell(tempFile.getAbsolutePath());
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
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


    /* This is the modification field. In this case, we want to add the HPO Modifier "Episodic" (HP:0025303).
    MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC
    This comes from the line
    OMIM-104290.tab:OMIM:104290	#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1					HP:0002445	Tetraplegia			IEA	IEA
    MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC	OMIM:104290	HPO:skoehler	06.06.2013
    */
    @Test
    public void testModification() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:104290").
                diseaseName("#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1").
                hpoId("HP:0002445").
                hpoName("Tetraplegia").
                description("MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC");
        OldSmallFile osm = makeOldSmallFile("tempfile2.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("OMIM:104290", entry.getDiseaseID());
        assertEquals("#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1", entry.getDiseaseName());
        assertEquals("HP:0002445", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Tetraplegia", entry.getPhenotypeName());
        assertEquals("TAS", entry.getEvidenceID()); // TAS because of OMIM-CS
        assertEquals("HP:0025303", entry.getModifierString());// id for modifier episodic
        assertEquals("OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC", entry.getDescription());

    }


    /* This entry has the free test "Mild" in the  description field. This should be transfered to the Modifier field
    OMIM:614255	#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9				KIF1A	HP:0006855	Cerebellar vermis atrophy			IEA	IEA	1/1					MildOMIM:614255	HPO:probinson	Aug 10, 2013
    */
    @Test
    public void testModification2() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:614255").
                diseaseName("#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9").
                hpoId("HP:0006855").
                hpoName("Cerebellar vermis atrophy").
                description("Mild");
        OldSmallFile osf = makeOldSmallFile("tempfile2.tab",builder);
        List<OldSmallFileEntry> entries = osf.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("OMIM:614255", entry.getDiseaseID());
        assertEquals("#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9", entry.getDiseaseName());
        assertEquals("HP:0006855", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Cerebellar vermis atrophy", entry.getPhenotypeName());
        assertEquals("IEA", entry.getEvidenceID());
        assertEquals("HP:0012825", entry.getModifierString()); // code for Mild
        assertEquals("", entry.getDescription());
    }

    /**
     * OMIM:608154	%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES
     * HP:0000938	Osteopenia			IEA	IEA
     * MODIFIER:PROGRESSIVE;OMIM-CS:SKELETAL > PROGRESSIVE OSTEOPENIA	OMIM:608154	HPO:skoehler	10.06.2013
     * We should pull out the modifier, Progressive (HP:0003676)
     */
    @Test
    public void testModification3() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0000938").
                hpoName("Osteopenia").
                description("MODIFIER:PROGRESSIVE;OMIM-CS:SKELETAL > PROGRESSIVE OSTEOPENIA");
        OldSmallFile osm = makeOldSmallFile("tempfile3.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES", entry.getDiseaseName());
        assertEquals("HP:0000938", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Osteopenia", entry.getPhenotypeName());
        assertEquals("TAS", entry.getEvidenceID()); // TAS because of OMIM-CS
        assertEquals("HP:0003676", entry.getModifierString()); // code for Progressive
        assertEquals("OMIM-CS:SKELETAL > PROGRESSIVE OSTEOPENIA", entry.getDescription());
    }


    /**
     * Test whether we assign this Free Text description the Hpo Frequency term Very rare
     * OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (RARE). Note that we map "RARE" to the HPO Frequency term Occasional!
     */
    @Test
    public void testModification4() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0000486").
                hpoName("Strabismus").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (RARE)");
        OldSmallFile osm = makeOldSmallFile("tempfile4.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES", entry.getDiseaseName());
        assertEquals("HP:0000486", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Strabismus", entry.getPhenotypeName());
        assertEquals("TAS", entry.getEvidenceID());
        assertEquals(HpoFrequencyTermIds.OCCASIONAL,entry.getFrequencyId()  );
        assertEquals("OMIM:608154",entry.getPub());
        assertEquals("OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (RARE)", entry.getDescription());

    }



    /**
     * Test whether we assign this Free Text description the Hpo Frequency term Occasional
     * OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (IN SOME PATIENTS)
     */
    @Test
    public void testModification5() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0000486").
                hpoName("Strabismus").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (IN SOME PATIENTS)");
        OldSmallFile osm = makeOldSmallFile("tempfile5.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES", entry.getDiseaseName());
        assertEquals("HP:0000486", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Strabismus", entry.getPhenotypeName());
        assertEquals("TAS", entry.getEvidenceID());
        TermId occasionalTermId = ImmutableTermId.constructWithPrefix("HP:0040283");// HP:0040283 is Occasional
        assertEquals(occasionalTermId,entry.getFrequencyId()  );
        assertEquals("OMIM:608154",entry.getPub());
        assertEquals("OMIM-CS:HEAD AND NECK_EYES > STRABISMUS (IN SOME PATIENTS)", entry.getDescription());
    }

    /** Test whether we assign this Free Text description the Hpo Frequency term Very rare and the modifer mild
     * OMIM-CS:SKELETAL_SPINE > SCOLIOSIS, MILD (RARE)
     * Note that we map "RARE" to the HPO Frequency term Occasional
     */
    @Test
    public void testModification6() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0000486").
                hpoName("Strabismus").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:SKELETAL_SPINE > SCOLIOSIS, MILD (RARE)");
        OldSmallFile osm = makeOldSmallFile("tempfile6.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals(HpoFrequencyTermIds.OCCASIONAL,entry.getFrequencyId()  );
        assertEquals("TAS", entry.getEvidenceID());
    }


    /**
     * id: HP:0006315
     name: Single median maxillary incisor
     alt_id: HP:0001568
     alt_id: HP:0001573
     alt_id: HP:0006356
     Check that an annotation with an alt_id gets updated to an annotation with the current primary id
     * Note that we map "RARE" to the HPO Frequency term Occasional
     * @throws IOException
     */
    @Test
    public void updateAltIdAnnotation1() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0001568"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Single median maxillary incisor").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:SKELETAL_SPINE > SCOLIOSIS, MILD (RARE)");
        OldSmallFile osm = makeOldSmallFile("tempfile6.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals(HpoFrequencyTermIds.OCCASIONAL,entry.getFrequencyId()  );
        assertEquals("TAS", entry.getEvidenceID());
        TermId primaryId= ImmutableTermId.constructWithPrefix("HP:0006315");
        assertEquals(primaryId,entry.getPhenotypeId());
    }

    /**
     *  [Term]
     id: HP:0006316
     name: Irregularly spaced teeth
     alt_id: HP:0009081
     Note we map RARE to the HPO Frequency term Occasional.
     */
    @Test
    public void updateAltIdAnnotation2() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0009081"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Irregularly spaced teeth").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:SKELETAL_SPINE > SCOLIOSIS, MILD (RARE)");
        OldSmallFile osm = makeOldSmallFile("tempfile6.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);

        assertEquals("OMIM:608154", entry.getDiseaseID());
        assertEquals(HpoFrequencyTermIds.OCCASIONAL,entry.getFrequencyId()  );
        assertEquals("TAS", entry.getEvidenceID());
        TermId primaryId= ImmutableTermId.constructWithPrefix("HP:0006316");
        assertEquals(primaryId,entry.getPhenotypeId());
    }


    /** Check that a missing date entry gets replaced with today's date */
    @Test
    public void testMissingDate() throws IOException {
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:608154").
                diseaseName("%608154 LIPODYSTROPHY, GENERALIZED, WITH MENTAL RETARDATION, DEAFNESS, SHORTSTATURE, AND SLENDER BONES").
                hpoId("HP:0009081"). // out of date alt_id, should be replaced with the primary id.
                hpoName("Irregularly spaced teeth").
                evidence("IEA").
                pub("OMIM:608154").
                description("OMIM-CS:SKELETAL_SPINE > SCOLIOSIS, MILD (RARE)").
                dateCreated("");
        OldSmallFile osm = makeOldSmallFile("tempfile9.tab",builder);
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        // The date should be replaced with today's date. Note--theoretically, this test could fail if run JUST at midnight, so beware :-0!

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        Date date = new Date();
        String expectedDate= dateFormat.format(date);
        assertEquals(expectedDate,entry.getDateCreated());

    }





}