package com.gomgomapps.math.core;

import java.util.*;

/** UI observation callbacks and learner answers, replayed against the public sentence. */
public final class KoreanRightAngleLesson {
    private KoreanRightAngleLesson(){}
    public static final int MAX_ACTIONS=64;
    public record State(int stage,boolean pending,boolean observed,String entry,boolean ready,boolean complete){}
    private static String scope(Question q){
        if(KoreanRightAngles.read(q)==null||q.id==null||q.id.isBlank())return null;
        return KoreanRightAngles.VERSION+"|"+q.id.length()+":"+q.id+"|"+q.prompt;
    }
    private static final class Replay {
        int stage;boolean pending,observed;List<String> committed=new ArrayList<>();
    }
    private static Replay replay(Question q,List<String> actions){
        StudyGuide h=KoreanRightAngles.guide(q);if(h==null||actions==null||actions.size()>MAX_ACTIONS)return null;
        Replay r=new Replay();
        for(String action:new ArrayList<>(actions)){
            if(action==null||r.stage>=h.frames.size())return null;
            if(action.equals("view:request")){
                if(r.stage!=0||r.pending)return null;r.pending=true;
            }else if(action.equals("view:finish")){
                if(r.stage!=0||!r.pending)return null;r.pending=false;r.observed=true;
            }else if(action.startsWith("answer:")){
                String raw=action.substring(7);
                if(raw.length()>120||r.pending||r.stage==0&&!r.observed||!new HelpPlan.Step(h.frames.get(r.stage)).accepts(raw))return null;
                r.committed.add(raw);r.stage++;
            }else return null;
        }
        return r;
    }
    public static State state(Question q,HelpPlan.Draft d){
        String key=scope(q);if(key==null||d==null||!Objects.equals(q.id,d.questionId)||!KoreanRightAngles.VERSION.equals(d.teachingVersion)||
            !key.equals(d.rightAngleScope)||d.entries==null)return null;
        Replay r=replay(q,d.rightAngleActions);if(r==null||d.stage!=r.stage||d.entries.size()<r.stage)return null;
        for(int i=0;i<r.stage;i++)if(!r.committed.get(i).equals(d.entries.get(i)))return null;
        boolean complete=r.stage==KoreanRightAngles.guide(q).frames.size();
        String entry=complete||d.entries.size()<=r.stage?"":d.entries.get(r.stage);
        if(entry==null)entry="";if(entry.length()>120)return null;
        return new State(r.stage,r.pending,r.observed,entry,!r.pending&&(r.stage>0||r.observed),complete);
    }
    /** Legacy migration belongs to HelpPlan.restore; mismatched public scopes fail closed here. */
    public static State restore(Question q,HelpPlan.Draft d){
        String key=scope(q);if(key==null||d==null||!Objects.equals(q.id,d.questionId)||!KoreanRightAngles.VERSION.equals(d.teachingVersion))return null;
        if(d.rightAngleScope==null&&d.stage==0&&(d.rightAngleActions==null||d.rightAngleActions.isEmpty())){
            d.rightAngleScope=key;d.rightAngleActions=new ArrayList<>();if(d.entries==null)d.entries=new ArrayList<>();
            if(d.entries.isEmpty())d.entries.add("");
        }
        return state(q,d);
    }
    private static List<String> compact(Question q,List<String> actions){
        Replay r=replay(q,actions);if(r==null)return null;List<String> out=new ArrayList<>();
        if(r.observed){out.add("view:request");out.add("view:finish");}
        if(r.pending)out.add("view:request");for(String raw:r.committed)out.add("answer:"+raw);return out;
    }
    private static boolean append(Question q,HelpPlan.Draft d,String action){
        List<String> actions=new ArrayList<>(d.rightAngleActions);actions.add(action);
        List<String> bounded=compact(q,actions);if(bounded==null||bounded.size()>MAX_ACTIONS)return false;
        d.rightAngleActions=bounded;return true;
    }
    /** A restored pending request may be replayed by the UI; this never fabricates its callback. */
    public static boolean observe(Question q,HelpPlan.Draft d){
        State s=state(q,d);if(s==null||s.complete()||s.stage()!=0)return false;
        return s.pending()||append(q,d,"view:request");
    }
    public static boolean finishObservation(Question q,HelpPlan.Draft d){
        State s=state(q,d);return s!=null&&s.stage()==0&&s.pending()&&append(q,d,"view:finish");
    }
    /** Failed current input is preserved, but no stage or observation proof is awarded. */
    public static boolean advance(Question q,HelpPlan.Draft d,String raw){
        State s=state(q,d);if(s==null||s.complete()||raw==null||raw.length()>120)return false;
        while(d.entries.size()<=s.stage())d.entries.add("");d.entries.set(s.stage(),raw);
        if(!s.ready()||!new HelpPlan.Step(KoreanRightAngles.guide(q).frames.get(s.stage())).accepts(raw)||!append(q,d,"answer:"+raw))return false;
        d.stage++;while(d.entries.size()<=d.stage)d.entries.add("");return true;
    }
    public static boolean pending(Question q,HelpPlan.Draft d){State s=state(q,d);return s!=null&&s.pending();}
    public static String feedback(Question q,HelpPlan.Draft d){
        State s=state(q,d);if(s==null||s.complete())return null;
        if(s.stage()==0&&!s.ready())return s.pending()?"직각까지 펼치는 움직임을 이어 보세요.":"직각까지 펼친 뒤 확인하세요.";
        return KoreanRightAngles.feedback(q,s.stage(),s.entry());
    }
    public static boolean restart(Question q,HelpPlan.Draft d){
        if(state(q,d)==null)return false;d.rightAngleActions=new ArrayList<>();d.entries=new ArrayList<>(List.of(""));d.stage=0;return true;
    }
}
