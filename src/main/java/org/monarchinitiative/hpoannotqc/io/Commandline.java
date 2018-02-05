package org.monarchinitiative.hpoannotqc.io;




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

    private String downloadDirectory;
    private String hpoOboPath = null;
    private String oldSmallFileAnnotationPath = null;
    private String termid = null;


    private String outputFilePath = null;
    private String outputDirectory = null;



    public Commandline(String args[]) {
        final CommandLineParser cmdLineGnuParser = new DefaultParser();

        final Options gnuOptions = constructGnuOptions();
        org.apache.commons.cli.CommandLine commandLine;

        String mycommand = null;
        String clstring = "";
        if (args != null && args.length > 0) {
            clstring = Arrays.stream(args).collect(Collectors.joining(" "));
        }
        try {
            commandLine = cmdLineGnuParser.parse(gnuOptions, args);
            String category[] = commandLine.getArgs();
            if (category.length < 1) {
                printUsage("command missing");
            } else {
                mycommand = category[0];

            }
            if (commandLine.getArgs().length < 1) {
                printUsage("no arguments passed");
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
            printUsage(msg);
        }
        if (mycommand.equals("download")) {
            this.command = new DownloadCommand(this.downloadDirectory);
        } else if (mycommand.equals("convert")) {
            this.command=new OldSmallFileConvertCommand(this.hpoOboPath,this.oldSmallFileAnnotationPath);
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
                .addOption("a", "annot", true, "path to HPO annotation file")
                .addOption("h", "hpo", true, "path to hp.obo");
//                .addOption("b", "bad", false, "output bad (rejected) reads to separated file")
//                .addOption(Option.builder("f1").longOpt("file1").desc("path to fastq file 1").hasArg(true).argName("file1").build())
//                .addOption(Option.builder("f2").longOpt("file2").desc("path to fastq file 2").hasArg(true).argName("file2").build());
        return options;
    }

    public static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = Commandline.class.getPackage();
            version = p.getImplementationVersion();
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
        final PrintWriter writer = new PrintWriter(System.out);
        // final HelpFormatter usageFormatter = new HelpFormatter();
        // final String applicationName="java -jar diachromatic.jar command";
        // final Options options=constructGnuOptions();
        writer.println(message);
        writer.println();
        //usageFormatter.printUsage(writer, 120, applicationName, options);
        writer.println("Program: HPO Annotation QC");
        writer.println("Version: " + version);
        writer.println();
        writer.println("Usage: java -jar HpoAnnotatQc.jar <command> [options]");
        writer.println();
        writer.println("Available commands:");
        writer.println();
        writer.println("download:");
        writer.println("\tjava -jar HPOWorkbench.jar download  [-d <directory>]");
        writer.println("\t<directory>: name of directory to which HPO data will be downloaded (default:\"data\")");
        writer.println();
        writer.println("transform:");
        writer.println("\tjava -jar HPOWorkbench.jar countfreq [-h <hpo.obo>] [-a <pheno_annot.tab>] -t <term id> \\");
        writer.println("\t<hp.obo>: path to hp.obo file (default: \"data/hp.obo\")");
        writer.println("\t<pheno_annot.tab>: path to annotation file (default \"data/phenotype_annotation.tab\")");
        writer.println("\t<term>: HPO term id (e.g., HP:0000123)");
        writer.println();
        writer.println("csv:");
        writer.println("\tjava -jar HPOWorkbench.jar csv -h <hpo> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println();
        writer.println("convert:");
        writer.println("\tjava -jar HPOWorkbench.jar convert -h <hpo> -d <directory> \\");
        writer.println("\t<hpo>: path to hp.obo file");
        writer.println("\t<directory>: path to directory with RD annotation files");
        writer.println(String.format("\t<outfile>: optional name of output file (Default: \"%s.bam\")", DEFAULT_OUTPUT_BAM_NAME));
        writer.println();

        System.exit(0);
    }

}
