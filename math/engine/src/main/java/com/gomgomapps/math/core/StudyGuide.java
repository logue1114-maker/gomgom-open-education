package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Teaching frames authored alongside the public givens. Expected values are never rendered. */
public final class StudyGuide implements Serializable {
    private static final long serialVersionUID=1L;
    public List<Frame> frames=new ArrayList<>();
    public boolean transfer=true;
    public static final class Frame implements Serializable {
        private static final long serialVersionUID=1L;
        public String instruction,before,after,expected;
        public Map<String,String> options=new LinkedHashMap<>();
        public Frame(){}
        Frame(String instruction,String before,String after,String expected){this.instruction=instruction;this.before=before;this.after=after;this.expected=expected;}
    }
    public StudyGuide step(String instruction,String before,String after,String expected){frames.add(new Frame(instruction,before,after,expected));return this;}
    public StudyGuide transfer(boolean value){transfer=value;return this;}
    public StudyGuide choice(String instruction,Map<String,String> labels,String expected){Frame f=new Frame(instruction,"","",expected);f.options.putAll(labels);frames.add(f);return this;}
}
