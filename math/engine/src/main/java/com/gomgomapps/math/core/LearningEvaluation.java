package com.gomgomapps.math.core;

import java.util.*;

/** Per-skill recent independent accuracy; separate from diagnostic placement and review scheduling. */
public final class LearningEvaluation {
    private LearningEvaluation(){}
    public static final int WINDOW=30, MINIMUM=10;
    public enum Level { INSUFFICIENT_RECORDS, NEEDS_PRACTICE, AVERAGE, EXCELLENT }
    public static void record(Learning.Progress progress,boolean independent){
        // Old serialized records have no recent outcomes. Do not invent their chronology.
        if(progress.evaluationResults==null)progress.evaluationResults=new LinkedList<>();
        progress.evaluationResults.add(independent);
        while(progress.evaluationResults.size()>WINDOW)progress.evaluationResults.removeFirst();
    }
    public static int count(Learning.Progress progress){return progress==null||progress.evaluationResults==null?0:progress.evaluationResults.size();}
    public static int correct(Learning.Progress progress){return progress==null||progress.evaluationResults==null?0:(int)progress.evaluationResults.stream().filter(Boolean.TRUE::equals).count();}
    public static Level level(Learning.Progress progress){
        int count=count(progress),correct=correct(progress);
        if(count<MINIMUM)return Level.INSUFFICIENT_RECORDS;
        // Integer ratios preserve the 60/90 percent boundaries without rounding up.
        if(correct*100<count*60)return Level.NEEDS_PRACTICE;
        return correct*100<count*90?Level.AVERAGE:Level.EXCELLENT;
    }
}
