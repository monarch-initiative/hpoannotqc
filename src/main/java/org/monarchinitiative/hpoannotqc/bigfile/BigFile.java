package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileEntry;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileEntryQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFile;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.Term;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * A class to encapsulate the data related to a V2 (2018 and onwards) "big file" that is called
 * {@code phenotype.hpoa}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson </a>
 */
class BigFile {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final SmallFileEntryQualityController v2qualityController;
    private final static String EMPTY_STRING="";
    private static final TermId phenotypeRoot= TermId.of("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =TermId.of("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =TermId.of("HP:0031797");
    private static final TermId CLINICAL_MODIFIER_ID =TermId.of("HP:0012823");
    /** These are the objects that represent the diseases contained in the V2 small files. */
    private final List<SmallFile> v2SmallFileList;


    /**
     * @param ont Reference to the HPO Ontology
     * @param v2SmallFiles List of V2 small files to be converted to the bigfile.
     */
    BigFile(HpoOntology ont, List<SmallFile> v2SmallFiles) {
        this.ontology=ont;
        v2SmallFileList=v2SmallFiles;
        v2qualityController=new SmallFileEntryQualityController(this.ontology);
    }





    void outputBigFile(BufferedWriter writer) throws IOException {
        int n = 0;
        SmallFileEntryQualityController v2qc = new SmallFileEntryQualityController(this.ontology);
        writer.write(getHeaderLine() + "\n");
        for (SmallFile v2 : v2SmallFileList) {
            List<SmallFileEntry> entryList = v2.getOriginalEntryList();
            for (SmallFileEntry entry : entryList) {
                v2qc.checkV2entry(entry);
                try {
                    String bigfileLine = transformEntry2BigFileLine(entry);
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

    /**
     * Transform one line from a Small File (represented as a {@link SmallFileEntry} object)
     * into one line of the Big File.
     * @param entry Representing a line from a Small File
     * @return A string that will be one line of the Big file
     * @throws HPOException if the Aspect of the line cannot be determined
     */
    String transformEntry2BigFileLine(SmallFileEntry entry) throws HPOException{

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
                getAspect(entry.getPhenotypeId()), // Aspect
                entry.getBiocuration() // Biocuration
        };
        return String.join("\t",elems);
    }

    /**
     * This method calculates the aspect of a term used for an annotation.
     * The aspect depends on the location of the term in the HPO hierarchy,
     * for instance it is "I" if the term is in the inheritance subhierarchy and it
     * is "P" if the term is in the phenotype subhierarchy.
     * @param tid The term id of an HPO Term
     * @return The Aspect (P,I,C,M) of the term.
     * @throws HPOException if the term cannot be identified as either P,C,I, or M.
     */
    private String getAspect(TermId tid) throws HPOException {
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
     * @return Header line for the big file (indicating column names for the data).
     */
    static String getHeaderLine() {
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
        return String.join("\t",fields);
    }


}
