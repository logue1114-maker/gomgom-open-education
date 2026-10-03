package com.gomgomapps.math.core;

import java.io.Serializable;

/** Only question givens: no correct-answer markers or hidden solution geometry. */
public final class StudyDiagram implements Serializable {
    private static final long serialVersionUID=1L;
    public String type;
    public double[] values;
    public String[] labels;
    public StudyDiagram(String type,double[] values,String...labels){this.type=type;this.values=values.clone();this.labels=labels.clone();}
}
