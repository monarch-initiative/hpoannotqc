package org.monarchinitiative.hpoannotqc.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.bigfile.V2SmallFileParser;
import org.monarchinitiative.hpoannotqc.smallfile.OldSmallFileEntry;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.phenomics.ontolib.ontology.algo.OntologyAlgorithm.existsPath;

public class BigFileCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    private final String v2smallFileDirectory;
    private final String hpOboPath;
    private List<V2SmallFile> v2filelist=new ArrayList<>();

    private HpoOntology ontology;
    /*
    one of O (Phenotypic abnormality), I (inheritance), C (onset and clinical course) or M (Mortality/Aging). This field is mandatory; cardinality 1

     */
//    private final Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology;
//    private final Ontology<HpoTerm, HpoTermRelation> onsetSubontology;
//    private final Ontology<HpoTerm, HpoTermRelation> mortalitySubontology;
//    private final Ontology<HpoTerm, HpoTermRelation> phenotypeSubontology;

    private int n_bad_aspect=0;

    private String bigFileOutputName="phenotype_annotation2.tab";

    private TermId phenotypeRoot,frequencyRoot,onsetRoot,modiferRoot,inheritanceRoot,mortalityRoot;


    public BigFileCommand(String hpopath, String dir) {
        hpOboPath=hpopath;
        v2smallFileDirectory =dir;
//        TermPrefix pref = new ImmutableTermPrefix("HP");
//        TermId inheritId = new ImmutableTermId(pref,"0000005");
//        inheritanceSubontology = this.ontology.subOntology(inheritId);
//        TermId onsetId = new ImmutableTermId(pref,"0003674");
//        onsetSubontology = this.ontology.subOntology(onsetId);
//        TermId mortalityId = new ImmutableTermId(pref,"0040006");
//        mortalitySubontology = ontology.subOntology(mortalityId);
//        phenotypeSubontology = ontology.getPhenotypicAbnormalitySubOntology();
    }



    @Override
    public void execute() {
        List<String> files=getListOfSmallFiles();
        initOntology();
        logger.trace("We found " + files.size() + " small files at " + v2smallFileDirectory);
        for (String path : files) {
            System.out.println(path);
            V2SmallFileParser parser=new V2SmallFileParser(path);
            v2filelist.add(parser.getV2eEntry());
        }

        logger.trace("We got " + v2filelist.size() + " V2 small files");
        outputBigFile();
    }




    private void outputBigFile() {
        int n=0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(bigFileOutputName));
            for (V2SmallFile v2 : v2filelist ) {
                List<V2SmallFileEntry> entryList = v2.getEntryList();
                for (V2SmallFileEntry entry : entryList) {
                    v2qc.checkV2entry(entry);

                   String bigfileLine = transformEntry2BigFileLine(entry);
                   writer.write(bigfileLine + "\n");
                   n++;
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.trace("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoShell();
        System.out.println("--Number of lines with bad aspect: " + n_bad_aspect);
    }

    /** Return frequency modifier if available, otherwise null. */
    private String getFrequencyString(V2SmallFileEntry entry) {
        if (entry.getFrequencyModifier()!=null) return entry.getFrequencyModifier();
        else return "";
    }

    private String getAspect(V2SmallFileEntry entry) {
        TermId tid = entry.getPhenotypeId();
        HpoTerm term = ontology.getTermMap().get(tid);
        if (existsPath(ontology, tid, phenotypeRoot)) {
            return "O"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, inheritanceRoot)) {
            return "I";
        } else if (existsPath(ontology, tid, onsetRoot)) {
            return "C";
        } else if (existsPath(ontology, tid, mortalityRoot)) {
            return "M";
        } else {
            logger.error("Could not identify aspect for entry with term id " + entry.getPhenotypeId().getIdWithPrefix() + "(+" +
                    entry.getPhenotypeName()+")");
            n_bad_aspect++;
            return "?";
        }
    }









    private String transformEntry2BigFileLine(V2SmallFileEntry entry) {
        String [] elems = {
                entry.getDB(),
                entry.getDB_Object_ID(),
                entry.getDiseaseName(),
                entry.getNegation(),
                entry.getPhenotypeId().getIdWithPrefix(),
                entry.getPhenotypeName(),
                entry.getPublication(),
                entry.getEvidenceCode(),
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(),
                getFrequencyString(entry),
                "", /* with*/
                getAspect(entry),
                "", /* synonym */
                entry.getDateCreated(),
                entry.getAssignedBy()
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }






    private List<String> getListOfSmallFiles() {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(v2smallFileDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    fileNames.add(path.toString());
                }
                // fileNames.add(path.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Could not get list of small files. Terminating...");
            System.exit(1);
        }
        return fileNames;
    }

    /** Parse the hp.obo file. Set the static ontology variables in OldSmallFileEntry that we will
     * use to check the entries. We use the ontology objects in {@link OldSmallFileEntry} so we
     * set them using a static setter
     */
    private void initOntology() {
        TermPrefix pref = new ImmutableTermPrefix("HP");
        TermId inheritId = new ImmutableTermId(pref,"0000005");
        try {
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();
            if (ontology==null) {
                logger.error("We could not parse the HPO ontology. Terminating ...");
                System.exit(1);// not a recoverable error
            }
            V2SmallFileParser.setOntology(ontology);
            phenotypeRoot=ImmutableTermId.constructWithPrefix("HP:0000118");
            frequencyRoot=ImmutableTermId.constructWithPrefix("HP:0040279");
            onsetRoot=ImmutableTermId.constructWithPrefix("HP:0003674");
            modiferRoot=ImmutableTermId.constructWithPrefix("HP:0012823");
            inheritanceRoot=ImmutableTermId.constructWithPrefix("HP:0000005");
            mortalityRoot=ImmutableTermId.constructWithPrefix("HP:0040006");
        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }


    }

}
