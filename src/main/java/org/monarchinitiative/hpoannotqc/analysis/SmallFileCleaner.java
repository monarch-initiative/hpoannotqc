package org.monarchinitiative.hpoannotqc.analysis;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SmallFileCleaner {

    private final String header;
    private final List<String> lines;
    private final List<String> cleansed;
    private final String preferredLabel;
    private final boolean labelWasChanged;

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
        this.header = lines.get(0);
        lines.remove(0);
        this.preferredLabel = setPreferredLabel();
        this.labelWasChanged = checkLabelChanged();
        this.cleansed = getCleansedLines();
    }

    /**
     * Just for testing
     */
    public SmallFileCleaner(List<String> mylines, String header) {
        this.lines = mylines;
        this.header = header;
        this.cleansed = getCleansedLines();
        this.preferredLabel = setPreferredLabel();
        this.labelWasChanged = checkLabelChanged();
    }


    public String getPreferredLabel() {
        return this.preferredLabel;
    }

    private String setPreferredLabel() {
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


    private boolean checkLabelChanged() {
        for (String line : lines) {
            String [] fields = line.split("\t");
            if (fields.length != 14) {
                throw new PhenolRuntimeException("Bad line: " + line);
            }
            String name = fields[1];
           if (! this.preferredLabel.equals(name)) {
               return true;
           }
        }
        return false;
    }


    private String replaceWithPreferedName(String line, String name) {
        String [] fields = line.split("\t");
        if (fields.length != 14) {
            throw new PhenolRuntimeException("Bad line: " + line);
        }
        fields[1] = name;
        return String.join("\t", fields);
    }


    private List<String> getCleansedLines() {
        String preferedName = getPreferredLabel();
        List<String> newlines = new ArrayList<>();
        Set<UniqueAnnotation> seenAnnotations = new HashSet<>();
        for (String line : lines) {
            UniqueAnnotation ua = new UniqueAnnotation(line);
            if (seenAnnotations.contains(ua)) {
                System.out.println("DUPLICATE LINE" + line);
                continue;
            } else {
                seenAnnotations.add(ua);
            }
            line = replaceWithPreferedName(line,preferedName);
            newlines.add(line);
        }
        return newlines;
    }

    public List<String> getNewLines() {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        lines.addAll(cleansed);
        return lines;
    }


    public boolean wasChanged() {
        if  (lines.size() != cleansed.size()) return true;
        return labelWasChanged;
    }




}
