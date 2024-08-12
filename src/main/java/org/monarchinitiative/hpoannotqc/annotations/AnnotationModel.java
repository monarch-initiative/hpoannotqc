package org.monarchinitiative.hpoannotqc.annotations;


import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorReport;

import java.util.ArrayList;
import java.util.List;

public interface AnnotationModel {

    List<AnnotationEntry> getEntryList();



    List<HpoaError> getErrors();

    String getTitle();

    default List<HpoaErrorReport> getHpoaErrorReportList() {
        List<HpoaErrorReport> reports = new ArrayList<>();
        for (HpoaError error : getErrors()) {
            reports.add(new HpoaErrorReport(getTitle(), error));
        }
       return reports;
    }

}
