package org.monarchinitiative.hpoannotqc.cmd;

/**
 * Interface for command execution in the HPO Annotation Quality Control system.
 *
 * <p>This interface defines the contract for all command implementations that can be
 * executed within the application. Each command encapsulates a specific operation
 * or workflow related to HPO annotation processing.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public interface Command {

    /**
     * Executes the command's primary operation.
     *
     * <p>Implementations should handle all necessary processing steps and error handling
     * for their specific functionality.</p>
     */
    void execute();
}
