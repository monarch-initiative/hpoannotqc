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
 * Command for downloading required data files for HPO annotation quality control.
 *
 * <p>This command downloads essential files needed for HPO annotation processing:</p>
 * <ul>
 *   <li>HPO ontology file (hp.json)</li>
 *   <li>Human gene information</li>
 *   <li>MedGen to MIM mappings</li>
 *   <li>Orphanet XML data files including disease definitions, inheritance patterns, and gene associations</li>
 * </ul>
 *
 * <p>The download process creates a data directory and retrieves the latest versions of these files
 * from their respective sources. Files can be optionally overwritten if they already exist.</p>
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 * @version 2.0.0
 */
@CommandLine.Command(name = "download", aliases = {"D"}, mixinStandardHelpOptions = true, description = "download files")
public final class DownloadCommand implements Callable<Integer> {
    private final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class);
    /** Directory to which to download the files. */
    @CommandLine.Option(names = {"-d", "--data"}, description = "directory to download data (default: ${DEFAULT-VALUE})")
    private final String downloadDirectory = "data";
    /** Overwrite previously downloaded files if true. */
    @CommandLine.Option(names={"-o","--overwrite"},
            description = "overwrite previously downloaded files, if any (default: ${DEFAULT-VALUE})")
    private final boolean overwrite = false;

    private final static String ORPHANET_XML = "en_product4.xml";

    private final static String ORPHANET_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product4.xml";

    private final static String ORPHANET_INHERITANCE_XML = "en_product9_ages.xml";

    private final static String ORPHANET_INHERITANCE_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product9_ages.xml";

    private final static String ORPHANET_GENES_XML = "en_product6.xml";

    private final static String ORPHANET_GENES_XML_URL_PATH = "http://www.orphadata.org/data/xml/en_product6.xml";


    /**
     * Default constructor for DownloadCommand.
     */
    public DownloadCommand() {
    }

    /**
     * Executes the download command by retrieving all required data files.
     *
     * @return exit code (0 for success)
     * @throws MalformedURLException if any download URL is malformed
     * @throws FileDownloadException if file download fails
     */
    @Override
    public Integer call() throws MalformedURLException, org.monarchinitiative.biodownload.FileDownloadException {
        biodownload();
        return 0;
    }


    /**
     * Performs the actual download of all required files using the BioDownloader library.
     *
     * @throws MalformedURLException if any download URL is malformed
     * @throws FileDownloadException if file download fails
     */
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
            LOGGER.info("[INFO] Downloaded:");
            for (var f: files) {
                LOGGER.info("[INFO]    {}", f.getAbsolutePath());
            }
        }
    }

    /**
     * Creates the download directory, removing any existing directory with the same name.
     *
     * @param dir path to the directory to create for downloaded files
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
