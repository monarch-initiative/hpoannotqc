package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAssociationData;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoGeneAnnotation;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
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

@CommandLine.Command(name = "phenotype-files", mixinStandardHelpOptions = true, description = "Create genes to phenotypes file and phenotype to genes")
public class HpoPhenotypeFiles implements Callable<Integer> {
    /**
     * Directory with hp.json and en_product6.xml files.
     */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory of hpo data (default: ${DEFAULT-VALUE})")
    private String dataDirectory = ".";
    @CommandLine.Option(names = {"-o, --output"},
            description = "path to output dir (default: ${DEFAULT-VALUE})")
    private String outputDirectory = ".";

    public HpoPhenotypeFiles() {
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

        if (!hpJson.exists()) {
            throw new RuntimeException("Could not find hp.json at " + hpJson);
        }
        Ontology hpoOntology = OntologyLoader.loadOntology(hpJson);
        Set<String> diseaseDatabases = Set.of(DiseaseDatabase.OMIM.prefix(), DiseaseDatabase.ORPHANET.prefix());
        HpoaDiseaseDataContainer diseases = HpoaDiseaseDataLoader.of(diseaseDatabases).loadDiseaseData(hpoAssociations);
        HpoAssociationData hpoAssocationData = HpoAssociationData.builder(hpoOntology).orphaToGenePath(orphaToGenePath)
        .hpoDiseases(diseases).mim2GeneMedgen(omimToGene).hgncCompleteSetArchive(hgncPath).build();
        hpoAssocationData.associations();

        Map<TermId, List<HpoGeneAnnotation>> phenotypeToGene = hpoAssocationData.hpoToGeneAnnotations().stream().collect(Collectors.groupingBy(HpoGeneAnnotation::id));
        // phenotype -> gene, inherits down al the childrens genes
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFilePhenotypeToGene), StandardOpenOption.CREATE)){
            writer.write(String.join("\t", "HpoId", "HpoName", "NCBIGeneId", "GeneSymbol"));
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
            writer.write(String.join("\t",  "NCBIGeneId", "GeneSymbol", "HpoId", "HpoName"));
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
        return 0;
    }
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
