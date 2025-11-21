package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generates lines for the phenotype.hpoa file from annotation models.
 * Handles conversion of annotation entries to formatted lines.
 *
 * This class follows the Single Responsibility Principle by only handling
 * line generation logic, not file I/O.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class PhenotypeDotHpoaLineGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhenotypeDotHpoaLineGenerator.class);

    private final Ontology ontology;
    public PhenotypeDotHpoaLineGenerator(Ontology ontology) {
        this.ontology = ontology;
    }

    /**
     * Generates all lines for the phenotype.hpoa file including headers and data.
     *
     * @param internalModels List of HPO annotation models from small files (OMIM, DECIPHER)
     * @param orphanetModels List of HPO annotation models from Orphanet
     * @param ontologyMetadata Metadata from the ontology (version info, etc.)
     * @return List of all lines ready to be written to file
     */
    public List<String> generateAllLines(
            List<HpoAnnotationModel> internalModels,
            List<HpoAnnotationModel> orphanetModels,
            Map<String, String> ontologyMetadata) {

		// Count diseases by type for header
        int nOmim = 0;
        int nDecipher = 0;
        int nUnknown = 0;
        for (HpoAnnotationModel model : internalModels) {
            if (model.isOMIM()) nOmim++;
            else if (model.isDECIPHER()) nDecipher++;
            else nUnknown++;
        }
        int nOrphanet = orphanetModels.size();

        // Add metadata headers
		List<String> allLines = new ArrayList<>(generateMetadataLines(nOmim, nDecipher, nOrphanet, nUnknown, ontologyMetadata));

        // Add column header line
        allLines.add(generateColumnHeaderLine());

        // Generate data lines from internal models
        int internalLineCount = 0;
        for (HpoAnnotationModel model : internalModels) {
            List<String> modelLines = generateLinesFromModel(model);
            allLines.addAll(modelLines);
            internalLineCount += modelLines.size();
        }
        LOGGER.info("Generated {} data lines from internal HPO annotation files", internalLineCount);

        // Generate data lines from Orphanet models
        int orphanetLineCount = 0;
        for (HpoAnnotationModel model : orphanetModels) {
            List<String> modelLines = generateLinesFromModel(model);
            allLines.addAll(modelLines);
            orphanetLineCount += modelLines.size();
        }
        LOGGER.info("Generated {} data lines from Orphanet annotation files", orphanetLineCount);
        LOGGER.info("Total lines generated: {}", allLines.size());

        return allLines;
    }

    /**
     * Generates metadata header lines (description, version, tracker, hpo-version).
     */
    private List<String> generateMetadataLines(
            int nOmim,
            int nDecipher,
            int nOrphanet,
            int nUnknown,
            Map<String, String> ontologyMetadata) {

        List<String> metadataLines = new ArrayList<>();
        String currentDate = getCurrentDate();

        // Description line
        String description = String.format(
                "#description: \"HPO annotations for rare diseases [%d: OMIM; %d: DECIPHER; %d ORPHANET]\"",
                nOmim, nDecipher, nOrphanet);
        if (nUnknown > 0) {
            description = String.format("%s -- warning: %d entries could not be assigned to a database",
                    description, nUnknown);
        }
        metadataLines.add(description);

        // Version line
        metadataLines.add(String.format("#version: %s", currentDate));

        // Tracker line
        metadataLines.add("#tracker: https://github.com/obophenotype/human-phenotype-ontology/issues");

        // HPO version line (if available)
        if (ontologyMetadata.containsKey("release")) {
            if (!ontologyMetadata.get("release").equals(currentDate)) {
                String warning = String.format(
                        "Mismatching release dates - now %s, ontology-release: %s",
                        currentDate, ontologyMetadata.get("release"));
                LOGGER.warn(warning);
            }
            metadataLines.add(String.format("#hpo-version: %s", ontologyMetadata.get("data-version")));
        }

        return metadataLines;
    }

    /**
     * Generates the column header line.
     */
    private String generateColumnHeaderLine() {
        String[] fields = {
                "database_id",
                "disease_name",
                "qualifier",
                "hpo_id",
                "reference",
                "evidence",
                "onset",
                "frequency",
                "sex",
                "modifier",
                "aspect",
                "biocuration"
        };
        return String.join("\t", fields);
    }

    /**
     * Generates data lines from a single annotation model.
     * Handles error checking and logging for problematic entries.
     */
    private List<String> generateLinesFromModel(HpoAnnotationModel model) {
        List<String> lines = new ArrayList<>();

        for (HpoAnnotationEntry entry : model.getEntryList()) {
            try {
                lines.add(entry.toBigFileLine(this.ontology));
            } catch (HpoAnnotQcException e) {
                LOGGER.error("Failed to generate line for entry: {}", e.getMessage());
                // Skip this entry and continue
            }
        }

        return lines;
    }

    /**
     * Gets the current date in yyyy-MM-dd format.
     */
    private String getCurrentDate() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(now);
    }
}
