package org.monarchinitiative.hpoannotqc.exception;

/**
 * Base runtime exception for HPO annotation quality control operations.
 *
 * <p>This exception is thrown when errors occur during HPO annotation processing,
 * including parsing errors, validation failures, and data inconsistencies.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public class HpoAnnotQcException extends RuntimeException {

    /**
     * Constructs a new HPO annotation QC exception with no detail message.
     */
    public HpoAnnotQcException() {
        super();
    }

    /**
     * Constructs a new HPO annotation QC exception with the specified detail message.
     *
     * @param msg the detail message explaining the cause of the exception
     */
    public HpoAnnotQcException(String msg) {
        super(msg);
    }
}
