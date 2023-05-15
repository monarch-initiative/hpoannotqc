package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.phenol.annotations.formats.hpo.AnnotatedItem;
import org.monarchinitiative.phenol.annotations.formats.hpo.AnnotatedItemContainer;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAssociationData;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoGeneAnnotation;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseData;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseDataContainer;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoaDiseaseDataLoader;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.algo.OntologyTerms;
import org.monarchinitiative.phenol.ontology.data.Identified;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
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

    Map<TermId, Collection<TermId>> phenotypeToDisease;
    Map<TermId, List<HpoGeneAnnotation>> phenotypeToGene;

    Map<TermId, Collection<TermId>> geneIdsToDisease;

    Map<TermId, Map<TermId, Collection<TermId>>> annotationCache = new HashMap<>();

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

        this.phenotypeToGene = hpoAssocationData.hpoToGeneAnnotations().stream().collect(Collectors.groupingBy(HpoGeneAnnotation::id));
        this.phenotypeToDisease = generatePhenotypeToDisease(diseases);
        this.geneIdsToDisease = hpoAssocationData.associations().geneIdToDiseaseIds();

        // phenotype -> gene, inherits down al the childrens genes
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFilePhenotypeToGene), StandardOpenOption.CREATE)){
            writer.write(String.join("\t", "hpo_id", "hpo_name", "ncbi_gene_id", "gene_symbol", "disease_id"));
            writer.newLine();
            hpoOntology.getTerms().stream().distinct().forEach(term -> {
                final TermId phenotype = term.id();
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
                                annotation.getEntrezGeneSymbol(),
                                intersecting_annotations(annotation.id(), annotation.getItemId()).stream().map(TermId::toString).collect(Collectors.joining(";"))
                        ));
                        writer.newLine();
                    } catch (IOException e) {
                        throw new PhenolRuntimeException(e);
                    }
                }
            });
            writer.flush();
        }
        // Gene -> Phenotype no inheritance
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFileGeneToPhenotype), StandardOpenOption.CREATE)){
            writer.write(String.join("\t",  "ncbi_gene_id", "gene_symbol", "hpo_id", "hpo_name", "disease_id"));
            writer.newLine();
            hpoAssocationData.hpoToGeneAnnotations().stream().sorted(Comparator.comparing(HpoGeneAnnotation::getEntrezGeneId))
                    .forEach(annotation -> {
                        try {
                            writer.write(String.join("\t",
                                    String.valueOf(annotation.getEntrezGeneId()),
                                    annotation.getEntrezGeneSymbol(),
                                    annotation.id().toString(),
                                    annotation.getTermName(),
                                    intersecting_annotations(annotation.id(), annotation.getItemId()).stream().map(TermId::toString).collect(Collectors.joining(";"))
                            ));
                            writer.newLine();
                        } catch (IOException e) {
                            throw new PhenolRuntimeException(e);
                        }
                    });
            writer.flush();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFileGeneToDisease), StandardOpenOption.CREATE)){
            writer.write(String.join("\t",  "ncbi_gene_id", "gene_symbol", "association_type", "disease_id", "source"));
            writer.newLine();
            Map<TermId, String> diseaseNames = diseases.diseaseData().stream().collect(Collectors.toUnmodifiableMap(HpoaDiseaseData::id, HpoaDiseaseData::name));
            hpoAssocationData.associations().diseaseToGeneAssociations().forEach(diseaseAssocation -> {
                    String source;
                    if(diseaseAssocation.diseaseId().getPrefix().contains("OMIM")){
                        source = "ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen";
                    } else if(diseaseAssocation.diseaseId().getPrefix().contains("ORPHA")){
                        source = "http://www.orphadata.org/data/xml/en_product6.xml";
                    } else {
                        source = "";
                    }
                diseaseAssocation.associations().forEach(diseaseGene -> {
                        try {
                            writer.write(String.join("\t",
                                    diseaseGene.geneIdentifier().id().toString(),
                                    diseaseGene.geneIdentifier().symbol(),
                                    diseaseGene.associationType().toString(),
                                    diseaseAssocation.diseaseId().getValue(),
                                    source
                            ));
                            writer.newLine();
                        } catch (IOException e) {
                            throw new PhenolRuntimeException(e);
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

    Collection<TermId> intersecting_annotations(TermId phenotype_id, TermId gene_id) {
        // Diseases with this phenotype
        final Collection<TermId> diseasePhenotypeAnnotations = this.phenotypeToDisease.getOrDefault(phenotype_id, Collections.emptyList());
        // Diseases with this gene
        final Collection<TermId> diseaseGeneAnnotations = this.geneIdsToDisease.getOrDefault(gene_id, Collections.emptyList());
        Map<TermId, Collection<TermId>> phenotypeToAnnotation = annotationCache.get(phenotype_id);
        if(phenotypeToAnnotation != null && phenotypeToAnnotation.get(gene_id) != null){
            return annotationCache.get(phenotype_id).get(gene_id);
        } else {
            Collection<TermId> intersecting = diseasePhenotypeAnnotations.stream().filter(diseaseGeneAnnotations::contains).collect(Collectors.toList());
            Map<TermId, Collection<TermId>> map = new HashMap<>();
            map.putIfAbsent(gene_id, intersecting);
            this.annotationCache.putIfAbsent(phenotype_id, map);
            return intersecting;
        }
    }

     Map<TermId, Collection<TermId>> generatePhenotypeToDisease(AnnotatedItemContainer<? extends AnnotatedItem> diseaseData) {
        Map<TermId, Collection<TermId>> phenotypeToDisease = new HashMap<>();
        diseaseData.stream().forEach(disease -> {
            disease.annotations().forEach(phenotype -> {
                TermId hpoId = phenotype.id();
                phenotypeToDisease.computeIfAbsent(hpoId, (k) -> {
                    return new HashSet<>();
                }).add(disease.id());
            });
        });
        return phenotypeToDisease;
    }

}
