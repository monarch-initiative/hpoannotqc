package org.monarchinitiative.hpoannotqc.cmd;


import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.*;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This command performs the conversion of the old small files to the new (V2) small file format.
 *
 */
public class OldSmallFileConvertCommand implements Command {
    private static final Logger logger = LogManager.getLogger();

    private final String hpOboPath;
    private final String oldSmallFileAnnotationDirectory;

    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;

    private List<OldSmallFile> osfList=new ArrayList<>();

    private List<V2SmallFile> v2sfList = new ArrayList<>();
    /** Default path for writing the new V2 small files. */
    private final String DEFAULT_OUTPUT_DIRECTORY ="v2files";
    private int n_total_lines=0;
    private int n_corrected_date=0;
    private int n_no_evidence=0;
    private int n_gene_data=0;
    private int n_alt_id=0;
    private int n_update_label=0;
    private int n_created_modifier=0;
    private int n_EQ_item=0;
    private int n_less_than_expected_number_of_lines=0;
    private int n_no_date_created=0;
    private int n_publication_prefix_in_lower_case=0;
    private int n_replaced_empty_publication_string=0;
    private int n_corrected_publication_with_database_but_no_id=0;
    private int n_changed_MIM_to_OMIM=0;
    private int n_changed_PUBMED_to_PMID=0;
    private int n_added_forgotten_colon=0;
    private int n_frequency_with_dash=0;
    private int n_frequency_with_other_correction=0;
    private int n_assigned_by_only_HPO=0;
    private int n_assigned_by_empty =0;
    private int n_converted_n_of_m=0;



    public OldSmallFileConvertCommand(String hpPath, String annotPath) {
        hpOboPath=hpPath;
        if (! annotPath.contains("hpo-annotation-data")) {
            logger.error("Malformed path to old small files. Was expecting /path/..../hpo-annotation-data");
            oldSmallFileAnnotationDirectory=null;
            System.exit(1);
        } else if (annotPath.contains("rare-diseases/annotated")) {
            oldSmallFileAnnotationDirectory=annotPath;
        } else {
            if (annotPath.endsWith(File.separator)) annotPath=annotPath.substring(0,annotPath.length()-1);
            oldSmallFileAnnotationDirectory = String.format("%s%s/rare-diseases/annotated",annotPath,File.separator);
        }
        logger.trace("We will convert the \"old\" small files in " + oldSmallFileAnnotationDirectory);
    }


