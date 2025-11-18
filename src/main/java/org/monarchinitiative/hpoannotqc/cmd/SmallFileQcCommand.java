package org.monarchinitiative.hpoannotqc.cmd;


import org.monarchinitiative.hpoannotqc.annotations.*;
import org.monarchinitiative.hpoannotqc.annotations.error.*;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
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
 * Performs sanity-checking on the entire repo of small HPO Annotation files.
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
						LOGGER.warn("Updatable obsolete term id or label found in file {}: {}", f.getName(), obsoleteTermError.getMessage());
						updateableLine++;
						fileHasUpdates = true;
						if (updateObsolete) {
							HpoAnnotationEntry updatedEntry = entry.withUpdatedPhenotype(
								obsoleteTermError.getPrimaryId(),
								obsoleteTermError.getTermLabel()
							);
							processedEntries.add(updatedEntry);
							LOGGER.info("Updated entry in {}: {} -> {}", f.getName(), entry.getPhenotypeId().getValue(), obsoleteTermError.getPrimaryId().getValue());
						} else {
							processedEntries.add(entry);
						}
					} catch (ObsoleteAspectError obsoleteAspectError) {
						LOGGER.warn("Updatable obsolete aspect id or label found in file {}: {}", f.getName(), obsoleteAspectError.getMessage());
						updateableLine++;
						fileHasUpdates = true;
						if (updateObsolete) {
							HpoAnnotationEntry updatedEntry = entry.withUpdatedOnset(
								obsoleteAspectError.getPrimaryId().getValue(),
								obsoleteAspectError.getTermLabel()
							);
							processedEntries.add(updatedEntry);
							LOGGER.info("Updated entry in {}: {} -> {}", f.getName(), entry.getAgeOfOnsetId(), obsoleteAspectError.getPrimaryId().getValue());
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

				// Write updated model back to file if updates were applied
				if (updateObsolete && fileHasUpdates) {
					HpoAnnotationModel updatedModel = new HpoAnnotationModel(model.getBasename(), processedEntries);
					HpoAnnotationFileWriter.write(updatedModel, f);
					LOGGER.info("Wrote updated file: {}", f.getName());
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
