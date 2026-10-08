package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

public final class Question implements Serializable {
    private static final long serialVersionUID=1L;
    // Missing in old saved questions: keep their published identity until they are finished.
    private int signatureVersion=1;
    public String id,skillId,prompt,expression,kind="number";
    public String[] answers,labels;
    public List<String> choices=new ArrayList<>();
    /** Optional natural-language labels; stored answer values remain stable across display shuffles. */
    public Map<String,String> choiceLabels=new LinkedHashMap<>();
    /** Optional public pictures keyed by stable answer values; absent in older saved questions. */
    public Map<String,StudyDiagram> choiceDiagrams=new LinkedHashMap<>();
    public List<String> distractorReasons=new ArrayList<>();
    public int correctChoice=-1;
    public boolean decimal,stepSupport=true;
    public NumberBond numberBond;
    public StudyGuide studyGuide;
    public StudyDiagram diagram;
    /** Learner display preference; absent in old saves and excluded from the published identity. */
    public Integer collectionGroupSize;
    /** Student's estimate, retained across regrouping and restore; excluded from public identity. */
    public String learnerEstimate;
    /** Given assumptions, never inferred from the student's work. Null in older serialized questions. */
    public Set<String> nonzeroVariables=new LinkedHashSet<>();
    /** Public givens used in scalar function work; never populated from the student's answer. */
    public Map<String,String> givenNumbers=new LinkedHashMap<>();
    public String resultSymbol="";
    /** Required final representation for conversion exercises; empty in ordinary exact-value questions. */
    public String answerFormat="";
    // Only needed while constructing choices. Published questions keep their actual choices.
    public transient Rational[] choiceInputs=new Rational[0];
    public Question withInputs(long... values){choiceInputs=new Rational[values.length];for(int i=0;i<values.length;i++)choiceInputs[i]=Rational.of(values[i]);return this;}
    public Question withInputs(Rational... values){choiceInputs=values.clone();return this;}
    public Question(String skillId,String prompt,String expression,String...answers){
        id=UUID.randomUUID().toString();this.skillId=skillId;this.prompt=prompt;this.expression=expression;this.answers=answers;
        labels=answers.length==1?new String[]{"답"}:new String[]{"첫 번째 답","두 번째 답"};
    }
    private void readObject(java.io.ObjectInputStream in)throws java.io.IOException,ClassNotFoundException{
        in.defaultReadObject();
        // All active/deferred questions pass through the same actual device deserialization path.
        if("sec_line_relation".equals(skillId))LineCircleRelations.attach(this);
    }
    String legacySignature(){return skillId+"|"+prompt;}
    boolean hasDiagramSignature(){return diagram!=null&&signatureVersion>0;}
    public String signature(){
        return hasDiagramSignature()?currentSignature():legacySignature();
    }
    String currentSignature(){
        String base=legacySignature();if(diagram==null)return base;
        StringBuilder key=new StringBuilder(base).append("|diagram:");
        appendPart(key,diagram.type);
        // Rotating the same pair of lines is a presentation change, not a new relation.
        int count="linePair".equals(diagram.type)?Math.min(1,diagram.values.length):diagram.values.length;
        key.append(count).append(':');
        for(int i=0;i<count;i++)key.append(Double.toHexString(diagram.values[i]==0?0:diagram.values[i])).append(';');
        key.append(diagram.labels.length).append(':');for(String label:diagram.labels)appendPart(key,label);
        return key.toString();
    }
    private static void appendPart(StringBuilder key,String value){
        if(value==null)key.append("-1:");else key.append(value.length()).append(':').append(value);
    }
}