    private void convertToNewSmallFiles() {
        int i=0;
        osfList.forEach(old -> {
            V2SmallFile v2 = new V2SmallFile(old);
            v2sfList.add(v2);
        });
        try {
            for (V2SmallFile v2 : v2sfList) {
                outputV2file(v2);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.trace(String.format("Wrote %d v2 small files." ,i));
    }


    @Override
    public void execute() {
        logger.trace("hpoPath="+hpOboPath + " annotation path="+ oldSmallFileAnnotationDirectory);
        initOntology();
        List<String> files=getListOfSmallFiles();
        logger.trace("We found " + files.size() + " small files at " + oldSmallFileAnnotationDirectory);

        for (String path : files) {
            OldSmallFile osf = new OldSmallFile(path);
            // The consructor processes the file and tallies the error in the file. The following lines
            // increment the corresponding error counters.
            n_total_lines            += osf.getEntrylist().size();
            n_alt_id                 += osf.getN_alt_id();
            n_corrected_date         += osf.getN_corrected_date();
            n_no_evidence            += osf.getN_no_evidence();
            n_gene_data              += osf.getN_gene_data();
            n_update_label           += osf.getN_update_label();
            n_created_modifier       += osf.getN_created_modifier();
            n_EQ_item                += osf.getN_EQ_item();
            n_less_than_expected_number_of_lines   += osf.getN_less_than_expected_number_of_lines();
            n_no_date_created        += osf.getN_no_date_created();
            n_publication_prefix_in_lower_case     += osf.getN_publication_prefix_in_lower_case();
            n_replaced_empty_publication_string    += osf.getN_replaced_empty_publication_string();
            n_corrected_publication_with_database_but_no_id += osf.getN_corrected_publication_with_database_but_no_id();
            n_changed_MIM_to_OMIM    += osf.getN_changed_MIM_to_OMIM();
            n_changed_PUBMED_to_PMID += osf.getN_changed_PUBMED_to_PMID();
            n_added_forgotten_colon  += osf.getN_added_forgotten_colon();
            n_frequency_with_dash    +=osf.getN_frequency_with_dash();
            n_frequency_with_other_correction      += osf.getN_frequency_with_other_correction();

            n_assigned_by_only_HPO   += osf.getN_assigned_by_only_HPO();
            n_assigned_by_empty      += osf.getN_assigned_by_empty();
            n_converted_n_of_m       += osf.getN_converted_n_of_m();
            osfList.add(osf);
        }

        convertToNewSmallFiles();
        dumpQCtoShell();
    }

    /** Dump a Q/C report about the conversion process to shell. */
    private void dumpQCtoShell() {
        System.out.println("\n\n################################################\n\n");
        System.out.println(String.format("We converted %d \"old\" small files into %d new (V2) small files",
                osfList.size(),v2sfList.size()));
        System.out.println();
        System.out.println("Summary of Q/C results:");
        System.out.println("\tNumber of lines in total: "+n_total_lines);
        System.out.println("\tNumber of lines with corrected date formats: " + n_corrected_date);
        System.out.println("\tNumber of lines with \"Gene\" data that was discarded for the V2 files: " + n_gene_data);
        System.out.println("\tNumber of lines with \"E/Q\" data that was discarded for the V2 files: " + n_EQ_item);
        System.out.println("\tNumber of lines with alt_ids updated to current ids: " + n_alt_id);
        System.out.println("\tNumber of lines with labels updated to current labels: " + n_update_label);
        System.out.println("\tNumber of lines for which no Evidence code was found: "+ n_no_evidence);
        System.out.println("\tNumber of lines for which a Clinical modifer was extracted: "+n_created_modifier);
        System.out.println("\tNumber of lines with less than expected number of fields (given number of fields in header): "+n_less_than_expected_number_of_lines);
        System.out.println("\tNumber of lines with no DATE_CREATED: "+ n_no_date_created);
        System.out.println("\tNumber of lines with publication prefix in lower case (e.g., pmid): "+n_publication_prefix_in_lower_case);
        System.out.println("\tNumber of lines with an empty publication field that was replaced by the databaseID field: " +
                        n_replaced_empty_publication_string);
        System.out.println("\tNumber of lines with a publication field with a database but no id (replaced by databaseID): "
                +n_corrected_publication_with_database_but_no_id);
        System.out.println("\tNumber of lines where we changed MIM to OMIM (prefix): " + n_changed_MIM_to_OMIM);
        System.out.println("\tNumber of lines where we changed PUBMED to PMID: " + n_changed_PUBMED_to_PMID);
        System.out.println("\tNumber of lines where we added a forgotten colon to the publication id: " + n_added_forgotten_colon);
        System.out.println("\tNumber of lines with a dash in the frequency data: " +  n_frequency_with_dash);
        System.out.println("\tNumber of lines with a other frequency correction (e.g., extra %): "+  n_frequency_with_other_correction );
        System.out.println("\tNumber of lines with a only \"HPO\" in assigned by: " +  n_assigned_by_only_HPO);
        System.out.println("\tNumber of lines with empty assigned by: "+  n_assigned_by_empty );
        System.out.println("\tNumber of lines where we converted \"n of m\" to \"n/m\": "+  n_converted_n_of_m );
        System.out.println();
        System.out.println("Number of lines that were Q/C'd or updated have been written to the log (before/after)");

        System.out.println();
        V2LineQualityController.dumpAssignedByMap();
    }

    /** Output the new (v2) small files. Make a new directory if needed. */
    private void outputV2file(V2SmallFile v2) throws IOException {
        String outdir= DEFAULT_OUTPUT_DIRECTORY;
        if (! new File(outdir).exists()) {
            new File(outdir).mkdir();
        }
        String filename = String.format("%s%s%s",outdir,File.separator,v2.getBasename());
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(V2SmallFileEntry.getHeader()+"\n");
        List<V2SmallFileEntry> entryList = v2.getEntryList();
        for (V2SmallFileEntry v2e:entryList) {
            writer.write(v2e.getRow() + "\n");
        }
        writer.close();
    }




    private List<String> getListOfSmallFiles() {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(oldSmallFileAnnotationDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    fileNames.add(path.toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Could not get list of small files. Terminating...");
            System.exit(1);
        }
        return fileNames;
    }


    /** Parse the hp.obo file. Set the static ontology variables in OldSmallFileEntry that we will
     * use to check the entries. We use the ontology objects in {@link OldSmallFileEntry} so we
     * set them using a static setter
     */
    private void initOntology() {
        TermPrefix pref = new ImmutableTermPrefix("HP");
        TermId inheritId = new ImmutableTermId(pref,"0000005");
        try {
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();
            this.abnormalPhenoSubOntology = ontology.getPhenotypicAbnormalitySubOntology();
            this.inheritanceSubontology = ontology.subOntology(inheritId);
        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }
        OldSmallFileEntry.setOntology(ontology);
        if (this.ontology==null) {
            logger.error("We could not parse the HPO ontology. Terminating ...");
            System.exit(1);// not a recoverable error
        }
    }
}
