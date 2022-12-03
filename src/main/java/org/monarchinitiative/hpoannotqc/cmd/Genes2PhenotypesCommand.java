package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcRuntimeException;

import org.monarchinitiative.phenol.annotations.assoc.HpoAssociationLoader;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAssociationData;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoGeneAnnotation;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import picocli.CommandLine;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "g2p", mixinStandardHelpOptions = true, description = "Create genes to phenotypes file")
public class Genes2PhenotypesCommand implements Callable<Integer> {
    /**
     * Directory with hp.obo and en_product4.xml files.
     */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    @CommandLine.Option(names = {"--phenotypefile"},
            description = "path to phenotype.hpoa file (as generated by this program; default: ${DEFAULT-VALUE})")
    private String phenotypeDotHpoa = "phenotype.hpoa";
    @CommandLine.Option(names = {"--outfile"},
            description = "name/path of output file (default: ${DEFAULT-VALUE})")
    private String outfile = "phenotype_to_genes.tsv";

    public Genes2PhenotypesCommand() {
    }


    @Override
    public Integer call() throws IOException {
        String hpOboPath = String.format("%s%s%s", downloadDirectory, File.separator, "hp.obo");
        String orphanetGenesXMLpath = String.format("%s%s%s", downloadDirectory, File.separator, "en_product6.xml");
        String mimgenepath = String.format("%s%s%s", downloadDirectory, File.separator, "mim2gene_medgen");
        String geneinfopath = String.format("%s%s%s", downloadDirectory, File.separator, "Homo_sapiens_gene_info.gz");

        File hpoFile = new File(hpOboPath);
        if (!hpoFile.exists()) {
            throw new RuntimeException("Could not find hp.obo at " + hpOboPath);
        }
        Ontology ontology = OntologyLoader.loadOntology(hpoFile);
        File hpoJson = new File(hpOboPath);
        Ontology hpo = OntologyLoader.loadOntology(hpoJson);

        Path homoSapiensGeneInfo = Path.of(geneinfopath);
        Path mimToMedgen = Path.of(mimgenepath);
        Path orphaToGene = Path.of(orphanetGenesXMLpath);
        Path hpoAssociations = Path.of(phenotypeDotHpoa);
        Set<DiseaseDatabase> diseaseDatabases = Set.of(DiseaseDatabase.OMIM, DiseaseDatabase.ORPHANET);

       // HpoAssociationData ASSOCIATION_DATA = HpoAssociationLoader.loadHpoAssociationData(hpo, homoSapiensGeneInfo, mimToMedgen, orphaToGene, hpoAssociations, HpoDiseaseLoaderOptions.of(diseaseDatabases, true, HpoDiseaseLoaderOptions.DEFAULT_COHORT_SIZE));

    /*

        HpoAssociationParser parser = new HpoAssociationParser(geneinfopath, mimgenepath, orphanetGenesXMLpath, phenotypeDotHpoa, ontology);
        List<HpoGeneAnnotation> annotLisrt = parser.getPhenotypeToGene();
        if (annotLisrt == null) {
            throw new HpoAnnotQcRuntimeException("Could not retrieve HPO gene annotation list");
        }
        int c = 0;
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(this.outfile));
            br.write("#Format: HPO-ID<tab>HPO-Name<tab>Gene-ID<tab>Gene-Name\n");
            for (HpoGeneAnnotation hga : annotLisrt) {
                br.write(hga.id().getValue() + "\t" +
                        hga.getTermName() + "\t" +
                        hga.getEntrezGeneId() + "\t" +
                        hga.getEntrezGeneSymbol() + "\n");
                c++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[INFO] We wrote " + c + " lines of the phenotype<->genes data to file");

     */
        return 0;
    }

}
