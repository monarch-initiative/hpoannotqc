package org.monarchinitiative.hpoannotqc.cmd;


import org.monarchinitiative.hpoannotqc.annotations.*;
import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteAspectError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteTermError;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.System.exit;

/**
 * Command for performing quality control on HPO annotation small files.
 *
 * <p>This command validates the entire repository of HPO annotation small files by:</p>
 * <ul>
 *   <li>Checking for obsolete HPO term IDs and suggesting current replacements</li>
 *   <li>Validating annotation format and structure</li>
 *   <li>Identifying updateable entries with obsolete terms</li>
 *   <li>Optionally updating obsolete terms to their current primary IDs</li>
 *   <li>Generating comprehensive quality control reports</li>
 * </ul>
 *
 * <p>The command processes all annotation files in the specified directory and
 * provides detailed feedback about data quality issues, helping maintainers
 * keep the annotation files current and valid.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */

@CommandLine.Command(name = "small-file-qc", aliases = {"Q"}, mixinStandardHelpOptions = true, description = "Quality Control the small HPO Annotation files")
public class SmallFileQcCommand implements Callable<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmallFileQcCommand.class);

	@CommandLine.Option(names = {"-a", "--annot"},
			description = "Path to directory with the ca. 7900 HPO Annotation files",
			required = true)
	private String hpoAnnotationFileDirectory;

	@CommandLine.Option(names = {"--hpo"},
			description = "Path to hpo.json file", required = true)
	private String hpoJson;
	@CommandLine.Option(names = {"--update-obsolete"},
			description = "Update obsolete HPO term IDs to primary IDs where possible (default: ${DEFAULT-VALUE})", required = true)
	private boolean updateObsolete = false;

	private int validLine = 0;
	private int invalidLine = 0;
	private int updateableLine = 0;

	@Override
	public Integer call() {
		LOGGER.info("Starting small file quality control.");

		// 1. Load HPO Ontology
		LOGGER.info("Loading HPO ontology from: {}", hpoJson);
		Ontology ontology = OntologyLoader.loadOntology(new File(hpoJson));

		// 2. Ingest internal annotation files (OMIM, DECIPHER)
		LOGGER.info("Ingesting internal annotation files from: {}", hpoAnnotationFileDirectory);
		Set<String> omitList = OmitListReader.readOmitList(null);
		List<File> smallFiles = AnnotationFileDiscovery.discoverFiles(hpoAnnotationFileDirectory, omitList);

		for (File f : smallFiles) {
			try {
				HpoAnnotationModel model = HpoAnnotationFileParser.parse(f);
				List<HpoAnnotationEntry> processedEntries = new ArrayList<>();
				boolean fileHasUpdates = false;

				for (HpoAnnotationEntry entry : model.getEntryList()) {
					try {
						HpoAnnotationEntryValidator.performQualityControl(entry, ontology);
						processedEntries.add(entry);
						validLine++;
					} catch (ObsoleteTermError obsoleteTermError) {
						updateableLine++;
						fileHasUpdates = true;
						if (updateObsolete) {
							HpoAnnotationEntry updatedEntry = entry.withUpdatedPhenotype(
								obsoleteTermError.getPrimaryId(),
								obsoleteTermError.getPrimaryLabel()
							);
							processedEntries.add(updatedEntry);
						} else {
							processedEntries.add(entry);
						}
					} catch (ObsoleteAspectError obsoleteAspectError) {
						updateableLine++;
						fileHasUpdates = true;
						if (updateObsolete) {
							HpoAnnotationEntry updatedEntry = entry.withUpdatedOnset(
								obsoleteAspectError.getPrimaryId().getValue(),
								obsoleteAspectError.getTermLabel()
							);
							processedEntries.add(updatedEntry);
						} else {
							processedEntries.add(entry);
						}
					} catch (HpoaEntryError entryError) {
						LOGGER.error("Entry error in file {}: {}", f.getName(), entryError.getMessageWithDisease());
						invalidLine++;
					} catch (PhenolRuntimeException e) {
						LOGGER.error("Validation error in file {}: {}", f.getName(), e.getMessage());
						invalidLine++;
					}
				}

				// Write updated model back to file if updates were applied only if we are completely valid
				if (updateObsolete && fileHasUpdates && invalidLine == 0){
					HpoAnnotationModel updatedModel = new HpoAnnotationModel(model.getBasename(), processedEntries);
					HpoAnnotationFileWriter.write(updatedModel, f);
				} else if (invalidLine > 0){
					System.exit(1);
				}
			} catch (IOException e) {
				LOGGER.error("Error reading file {}: {}", f.getName(), e.getMessage());
				exit(1);
			}
		}

		LOGGER.info("Valid lines: {}; updateable lines: {} invalid lines: {}", validLine, updateableLine, invalidLine);

		return null;
	}
}
