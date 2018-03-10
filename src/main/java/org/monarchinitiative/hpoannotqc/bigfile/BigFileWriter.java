package org.monarchinitiative.hpoannotqc.bigfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;


public class BigFileWriter {
    private static final Logger logger = LogManager.getLogger();
    /** The paths to all of the v2 small files. */
    private final List<String> v2smallFilePaths;
    /** List of all of the {@link V2SmallFile} objects, which represent annotated diseases. */
    private List<V2SmallFile> v2SmallFileList =new ArrayList<>();
    /** Representation of the version 1 Big file and all its data for export. */
    private V1BigFile v1BigFile;
    /** Representation of the version 2 Big file and all its data for export. */
    private V2BigFile v2BigFile;

    private final static String bigFileOutputNameV1="phenotype_annotation2.tab";
    private final static String bigFileOutputNameV2="phenotype.hpoa";
    private BufferedWriter writerV1;
    /** This is the file handle for writing the small files and Orphanet data to file with the new (2018-02) format*/
    private BufferedWriter writerV2;
    private final HpoOntology ontology;


    public BigFileWriter(HpoOntology ont, String directoryPath) {
        this.v2smallFilePaths = getListOfV2SmallFiles(directoryPath);
        this.ontology=ont;
        V2SmallFileParser.setOntology(ont);
        inputV2files();
        this.v1BigFile=new V1BigFile(ont, v2SmallFileList);
        this.v2BigFile=new V2BigFile(ont,v2SmallFileList);
        try {
            this.writerV1= new BufferedWriter(new FileWriter(bigFileOutputNameV1));
            this.writerV2 = new BufferedWriter(new FileWriter(bigFileOutputNameV2));
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not open file for writing " ,e);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
    }


    public void closeV1() {
        try {
            writerV1.close();
        } catch (IOException e) {
            logger.error("[ERROR] I/O Problem: ", e);
        }
    }

    public void closeV2() {
        try {
            writerV2.close();
        } catch (IOException e) {
            logger.error("[ERROR] I/O Problem: ", e);
        }
    }


    public void outputBothBigFileVersions() {
        try {
            outputBigFileV1();
            writerV1.close();
            writerV2.close();
        } catch (IOException e) {
            logger.error("[ERROR] I/O Problem: ", e);
        }
    }



    public void outputBigFileV1() throws IOException {
       this.v1BigFile.outputBigFileV1(this.writerV1);
    }

    public void outputBigFileV2() throws IOException {
        this.v2BigFile.outputBigFileV2(this.writerV2);
    }

    public void appendOrphanetV1(List<OrphanetDisorder> orphanetDisorders) throws IOException {
        Orphanet2BigFile orph2big = new Orphanet2BigFile(orphanetDisorders,writerV1,this.ontology);
        orph2big.writeOrphanetV1();
    }

    public void appendOrphanetV2(List<OrphanetDisorder> orphanetDisorders) throws IOException {
        Orphanet2BigFile orph2big = new Orphanet2BigFile(orphanetDisorders,writerV1,this.ontology);
        orph2big.writeOrphanetV2();
    }













    private void inputV2files() {
        logger.trace("We found " + v2smallFilePaths.size() + " small files.");
        int i=0;
        for (String path : v2smallFilePaths) {
            if (++i%1000==0) {
                logger.trace(String.format("Inputting %d-th file at ",i,path));
            }
            V2SmallFileParser parser=new V2SmallFileParser(path);
            v2SmallFileList.add(parser.getV2eEntry());
        }
        logger.trace(String.format("Done with input of %d files",i));
    }



    private List<String> getListOfV2SmallFiles(String v2smallFileDirectory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(v2smallFileDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    fileNames.add(path.toString());
                }
            }
        } catch (IOException ex) {
            logger.error(String.format("Could not get list of small v2smallFilePaths from %s. Terminating...",v2smallFileDirectory),ex);
            System.exit(1);
        }
        return fileNames;
    }



}
