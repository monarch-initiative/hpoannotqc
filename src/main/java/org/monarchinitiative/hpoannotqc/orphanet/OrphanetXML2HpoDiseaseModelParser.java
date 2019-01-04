package org.monarchinitiative.hpoannotqc.orphanet;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoFrequencyTermIds;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class OrphanetXML2HpoDiseaseModelParser {
    private static final Logger logger = LogManager.getLogger();
    private final String orphanetXmlPath;
    /** A list of diseases parsed from Orphanet. */
    private final List<OrphanetDisorder> disorders;


    private final HpoOntology ontology;
    private int n_could_not_find_orphanet_HpoId=0;
    private int n_updatedTermId=0;
    private int n_updatedTermLabel=0;


    private boolean inDisorderList = false;
    private boolean inAssociationList = false;
    private boolean inFrequency = false;
    private boolean inAssociation = false;
    private boolean inDiagnosticCriterion = false;


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
        TermId tid = TermId.of(id);
        if (! ontology.getTermMap().containsKey(tid)) {
            logger.error("[ERROR] Could not find TermId for Orphanet HPO ID \""+ id + "\"");
            n_could_not_find_orphanet_HpoId++;
            return null; // probably an obsolete term.
        }
        TermId currentId =  ontology.getTermMap().get(tid).getId();
        if (!currentId.equals(tid)) {
            n_updatedTermId++;
        }
        return currentId;
    }

    private String getCurrentHpoLabel(TermId tid, String orphalabel) {

        if (! ontology.getTermMap().containsKey(tid)) {
            logger.error(String.format("[ERROR] Using label for non-findable TermId for Orphanet HPO ID %s[%s] -- will skip this annotation", tid.getValue(),orphalabel));
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
                switch (startElement.getName().getLocalPart()) {
                    case "DisorderList":
                        inDisorderList = true; // nothing to do here, we are starting the list of disorders

                        break;
                    case "Disorder":
                        disorder = new OrphanetDisorder();
                        break;
                    case "OrphaNumber":
                        if (inFrequency || inDiagnosticCriterion) {
                            continue;
                        } // Orphanumbers are used for the Disorder but also for the Frequency nodes
                        xmlEvent = xmlEventReader.nextEvent();
                        String orphanumber = xmlEvent.asCharacters().getData();
                        disorder.setOrphaNumber(Integer.parseInt(orphanumber));
                        break;
                    case "Name":
                        if (inFrequency || inDiagnosticCriterion) {
                            continue;
                        } // skip, we have no need to parse the name of the frequency element
                        // since we get the class from the attribute "id"
                        xmlEvent = xmlEventReader.nextEvent();
                        String diseaseName = xmlEvent.asCharacters().getData();
                        disorder.setName(diseaseName);
                        break;
                    case "HPODisorderAssociationList":
                        inAssociationList = true;
                        break;
                    case "HPODisorderAssociation":
                        inAssociation = true;
                        break;
                    case "HPOId":
                        xmlEvent = xmlEventReader.nextEvent();
                        currentHpoId = xmlEvent.asCharacters().getData();
                        break;
                    case "HPOTerm":
                        xmlEvent = xmlEventReader.nextEvent();
                        if (currentHpoId != null) {
                            TermId tid = currentNotAltHpoId(currentHpoId);
                            if (tid == null) continue;
                            String termLabel = getCurrentHpoLabel(tid, xmlEvent.asCharacters().getData());
                            disorder.setHPO(tid, termLabel);
                        }
                        currentHpoId = null;
                        break;
                    case "HPOFrequency":
                        // if we are here, then we can grab the frequency from the id attribute.
                        Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                        if (idAttr != null) {
                            TermId freq = string2frequency(idAttr.getValue());
                            disorder.setFrequency(freq);
                        }
                        inFrequency = true;
                        break;
                    case "DiagnosticCriteria":
                        inDiagnosticCriterion=true;
                        break;
                    case "HPO":
                        // no-op, no need to get the id attribute from this node
                        break;
                    case "JDBOR":
                        // no-op, no need to do anything for the very top level node
                        break;
                    default:
                        System.out.println("NO MAP: " + xmlEvent.toString());
                        break;
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
                } else if ( endElementName.equals("DiagnosticCriteria")) {
                    inDiagnosticCriterion = false;
                } else if ( endElementName.equals("JDBOR")) {
                    logger.trace("Done parsing Orphanet XML document");
                } else if (endElementName.equals("Disorder")) {
                    if (disorder != null) {
                        disorders.add(disorder);
                    }
                }
            }
        }
    }


}
