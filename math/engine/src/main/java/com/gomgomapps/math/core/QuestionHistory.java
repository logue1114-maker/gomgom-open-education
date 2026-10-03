package com.gomgomapps.math.core;

import java.util.*;

/** A small per-skill history survives intervening units, calendar days and app restarts. */
public final class QuestionHistory {
    private QuestionHistory(){}
    public static final int PER_SKILL_LIMIT=100;
    static List<String> recent(Learning.State state,String id){
        Learning.Progress progress=state.progress.get(id);
        return progress==null||progress.recentQuestions==null?List.of():progress.recentQuestions;
    }
    static void record(Learning.State state,String id,String signature){
        Learning.Progress progress=state.progress(id);
        if(progress.recentQuestions==null)progress.recentQuestions=new LinkedList<>();
        progress.recentQuestions.remove(signature);
        progress.recentQuestions.addLast(signature);
        while(progress.recentQuestions.size()>PER_SKILL_LIMIT)progress.recentQuestions.removeFirst();
    }
}
