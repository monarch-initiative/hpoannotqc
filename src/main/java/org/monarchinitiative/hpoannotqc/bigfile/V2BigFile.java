package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

public class V2BigFile {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final V2LineQualityController v2qualityController;
    private final static String EMPTY_STRING="";
    private static final TermId phenotypeRoot= ImmutableTermId.constructWithPrefix("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =ImmutableTermId.constructWithPrefix("HP:0031797");
    private static final TermId ONSET_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0003674");
    /** These are the objects that represent the diseases contained in the V2 small files. */
    private final List<V2SmallFile> v2SmallFileList;



    public V2BigFile(HpoOntology ont, List<V2SmallFile> v2SmallFiles) {
        this.ontology=ont;
        v2SmallFileList=v2SmallFiles;
        v2qualityController=new V2LineQualityController(this.ontology);

    }

    public void outputBigFileV2(BufferedWriter writer) throws IOException {
        int n = 0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        writer.write(BigFileHeader.getHeaderV2() + "\n");
        for (V2SmallFile v2 : v2SmallFileList) {
            List<V2SmallFileEntry> entryList = v2.getEntryList();
            for (V2SmallFileEntry entry : entryList) {
                v2qc.checkV2entry(entry);
                String bigfileLine = transformEntry2BigFileLineV2(entry);
                writer.write(bigfileLine + "\n");
                n++;
            }
        }
        logger.trace("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoShell();
    }
    /** Construct one line for the V1 big file that was in use from 2009-2018. */
    String transformEntry2BigFileLineV2(V2SmallFileEntry entry) {
        String [] elems = {
                entry.getDB(), //DB
                entry.getDB_Object_ID(), //DB_Object_ID
                entry.getDiseaseName(), // DB_Name
                entry.getNegation(), // Qualifier
                entry.getPhenotypeId().getIdWithPrefix(), // HPO_ID
                entry.getPublication(), // DB_Reference
                entry.getEvidenceCode(), // Evidence_Code
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(), // Onset
                entry.getFrequencyModifier()!=null?entry.getFrequencyModifier():EMPTY_STRING, // Frequency
                entry.getSex(), // Sex
                entry.getModifier(), // Modifier
                getAspectV2(entry.getPhenotypeId()), // Aspect
                entry.getDateCreated(), // Date
                entry.getAssignedBy() // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    private String getAspectV2(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getIdWithPrefix());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            v2qualityController.incrementGoodAspect();//
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "I";
        } else if (existsPath(ontology, tid, ONSET_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "C";
        } else {
            this.v2qualityController.incrementBadAspect();
            return "?";
        }
    }


}
