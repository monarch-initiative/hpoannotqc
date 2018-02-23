package org.monarchinitiative.hpoannotqc.orphanet;


import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class transforms the annotation data derived from the Orphanet XML code to the old and New BigFile format.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class OrphaXml2BigFileWriter {


    private final BufferedWriter writer;

    private final static String DATABASE="ORPHA";
    private final static String EMPTY_STRING="";
    private final static String ORPHA_EVIDENCE_CODE="TAS";
    private final static String NO_ONSET_CODE_AVAILABLE=EMPTY_STRING;
    private final static String ASSIGNED_BY="ORPHA:orphadata";



    OrphaXml2BigFileWriter(BufferedWriter out) {
        writer=out;
    }


    public void write(List<OrphanetDisorder> disorderList) {
        for (OrphanetDisorder disorder: disorderList) {
            try {
                transformEntry2BigFileLine(disorder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        Date date = new Date();
        return dateFormat.format(date); //2016/11/16 12:08:43
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
                ORPHA_EVIDENCE_CODE,
                NO_ONSET_CODE_AVAILABLE,
                entry.getFrequency(),
                EMPTY_STRING, /* with*/
                "TODO-ASPECT",
                "", /* synonym */
                getTodaysDate(),
                ASSIGNED_BY
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }
}
