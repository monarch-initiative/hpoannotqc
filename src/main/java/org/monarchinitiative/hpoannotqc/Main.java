package org.monarchinitiative.hpoannotqc;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.cmd.Command;
import org.monarchinitiative.hpoannotqc.io.Commandline;



/**
 * This application will perform Q/C on the HPO rare disease "small file" annotation files, will transform the
 * small files into an integrate "large" file (phenotype_annotation.tab), and print a report to file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.16 (2018-01-02)
 */

public class Main {
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args) {
        Commandline clp = new Commandline(args);
        Command command = clp.getCommand();
        command.execute();
    }
}
