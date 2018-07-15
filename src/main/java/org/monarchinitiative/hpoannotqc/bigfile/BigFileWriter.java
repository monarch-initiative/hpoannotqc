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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


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
    /** Number of annotated Orphanet entries. */
    private int n_orphanet;
    private int n_decipher;
    private int n_omim;
    /** Number of database sources that could not be identified (should be zero!). */
    private int n_unknown;

    private Map<String,String> ontologyMetaInfo;



    public BigFileWriter(HpoOntology ont, List<V2SmallFile> v2list, String outpath) throws HPOException {
        this.ontology=ont;
        this.v2SmallFileList=v2list;
        this.bigFileOutputNameV2=outpath;
        this.v2BigFile=new V2BigFile(ont,v2SmallFileList);
    }


    public void closeFileHandle() throws IOException {
        writer.close();
    }

    public void setNumberOfDiseasesForHeader(int n_orpha) {
        this.n_orphanet=n_orpha;
        this.n_decipher=0;
        this.n_omim=0;
        this.n_unknown=0;
        for (V2SmallFile v2f : v2SmallFileList) {
            if (v2f.isOMIM()) n_omim++;
            else if (v2f.isDECIPHER()) n_decipher++;
            else n_unknown++;
        }
    }


    public void initializeV2filehandle() throws HPOException {
        try {
            this.writer = new BufferedWriter(new FileWriter(bigFileOutputNameV2));
        } catch (IOException ioe) {
            String msg = String.format("IOException encountered while trying to create BufferedWriter for bigfile at file %s",bigFileOutputNameV2);
            throw new HPOException(msg);
        }
    }

    public void setOntologyMetadata(Map<String,String> meta) { this.ontologyMetaInfo=meta;}

    private String getDate() {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        return ft.format(dNow);
    }

    public void outputBigFileV2() throws IOException {
        String description = String.format("#description: HPO annotations for rare diseases [%d: OMIM; %d: DECIPHER; %d ORPHANET]",n_omim,n_decipher,n_orphanet);
        if (n_unknown>0) description=String.format("%s -- warning: %d entries could not be assigned to a database",description,n_unknown);
        writer.write(description + "\n");
        writer.write(String.format("#date: %s\n",getDate()));
        writer.write("#tracker: https://github.com/obophenotype/human-phenotype-ontology\n");
        if (ontologyMetaInfo.containsKey("data-version")) {
            writer.write(String.format("#HPO-version: %s\n",ontologyMetaInfo.get("data-version")));
        }
        if (ontologyMetaInfo.containsKey("saved-by")) {
            writer.write(String.format("#HPO-contributors: %s\n",ontologyMetaInfo.get("saved-by")));
        }


        this.v2BigFile.outputBigFileV2(this.writer);
    }

    public void appendOrphanetV2(List<OrphanetDisorder> orphanetDisorders) throws IOException {
        Orphanet2BigFile orph2big = new Orphanet2BigFile(orphanetDisorders, writer,this.ontology);
        orph2big.writeOrphanetV2();
    }


}
