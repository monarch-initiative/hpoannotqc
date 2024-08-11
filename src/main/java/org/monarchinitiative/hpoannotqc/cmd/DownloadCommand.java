package org.monarchinitiative.hpoannotqc.cmd;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import org.monarchinitiative.biodownload.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;


/**
 * Implementation of download in HpoWorkbench. The command is intended to download
 * both the OBO file and the association file. For HPO, this is {@code hp.json} and
 * {@code phenotype.hpoa}.
 * Code modified from Download command in Jannovar.
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.2 (Jan 2, 2019)
 */
@CommandLine.Command(name = "download", aliases = {"D"}, mixinStandardHelpOptions = true, description = "download files")
public final class DownloadCommand implements Callable<Integer> {
    private final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class);
    /** Directory to which to download the files. */
    @CommandLine.Option(names = {"-d", "--data"}, description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    /** Overwrite previously downloaded files if true. */
    @CommandLine.Option(names={"-o","--overwrite"},
            description = "overwrite previously downloaded files, if any (default: ${DEFAULT-VALUE})")
    private boolean overwrite = false;

    private final static String ORPHANET_XML = "en_product4.xml";

    private final static String ORPHANET_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product4.xml";

    private final static String ORPHANET_INHERITANCE_XML = "en_product9_ages.xml";

    private final static String ORPHANET_INHERITANCE_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product9_ages.xml";

    private final static String ORPHANET_GENES_XML = "en_product6.xml";

    private final static String ORPHANET_GENES_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product6.xml";


    public DownloadCommand() {
    }

    /**
     * Perform the downloading.
     */
    @Override
    public Integer call() throws MalformedURLException, org.monarchinitiative.biodownload.FileDownloadException {
        biodownload();
        return 0;
    }


    private void biodownload() throws MalformedURLException, org.monarchinitiative.biodownload.FileDownloadException {
        createDownloadDir(downloadDirectory);
        Path destination = Paths.get(downloadDirectory);
        BioDownloaderBuilder builder = BioDownloader.builder(destination);
        URL ORPHANET_XML_URL = URI.create(ORPHANET_XML_URL_PATH).toURL();
        URL ORPHANET_INHERITANCE_XML_URL = URI.create(ORPHANET_INHERITANCE_XML_URL_PATH).toURL();
        URL ORPHANET_GENES_XML_URL = URI.create(ORPHANET_GENES_XML_URL_PATH).toURL();
        builder.hpoJson()
                .geneInfoHuman()
                .medgene2MIM()
                .custom(ORPHANET_XML, ORPHANET_XML_URL)
                .custom(ORPHANET_INHERITANCE_XML, ORPHANET_INHERITANCE_XML_URL)
                .custom(ORPHANET_GENES_XML, ORPHANET_GENES_XML_URL)
                .overwrite(overwrite);
        BioDownloader downloader = builder.build();
        List<File> files = downloader.download();
        if (!files.isEmpty()) {
            System.out.println("[INFO] Downloaded:");
            for (var f: files) {
                System.out.printf("[INFO]    %s", f.getAbsolutePath());
            }
        }
    }

    /**
     * @param dir Directory to which to download files.
     */
    private void createDownloadDir(String dir) {
        LOGGER.trace("creating download dir (and deleting previous version) at {}.", dir);
        File d =new File(dir);
        if (d.exists()) {
            d.delete();
        }
        d.mkdir();
    }
}