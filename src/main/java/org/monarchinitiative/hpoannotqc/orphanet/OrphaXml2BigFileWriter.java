package org.monarchinitiative.hpoannotqc.orphanet;

import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrphaXml2BigFileWriter {


    private final BufferedWriter writer;

    private final static String DATABASE="ORPHA";
    private final static String EMPTY_STRING="";
    private final static String ORPHA_EVIDENCE_CODE="TAS";



    OrphaXml2BigFileWriter(BufferedWriter out) {
        writer=out;
    }


    public void write(List<OrphanetDisorder disorderList>) {
        for (OrphanetDisorder disorder: disorderList) {
            try {
                transformEntry2BigFileLine(disorder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String transformEntry2BigFileLine(OrphanetDisorder entry) throws IOException {
        String diseaseID=String.format("%s:%d",DATABASE,entry.getOrphaNumber());
        String [] elems = {
                DATABASE,
                diseaseID,
                entry.getName(),
                EMPTY_STRING,
                entry.getHpoId(),
                entry.getHpoLabel(),
                diseaseID,
                entry.getEvidenceCode(),
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(),
                getFrequencyString(entry),
                "", /* with*/
                getAspect(entry),
                "", /* synonym */
                entry.getDateCreated(),
                entry.getAssignedBy()
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }
}
