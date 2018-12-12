package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.Term;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * A class to encapsulate the data related to a V2 (2018 and onwards) "big file" that is called
 * {@code phenotype.hpoa}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson </a>
 */
class V2BigFile {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final V2LineQualityController v2qualityController;
    private final static String EMPTY_STRING="";
    private static final TermId phenotypeRoot= TermId.of("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =TermId.of("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =TermId.of("HP:0031797");
    private static final TermId CLINICAL_MODIFIER_ID =TermId.of("HP:0012823");
    /** These are the objects that represent the diseases contained in the V2 small files. */
    private final List<V2SmallFile> v2SmallFileList;


    /**
     * @param ont Reference to the HPO Ontology
     * @param v2SmallFiles List of V2 small files to be converted to the bigfile.
     */
    V2BigFile(HpoOntology ont, List<V2SmallFile> v2SmallFiles) {
        this.ontology=ont;
        v2SmallFileList=v2SmallFiles;
        v2qualityController=new V2LineQualityController(this.ontology);
    }





    void outputBigFileV2(BufferedWriter writer) throws IOException {
        int n = 0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        writer.write(getHeaderV2() + "\n");
        for (V2SmallFile v2 : v2SmallFileList) {
            List<V2SmallFileEntry> entryList = v2.getOriginalEntryList();
            for (V2SmallFileEntry entry : entryList) {
                v2qc.checkV2entry(entry);
                try {
                    String bigfileLine = transformEntry2BigFileLineV2(entry);
                    writer.write(bigfileLine + "\n");
                } catch (HPOException e) {
                    e.printStackTrace();
                }
                n++;
            }
        }
        System.out.println("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoLog();
    }
    /** Construct one line for the V1 big file that was in use from 2009-2018. */
    String transformEntry2BigFileLineV2(V2SmallFileEntry entry) throws HPOException{

        String [] elems = {
                entry.getDiseaseID(), //DB_Object_ID
                entry.getDiseaseName(), // DB_Name
                entry.getNegation(), // Qualifier
                entry.getPhenotypeId().getValue(), // HPO_ID
                entry.getPublication(), // DB_Reference
                entry.getEvidenceCode(), // Evidence_Code
                entry.getAgeOfOnsetId()!=null?entry.getAgeOfOnsetId():EMPTY_STRING, // Onset
                entry.getFrequencyModifier()!=null?entry.getFrequencyModifier():EMPTY_STRING, // Frequency
                entry.getSex(), // Sex
                entry.getModifier(), // Modifier
                getAspectV2(entry.getPhenotypeId()), // Aspect
                entry.getBiocuration() // Biocuration
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    private String getAspectV2(TermId tid) throws HPOException {
        Term term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getValue());
            return "?";
        }
        TermId primaryTid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, primaryTid, phenotypeRoot)) {
            v2qualityController.incrementGoodAspect();//
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, primaryTid, INHERITANCE_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "I";
        } else if (existsPath(ontology, primaryTid, CLINICAL_COURSE_ID)) {
            v2qualityController.incrementGoodAspect();
            return "C";
        } else if (existsPath(ontology,primaryTid,CLINICAL_MODIFIER_ID)) {
            v2qualityController.incrementGoodAspect();
            return "M";
        } else {
            this.v2qualityController.incrementBadAspect();
           throw new HPOException("Could not determine aspect of TermId "+tid.getValue());
        }
    }
    /**
     * @return Header line for the V2 big file.
     */
    static String getHeaderV2() {
        String []fields={"DatabaseID",
                "DiseaseName",
                "Qualifier",
                "HPO_ID",
                "Reference",
                "Evidence",
                "Onset",
                "Frequency",
                "Sex",
                "Modifier",
                "Aspect",
                "Biocuration"};
        return "#" + Arrays.stream(fields).collect(Collectors.joining("\t"));
    }


}
