package org.monarchinitiative.hpoannotqc.bigfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class BigFileWriter {
    private static final Logger logger = LogManager.getLogger();
    /** The paths to all of the v2 small files. */
    private final List<String> v2smallFilePaths;
    /** List of all of the {@link V2SmallFile} objects, which represent annotated diseases. */
    private List<V2SmallFile> v2SmallFileList =new ArrayList<>();
    /** Representation of the version 2 Big file and all its data for export. */
    private V2BigFile v2BigFile;
    /** Total number of annotations of all of the annotation files. */
    private int n_total_annotation_lines=0;
    /**Usually "phenotype.hpoa", but may also include path. */
    private final String bigFileOutputNameV2;
    private BufferedWriter writer;
    private final HpoOntology ontology;


    public BigFileWriter(HpoOntology ont, String directoryPath, String outpath) {
        this.v2smallFilePaths = getListOfV2SmallFiles(directoryPath);
        this.ontology=ont;
        V2SmallFileParser.setOntology(ont);
        inputV2files();
        this.bigFileOutputNameV2=outpath;
        System.out.println(String.format("We finished with the input of %d V2 small files with a total of %d annotation lines", v2SmallFileList.size(),n_total_annotation_lines));
        this.v2BigFile=new V2BigFile(ont,v2SmallFileList);
    }


    public void closeFileHandle() throws IOException {
        writer.close();
    }


    public void initializeV2filehandle() throws HPOException {
        try {
            this.writer = new BufferedWriter(new FileWriter(bigFileOutputNameV2));
        } catch (IOException ioe) {
            String msg = String.format("IOException encountered while trying to create BufferedWriter for bigfile at file %s",bigFileOutputNameV2);
            throw new HPOException(msg);
        }
    }



    public void outputBigFileV2() throws IOException {
        this.v2BigFile.outputBigFileV2(this.writer);
    }

    public void appendOrphanetV2(List<OrphanetDisorder> orphanetDisorders) throws IOException {
        Orphanet2BigFile orph2big = new Orphanet2BigFile(orphanetDisorders, writer,this.ontology);
        orph2big.writeOrphanetV2();
    }



    private void inputV2files() {
        logger.trace("We found " + v2smallFilePaths.size() + " small files.");
        int i=0;
        for (String path : v2smallFilePaths) {
            if (++i%1000==0) {
                logger.trace(String.format("Inputting %d-th file at %s",i,path));
            }
            V2SmallFileParser parser=new V2SmallFileParser(path);
            V2SmallFile v2sf = parser.getV2eEntry();
            n_total_annotation_lines += v2sf.getNumberOfAnnotations();
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
