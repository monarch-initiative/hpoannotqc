package org.monarchinitiative.hpoannotqc.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.assoc.HpoAssociationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.Map;

@Parameters(commandDescription = "Create genes to phenotypes file")
public class Genes2PhenotypesCommand implements Command  {
    /** Directory with hp.obo and en_product>HPO.xml files. */
    @Parameter(names={"-d","--data"}, description ="directory to download data (default: data)" )
    private String downloadDirectory="data";

    public Genes2PhenotypesCommand(){}




    public void execute() {
        String hpOboPath = String.format("%s%s%s",downloadDirectory,File.separator, "hp.obo" );
        String orphanetXMLpath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product4_HPO.xml" );
        String orphanetGenesXMLpath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product6.xml" );
        String mimgenepath = String.format("%s%s%s",downloadDirectory,File.separator, "mim2gene_medgen" );
        String geneinfopath =  String.format("%s%s%s",downloadDirectory,File.separator, "Homo_sapiens_gene_info.gz" );

        File hpoFile = new File(hpOboPath);
        if (! hpoFile.exists()) {
            throw new RuntimeException("Could not find hp.obo at " + hpOboPath);
        }
       Ontology ontology = OntologyLoader.loadOntology(hpoFile);


       HpoAssociationParser parser = new HpoAssociationParser(geneinfopath,mimgenepath,new File(orphanetGenesXMLpath),ontology);
       Map<TermId,String > mp = parser.getGeneIdToSymbolMap();
       if (mp==null) {
           System.err.println("[ERROR] Could not parse gene assocs");
           return;
       }
       for (TermId v: mp.keySet()) {
           System.out.println(v +": " + mp.get(v));
       }


    }




}
