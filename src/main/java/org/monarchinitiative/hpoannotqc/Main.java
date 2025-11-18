package org.monarchinitiative.hpoannotqc;
import org.monarchinitiative.hpoannotqc.cmd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;


/**
 * This application will perform quality control on the HPO rare disease "small file" annotation files, will transform the
 * small files into an integrate "large" file (phenotype_annotation.tab), and print a report to file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.16 (2018-01-02)
 */
@CommandLine.Command(name = "java -jar HpoAnnotQc.jar",
        mixinStandardHelpOptions = true,
        version = "2.0.0",
        description = "Hpo Annotation Quality Control.")
public class Main implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        logger.trace("Starting HPO Annotation Quality Control");
        CommandLine cline = new CommandLine(new Main()).
                addSubcommand("download", new DownloadCommand()).
                addSubcommand("big-file", new BigFileCommand()).
                addSubcommand("small-file-qc", new SmallFileQcCommand()).
                addSubcommand("supplemental-files", new SupplementalFilesCommand());
        cline.setToggleBooleanFlags(false);
        if (args.length == 0) {
            args = new String[]{"-h"};
        }
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }
}
