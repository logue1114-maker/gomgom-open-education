package com.gomgomapps.math.core;

import java.util.*;

/** Replayable observation and actual learner answers, bound to one public picture. */
public final class KoreanPolygonLesson {
    private KoreanPolygonLesson(){}
    public static final int MAX_ACTIONS=128;
    public record State(int stage,List<Integer> marked,boolean ready,boolean complete){
        public State {marked=List.copyOf(marked);}
    }
    private static String scope(Question q){
        if(KoreanPolygons.read(q)==null||q.id==null||q.id.isBlank())return null;
        return KoreanPolygons.VERSION+"|"+q.id.length()+":"+q.id+"|"+q.prompt+"|"+Arrays.toString(q.diagram.values)+"|"+Arrays.toString(q.diagram.labels)+"|"+new TreeMap<>(q.givenNumbers);
    }
    public static List<Integer> required(Question q,int stage){
        var g=KoreanPolygons.read(q);StudyGuide h=KoreanPolygons.guide(q);
        if(g==null||stage<0||stage>=h.frames.size()||!KoreanPolygons.ACTION_FORMAT.equals(h.frames.get(stage).inputFormat))return List.of();
        if(KoreanPolygons.DIAGONALS.equals(g.kind()))return g.diagonalEnds();
        int n=stage==0?g.edgeCount():g.vertices().size();List<Integer> out=new ArrayList<>();for(int i=0;i<n;i++)out.add(i);return List.copyOf(out);
    }
    public static boolean isAction(Question q,int stage){StudyGuide h=KoreanPolygons.guide(q);return h!=null&&stage>=0&&stage<h.frames.size()&&KoreanPolygons.ACTION_FORMAT.equals(h.frames.get(stage).inputFormat);}
    /** Pure getters never mark an unseen boundary, corner or connection. */
    public static State state(Question q,HelpPlan.Draft draft){
        String key=scope(q);StudyGuide h=KoreanPolygons.guide(q);
        if(key==null||draft==null||!Objects.equals(q.id,draft.questionId)||!KoreanPolygons.VERSION.equals(draft.teachingVersion)||!key.equals(draft.polygonScope)||draft.polygonActions==null||draft.polygonActions.size()>MAX_ACTIONS)return null;
        int stage=0;Set<Integer> marked=new LinkedHashSet<>();List<String> committed=new ArrayList<>();
        for(String action:new ArrayList<>(draft.polygonActions)){
            if(action==null||stage>=h.frames.size())return null;
            if(action.matches("visit:[0-7]")){
                int index=Integer.parseInt(action.substring(6));
                if(!isAction(q,stage)||!required(q,stage).contains(index)||!marked.add(index))return null;
            }else if(action.equals("reset")){
                if(!isAction(q,stage)||marked.isEmpty())return null;marked.clear();
            }else if(action.startsWith("advance:")){
                String raw=action.substring(8);StudyGuide.Frame frame=h.frames.get(stage);
                if(!frame.expected.equals(raw)||isAction(q,stage)&&!marked.containsAll(required(q,stage)))return null;
                committed.add(raw);stage++;marked.clear();
            }else return null;
        }
        if(draft.stage!=stage||draft.entries==null||draft.entries.size()<stage)return null;
        for(int i=0;i<stage;i++)if(draft.entries.get(i)==null||!committed.get(i).equals(draft.entries.get(i).trim()))return null;
        boolean complete=stage==h.frames.size();return new State(stage,new ArrayList<>(marked),complete||!isAction(q,stage)||marked.containsAll(required(q,stage)),complete);
    }
    /** Keep entered values, including a wrong current choice, when rebinding an invalid tool trace. */
    public static State restore(Question q,HelpPlan.Draft draft){
        if(draft==null)return null;String key=scope(q);if(key==null||!Objects.equals(q.id,draft.questionId)||!KoreanPolygons.VERSION.equals(draft.teachingVersion))return null;
        State s=state(q,draft);if(s!=null)return s;
        draft.polygonScope=key;draft.polygonActions=new ArrayList<>();draft.stage=0;
        if(draft.entries==null)draft.entries=new ArrayList<>();while(draft.entries.size()<1)draft.entries.add("");
        return state(q,draft);
    }
    private static boolean append(HelpPlan.Draft draft,String action){
        if(draft.polygonActions.size()>=MAX_ACTIONS)return false;
        List<String> copy=new ArrayList<>(draft.polygonActions);copy.add(action);draft.polygonActions=copy;return true;
    }
    public static boolean visit(Question q,HelpPlan.Draft draft,int index){
        State s=state(q,draft);if(s==null||s.complete()||!isAction(q,s.stage())||!required(q,s.stage()).contains(index)||s.marked().contains(index))return false;
        return append(draft,"visit:"+index);
    }
    /** A real validated advance owns both the trace and the ordinary help fields. Wrong input stays. */
    public static boolean advance(Question q,HelpPlan.Draft draft,String raw){
        State s=state(q,draft);if(s==null||s.complete()||raw==null||raw.length()>120)return false;
        while(draft.entries.size()<=s.stage())draft.entries.add("");draft.entries.set(s.stage(),raw);
        StudyGuide.Frame frame=KoreanPolygons.guide(q).frames.get(s.stage());String value=raw.trim();
        if(!s.ready()||!frame.expected.equals(value)||!append(draft,"advance:"+value))return false;
        draft.stage++;while(draft.entries.size()<=draft.stage)draft.entries.add("");return true;
    }
    public static boolean resetInspection(Question q,HelpPlan.Draft draft){State s=state(q,draft);return s!=null&&!s.complete()&&isAction(q,s.stage())&&!s.marked().isEmpty()&&append(draft,"reset");}
    public static boolean ready(Question q,HelpPlan.Draft draft){State s=state(q,draft);return s!=null&&s.ready();}
    public static List<Integer> marked(Question q,HelpPlan.Draft draft){State s=state(q,draft);return s==null?List.of():s.marked();}
}
