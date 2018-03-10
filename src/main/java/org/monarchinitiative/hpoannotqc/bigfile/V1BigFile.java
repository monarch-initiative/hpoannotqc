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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * This class represents the data we export to the V1 big file. Note that I am not going to make an inheritance
 * hierarch despite the code duplication with {@link V2BigFile} because we will be removing the V1 big file classes
 * once we are finishing with the conversion.
 */
class V1BigFile {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final V2LineQualityController v2qualityController;
    private final static String EMPTY_STRING="";
    private static final TermId phenotypeRoot= ImmutableTermId.constructWithPrefix("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =ImmutableTermId.constructWithPrefix("HP:0031797");
    /** These are the objects that represent the diseases contained in the V2 small files. */
    private final List<V2SmallFile> v2SmallFileList;



    V1BigFile(HpoOntology ont, List<V2SmallFile> v2SmallFiles) {
        this.ontology=ont;
        v2SmallFileList=v2SmallFiles;
        v2qualityController=new V2LineQualityController(this.ontology);
    }

    V1BigFile(HpoOntology ont) {
        this.ontology=ont;
        v2SmallFileList=new ArrayList<>();
        v2qualityController=new V2LineQualityController(this.ontology);
    }



    /**
     * Output the big file in the V1 format used from 2009-2018. Note -- given the transformation of the old files and
     * the new structure of the ontology, it is not possible or desirable to recreate the original phenotype_annotation.tab
     * exactly -- this is just for Q/C purposes, and in general the V2 version should be used.
     */
    void outputBigFileV1(BufferedWriter writer) throws IOException {
        int n = 0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        writer.write(BigFileHeader.getHeaderV1() + "\n");
        for (V2SmallFile v2 : v2SmallFileList) {
            List<V2SmallFileEntry> entryList = v2.getEntryList();
            for (V2SmallFileEntry entry : entryList) {
                v2qc.checkV2entry(entry);
                String bigfileLine = transformEntry2BigFileLineV1(entry);
                writer.write(bigfileLine + "\n");
                n++;
            }
        }
        logger.trace("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoShell();
        v2qc.dumpAssignedByMap();
    }



    /** Construct one line for the V1 big file that was in use from 2009-2018. */
    String transformEntry2BigFileLineV1(V2SmallFileEntry entry) {
        String [] elems = {
                entry.getDB(), // DB
                entry.getDB_Object_ID(), //DB_Object_ID
                entry.getDiseaseName(), // DB_Name
                entry.getNegation(), // Qualifier
                entry.getPhenotypeId().getIdWithPrefix(), // HPO ID
                entry.getPublication(), // DB:Reference
                entry.getEvidenceCode(), // Evidence code
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(), // Onset modifier
                entry.getFrequencyModifier()!=null?entry.getFrequencyModifier():EMPTY_STRING, // Frequency modifier
                EMPTY_STRING, // With (not used)
                getAspectV1(entry.getPhenotypeId()), // Aspect
                EMPTY_STRING, // Synonym (not used)
                entry.getDateCreated(), // Date
                entry.getAssignedBy() // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    /**
     * Figure out the Aspect of this line (O,I, or C) based on the location of the term in the ontology.
     * @param tid Term for which we want to calculate the Aspect
     * @return One-letter String representing the Aspect (O, I, or C).
     */
    private String getAspectV1(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getIdWithPrefix());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            v2qualityController.incrementGoodAspect();//
            return "O"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "I";
        } else if (existsPath(ontology, tid, CLINICAL_COURSE_ID)) {
            v2qualityController.incrementGoodAspect();
            return "C";
        }  else {
//            logger.error("Could not identify aspect for entry with term id " + entry.getPhenotypeId().getIdWithPrefix() + "(+" +
//                    entry.getPhenotypeName()+")");
            this.v2qualityController.incrementBadAspect();
            return "?";
        }
    }



}
