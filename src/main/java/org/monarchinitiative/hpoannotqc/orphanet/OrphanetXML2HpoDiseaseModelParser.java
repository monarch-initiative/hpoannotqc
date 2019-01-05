package org.monarchinitiative.hpoannotqc.orphanet;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoFrequencyTermIds;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This class is an XML parser for the Orphanet file with HPO-based disease annotations
 * ({@code en_product4_HPO.xml} (see http://www.orphadata.org/).
 */
public class OrphanetXML2HpoDiseaseModelParser {
    private static final Logger logger = LogManager.getLogger();
    /** Path to {@code en_product4_HPO.xml} file. */
    private final String orphanetXmlPath;
    /** A list of diseases parsed from Orphanet. */
    private final List<OrphanetDisorder> disorders;
    /** Reference to the HPO Ontology. */
    private final HpoOntology ontology;
    private int n_could_not_find_orphanet_HpoId=0;
    private int n_updatedTermId=0;
    private int n_updatedTermLabel=0;





    public OrphanetXML2HpoDiseaseModelParser(String xmlpath, HpoOntology onto) {
        orphanetXmlPath = xmlpath;
        this.ontology=onto;
        disorders = new ArrayList<>();
        try {
            parse();
        } catch (XMLStreamException | IOException e) {
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

    /**
     * @param id A String representing an HPO id
     * @return the current TermId (replaces alt_ids of merged terms if necessary).
     */
    private TermId getCurrentHpoId(String id) {
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

    /**
     * @param tid An HPO term id
     * @param orphalabel The HPO label used in the Orphanet file
     * @return The label (updated to the current version if necessary for merged terms).
     */
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

    /**
     * @return A list of all Orphanet diseases in the dataset.
     */
    public List<OrphanetDisorder> getDisorders() {
        return disorders;
    }

    /**
     * This method performs the XML parse of the Orphanet file
     * @throws XMLStreamException If there is an XML stream issue
     * @throws IOException If the file cannot be opened
     */
    private void parse() throws XMLStreamException , IOException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(orphanetXmlPath));

        boolean inFrequency = false;
        boolean inDiagnosticCriterion = false;
        OrphanetDisorder disorder = null;
        String currentHpoId=null;
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                switch (startElement.getName().getLocalPart()) {
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
                    case "HPOId":
                        xmlEvent = xmlEventReader.nextEvent();
                        currentHpoId = xmlEvent.asCharacters().getData();
                        break;
                    case "HPOTerm":
                        xmlEvent = xmlEventReader.nextEvent();
                        if (currentHpoId != null) {
                            TermId tid = getCurrentHpoId(currentHpoId);
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
                        // no-op, no need to do anything for many node types!
                        break;
                }
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                String endElementName = endElement.getName().getLocalPart();
                if ( endElementName.equals("HPOFrequency")) {
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
