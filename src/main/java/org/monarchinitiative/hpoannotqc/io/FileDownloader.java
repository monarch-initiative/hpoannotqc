package org.monarchinitiative.hpoannotqc.io;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.monarchinitiative.hpoannotqc.exception.FileDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;


/**
 * Helper class for downloading files over HTTP and FTP.
 * <p>
 * The implementation of FTP downloads is more complex since we need passive FTP transfer through firewalls. This is not
 * possible when just opening a stream through an {@link URL} object with Java's builtin features.
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
public class FileDownloader {
    private final Logger LOGGER = LoggerFactory.getLogger(FileDownloader.class);
    static class ProxyOptions {
        final String host = null;
        final int port = -1;
        final String user = null;
        final String password = null;
    }

    /**
     * Configuration for the {@link FileDownloader}.
     */
    static class Options {
        final boolean printProgressBar = false;
        final ProxyOptions http = new ProxyOptions();
        final ProxyOptions https = new ProxyOptions();
        final ProxyOptions ftp = new ProxyOptions();
    }

    /** configuration for the downloader */
    private final Options options;

    /** Initializer FileDownloader with the given options string */
    public FileDownloader(Options options) {
        this.options = options;
    }

    /** Downloader with default options */
    public FileDownloader() {
        options=new Options();
    }

    /**
     * This method downloads a file to the specified local file path. If the file already exists, it emits a warning
     * message and does nothing.
     *
     * @param src
     *            {@link URL} with file to download
     * @param dest
     *            {@link File} with destination path
     * @throws FileDownloadException
     *             on problems with downloading
     */
    public void copyURLToFile(URL src, File dest) throws FileDownloadException {
        if (!dest.getParentFile().exists()) {
            LOGGER.info("Creating directory {}", dest.getParentFile());
            dest.getParentFile().mkdirs();
        }

        if (src.getProtocol().equals("ftp") && options.ftp.host != null) {
            copyURLToFileWithFTP(src, dest);
        } else {
            copyURLToFileThroughURL(src, dest);
        }
    }

    private boolean copyURLToFileWithFTP(URL src, File dest) throws FileDownloadException {
        final FTPClient ftp = new FTPClient();
        ftp.enterLocalPassiveMode(); // passive mode for firewalls

        try {
            if (src.getPort() != -1)
                ftp.connect(src.getHost(), src.getPort());
            else
                ftp.connect(src.getHost());
            if (!ftp.login("anonymous", "anonymous@example.com"))
                throw new IOException("Could not login with anonymous:anonymous@example.com");
            if (!ftp.isConnected())
                LOGGER.error("Weird, not connected!");
        } catch (IOException e) {
            throw new FileDownloadException("ERROR: problem connecting when downloading file.", e);
        }
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE); // binary file transfer
        } catch (IOException e) {
            try {
                ftp.logout();
            } catch (IOException e1) {
                // swallow, nothing we can do about it
            }
            try {
                ftp.disconnect();
            } catch (IOException e1) {
                // swallow, nothing we can do about it
            }
            throw new FileDownloadException("ERROR: could not use binary transfer.", e);
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            final String parentDir = new File(src.getPath()).getParent().substring(1);
            final String fileName = new File(src.getPath()).getName();
            if (!ftp.changeWorkingDirectory(parentDir))
                throw new FileNotFoundException("Could not change directory to " + parentDir);
            // Try to get file size.
            FTPFile[] files = ftp.listFiles(fileName);
            long fileSize = -1;
            for (FTPFile ftpfile : files)
                if (ftpfile.getName().equals(fileName))
                    fileSize = ftpfile.getSize();
            ftp.pwd();
            ProgressBar pb = null;
            if (fileSize != -1)
                pb = new ProgressBar(0, fileSize, options.printProgressBar);
            else
                LOGGER.info("(server did not tell us the file size, no progress bar)");
            // Download file.
            in = ftp.retrieveFileStream(fileName);
            if (in == null)
                throw new FileNotFoundException("Could not open connection for file " + fileName);
            out = new FileOutputStream(dest);
            BufferedInputStream inBf = new BufferedInputStream(in);
            byte [] buffer = new byte[128 * 1024];
            int readCount;
            long pos = 0;
            if (pb != null)
                pb.print(pos);

            while ((readCount = inBf.read(buffer)) > 0) {
                out.write(buffer, 0, readCount);
                pos += readCount;
                if (pb != null)
                    pb.print(pos);
            }
            in.close();
            out.close();
            if (pb != null && pos != pb.getMax())
                pb.print(fileSize);

        } catch (IOException e) {
            dest.delete();
            try {
                ftp.logout();
            } catch (IOException e1) {
                // swallow, nothing we can do about it
            }
            try {
                ftp.disconnect();
            } catch (IOException e1) {
                // swallow, nothing we can do about it
            }
            throw new FileDownloadException("ERROR: problem downloading file.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // swallow, nothing we can do
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // swallow, nothing we can do
                }
            }
        }
        return false;
    }

    /**
     * Copy contents of a URL to a file using the {@link URL} class.
     * <p>
     * This works for the HTTP and the HTTPS protocol and for FTP through a proxy. For plain FTP, we need to use the
     * passive mode.
     */
    private boolean copyURLToFileThroughURL(URL src, File dest) throws FileDownloadException {
        setProxyProperties();

        // actually copy the file
        BufferedInputStream in;
        FileOutputStream out;
        try {
            URLConnection connection =  src.openConnection();
            final int fileSize = connection.getContentLength();
            in = new BufferedInputStream(connection.getInputStream());
            out = new FileOutputStream(dest);

            ProgressBar pb = null;
            if (fileSize != -1)
                pb = new ProgressBar(0, fileSize, options.printProgressBar);
            else
                LOGGER.info("(server did not tell us the file size, no progress bar)");

            // Download file.
            byte [] buffer = new byte[128 * 1024];
            int readCount;
            long pos = 0;
            if (pb != null)
                pb.print(pos);

            while ((readCount = in.read(buffer)) > 0) {
                out.write(buffer, 0, readCount);
                pos += readCount;
                if (pb != null)
                    pb.print(pos);
            }
            in.close();
            out.close();
            if (pb != null && pos != pb.getMax())
                pb.print(fileSize);
        } catch (IOException e) {
            throw new FileDownloadException("ERROR: Problem downloading file: " + e.getMessage());
        }
        return true;
    }

    /**
     * Set system properties from {@link #options}.
     */
    private void setProxyProperties() {
        if (options.ftp.host != null)
            System.setProperty("ftp.proxyHost", options.ftp.host);
        if (options.ftp.port != -1)
            System.setProperty("ftp.proxyPort", Integer.toString(options.ftp.port));
        if (options.ftp.user != null)
            System.setProperty("ftp.proxyUser", options.ftp.user);
        if (options.ftp.password != null)
            System.setProperty("ftp.proxyPassword", options.ftp.password);

        if (options.http.host != null)
            System.setProperty("http.proxyHost", options.http.host);
        if (options.http.port != -1)
            System.setProperty("http.proxyPort", Integer.toString(options.http.port));
        if (options.http.user != null)
            System.setProperty("http.proxyUser", options.http.user);
        if (options.http.password != null)
            System.setProperty("http.proxyPassword", options.http.password);

        if (options.https.host != null)
            System.setProperty("https.proxyHost", options.https.host);
        if (options.https.port != -1)
            System.setProperty("https.proxyPort", Integer.toString(options.https.port));
        if (options.https.user != null)
            System.setProperty("https.proxyUser", options.https.user);
        if (options.https.password != null)
            System.setProperty("https.proxyPassword", options.https.password);
    }

}
