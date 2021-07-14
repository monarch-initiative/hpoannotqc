package org.monarchinitiative.hpoannotqc.cmd;


import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.hpoannotqc.analysis.SmallFileCleaner;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationFileParser;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModel;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModelException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "clean", aliases = {"C"}, mixinStandardHelpOptions = true, description = "Create clean phenotype.hpoa file")
public class CleanCommand implements Callable<Integer> {
    private final Logger logger = LoggerFactory.getLogger(CleanCommand.class);
    /** Directory with hp.obo and en_product>HPO.xml files. */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    @CommandLine.Option(names={"-a","--annot"},
            description = "Path to directory with the ca. 7900 HPO Annotation files",
            required = true)
    private String hpoAnnotationFileDirectory;
    /** Should usually be phenotype.hpoa, may also include path */
    @CommandLine.Option(names={"-o","--output"},
            description="name of output file (default: ${DEFAULT-VALUE})")
    private String outputFilePath = "phenotype.hpoa";
    @CommandLine.Option(names={"-m","--merge"},
            description="merge frequency data (default: ${DEFAULT-VALUE})")
    private boolean merge_frequency=true;
    @CommandLine.Option(names="--tolerant",
            description = "tolerant mode (update obsolete term ids if possible; default: ${DEFAULT-VALUE})")
    private boolean tolerant = true;

    @CommandLine.Option(names = {"--progress"}, required = true, description = "file with entries that are done")
    private String progressDoc;

    /**
     * List of all of the {@link HpoAnnotationModel} objects, which represent annotated diseases.
     */
    private final List<HpoAnnotationModel> smallFileList = new ArrayList<>();

    int n_total_annotation_lines = 0;

    /**
     * The paths to all of the small files, e.g., OMIM-600301.tab.
     */
    private List<File> smallFilePaths;

    /**
     * Names of entries (small files) that we will omit because they do not represent diseases.
     */
    private Set<String> omitEntries;

    private  Ontology ontology;
    /** We keep a set of files we have previously processed. This allows us to go through step by step and check things.*/
    private Set<String> progressDoneAlready;

    /** Command to create the{@code phenotype.hpoa} file from the various small HPO Annotation files. */
    public CleanCommand() {


    }

    @Override
    public Integer call() {
        String hpOboPath = String.format("%s%s%s",downloadDirectory, File.separator, "hp.obo" );
        ontology = OntologyLoader.loadOntology(new File(hpOboPath));
        // path to the omit-list.txt file, which is located with the small files in the same directory
        logger.info("annotation directory = "+hpoAnnotationFileDirectory);
        String omitFile = hpoAnnotationFileDirectory + File.separator + "omit-list.txt";
        omitEntries = getOmitEntries(omitFile);
        smallFilePaths = getListOfV2SmallFiles(hpoAnnotationFileDirectory);
        getProgressDoc();
        int N_ENTRIES = 7;
        doNextEntries(N_ENTRIES);
        outputDoneEntries();
        return 0;
    }

    private void outputDoneEntries() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(progressDoc))) {
            for (String s : progressDoneAlready) {
                writer.write(s + "\n");
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
    }

    private void doNextEntries(int n_entries) {
        int n = 0;
        for (File file : smallFilePaths) {
            if (progressDoneAlready.contains(file.getAbsolutePath())) {
                continue;
            } else {
                progressDoneAlready.add(file.getAbsolutePath());
            }
            SmallFileCleaner cleaner = new SmallFileCleaner(file.getAbsolutePath());
            if (cleaner.wasChanged()) {
                System.out.println("[INFO] Cleaning " + file.getName());
                List<String> cleansedLines = cleaner.getNewLines();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : cleansedLines) {
                        writer.write(line + "\n");
                    }
                } catch (IOException e) {
                    throw new PhenolRuntimeException(e.getMessage());
                }
                if (++n > n_entries) {
                    break;
                }
            }

        }
        System.out.printf("Done: n=%d\n", progressDoneAlready.size());
    }


    private void getProgressDoc() {
        progressDoneAlready = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.progressDoc))) {
            String line;
            while((line = br.readLine()) != null) {
                progressDoneAlready.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This is the format of the omit-list.txt file.
     * Thus, we need to extract only the first field.
     * <pre></pre>
     * #List of OMIM entries that we want to omit from further analysis
     * #DiseaseId    Reason
     * OMIM:107850   trait
     * OMIM:147320   legacy
     * </pre>
     * If this file is not passed (is null or empty), then we return the empty set.
     *
     * @param path the path to {@code omit-list.txt}
     * @return List of entries (encoded as strings like "OMIM:600123") that should be omitted
     */
    private Set<String> getOmitEntries(String path) {
        if (path == null || path.isEmpty()) return ImmutableSet.of();
        Set<String> entrylist = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue; // skip comment
                String[] A = line.split("\\s+");
                String id = A[0]; // the first field has items such as OMIM:500123
                entrylist.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entrylist;
    }


    /**
     * Get the entry Curie for a certain path
     *
     * @param path e.g., /.../rare-diseases/annotated/OMIM-600123.tab
     * @return the corresinding Curie, e.g., OMIM:600123
     */
    private String baseName(Path path) {
        String bname = path.getFileName().toString();
        bname = bname.replace('-', ':').replace(".tab", "");
        return bname;
    }


    private List<File> getListOfV2SmallFiles(String smallFileDirectory) {
        List<File> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(smallFileDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    String basename = baseName(path);
                    if (omitEntries.contains(basename)) {
                        continue; // skip this one!
                    }
                    fileNames.add(new File(path.toString()));
                }
            }
        } catch (IOException ex) {
            logger.error("Could not get list of small smallFilePaths from {} [{}]. Terminating...",
                    smallFileDirectory, ex);
            throw new PhenolRuntimeException("Could not get small files");
        }
        return fileNames;
    }


}
