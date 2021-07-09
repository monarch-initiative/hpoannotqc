package org.monarchinitiative.hpoannotqc.analysis;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SmallFileCleaner {


    private List<String> lines;

    private String preferredLabel;

    public SmallFileCleaner(String pathToSmallFile) {
        File f = new File(pathToSmallFile);
        if (! f.isFile()) {
            throw new PhenolRuntimeException("Could not open " + pathToSmallFile + " (small file)");
        }
        try {
            lines = Files.readAllLines(f.toPath());
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not read " + pathToSmallFile + " (small file)");
        }
    }


    public String getPreferredLabel() {
        List<NameDate> nameSet = new ArrayList<>();
        for (String line : lines) {
            String [] fields = line.split("\t");
            if (fields.length != 14) {
                throw new PhenolRuntimeException("Bad line: " + line);
            }
            String name = fields[1];
            String biocuration = fields[13];
            nameSet.add(new NameDate(name, biocuration));
        }
        Collections.sort(nameSet);
        NameDate preferedNameDate = nameSet.get(0);
        return preferedNameDate.getPrettyVersion();
    }



    private String replaceWithPreferedName(String line, String name) {
        String [] fields = line.split("\t");
        if (fields.length != 14) {
            throw new PhenolRuntimeException("Bad line: " + line);
        }
        fields[1] = name;
        return String.join("\t", fields);
    }


    public List<String> getCleansedLines() {
        String preferedName = getPreferredLabel();
        List<String> newlines = new ArrayList<>();
        for (String line : lines) {
            line = replaceWithPreferedName(line,preferedName);
            newlines.add(line);
        }
        return newlines;
    }




}
