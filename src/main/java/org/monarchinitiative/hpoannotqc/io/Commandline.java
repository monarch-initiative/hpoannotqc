package org.monarchinitiative.hpoannotqc.io;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.cmd.BigFileCommand;
import org.monarchinitiative.hpoannotqc.cmd.Command;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.monarchinitiative.hpoannotqc.cmd.DownloadCommand;


import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class to capture options and command from the command line.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.3 (2018-03-12)
 */
public class Commandline {
    private static final Logger logger = LogManager.getLogger();
    private Command command = null;
    /**
     * The default name of the file that is produced by the {@code digest} command.
     */
    private final static String DEFAULT_DOWNLOAD_DIRECTORY = "data";

    private final static String DEFAULT_HPO_OBOPATH = String.format("%s%s%s",
            DEFAULT_DOWNLOAD_DIRECTORY, File.separator,"hp.obo");

    private final static String DEFAULT_ANNOTATION_OBOPATH = String.format("%s%s%s",
            DEFAULT_DOWNLOAD_DIRECTORY, File.separator,"phenotype_annotation.tab");


    private final static String DEFAULT_TRUNCATION_SUFFIX = "truncated";

    private final static String DEFAULT_OUTPUT_BAM_NAME = "diachromatic-processed";

    private final static String DEFAULT_V2_SMALL_FILE_DIRECTORY="v2files";

    private final static String DEFAULT_ORPHANET_XML_FILE="data/en_product4_HPO.xml";

    private final static String DEFAULT_BIG_FILE_VERSION="v2";


    private String downloadDirectory;
    private String hpoOboPath = null;
    private String oldSmallFileAnnotationPath = null;
    private String termid = null;
    private String bigFileVersion = null;
    private String orphanetXmlPath = null;
    private String smallFileDirectory = null;
    /** Depending on the command, path to output directory or output file. */
    private String outputPath = null;




    public Commandline(String args[]) {
        final CommandLineParser cmdLineGnuParser = new DefaultParser();
        final Options gnuOptions = constructGnuOptions();
        org.apache.commons.cli.CommandLine commandLine;

        String mycommand = null;
        String clstring = Arrays.stream(args).collect(Collectors.joining(" "));
        if (args == null || args.length ==0) {
            printUsage("[ERROR] Failed to pass a command");
        }
        try {
            commandLine = cmdLineGnuParser.parse(gnuOptions, args);
            String category[] = commandLine.getArgs();
            if (category.length < 1) {
                logger.trace("mycommand is NOT ");
                printUsage("command missing");
            } else {
                mycommand = category[0];
            }

            if (commandLine.getArgs().length < 1) {
                printUsage("no arguments passed");
                logger.error("no arguments pas");
                return;
            }
            if (commandLine.hasOption("a")) {
                oldSmallFileAnnotationPath = commandLine.getOptionValue("a");
            }
            if (commandLine.hasOption("d")) {
                this.downloadDirectory = commandLine.getOptionValue("d");
            } else {
                this.downloadDirectory = DEFAULT_DOWNLOAD_DIRECTORY;
            }
            if (commandLine.hasOption("h")) {
                this.hpoOboPath = commandLine.getOptionValue("h");
            } else {
                this.hpoOboPath=DEFAULT_HPO_OBOPATH;
            }
            if (commandLine.hasOption("o")) {
                this.outputPath=commandLine.getOptionValue("o");
            }
            if (commandLine.hasOption("s")) {
                this.smallFileDirectory= commandLine.getOptionValue("s");
            } else {
                this.smallFileDirectory = DEFAULT_V2_SMALL_FILE_DIRECTORY;
            }
            if (commandLine.hasOption("t")) {
                this.termid = commandLine.getOptionValue("t");
            }
            if (commandLine.hasOption("v")) {
                this.bigFileVersion = commandLine.getOptionValue("v");
            } else {
                this.bigFileVersion = DEFAULT_BIG_FILE_VERSION;
            }
            if (commandLine.hasOption("x")) {
                this.orphanetXmlPath= commandLine.getOptionValue("x");
            } else {
                this.orphanetXmlPath = DEFAULT_ORPHANET_XML_FILE;
            }
        } catch (ParseException parseException)  // checked exception
        {
            String msg = String.format("Could not parse options %s [%s]", clstring, parseException.toString());
            logger.error(msg);
            printUsage(msg);
        }
        if (mycommand.equals("download")) {
            this.command = new DownloadCommand(this.downloadDirectory);
        } else if (mycommand.equals("big-file")) {
            if (outputPath==null) {
                outputPath="phenotype.hpoa";
            }
            this.command=new BigFileCommand(hpoOboPath,smallFileDirectory,orphanetXmlPath,bigFileVersion, outputPath);
        } else {
            printUsage(String.format("[ERROR] Did not recognize command: %s", mycommand));
        }

    }


    public Command getCommand() {
        return command;
    }

    /**
     * Construct and provide GNU-compatible Options.
     *
     * @return Options expected from command-line of GNU form.
     */
    private static Options constructGnuOptions() {
        final Options options = new Options();
        options .addOption("a", "annot", true, "path to HPO annotation directory (old small files")
                .addOption("d", "download", true, "directory to download HPO data (default \"data\")")
                .addOption("h", "hpo", true, "path to hp.obo")
                .addOption("o", "out", true, "name/path of output file/directory")
                .addOption("s","small-files",true,"small file directory")
                .addOption("t", "term", true, "HPO id (e.g., HP:0000123)")
                .addOption("x","orphadata",true,"Orphanet XML file path")
                .addOption("v","bigfile-version",true,"big-file version (v1 or v2 [default])");
        return options;
    }

    public static String getVersion() {
        String version = "0.1.7";// default, should be overwritten by the following.
        try {
            Package p = Commandline.class.getPackage();
            version= p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        return version;
    }

    /**
     * Print usage information to provided OutputStream.
     */
    private static void printUsage(String message) {


        String version = getVersion();
        System.out.println();
        System.out.println(message);
        System.out.println();
        System.out.println("Program: HPO Annotation QC");
        System.out.println("Version: " + version);
        System.out.println();
        System.out.println("Usage: java -jar HpoAnnotatQc.jar <command> [options]");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println();
        System.out.println("download:");
        System.out.println("\tjava -jar HPOWorkbench.jar download  [-d <directory>]");
        System.out.println("\t<directory>: name of directory to which HPO data will be downloaded (default:\"data\")");
        System.out.println();
        System.out.println("big-file:");
        System.out.println("\tjava -jar HPOWorkbench.jar big-file [-s <small>] [-x <xml>]");
        System.out.println("\t<small>: path to directory with small files");
        System.out.println("\t<xml>: path to Orphanet XML file");
        System.out.println();

        System.exit(0);
    }

}
