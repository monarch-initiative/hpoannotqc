package org.monarchinitiative.hpoannotqc;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


import org.monarchinitiative.hpoannotqc.cmd.BigFileCommand;
import org.monarchinitiative.hpoannotqc.cmd.Command;
import org.monarchinitiative.hpoannotqc.cmd.DownloadCommand;
import org.monarchinitiative.hpoannotqc.cmd.Genes2PhenotypesCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This application will perform Q/C on the HPO rare disease "small file" annotation files, will transform the
 * small files into an integrate "large" file (phenotype_annotation.tab), and print a report to file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.16 (2018-01-02)
 */

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    @Parameter(names = {"-h", "--help"}, help = true, description = "display this help message")
    private boolean usageHelpRequested;


    public static void main(String[] args) {
        logger.trace("Starting HPO AnnotQC");
        DownloadCommand download = new DownloadCommand();
        BigFileCommand bigfile = new BigFileCommand();
        Genes2PhenotypesCommand genes2phenes = new Genes2PhenotypesCommand();
        Main main = new Main();

        JCommander jc = JCommander.newBuilder()
                .addObject(main)
                .addCommand("download", download)
                .addCommand("big-file", bigfile)
                .addCommand("gene2phen",genes2phenes)
                .build();
        jc.setProgramName("java -jar HpoAnnotQc.jar");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println("[ERROR] "+e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (jc.getParsedCommand()==null ) {
            System.err.println("[ERROR] no command passed");
            jc.usage();
            System.exit(1);
        }

        if ( main.usageHelpRequested) {
            jc.usage();
            System.exit(1);
        }

        String command = jc.getParsedCommand();
        Command qccommand=null;

        switch (command) {
            case "download":
                qccommand = download;
                break;
            case "big-file":
                qccommand = bigfile;
                break;
            case "gene2phen":
                qccommand = genes2phenes;
                break;
            default:
                System.err.println(String.format("[ERROR] command \"%s\" not recognized",command));
                jc.usage();
                System.exit(1);
        }
        try {
            qccommand.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
