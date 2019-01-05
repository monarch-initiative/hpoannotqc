package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.smallfile.HpoAnnotationFile;
import org.monarchinitiative.hpoannotqc.smallfile.HpoAnnotationFileEntry;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.Term;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * A class to encapsulate the data related to the "big file" that is called
 * {@code phenotype.hpoa}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson </a>
 */
class PhenotypeDotHpoaFile {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final static String EMPTY_STRING="";
    private static final TermId phenotypeRoot= TermId.of("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =TermId.of("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =TermId.of("HP:0031797");
    private static final TermId CLINICAL_MODIFIER_ID =TermId.of("HP:0012823");
    /** These are the objects that represent the diseases contained in the V2 small files. */
    private final List<HpoAnnotationFile> hpoAnnotationFileList;


    /**
     * @param ont Reference to the HPO Ontology
     * @param smallFiles List of HPO Annotation files to be converted to the bigfile.
     */
    PhenotypeDotHpoaFile(HpoOntology ont, List<HpoAnnotationFile> smallFiles) {
        this.ontology=ont;
        hpoAnnotationFileList =smallFiles;
    }


    /**
     * This method outputs lines for each of the HPO-project small files into the big file
     * @param writer Handle for the {@code phenotype.hpoa} file.
     * @throws IOException if there is a difficulty with the {@code phenotype.hpoa} file.
     */
    void outputBigFile(BufferedWriter writer) throws IOException {
        int n = 0;
        writer.write(getHeaderLine() + "\n");
        for (HpoAnnotationFile smallFile : hpoAnnotationFileList) {
            List<HpoAnnotationFileEntry> entryList = smallFile.getEntryList();
            for (HpoAnnotationFileEntry entry : entryList) {
                try {
                    String bigfileLine = transformEntry2BigFileLine(entry);
                    writer.write(bigfileLine + "\n");
                } catch (HPOException e) {
                    System.err.println("Error encountered with entry " + entry.toString());
                    e.printStackTrace();
                }
                n++;
            }
        }
        System.out.println("We output a total of " + n + " big file lines");
    }

    /**
     * Transform one line from a Small File (represented as a {@link HpoAnnotationFileEntry} object)
     * into one line of the Big File.
     * @param entry Representing a line from a Small File
     * @return A string that will be one line of the Big file
     * @throws HPOException if the Aspect of the line cannot be determined
     */
    String transformEntry2BigFileLine(HpoAnnotationFileEntry entry) throws HPOException{

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
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, primaryTid, INHERITANCE_TERM_ID)) {
            return "I";
        } else if (existsPath(ontology, primaryTid, CLINICAL_COURSE_ID)) {
            return "C";
        } else if (existsPath(ontology,primaryTid,CLINICAL_MODIFIER_ID)) {
            return "M";
        } else {
           throw new HPOException("Could not determine aspect of TermId "+tid.getValue() );
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
