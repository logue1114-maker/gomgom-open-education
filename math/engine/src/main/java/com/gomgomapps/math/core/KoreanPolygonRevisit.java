package com.gomgomapps.math.core;

import java.util.*;

/** Optional same-picture comparison. Its trace never advances or repairs the parent lesson. */
public final class KoreanPolygonRevisit {
    private KoreanPolygonRevisit(){}
    public static final String VERSION="korean-polygon-revisit-v1";
    public static final int MAX_ACTIONS=128;
    public record State(int phase,List<Integer> marked,boolean ready,boolean complete,String entry){
        public State {marked=List.copyOf(marked);}
    }

    /** Only a real, recognized wrong choice after the parent's required observations can reopen them. */
    public static boolean available(Question q,HelpPlan.Draft draft){
        if(draft==null)return false;
        KoreanPolygonLesson.State parent=KoreanPolygonLesson.state(q,draft);
        if(parent==null||parent.complete()||KoreanPolygonLesson.isAction(q,parent.stage())||draft.entries.size()<=parent.stage())return false;
        String raw=draft.entries.get(parent.stage());StudyGuide h=KoreanPolygons.guide(q);
        if(raw==null||raw.length()>120)return false;
        StudyGuide.Frame frame=h.frames.get(parent.stage());String value=raw.trim();
        return frame.options.containsKey(value)&&!frame.expected.equals(value);
    }
    private static List<Integer> parentStages(Question q,HelpPlan.Draft draft){
        if(!available(q,draft))return List.of();
        if(KoreanPolygons.IDENTIFY.equals(q.skillId))return draft.stage==1?List.of(0,1):draft.stage==2?List.of(0,2):List.of(0,1,2);
        if(KoreanPolygons.NAME.equals(q.skillId))return List.of(0,1,2);
        if(KoreanPolygons.REGULAR.equals(q.skillId))return draft.stage==2?List.of(0,2):draft.stage==3?List.of(1,3):List.of(0,1,2,3,4);
        return List.of(0,1);
    }
    /** Fresh public-condition frames; never use the stored guide or change the parent's fields. */
    public static StudyGuide guide(Question q,HelpPlan.Draft draft){
        List<Integer> stages=parentStages(q,draft);if(stages.isEmpty())return null;
        StudyGuide original=KoreanPolygons.guide(q),out=new StudyGuide().transfer(false);out.teachingVersion=VERSION;
        for(int stage:stages)out.frames.add(original.frames.get(stage));
        return out;
    }
    /** 0: boundary/diagonal, 1: corner/angle, -1: no observation tool at this phase. */
    public static int pictureStage(Question q,HelpPlan.Draft draft,int phase){
        List<Integer> stages=parentStages(q,draft);
        if(phase<0||phase>=stages.size())return -1;
        int parent=stages.get(phase);
        return KoreanPolygonLesson.isAction(q,parent)?parent:-1;
    }
    public static List<Integer> required(Question q,HelpPlan.Draft draft,int phase){
        List<Integer> stages=parentStages(q,draft);
        return phase<0||phase>=stages.size()?List.of():KoreanPolygonLesson.required(q,stages.get(phase));
    }
    private static void field(StringBuilder out,String value){out.append(value.length()).append(':').append(value).append('|');}
    private static String scope(Question q,HelpPlan.Draft draft){
        if(!available(q,draft))return null;
        StringBuilder key=new StringBuilder(VERSION+"|");
        for(String value:List.of(q.id,q.skillId,q.prompt,Arrays.toString(q.diagram.values),Arrays.toString(q.diagram.labels),
                new TreeMap<>(q.givenNumbers).toString(),draft.teachingVersion,Integer.toString(draft.stage),draft.entries.get(draft.stage)))field(key,value);
        for(String action:draft.polygonActions)field(key,action);
        return key.toString();
    }
    /** Pure replay, including attempted wrong choices. A malformed prefix invalidates the whole trace. */
    public static State state(Question q,HelpPlan.Draft draft){
        String key=scope(q,draft);StudyGuide h=guide(q,draft);
        if(key==null||!key.equals(draft.polygonRevisitScope)||draft.polygonRevisitActions==null||draft.polygonRevisitActions.size()>MAX_ACTIONS)return null;
        int phase=0;Set<Integer> marked=new LinkedHashSet<>();String entry="";
        for(String action:new ArrayList<>(draft.polygonRevisitActions)){
            if(action==null||phase>=h.frames.size())return null;
            StudyGuide.Frame frame=h.frames.get(phase);boolean observation=KoreanPolygons.ACTION_FORMAT.equals(frame.inputFormat);
            if(action.matches("visit:[0-7]")){
                int index=Integer.parseInt(action.substring(6));
                if(!observation||!required(q,draft,phase).contains(index)||!marked.add(index))return null;
            }else if(action.equals("advance:ok")){
                if(!observation||!marked.containsAll(required(q,draft,phase)))return null;
                phase++;marked.clear();entry="";
            }else if(action.startsWith("answer:")){
                String raw=action.substring(7);
                if(observation||raw.length()>120||!frame.options.containsKey(raw))return null;
                entry=raw;
                if(frame.expected.equals(raw)){phase++;marked.clear();entry="";}
            }else return null;
        }
        boolean complete=phase==h.frames.size();
        boolean ready=complete||!KoreanPolygons.ACTION_FORMAT.equals(h.frames.get(phase).inputFormat)||marked.containsAll(required(q,draft,phase));
        return new State(phase,new ArrayList<>(marked),ready,complete,entry);
    }
    private static void reset(HelpPlan.Draft draft,String key){
        draft.polygonRevisitScope=key;draft.polygonRevisitOpen=false;draft.polygonRevisitActions=new ArrayList<>();
    }
    /** Only child fields reset. Wrong parent input and completed parent observations remain byte-for-byte. */
    public static State restore(Question q,HelpPlan.Draft draft){
        if(draft==null)return null;String key=scope(q,draft);
        if(key==null){reset(draft,null);return null;}
        State current=state(q,draft);
        if(current==null){reset(draft,key);current=state(q,draft);}
        return current;
    }
    public static State open(Question q,HelpPlan.Draft draft){
        if(draft==null)return null;String key=scope(q,draft);
        boolean corrupted=key!=null&&key.equals(draft.polygonRevisitScope)&&state(q,draft)==null;
        State current=restore(q,draft);if(current==null||corrupted)return null;
        draft.polygonRevisitOpen=true;return current;
    }
    public static State restart(Question q,HelpPlan.Draft draft){
        if(draft==null)return null;String key=scope(q,draft);reset(draft,key);
        if(key==null)return null;draft.polygonRevisitOpen=true;return state(q,draft);
    }
    public static boolean close(Question q,HelpPlan.Draft draft){
        if(draft==null)return false;draft.polygonRevisitOpen=false;return true;
    }
    private static State active(Question q,HelpPlan.Draft draft){
        State current=state(q,draft);if(current==null){restore(q,draft);return null;}
        return draft.polygonRevisitOpen?current:null;
    }
    private static boolean append(HelpPlan.Draft draft,String action){
        if(draft.polygonRevisitActions.size()>=MAX_ACTIONS)return false;
        List<String> trace=new ArrayList<>(draft.polygonRevisitActions);trace.add(action);draft.polygonRevisitActions=trace;return true;
    }
    public static boolean visit(Question q,HelpPlan.Draft draft,int index){
        State current=active(q,draft);
        return current!=null&&!current.complete()&&pictureStage(q,draft,current.phase())>=0&&
                required(q,draft,current.phase()).contains(index)&&!current.marked().contains(index)&&append(draft,"visit:"+index);
    }
    /** A public correctness predicate for the current child field; it does not record or advance. */
    public static boolean accepted(Question q,HelpPlan.Draft draft,String raw){
        State current=state(q,draft);if(current==null||current.complete()||raw==null||raw.length()>120||!current.ready())return false;
        return guide(q,draft).frames.get(current.phase()).expected.equals(raw.trim());
    }
    /** Wrong recognized choices are retained in this trace; incomplete observations have no side effect. */
    public static boolean advance(Question q,HelpPlan.Draft draft,String raw){
        State current=active(q,draft);if(current==null||current.complete()||raw==null||raw.length()>120)return false;
        StudyGuide.Frame frame=guide(q,draft).frames.get(current.phase());String value=raw.trim();
        if(KoreanPolygons.ACTION_FORMAT.equals(frame.inputFormat))return current.ready()&&value.equals("ok")&&append(draft,"advance:ok");
        if(!frame.options.containsKey(value)||!append(draft,"answer:"+value))return false;
        return frame.expected.equals(value);
    }
    /** Explain only a valid wrong response, using its corresponding public comparison. */
    public static String feedback(Question q,HelpPlan.Draft draft,String raw){
        State current=state(q,draft);if(current==null||current.complete())return null;
        return KoreanPolygons.feedback(q,parentStages(q,draft).get(current.phase()),raw);
    }
}
