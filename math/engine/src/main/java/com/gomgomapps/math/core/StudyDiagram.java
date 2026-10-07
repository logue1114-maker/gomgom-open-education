package com.gomgomapps.math.core;

import java.io.Serializable;

/** Only question givens: no correct-answer markers or hidden solution geometry. */
public final class StudyDiagram implements Serializable {
    private static final long serialVersionUID=1L;
    public String type;
    public double[] values;
    public String[] labels;
    /** Optional drawing orientation only. Missing in saved diagrams means zero rotation. */
    public double[] rotationDegrees;
    /** Optional positive affine stretch; absent means an unstretched drawing. */
    public double[] horizontalScale;
    public StudyDiagram scaledHorizontally(double...scales){for(double v:scales)if(!Double.isFinite(v)||v<.6||v>1)throw new IllegalArgumentException("Drawing scale outside 0.6..1");horizontalScale=scales.clone();return this;}
    public StudyDiagram rotated(double...degrees){for(double v:degrees)if(!Double.isFinite(v)||v<0||v>=360)throw new IllegalArgumentException("Drawing rotation outside 0..359");rotationDegrees=degrees.clone();return this;}
    public StudyDiagram(String type,double[] values,String...labels){this.type=type;this.values=values.clone();this.labels=labels.clone();}
}
