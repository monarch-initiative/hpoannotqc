package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAssociationData;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoGeneAnnotation;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseData;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseDataContainer;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseDataLoader;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.OntologyTerms;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import picocli.CommandLine;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@CommandLine.Command(name = "supplemental-files", mixinStandardHelpOptions = true, description = "Create g2p, p2g, g2d files")
public class SupplementalFiles implements Callable<Integer> {
    /**
     * Directory with hp.json and en_product6.xml files.
     */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory of hpo data (default: ${DEFAULT-VALUE})")
    private String dataDirectory = ".";
    @CommandLine.Option(names = {"-o, --output"},
            description = "path to output dir (default: ${DEFAULT-VALUE})")
    private String outputDirectory = ".";

    public SupplementalFiles() {
    }


    @Override
    public Integer call() throws IOException {
        final File hpJson = new File(String.format("%s%s%s", dataDirectory, File.separator, "hp.json"));
        final Path orphaToGenePath = Path.of(String.format("%s%s%s", dataDirectory, File.separator, "en_product6.xml"));
        final Path omimToGene = Path.of(String.format("%s%s%s", dataDirectory, File.separator, "mim2gene_medgen"));
        final Path hgncPath = Path.of(String.format("%s%s%s", dataDirectory, File.separator, "hgnc_complete_set.txt"));
        final Path hpoAssociations = Path.of(String.format("%s%s%s", dataDirectory, File.separator, "phenotype.hpoa"));
        final String geneToPhenotypeFileName = "genes_to_phenotype.txt";
        final String outputFileGeneToPhenotype = String.format("%s%s%s", outputDirectory, File.separator, geneToPhenotypeFileName);
        final String phenotypeToGeneFileName = "phenotype_to_genes.txt";
        final String outputFilePhenotypeToGene = String.format("%s%s%s", outputDirectory, File.separator, phenotypeToGeneFileName);
        final String geneToDiseaseFileName = "genes_to_disease.txt";
        final String outputFileGeneToDisease = String.format("%s%s%s", outputDirectory, File.separator, geneToDiseaseFileName);
        if (!hpJson.exists()) {
            throw new RuntimeException("Could not find hp.json at " + hpJson);
        }
        Ontology hpoOntology = OntologyLoader.loadOntology(hpJson);
        Set<DiseaseDatabase> diseaseDatabases = Set.of(DiseaseDatabase.OMIM, DiseaseDatabase.ORPHANET);
        HpoaDiseaseDataContainer diseases = HpoaDiseaseDataLoader.of(diseaseDatabases).loadDiseaseData(hpoAssociations);
        HpoAssociationData hpoAssocationData = HpoAssociationData.builder(hpoOntology).orphaToGenePath(orphaToGenePath)
        .hpoDiseases(diseases).mim2GeneMedgen(omimToGene).hgncCompleteSetArchive(hgncPath).build();
        hpoAssocationData.associations();

        Map<TermId, List<HpoGeneAnnotation>> phenotypeToGene = hpoAssocationData.hpoToGeneAnnotations().stream().collect(Collectors.groupingBy(HpoGeneAnnotation::id));
        // phenotype -> gene, inherits down al the childrens genes
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFilePhenotypeToGene), StandardOpenOption.CREATE)){
            writer.write(String.join("\t", "hpo_id", "hpo_name", "ncbi_gene_id", "gene_symbol"));
            writer.newLine();
            phenotypeToGene.keySet().forEach(phenotype -> {
                        Set<TermId> children = OntologyTerms.childrenOf(phenotype, hpoOntology);
                        final Optional<String> phenotypeLabel = hpoOntology.getTermLabel(phenotype);;
                        if(phenotypeLabel.isEmpty()) {
                            throw new RuntimeException(String.format("Can not find label for phenotype id %s.", phenotype));
                        }
                        // Get all the children genes and unique them
                        // Filter out genes with no symbol
                        List<HpoGeneAnnotation> annotations = children.stream()
                                .flatMap(termId -> phenotypeToGene.getOrDefault(termId, Collections.emptyList()).stream())
                                .filter(distinctByKey(HpoGeneAnnotation::getEntrezGeneId))
                                .filter(g -> !g.getEntrezGeneSymbol().equals("-"))
                                .sorted(Comparator.comparing(HpoGeneAnnotation::id)).collect(Collectors.toList());
                        for (HpoGeneAnnotation annotation: annotations) {
                            try {
                                writer.write(String.join("\t",
                                        phenotype.toString(),
                                        phenotypeLabel.get(),
                                        String.valueOf(annotation.getEntrezGeneId()),
                                        annotation.getEntrezGeneSymbol()));
                                writer.newLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
            writer.flush();
        }
        // Gene -> Phenotype no inheritance
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFileGeneToPhenotype), StandardOpenOption.CREATE)){
            writer.write(String.join("\t",  "ncbi_gene_id", "gene_symbol", "hpo_id", "hpo_name"));
            writer.newLine();
            hpoAssocationData.hpoToGeneAnnotations().stream().sorted(Comparator.comparing(HpoGeneAnnotation::getEntrezGeneId))
                    .forEach(annotation -> {
                        try {
                            writer.write(String.join("\t",
                                    String.valueOf(annotation.getEntrezGeneId()),
                                    annotation.getEntrezGeneSymbol(),
                                    annotation.id().toString(),
                                    annotation.getTermName()
                                    ));
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            writer.flush();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFileGeneToDisease), StandardOpenOption.CREATE)){
            writer.write(String.join("\t",  "ncbi_gene_id", "gene_symbol", "association_type", "disease_id"));
            writer.newLine();
            Map<TermId, String> diseaseNames = diseases.diseaseData().stream().collect(Collectors.toUnmodifiableMap(HpoaDiseaseData::id, HpoaDiseaseData::name));
            hpoAssocationData.associations().diseaseToGeneAssociations().forEach(diseaseAssocation -> {
                    diseaseAssocation.associations().forEach(diseaseGene -> {
                        try {
                            writer.write(String.join("\t",
                                    diseaseGene.geneIdentifier().id().toString(),
                                    diseaseGene.geneIdentifier().symbol(),
                                    diseaseGene.associationType().toString(),
                                    diseaseAssocation.diseaseId().getValue()
                            ));
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            });
            writer.flush();
        }
        return 0;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
