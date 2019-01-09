package org.monarchinitiative.hpoannotqc.exception;


import org.monarchinitiative.phenol.base.PhenolException;

/**
 * Exception that can be called if something went wrong while downloading the transcript files.
 *
 * @author <a href="mailto:marten.jaeger@charite.de">Marten Jaeger</a>
 */
public class FileDownloadException extends PhenolException {
    private static final long serialVersionUID = 1L;

    public FileDownloadException(String msg) {
        super(msg);
    }

    public FileDownloadException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
