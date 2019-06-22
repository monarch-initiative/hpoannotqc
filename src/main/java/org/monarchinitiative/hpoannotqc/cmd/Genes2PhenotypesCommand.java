package org.monarchinitiative.hpoannotqc.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.assoc.HpoAssociationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

@Parameters(commandDescription = "Create genes to phenotypes file")
public class Genes2PhenotypesCommand implements Command  {
    /** Path to the {@code hp.obo} file. */
    private String hpOboPath;
    /** Path to the downloaded Orphanet XML file */
    private String orphanetXMLpath;
    /** Directory with hp.obo and en_product>HPO.xml files. */
    @Parameter(names={"-d","--data"}, description ="directory to download data (default: data)" )
    private String downloadDirectory="data";

    public Genes2PhenotypesCommand(){}




    public void execute() {
        hpOboPath = String.format("%s%s%s",downloadDirectory,File.separator, "hp.obo" );
        orphanetXMLpath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product4_HPO.xml" );

        File hpoFile = new File(hpOboPath);
        if (! hpoFile.exists()) {
            throw new RuntimeException("Could not find hp.obo at " + hpOboPath);
        }
       Ontology ontology = OntologyLoader.loadOntology(hpoFile);


       // HpoAssociationParser parser = new HpoAssociationParser();

//
//               (String geneInfoPath, String mim2geneMedgenPath, File orphaToGenePath, Ontology hpoOntology){
//
//            this.homoSapiensGeneInfoPath = geneInfoPath;
//            this.mim2geneMedgenPath = mim2geneMedgenPath;
//            this.orphaToGenePath = orphaToGenePath;

    }




}
