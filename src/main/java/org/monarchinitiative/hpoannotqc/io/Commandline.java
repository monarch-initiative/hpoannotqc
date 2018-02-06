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
import org.monarchinitiative.hpoannotqc.cmd.OldSmallFileConvertCommand;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class to capture options and command from the command line.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.2 (2018-01-05)
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




    private String downloadDirectory;
    private String hpoOboPath = null;
    private String oldSmallFileAnnotationPath = null;
    private String termid = null;


    private String outputFilePath = null;
    private String outputDirectory = null;



    public Commandline(String args[]) {
        final CommandLineParser cmdLineGnuParser = new DefaultParser();
        logger.error("CL");
        final Options gnuOptions = constructGnuOptions();
        org.apache.commons.cli.CommandLine commandLine;

        String mycommand = null;
        String clstring = Arrays.stream(args).collect(Collectors.joining(" "));;
        if (args != null && args.length > 0) {
            logger.trace("Starting with command "+clstring);
        } else {
            String msg = String.format("need to pass command. You passed %s",clstring);
            logger.trace(msg);
            printUsage("Failed to pass a command");
        }
        try {
            commandLine = cmdLineGnuParser.parse(gnuOptions, args);
            String category[] = commandLine.getArgs();
            if (category.length < 1) {
                logger.trace("mycommand is NOT ");
                printUsage("command missing");
            } else {
                mycommand = category[0];
                logger.trace("mycommand is " + mycommand);
            }

            if (commandLine.getArgs().length < 1) {
                printUsage("no arguments passed");
                logger.error("no arguments pas");
                return;
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
            if (commandLine.hasOption("a")) {
                oldSmallFileAnnotationPath = commandLine.getOptionValue("a");
            } else {
                oldSmallFileAnnotationPath =  DEFAULT_ANNOTATION_OBOPATH;
            }
            if (commandLine.hasOption("t")) {
                this.termid = commandLine.getOptionValue("t");
            }
        } catch (ParseException parseException)  // checked exception
        {
            String msg = String.format("Could not parse options %s [%s]", clstring, parseException.toString());
            logger.error(msg);
            printUsage(msg);
        }
        if (mycommand.equals("download")) {
            this.command = new DownloadCommand(this.downloadDirectory);
        } else if (mycommand.equals("convert")) {
            this.command=new OldSmallFileConvertCommand(this.hpoOboPath,this.oldSmallFileAnnotationPath);
        } else if (mycommand.equals("big-file")) {
            this.command=new BigFileCommand(hpoOboPath,DEFAULT_V2_SMALL_FILE_DIRECTORY);
        } else {
            printUsage(String.format("Did not recognize command: %s", mycommand));
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
    public static Options constructGnuOptions() {
        final Options options = new Options();
        options.addOption("o", "out", true, "name/path of output file/directory")
                .addOption("d", "download", true, "directory to download HPO data (default \"data\")")
                .addOption("t", "term", true, "HPO id (e.g., HP:0000123)")
                .addOption("a", "annot", true, "path to HPO annotation directory (old small files")
                .addOption("h", "hpo", true, "path to hp.obo");
//                .addOption("b", "bad", false, "output bad (rejected) reads to separated file")
//                .addOption(Option.builder("f1").longOpt("file1").desc("path to fastq file 1").hasArg(true).argName("file1").build())
//                .addOption(Option.builder("f2").longOpt("file2").desc("path to fastq file 2").hasArg(true).argName("file2").build());
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
    public static void printUsage(String message) {


        String version = getVersion();
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
        System.out.println("convert:");
        System.out.println("\tjava -jar HPOWorkbench.jar convert -h <hpo> -d <directory> ");
        System.out.println("\t<hpo>: path to hp.obo file");
        System.out.println("\t<directory>: path to directory with RD annotation files");
        System.out.println(String.format("\t<outfile>: optional name of output file (Default: \"%s.bam\")", DEFAULT_OUTPUT_BAM_NAME));
        System.out.println();
        System.out.println("big-file:");
        System.out.println("\tjava -jar HPOWorkbench.jar big-file ");
        System.out.println();

        System.exit(0);
    }

}
