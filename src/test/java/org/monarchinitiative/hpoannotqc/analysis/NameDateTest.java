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

}
