package org.monarchinitiative.hpoannotqc.cmd;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.FileDownloadException;
import org.monarchinitiative.hpoannotqc.io.FileDownloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;



/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.2 (Jan 2, 2019)
 */
public final class DownloadCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger();
    /** Directory to which to download the files. */
    private final String downloadDirectory;
    /** Overwrite previously downloaded files if true. */
    private final boolean overwrite;

    /**
     * @param downloadDir directory to download to
     * @param overw Overwrite previously downloaded files if true.
     */
    public DownloadCommand(String downloadDir, boolean overw)  {
        this.downloadDirectory=downloadDir;
        this.overwrite=overw;
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void execute()  {
        createDownloadDir(downloadDirectory);
        downloadHpObo();
        downloadOrphanet();
    }

    /**
     * Download the Oprhanet HPO annotations, which are in XML
     */
    private void downloadOrphanet() {
        //http://www.orphadata.org/data/xml/en_product4_HPO.xml
        String downloadLocation=String.format("%s%sen_product4_HPO.xml",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            if (overwrite) {
                LOGGER.trace("Will overwrite existing file " + f.getAbsolutePath());
            } else {
                LOGGER.trace("cowardly refusing to download en_product4_HPO.xml, since it is already there");
                System.out.println("cowardly refusing to download en_product4_HPO.xml, since it is already there");
                return;
            }
        }
        try {
            URL url = new URL("http://www.orphadata.org/data/xml/en_product4_HPO.xml");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url, f);
            if (result) {
                String msg = String.format("Downloaded en_product4_HPO.xml to \"%s\"", downloadLocation);
                LOGGER.trace(msg);
                System.out.println(msg);
            } else {
                LOGGER.error("Could not download en_product4_HPO.xml to " + downloadLocation);
            }
        } catch (FileDownloadException | MalformedURLException fde) {
            fde.printStackTrace();
        }
    }



    private void downloadHpObo() {
        String downloadLocation=String.format("%s%shp.obo",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            if (overwrite) {
                LOGGER.trace("Will overwrite existing file " + f.getAbsolutePath());
            } else {
                LOGGER.trace("cowardly refusing to hp.obo, since it is already there");
                System.out.println("cowardly refusing to download hp.obo, since it is already there");
                return;
            }
        }
        try {
            URL url = new URL("https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url, f);
            if (result) {
                String msg =String.format("Downloaded hp.obo to %s",downloadLocation );
                        LOGGER.trace( msg);
                        System.out.println(msg);
            } else {
                LOGGER.error("Could not download hp.obo to " + downloadLocation);
            }
        } catch (FileDownloadException | MalformedURLException fde) {
            fde.printStackTrace();
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