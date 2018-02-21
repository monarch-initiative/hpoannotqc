package org.monarchinitiative.hpoannotqc.orphanet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


public class OrphanetXML2HpoDiseaseModelParser {

    private final String orphanetXmlPath;

    List<OrphanetDisorder> disorders;


    private boolean inDisorderList = false;
    private boolean inAssociationList = false;
    private boolean inAssociation = false;
    private boolean inFrequency = false;


    public OrphanetXML2HpoDiseaseModelParser(String xmlpath) {
        orphanetXmlPath = xmlpath;
        disorders = new ArrayList<>();
        try {
            parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (OrphanetDisorder od : disorders) {
            System.out.println(od);
        }
    }


    private void parse() throws Exception {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(orphanetXmlPath));
        OrphanetDisorder disorder = null;
        String currentHpoId=null;
        String currentHpoName=null;
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("DisorderList")) {
                    inDisorderList = true;
                } else if (startElement.getName().getLocalPart().equals("Disorder")) {
                    if (disorder != null) {
                        disorders.add(disorder);
                    }
                    disorder = new OrphanetDisorder();
                    Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                    if (idAttr != null) {
                        disorder.setId(Integer.parseInt(idAttr.getValue()));
                    }
                } else if (startElement.getName().getLocalPart().equals("OrphaNumber")) {
                    xmlEvent = xmlEventReader.nextEvent();
                    disorder.setOrphaNumber(Integer.parseInt(xmlEvent.asCharacters().getData()));
                } else if (startElement.getName().getLocalPart().equals("Name") && !inAssociationList) {
                    xmlEvent = xmlEventReader.nextEvent();
                    disorder.setName(xmlEvent.asCharacters().getData());
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
                        disorder.setHPO(currentHpoId,xmlEvent.asCharacters().getData());
                    }
                    currentHpoId=null;
                } else if (startElement.getName().getLocalPart().equals("HPOFrequency")) {
                    inFrequency=true;
                } else if (startElement.getName().getLocalPart().equals("Name") && inFrequency) {
                    xmlEvent = xmlEventReader.nextEvent();
                    disorder.setFrequency(xmlEvent.asCharacters().getData());
                }else if (startElement.getName().getLocalPart().equals("DiagnosticCriteria")) {
                    disorder.setDiagnosticCriterion();
                }  else {
                    System.out.println("NO MAP: " + xmlEvent.toString());
                }
            } else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("HPODisorderAssociationList")) {
                    inAssociationList = false;
                } else if ( endElement.getName().getLocalPart().equals("HPOFrequency")) {
                    inFrequency = false;
                }

            }
            //
        }
    }


}
