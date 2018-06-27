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
import java.util.ArrayList;
import java.util.List;


public class BigFileWriter {
    private static final Logger logger = LogManager.getLogger();
    /** List of all of the {@link V2SmallFile} objects, which represent annotated diseases. */
    private List<V2SmallFile> v2SmallFileList =new ArrayList<>();
    /** Representation of the version 2 Big file and all its data for export. */
    private final V2BigFile v2BigFile;
    /** Total number of annotations of all of the annotation files. */
    private int n_total_annotation_lines=0;
    /**Usually "phenotype.hpoa", but may also include path. */
    private final String bigFileOutputNameV2;
    private BufferedWriter writer;
    private final HpoOntology ontology;


    public BigFileWriter(HpoOntology ont, List<V2SmallFile> v2list, String outpath) throws HPOException {
        this.ontology=ont;
        this.v2SmallFileList=v2list;
        this.bigFileOutputNameV2=outpath;
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


}
