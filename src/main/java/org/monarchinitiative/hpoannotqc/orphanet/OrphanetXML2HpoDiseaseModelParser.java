package org.monarchinitiative.hpoannotqc.orphanet;

import com.github.phenomics.ontolib.formats.hpo.HpoFrequencyTermIds;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class OrphanetXML2HpoDiseaseModelParser {
    private static final Logger logger = LogManager.getLogger();
    private final String orphanetXmlPath;
    /** A list of diseases parsed from Orphanet. */
    private List<OrphanetDisorder> disorders;


    private final HpoOntology ontology;
    private int n_could_not_find_orphanet_HpoId=0;
    private int n_updatedTermId=0;
    private int n_updatedTermLabel=0;


    private boolean inDisorderList = false;
    private boolean inAssociationList = false;
    private boolean inFrequency = false;
    private boolean inAssociation = false;


    public OrphanetXML2HpoDiseaseModelParser(String xmlpath, HpoOntology onto) {
        orphanetXmlPath = xmlpath;
        this.ontology=onto;
        disorders = new ArrayList<>();
        try {
            parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Transform the Orphanet codes into HPO Frequency TermId's.
     * <p>
     * The frequency ids are
     * </p>
     * <ul><li>Excluded (0%): Orphanet id 28440</li>
     * <li>Frequent (79-30%): Orphanet id: 28419</li>
     * <li>Obligate (100%): Orphanet id: 28405</li>
     * <li>Occasional (29-5%): Orphanet id: 28426</li>
     * <li>Very frequent (99-80%): Orphanet id 28412</li>
     * <li>Very rare : Orphanet id 28433</li>
     * </ul>
     * @param fstring An Orphanet id (attribute in XML file) corresponding to a frequency category
     * @return corresponding HPO Frequency TermId
     */
    private TermId string2frequency(String fstring) {
        switch (fstring) {
            case "28405": return HpoFrequencyTermIds.ALWAYS_PRESENT;// Obligate
            case "28412": return HpoFrequencyTermIds.VERY_FREQUENT;
            case "28419": return HpoFrequencyTermIds.FREQUENT;
            case "28426": return HpoFrequencyTermIds.OCCASIONAL;
            case "28433": return HpoFrequencyTermIds.VERY_RARE;
            case "28440": return HpoFrequencyTermIds.EXCLUDED;
        }
        logger.fatal("[ERROR] Could not find TermId for Orphanet frequency "+ fstring);
        logger.fatal("Not a recoverable error -- check and recompile");
        System.exit(1);
        return null; // needed to avoid warning
    }

    private TermId currentNotAltHpoId(String id) {
        TermId tid = ImmutableTermId.constructWithPrefix(id);
        if (! ontology.getTermMap().containsKey(tid)) {
            logger.error("[ERROR] Could not find TermId for Orphanet HPO ID "+ id);
            n_could_not_find_orphanet_HpoId++;
            return tid; // probably an obsolete term.
        }
        TermId currentId =  ontology.getTermMap().get(tid).getId();
        if (!currentId.equals(tid)) {
            n_updatedTermId++;
        }
        return currentId;
    }

    private String getCurrentHpoLabel(TermId tid, String orphalabel) {

        if (! ontology.getTermMap().containsKey(tid)) {
            logger.error(String.format("[ERROR] Using label for non-findable TermId for Orphanet HPO ID %s[%s]", tid.getIdWithPrefix(),orphalabel));
            n_could_not_find_orphanet_HpoId++;
            return orphalabel; // probably an obsolete term.
        }
        String label = ontology.getTermMap().get(tid).getName();
        if (! label.equals(orphalabel)) {
            n_updatedTermLabel++;
        }
        return label;
    }

    public List<OrphanetDisorder> getDisorders() {
        return disorders;
    }

    private void parse() throws Exception {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(orphanetXmlPath));
        OrphanetDisorder disorder = null;
        String currentHpoId=null;
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("DisorderList")) {
                    inDisorderList = true; // nothing to do here, we are starting the list of disorders
                } else if (startElement.getName().getLocalPart().equals("Disorder")) {
                    disorder = new OrphanetDisorder();
                } else if (startElement.getName().getLocalPart().equals("OrphaNumber")) {
                    if (inFrequency) { continue; } // Orphanumbers are used for the Disorder but also for the Frequency nodes
                    xmlEvent = xmlEventReader.nextEvent();
                    String orphanumber = xmlEvent.asCharacters().getData();
                    disorder.setOrphaNumber(Integer.parseInt(orphanumber));
                } else if (startElement.getName().getLocalPart().equals("Name")  ) {
                    if (inFrequency) { continue; } // skip, we have no need to parse the name of the frequency element
                    // since we get the class from the attribute "id"
                    xmlEvent = xmlEventReader.nextEvent();
                    String diseaseName = xmlEvent.asCharacters().getData();
                    disorder.setName(diseaseName);
                } else if (startElement.getName().getLocalPart().equals("HPODisorderAssociationList")) {
                    inAssociationList = true;
                } else if (startElement.getName().getLocalPart().equals("HPODisorderAssociation")) {
                    inAssociation = true;
                } else if (startElement.getName().getLocalPart().equals("HPOId")) {
                    xmlEvent = xmlEventReader.nextEvent();
                    currentHpoId=xmlEvent.asCharacters().getData();
                } else if (startElement.getName().getLocalPart().equals("HPOTerm")) {
                    xmlEvent = xmlEventReader.nextEvent();
                    if (currentHpoId!=null) {
                        TermId tid = currentNotAltHpoId(currentHpoId);
                        String termLabel = getCurrentHpoLabel(tid, xmlEvent.asCharacters().getData());
                        disorder.setHPO(tid,termLabel);
                    }
                    currentHpoId=null;
                } else if (startElement.getName().getLocalPart().equals("HPOFrequency")) {
                    // if we are here, then we can grab the frequency from the id attribute.
                    Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                    if (idAttr != null) {
                        TermId freq = string2frequency(idAttr.getValue());
                        disorder.setFrequency(freq);
                    }
                    inFrequency=true;
                } else if (startElement.getName().getLocalPart().equals("DiagnosticCriteria")) {
                    disorder.setDiagnosticCriterion();
                } else if (startElement.getName().getLocalPart().equals("HPO")) {
                    // no-op, no need to get the id attribute from this node
                } else if (startElement.getName().getLocalPart().equals("JDBOR")) {
                    // no-op, no need to do anything for the very top level node
                } else {
                    System.out.println("NO MAP: " + xmlEvent.toString());
                }
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                String endElementName = endElement.getName().getLocalPart();
                //System.err.println("END ="+endElement.getName().getLocalPart());
                if (endElementName.equals("HPODisorderAssociationList")) {
                    inAssociationList = false;
                } else if (endElementName.equals("HPODisorderAssociation")) {
                    inAssociation = false;
                }else if ( endElementName.equals("HPOFrequency")) {
                    inFrequency = false;
                }  else if ( endElementName.equals("JDBOR")) {
                    logger.trace("Done parsing Orphanet XML document");
                } else if (endElementName.equals("Disorder")) {
                    if (disorder != null) {
                        List<SmallFileQCCode> qclist = disorder.qcCheck();
                        if (qclist.size()>0) {
                            logger.error("QC Issues for Orphanet disease " + disorder.toString() +" (see next line)");
                            String codes = qclist.stream().map(SmallFileQCCode::getName).collect(Collectors.joining(";"));
                            logger.error("\t" + codes);
                        }
                        disorders.add(disorder);
                    }
                }
            }
            //
        }
    }


}
