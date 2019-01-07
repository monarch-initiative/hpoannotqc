package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * This class helps transform the Orphanet {@code en_product6.xml} file, which contains Orphanet's HPO annotations of
 * selected diseases, into (part of) the {@code phenotype.hpoa} file (the "big file").
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
class Orphanet2PhenotypeDotHpoaFile {
    private static final Logger logger = LogManager.getLogger();
    private final static String ORPHANET_DB ="ORPHA";
    private final static String EMPTY_STRING="";
    private final static String ORPHA_EVIDENCE_CODE="TAS";
    private final static String NO_ONSET_CODE_AVAILABLE=EMPTY_STRING;
    private final static String ASSIGNED_BY="ORPHA:orphadata";
    private static final TermId phenotypeRoot= TermId.of("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =TermId.of("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =TermId.of("HP:0031797");
    private static final TermId CLINICAL_MODIFIER_ID =TermId.of("HP:0012823");
    private final HpoOntology ontology;


    private final List<OrphanetDisorder> orphanetDisorders;
    private final  BufferedWriter writer;

    /**
     * Constructor for the class that helps transform a list of Orphanet-XML file derived disease models
     * (see {@link OrphanetDisorder} into
     * the {@code phenotype.hpoa} file
     * @param orphDisorders  a list of Orphanet-XML file derived disease models
     * @param bw A Buffered writer
     * @param ont reference to the HPO Ontology
     */
    public Orphanet2PhenotypeDotHpoaFile(List<OrphanetDisorder> orphDisorders, BufferedWriter bw, HpoOntology ont){
        this.orphanetDisorders=orphDisorders;
        this.writer=bw;
        this.ontology=ont;
    }


    void writeOrphanetDiseaseData() {
        int n=0;
        System.out.println("About to write Orphanet diseases. n="+ orphanetDisorders.size());
        try {
            for (OrphanetDisorder disorder : orphanetDisorders) {
                List<TermId> hpoIds = disorder.getHpoIds();
                for (TermId tid: hpoIds) {
                    try {
                        String line = transformOrphanetEntry2BigFileLineV2(disorder, tid);
                        writer.write(line + "\n");
                        n++;
                    } catch (HPOException hpoe) {
                        logger.error(String.format("Could not make annotation for term %s of disorder %s ",tid.getValue(),disorder.getName()));
                        logger.error("Will skip this line: "+hpoe.getMessage());
                        // just go to next one.
                    }
                }
            }
            System.out.println(String.format("We have output a total of %d Orphanet annotations from %d diseases",n,orphanetDisorders.size()));
        } catch (IOException e) {
            logger.fatal(e);
            System.err.println("Exception trying to write Orphanet");
            e.printStackTrace();
            logger.fatal("Could not write orphanet disorder ", e);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }

    }

    /**
     * This is identical to the analogous function in {@link PhenotypeDotHpoaFile} except that it does not use
     * the Q/C function of that function.
     * @param tid An HPO term id for which we want to get the aspect
     * @return A one-letter String representing the aspect (P,I,C,M).
     */
    private String getAspect(TermId tid) throws HPOException {
        Term term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getValue());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            return "I";
        } else if (existsPath(ontology, tid, CLINICAL_COURSE_ID)) {
            return "C";
        } else if (existsPath(ontology,tid,CLINICAL_MODIFIER_ID)) {
            return "M";
        } else {
            String label=term.getName();
            String msg=String.format("Could not get aspect for term %s [%s]",label,tid.getValue());
            throw new HPOException(msg);
        }
    }



    /** Output a line of an Orphanet entry to the V2 big file, phenotype.hpoa. */
    private String transformOrphanetEntry2BigFileLineV2(OrphanetDisorder entry, TermId hpoId) throws HPOException {
        String diseaseID=String.format("%s:%d", ORPHANET_DB,entry.getOrphaNumber());
        String diseaseId = String.format("%s:%d",ORPHANET_DB,entry.getOrphaNumber());
        String biocuration = String.format("%s[%s]",ASSIGNED_BY,getTodaysDate());
        String [] elems = {
                diseaseId, // diseaseId, e.g., ORPHA:123
                entry.getName(), // Name
                EMPTY_STRING, // Qualifier
                hpoId.getValue(), // HPO ID
                diseaseID, // DB_Reference
                ORPHA_EVIDENCE_CODE, // Evidence_Code
                NO_ONSET_CODE_AVAILABLE, // Onset
                entry.getFrequency().getValue(),// Frequency (An HPO TermId, always)
                EMPTY_STRING, // Sex (not used)
                EMPTY_STRING, // Modifier (not used)
                getAspect(hpoId),
                biocuration// Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }

    /** We are using this to supply a date created value for the Orphanet annotations.
     * After some research, no better way of getting the current date was found.
     * @return A String such as 2018-02-22
     */
    private String getTodaysDate() {
        Date date = new Date();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }


}
