package org.monarchinitiative.hpoannotqc.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.bigfile.V2SmallFileParser;
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

public class BigFileCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    private final String v2smallFileDirectory;
    private final String hpOboPath;
    private List<V2SmallFile> v2filelist=new ArrayList<>();

    private String bigFileOutputName="phenotype_annotation2.tab";



    public BigFileCommand(String hpopath, String dir) {
        hpOboPath=hpopath;
        v2smallFileDirectory =dir;
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

    }




    private void outputBigFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(bigFileOutputName));
            for (V2SmallFile v2 : v2filelist ) {

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



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
            HpoOntology ontology = hpoOboParser.parse();
            if (ontology==null) {
                logger.error("We could not parse the HPO ontology. Terminating ...");
                System.exit(1);// not a recoverable error
            }
            V2SmallFileParser.setOntology(ontology);
        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }


    }

}
