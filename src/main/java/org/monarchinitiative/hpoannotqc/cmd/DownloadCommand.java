package org.monarchinitiative.hpoannotqc.cmd;



import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


import org.monarchinitiative.hpoannotqc.exception.FileDownloadException;
import org.monarchinitiative.hpoannotqc.io.FileDownloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.2 (Jan 2, 2019)
 */
@Parameters(commandDescription = "Download files")
public final class DownloadCommand implements Command {
    private final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class);

    /** Directory to which to download the files. */
    @Parameter(names={"-d","--data"}, description ="directory to download data (default: data)" )
    private String downloadDirectory="data";
    /** Overwrite previously downloaded files if true. */
    @Parameter(names={"-o","--overwrite"}, description = "overwrite prevously downloaded files, if any")
    private boolean overwrite;

    private final static String MIM2GENE_MEDGEN = "mim2gene_medgen";

    private final static String MIM2GENE_MEDGEN_URL = "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen";

    private final static String GENE_INFO = "Homo_sapiens_gene_info.gz";

    private final static String GENE_INFO_URL = "ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz";

    private final static String HP_OBO = "hp.obo";
    /** URL of the hp.obo file. */
    private final static String HP_OBO_URL ="https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo";

    private final static String ORPHANET_XML = "en_product4.xml";

    private final static String ORPHANET_XML_URL = "http://www.orphadata.org/data/xml/en_product4.xml";

    private final static String ORPHANET_INHERITANCE_XML = "en_product9_ages.xml";

    private final static String ORPHANET_INHERITANCE_XML_URL = "http://www.orphadata.org/data/xml/en_product9_ages.xml";

    private final static String ORPHANET_GENES_XML = "en_product6.xml";

    private final static String ORPHANET_GENES_XML_URL = "http://www.orphadata.org/data/xml/en_product6.xml";


    public DownloadCommand()  {

    }

    /**
     * Perform the downloading.
     */
    @Override
    public void execute()  {
        createDownloadDir(downloadDirectory);
        downloadFile(HP_OBO,HP_OBO_URL,overwrite);
        downloadFile(ORPHANET_XML,ORPHANET_XML_URL,overwrite);
        downloadFile(ORPHANET_INHERITANCE_XML,ORPHANET_INHERITANCE_XML_URL,overwrite);
        downloadFile(GENE_INFO,GENE_INFO_URL,overwrite);
        downloadFile(ORPHANET_GENES_XML,ORPHANET_GENES_XML_URL,overwrite);
        downloadFile(MIM2GENE_MEDGEN,MIM2GENE_MEDGEN_URL,overwrite);
    }






    private void downloadFile(String filename, String webAddress, boolean overwrite) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            LOGGER.trace(String.format("Cowardly refusing to download %s since we found it at %s",
                    filename,
                    f.getAbsolutePath()));
            return;
        }
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(webAddress);
            LOGGER.debug("Created url from "+webAddress+": "+url.toString());
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (MalformedURLException e) {
            LOGGER.error(String.format("Malformed URL for %s [%s]",filename, webAddress));
            LOGGER.error(e.getMessage());
        } catch (FileDownloadException e) {
            LOGGER.error(String.format("Error downloading %s from %s" ,filename, webAddress));
            LOGGER.error(e.getMessage());
        }
    }


    /**
     * @param dir Directory to which to download files.
     */
    private void createDownloadDir(String dir) {
        LOGGER.trace("creating download dir (and deleting previous version) at "+ dir);
        File d =new File(dir);
        if (d.exists()) {
            d.delete();
        }
        d.mkdir();
    }

}