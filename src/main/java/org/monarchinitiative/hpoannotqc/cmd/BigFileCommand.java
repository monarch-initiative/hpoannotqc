package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.hpoannotqc.annotations.*;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteAspectError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteTermError;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Orchestrates the generation of the {@code phenotype.hpoa} file by:
 * 1. Ingesting HPO annotation small files (OMIM, DECIPHER)
 * 2. Parsing Orphanet XML data
 * 3. Merging inheritance information
 * 4. Generating output lines
 * 5. Writing the final file
 *
 * This command follows the Single Responsibility Principle by delegating
 * specific tasks to specialized classes.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@CommandLine.Command(name = "big-file", aliases = {
        "B" }, mixinStandardHelpOptions = true, description = "Create phenotype.hpoa file")
public class BigFileCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BigFileCommand.class);
    /**
     * Path to the {@code hp.json} file (optional; will be derived from the data
     * download by default).
     */
    @CommandLine.Option(names = { "-j",
            "--hpo" }, description = "custom path to hp.json (default: get it from data directory)")
    private String hpJsonPath = null;
    /** Directory with hp.json and en_product>HPO.xml files. */
    @CommandLine.Option(names = { "-d",
            "--data" }, description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    @CommandLine.Option(names = { "-a",
            "--annot" }, description = "Path to directory with the ca. 7900 HPO Annotation files", required = true)
    private String hpoAnnotationFileDirectory;
    /** Should usually be phenotype.hpoa, may also include path */
    @CommandLine.Option(names = { "-o", "--output" }, description = "name of output file (default: ${DEFAULT-VALUE})")
    private String outputFilePath = "phenotype.hpoa";
    @CommandLine.Option(names = "--tolerant", description = "tolerant mode (update obsolete term ids if possible; default: ${DEFAULT-VALUE})")
    private boolean tolerant = true;

    /**
     * Command to create the{@code phenotype.hpoa} file from the various small HPO
     * Annotation files.
     */
    public BigFileCommand() {
        if (hpJsonPath == null) {
            hpJsonPath = String.format("%s%s%s", downloadDirectory, File.separator, "hp.json");
        }
        File f = new File(hpJsonPath);
        if (!f.isFile()) {
            String err = String.format("Could not find hp.jon file at \"%s\".", hpJsonPath);
            LOGGER.error(err);
            throw new PhenolRuntimeException(err);
        }
    }

    @Override
    public Integer call() {
        try {
            LOGGER.info("Starting phenotype.hpoa file generation");
            LOGGER.info("Annotation directory: {}", hpoAnnotationFileDirectory);

            // 1. Load HPO Ontology
            LOGGER.info("Loading HPO ontology from: {}", hpJsonPath);
            Ontology ontology = OntologyLoader.loadOntology(new File(hpJsonPath));

            // 2. Ingest internal annotation files (OMIM, DECIPHER)
            LOGGER.info("Ingesting internal annotation files from: {}", hpoAnnotationFileDirectory);
            Set<String> omitList = OmitListReader.readOmitList(null);
            List<File> smallFiles = AnnotationFileDiscovery.discoverFiles(hpoAnnotationFileDirectory, omitList);
            List<HpoAnnotationModel> internalModels = new ArrayList<>();

            for (File f : smallFiles) {
                internalModels.add(HpoAnnotationFileParser.parse(f).getMergedModel());
            }

            LOGGER.info("Ingested {} internal annotation models", internalModels.size());

            // 3. Parse Orphanet inheritance data
            String orphanetInheritanceXmlPath = String.format("%s%s%s",
                    downloadDirectory, File.separator, "en_product9_ages.xml");
            LOGGER.info("Parsing Orphanet inheritance data from: {}", orphanetInheritanceXmlPath);
            OrphanetInheritanceXMLParser inheritanceParser = new OrphanetInheritanceXMLParser(
                    orphanetInheritanceXmlPath,
                    ontology
            );
            Map<TermId, Collection<HpoAnnotationEntry>> inheritanceMap =
                    inheritanceParser.getDisease2inheritanceMultimap();
            LOGGER.info("Parsed {} Orphanet inheritance entries", inheritanceMap.size());

            if (inheritanceParser.hasError()) {
                for (String error : inheritanceParser.getErrorlist()) {
                    LOGGER.warn(error);
                }
            }

            // 4. Parse Orphanet phenotype data
            String orphanetPhenotypeXmlPath = String.format("%s%s%s",
                    downloadDirectory, File.separator, "en_product4.xml");
            LOGGER.info("Parsing Orphanet phenotype data from: {}", orphanetPhenotypeXmlPath);
            OrphanetXML2HpoDiseaseModelParser orphanetParser = new OrphanetXML2HpoDiseaseModelParser(
                    orphanetPhenotypeXmlPath,
                    ontology,
                    tolerant
            );
            Map<TermId, HpoAnnotationModel> orphanetDiseaseMap = orphanetParser.getOrphanetDiseaseMap();
            LOGGER.info("Parsed {} Orphanet disease entries", orphanetDiseaseMap.size());

            // Auto-update orphanet inconsistencies with phenotype or onset
            int updateCount = 0;
            int obsoleteCount = 0;
            for (HpoAnnotationModel model : orphanetDiseaseMap.values()){
                List<HpoAnnotationEntry> entries = new ArrayList<>();
                boolean updated = false;
                for (HpoAnnotationEntry entry : model.getEntryList()) {
                    try {
                        HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
                        entries.add(entry);
                    } catch (ObsoleteTermError e){
                        entries.add(entry.withUpdatedPhenotype(e.getPrimaryId(), e.getTermLabel()));
                        updated = true;
                    } catch (ObsoleteAspectError e){
                        entries.add(entry.withUpdatedOnset(e.getPrimaryId().toString(), e.getTermLabel()));
                        updated = true;
                    } catch (HpoaEntryError e) {
                        LOGGER.warn("Obsolete term update failed for Orphanet entry: {}", e.getMessage());
                        if (e.getMessage().contains("Could not find")){
                            // likely an unresolvable obsolete term
                            updated = true;
                            obsoleteCount++;
                        }
                    } catch (PhenolRuntimeException e){
                        LOGGER.error("Validation error for Orphanet entry: {}", e.getMessage());
                        System.exit(1);
                    }
                }
                if (updated){
                    updateCount++;
                    HpoAnnotationModel updatedModel = new HpoAnnotationModel(model.getBasename(), entries);
                    orphanetDiseaseMap.put(model.getDiseaseId(), updatedModel);
                }
            }
            LOGGER.info("Removed {} Orphanet entries with unresolvable obsolete terms", obsoleteCount);
            LOGGER.info("Updated {} Orphanet disease models with obsolete term fixes", updateCount);

            // 5. Merge inheritance data with Orphanet models
            int mergedCount = 0;
            for (TermId diseaseId : orphanetDiseaseMap.keySet()) {
                if (inheritanceMap.containsKey(diseaseId)) {
                    HpoAnnotationModel model = orphanetDiseaseMap.get(diseaseId);
                    Collection<HpoAnnotationEntry> inheritanceEntries = inheritanceMap.get(diseaseId);
                    HpoAnnotationModel mergedModel = model.mergeWithInheritanceAnnotations(inheritanceEntries);
                    orphanetDiseaseMap.put(diseaseId, mergedModel);
                    mergedCount++;
                }
            }
            LOGGER.info("Merged inheritance data into {} Orphanet disease models", mergedCount);

            List<HpoAnnotationModel> orphanetModels = new ArrayList<>(orphanetDiseaseMap.values());

            // 6. Generate all output lines
            LOGGER.info("Generating output lines");
            PhenotypeDotHpoaLineGenerator lineGenerator = new PhenotypeDotHpoaLineGenerator(ontology);
            List<String> allLines = lineGenerator.generateAllLines(
                    internalModels,
                    orphanetModels,
                    ontology.getMetaInfo()
            );

            // 7. Write lines to output file
            LOGGER.info("Writing output to: {}", outputFilePath);
            PhenotypeDotHpoaFileWriter.writeLines(allLines, outputFilePath);

            LOGGER.info("Successfully generated phenotype.hpoa file");
            return 0;

        } catch (IOException e) {
            LOGGER.error("Failed to write phenotype.hpoa file", e);
            return 1;
        } catch (PhenolRuntimeException e) {
            LOGGER.error("Runtime error during file generation", e);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during file generation", e);
            return 1;
        }
    }

}
