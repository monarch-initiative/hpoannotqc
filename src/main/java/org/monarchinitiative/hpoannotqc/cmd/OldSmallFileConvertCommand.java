package org.monarchinitiative.hpoannotqc.cmd;


import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.OldSmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.OldSmallFileEntry;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OldSmallFileConvertCommand implements Command {
    private static final Logger logger = LogManager.getLogger();

    private final String hpOboPath;
    private final String oldSmallFileAnnotationDirectory;

    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;

    private List<OldSmallFile> osfList=new ArrayList<>();

    private List<V2SmallFile> v2sfList = new ArrayList<>();
    /** Default path for writing the new V2 small files. */
    private final String DEFAULT_OUTPUT_DIRECTORY ="v2files";

    private int n_corrected_date=0;
    private int n_no_evidence=0;
    private int n_gene_data=0;
    private int n_alt_id=0;
    private int n_update_label=0;
    private int n_created_modifier=0;
    private int n_EQ_item=0;
    private int n_less_than_expected_number_of_lines=0;


    public OldSmallFileConvertCommand(String hpPath, String annotPath) {
        hpOboPath=hpPath;
        oldSmallFileAnnotationDirectory =annotPath;
    }


    private void convertToNewSmallFiles() {
        osfList.forEach(old -> {
            V2SmallFile v2 = new V2SmallFile(old);
            v2sfList.add(v2);
        });
        try {
            for (V2SmallFile v2 : v2sfList) {
                outputV2file(v2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void execute() {
        logger.trace("hpoPath="+hpOboPath + " annotation path="+ oldSmallFileAnnotationDirectory);
        initOntology();
        List<String> files=getListOfSmallFiles();
        logger.trace("We found " + files.size() + " small files at " + oldSmallFileAnnotationDirectory);
        for (String path : files) {
            OldSmallFile osf = new OldSmallFile(path);
            this.n_alt_id += osf.getN_alt_id();
            this.n_corrected_date += osf.getN_corrected_date();
            n_no_evidence += osf.getN_no_evidence();
            n_gene_data += osf.getN_gene_data();
            n_update_label += osf.getN_update_label();
            n_created_modifier += osf.getN_created_modifier();
            n_EQ_item += osf.getN_EQ_item();
            n_less_than_expected_number_of_lines += osf.getN_less_than_expected_number_of_lines();
            osfList.add(osf);
        }

        convertToNewSmallFiles();
        dumpQCtoShell();
    }


    private void dumpQCtoShell() {
        System.out.println("\n\n################################################\n\n");
        System.out.println(String.format("We converted %d \"old\" small files into %d new (V2) small files",
                osfList.size(),v2sfList.size()));
        System.out.println();
        System.out.println("Summary of Q/C results:");
        System.out.println("\tNumber of lines with corrected date formats: " + n_corrected_date);
        System.out.println("\tNumber of lines with \"Gene\" data that was discarded for the V2 files: " + n_gene_data);
        System.out.println("\tNumber of lines with \"E/Q\" data that was discarded for the V2 files: " + n_EQ_item);
        System.out.println("\tNumber of lines with alt_ids updated to current ids: " + n_alt_id);
        System.out.println("\tNumber of lines with labels updated to current labels: " + n_update_label);
        System.out.println("\tNumber of lines for which no Evidence code was found: "+ n_no_evidence);
        System.out.println("\tNumber of lines for which a Clinical modifer was extracted: "+n_created_modifier);
        System.out.println("\tNumber of lines with less than expected number of fields (given number of fields in header): "+n_less_than_expected_number_of_lines);
        System.out.println();
        System.out.println("Lines that were Q/C'd or updated have been written to the log (before/after)");
        System.out.println();
    }


    private void outputV2file(V2SmallFile v2) throws IOException {
        String outdir= DEFAULT_OUTPUT_DIRECTORY;
        if (! new File(outdir).exists()) {
            new File(outdir).mkdir();
        }
        String filename = String.format("%s%s%s",outdir,File.separator,v2.getBasename());
        logger.trace("Writing v2 to file " + filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(V2SmallFileEntry.getHeader()+"\n");
        List<V2SmallFileEntry> entryList = v2.getEntryList();
        for (V2SmallFileEntry v2e:entryList) {
            writer.write(v2e.getRow() + "\n");
        }
        writer.close();

    }




    private List<String> getListOfSmallFiles() {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(oldSmallFileAnnotationDirectory))) {
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
            this.abnormalPhenoSubOntology = ontology.getPhenotypicAbnormalitySubOntology();
            this.inheritanceSubontology = ontology.subOntology(inheritId);
        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }
        OldSmallFileEntry.setOntology(ontology, inheritanceSubontology, abnormalPhenoSubOntology);
        if (this.ontology==null) {
            logger.error("We could not parse the HPO ontology. Terminating ...");
            System.exit(1);// not a recoverable error
        }
    }
}
