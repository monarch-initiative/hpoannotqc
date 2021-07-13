package org.monarchinitiative.hpoannotqc.analysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NameDateTest {


    @Test
    public void testNameThatShouldNotBeChanged() {
        String originalName = "Mitochondrial complex IV deficiency, nuclear type 22";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(originalName, biocuration);
        Assertions.assertEquals(originalName, nd.getPrettyVersion());
    }


    @Test
    public void testCompoundNameWithRomanNumerals() {
        String original = "#614856 OSTEOGENESIS IMPERFECTA, TYPE XIII; OI13;;OI, TYPE XIII";
        String expected = "Osteogenesis imperfecta, type XIII";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void testType1G() {
        // We do not want to covert 1G to 1g
        String original = "#609115 LIMB-GIRDLE MUSCULAR DYSTROPHY, TYPE 1G; LGMD1G";
        String expected = "Limb-girdle muscular dystrophy, type 1G";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }

    @Test
    void testOminIdWithoutHash () {
        String original = "613623 AGENESIS OF THE CORPUS CALLOSUM AND CONGENITAL LYMPHEDEMA";
        String expected = "Agenesis of the corpus callosum and congenital lymphedema";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void testLongOriginalLabel() {

        String original = "#116860 CEREBRAL CAVERNOUS MALFORMATIONS; CCM;;CAVERNOUS ANGIOMA, FAMILIAL;;CAVERNOUS ANGIOMATOUS MALFORMATIONS; CAM;;CEREBRAL CAPILLARY MALFORMATIONSCEREBRAL CAVERNOUS MALFORMATIONS 1, INCLUDED; CCM1, INCLUDED;;CAVERNOUS MALFORMATIONS OF CNS AND RETINA, INCLUDED;;HYPERKERATOTIC CUTANEOUS CAPILLARY-VENOUS MALFORMATIONS ASSOCIATEDWITH CEREBRAL CAPILLARY MALFORMATIONS, INCLUDED";
        String expected = "Cerebral cavernous malformations";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void testAnotherOne() {
        String original = "#604117 VOHWINKEL SYNDROME, VARIANT FORM;;VOHWINKEL SYNDROME WITH ICHTHYOSIS;;MUTILATING KERATODERMA WITH ICHTHYOSIS;;LORICRIN KERATODERMA";
        String expected = "Vohwinkel syndrome, variant form";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


//    @Test
//    public void testHELIX() {
//        String original = "HELIX syndrome"; // Should not be changed, HELIX is an acronym
//        String biocuration = "HPO:probinson[2021-06-21]";
//        NameDate nd = new NameDate(original, biocuration);
//        Assertions.assertEquals(original, nd.getPrettyVersion());
//    }


    @Test
    public void testEPIDERMOLYSISBULLOSA () {
        String original = "EPIDERMOLYSIS BULLOSA SIMPLEX, AUTOSOMAL RECESSIVE 1";
        String expected = "Epidermolysis bullosa simplex, autosomal recessive 1";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    public void as() {
        String original = "MITOCHONDRIAL DNA DEPLETION SYNDROME 12A (CARDIOMYOPATHIC TYPE), AUTOSOMAL DOMINANT; MTDPS12A";
        String expected = "Mitochondrial DNA depletion syndrome 12A (cardiomyopathic type), autosomal dominant";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


//    @Test void xSyndrome() {
//        // labels such as XYZ syndrome should assume the first worrd is an acronym and not deshout.
//        String original = "MASA SYNDROME";
//        String expected = "MASA syndrome";
//        String biocuration = "HPO:probinson[2021-06-21]";
//        NameDate nd = new NameDate(original, biocuration);
//        Assertions.assertEquals(expected, nd.getPrettyVersion());
//    }
    @Test void fanconi() {
        String original = "FANCONI ANEMIA, COMPLEMENTATION GROUP E";
        String expected = "Fanconi anemia, complementation group E";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void duchenne() {
        String original = "MUSCULAR DYSTROPHY, DUCHENNE TYPE";
        String expected = "Muscular dystrophy, Duchenne type";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }

    @Test
    void MRX101() {
        String original = "#300928 MENTAL RETARDATION, X-LINKED 101; MRX101";
        String expected = "Mental retardation, X-linked 101";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void Amelogenesis() {
        String original = "AMELOGENESIS IMPERFECTA, TYPE IH";
        String expected = "Amelogenesis imperfecta, type IH";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void factor() {
        String original = "FACTOR XIII, B SUBUNIT, DEFICIENCY OF";
        String expected = "Factor XIII, B subunit, deficiency of";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void percentage() {
        String original = "%605735 BLEEDING DISORDER, PLATELET-TYPE, 12; BDPLT12;;PROSTAGLANDIN-ENDOPEROXIDE SYNTHASE 1 DEFICIENCY, PLATELET;;";
        String expected = "Bleeding disorder, platelet-type, 12";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }

    @Test
    void Corpuscallosum() {
        String original = "%217980 CORPUS CALLOSUM, AGENESIS OF, WITH FACIAL ANOMALIES AND ROBIN SEQUENCE;;TORIELLO-CAREY SYNDROME";
        String expected = "Corpus callosum, agenesis of, with facial anomalies and Robin sequence";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void Fallot() {
        String original = "HYPERTELORISM AND TETRALOGY OF FALLOT";
        String expected = "Hypertelorism and tetralogy of Fallot";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }


    @Test
    void testACTH() {
        String original = "PITUITARY ADENOMA 4, ACTH-SECRETING";
        String expected = "Pituitary adenoma 4, ACTH-secreting";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }
    @Test
    void testDBA() {
        String original = "DIAMOND-BLACKFAN ANEMIA 20; DBA20";
        String expected = "Diamond-Blackfan anemia 20";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }



    //


    @Test
    void testMRXS12() {
        String original = "%309545 MENTAL RETARDATION, X-LINKED, SYNDROMIC 12; MRXS12";
        String expected = "Mental retardation, X-linked, syndromic 12";
        String biocuration = "HPO:probinson[2021-06-21]";
        NameDate nd = new NameDate(original, biocuration);
        Assertions.assertEquals(expected, nd.getPrettyVersion());
    }




}
