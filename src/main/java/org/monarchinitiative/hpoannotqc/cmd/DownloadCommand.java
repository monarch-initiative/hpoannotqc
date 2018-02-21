package org.monarchinitiative.hpoannotqc.cmd;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.FileDownloadException;
import org.monarchinitiative.hpoannotqc.io.FileDownloader;

import java.io.File;
import java.net.URL;



/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.obo} and
 * {@code phenotype_annotation.tab}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.1 (May 10, 2017)
 */
public final class DownloadCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String downloadDirectory;

    public String getName() { return "download"; }

    /**

     */
    public DownloadCommand(String downloadDir)  {
        this.downloadDirectory=downloadDir;
    }

    /**
     * Perform the downloading.
     */
    @Override
    public void execute()  {
        createDownloadDir(downloadDirectory);
        downloadHpObo();
        downloadOrphanet();
       // downloadPhenotypeAnnotationDotTab();
    }

    /**
     * Download the Oprhanet HPO annotations, which are in XML
     */
    private void downloadOrphanet() {
        //http://www.orphadata.org/data/xml/en_product4_HPO.xml
        String downloadLocation=String.format("%s%sen_product4_HPO.xml",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download en_product4_HPO.xml, since it is already there");
            return;
        }
        try {
            URL url = new URL("http://www.orphadata.org/data/xml/en_product4_HPO.xml");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url, f);
            if (result) {
                LOGGER.trace("Downloaded en_product4_HPO.xml to " + downloadLocation);
            } else {
                LOGGER.error("Could not download en_product4_HPO.xml to " + downloadLocation);
            }
        } catch (FileDownloadException fde) {
            fde.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void downloadPhenotypeAnnotationDotTab() {
        // Now the same for the phenotype_annotation.tab file
        String downloadLocation = String.format("%s%sphenotype_annotation.tab", downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download phenoptype_annotation.tab, since it is already there");
            return;
        }
        try {
            URL url = new URL("http://compbio.charite.de/jenkins/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url, f);
            if (result) {
                LOGGER.trace("Downloaded phenotype_annotation.tab to " + downloadLocation);
            } else {
                LOGGER.error("[ERROR] Could not phenotype_annotation.tab hp.obo to " + downloadLocation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void downloadHpObo() {
        String downloadLocation=String.format("%s%shp.obo",downloadDirectory, File.separator);
        File f = new File(downloadLocation);
        if (f.exists()) {
            LOGGER.trace("cowardly refusing to download hp.obo, since it is already there");
            return;
        }
        try {
            URL url = new URL("https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo");
            FileDownloader downloader = new FileDownloader();
            boolean result = downloader.copyURLToFile(url, f);
            if (result) {
                LOGGER.trace("Downloaded hp.obo to " + downloadLocation);
            } else {
                LOGGER.error("Could not download hp.obo to " + downloadLocation);
            }
        } catch (FileDownloadException fde) {
            fde.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }





    /**
     * Todo make robust
     * @param dir
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