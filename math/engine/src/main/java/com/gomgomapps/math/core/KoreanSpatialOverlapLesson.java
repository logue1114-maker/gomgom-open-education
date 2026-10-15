package com.gomgomapps.math.core;

import java.util.*;

/** Optional directional replay and learner drawing. Only the four child draft fields are changed. */
public final class KoreanSpatialOverlapLesson {
    private KoreanSpatialOverlapLesson(){}
    public static final String VERSION="korean-spatial-overlap-v2";
    public static final int MAX_ACTIONS=128;

    public record Model(String kind,int line,List<Integer> cells,List<Integer> heights){
        public Model {cells=List.copyOf(cells);heights=List.copyOf(heights);}
    }
    /** viewed records the UI's animation callback, never attention or comprehension. viewing is an unfinished request. */
    public record State(int phase,boolean viewed,boolean viewing,List<Integer> drawn,String entry,boolean ready,boolean complete){
        public State {drawn=List.copyOf(drawn);}
    }

    public static Model model(Question q,HelpPlan.Draft d){
        if(d==null)return null;
        var g=KoreanSpatialSolids.read(q);var parent=KoreanSpatialLesson.state(q,d);
        if(g==null||parent==null||parent.complete()||parent.stage()!=0||parent.selected()<0||!(KoreanSpatialSolids.FRONT.equals(g.kind())||KoreanSpatialSolids.SIDE.equals(g.kind())))return null;
        List<Integer> cells=new ArrayList<>(KoreanSpatialLesson.lineCells(g,parent.selected()));
        if(KoreanSpatialSolids.SIDE.equals(g.kind()))Collections.reverse(cells);
        List<Integer> heights=new ArrayList<>();for(int cell:cells)heights.add(g.height(cell));
        return new Model(g.kind(),parent.selected(),cells,heights);
    }
    public static boolean available(Question q,HelpPlan.Draft d){return model(q,d)!=null;}
    private static void field(StringBuilder out,String s){if(s==null)out.append("-1:");else out.append(s.length()).append(':').append(s);out.append('|');}
    private static void fields(StringBuilder out,List<String> list){if(list==null){out.append("-1[]|");return;}out.append(list.size()).append("[|");for(String s:list)field(out,s);out.append("]|");}
    private static String scope(Question q,HelpPlan.Draft d){
        if(!available(q,d))return null;StringBuilder out=new StringBuilder(VERSION+"|");
        for(String value:Arrays.asList(q.id,q.skillId,q.prompt,q.diagram.type,Arrays.toString(q.diagram.values),Arrays.toString(q.diagram.labels),Arrays.toString(q.diagram.rotationDegrees),Arrays.toString(q.diagram.horizontalScale),d.questionId,d.teachingVersion,d.spatialScope,Integer.toString(d.stage),Integer.toString(KoreanSpatialLesson.state(q,d).selected()),d.spatialPendingInput))field(out,value);
        fields(out,q.choices);fields(out,d.entries);fields(out,d.spatialActions);return out.toString();
    }
    private static int unionHeight(Model m){int result=0;for(int h:m.heights)result=Math.max(result,h);return result;}
    private static boolean number(String raw){return raw!=null&&raw.matches("[0-9]{1,3}");}
    private static boolean sameHeight(Model m,String raw){return number(raw)&&Integer.parseInt(raw)==unionHeight(m);}
    private static boolean drawing(Model m,Set<Integer> drawn){int height=unionHeight(m);for(int z=0;z<3;z++)if(drawn.contains(z)!=(z<height))return false;return true;}
    /** Pure replay. Only a request followed by the UI callback can finish a view. */
    public static State state(Question q,HelpPlan.Draft d){
        String key=scope(q,d);Model m=model(q,d);
        if(key==null||!key.equals(d.spatialOverlapScope)||d.spatialOverlapActions==null||d.spatialOverlapActions.size()>MAX_ACTIONS)return null;
        int phase=0;boolean viewed=false,viewing=false;Set<Integer> drawn=new LinkedHashSet<>();String entry="";
        for(String action:new ArrayList<>(d.spatialOverlapActions)){
            if(action==null||action.length()>128||phase==3)return null;
            if(action.equals("view:request")){if(viewing)return null;viewing=true;}
            else if(action.equals("view:finish")){if(!viewing)return null;viewing=false;viewed=true;}
            else if(action.matches("toggle:[0-2]")){
                if(phase!=1||viewing)return null;int z=Integer.parseInt(action.substring(7));if(!drawn.add(z))drawn.remove(z);entry="";
            }else if(action.equals("try:drawing")){
                if(phase!=1||viewing||drawing(m,drawn))return null;entry="drawing";
            }else if(action.startsWith("try:number:")){
                String raw=action.substring(11);if(phase!=2||viewing||!number(raw)||sameHeight(m,raw))return null;entry=raw;
            }else if(action.equals("advance:ok")){
                if(viewing)return null;if(phase==0){if(!viewed)return null;}
                else if(phase!=1||!drawing(m,drawn))return null;
                phase++;entry="";
            }else if(action.startsWith("answer:")){
                if(phase!=2||viewing||!sameHeight(m,action.substring(7)))return null;phase=3;entry="";
            }else return null;
        }
        boolean ready=!viewing&&(phase>=2||phase==0&&viewed||phase==1&&drawing(m,drawn));
        return new State(phase,viewed,viewing,new ArrayList<>(drawn),entry,ready,phase==3);
    }
    private static void reset(HelpPlan.Draft d,String key){d.spatialOverlapScope=key;d.spatialOverlapOpen=false;d.spatialOverlapActions=new ArrayList<>();d.spatialOverlapPendingInput=null;}
    /** Old v1 actions are never interpreted as v2 viewing or drawing evidence. */
    public static State restore(Question q,HelpPlan.Draft d){
        if(d==null)return null;String key=scope(q,d);if(key==null){reset(d,null);return null;}
        State s=state(q,d);if(s==null){reset(d,key);s=state(q,d);}return s;
    }
    public static State open(Question q,HelpPlan.Draft d){
        if(d==null)return null;String key=scope(q,d);boolean corrupt=key!=null&&key.equals(d.spatialOverlapScope)&&state(q,d)==null;
        State s=restore(q,d);if(s==null||corrupt)return null;d.spatialOverlapOpen=true;return s;
    }
    public static State close(Question q,HelpPlan.Draft d){State s=restore(q,d);if(d!=null)d.spatialOverlapOpen=false;return s;}
    public static State restart(Question q,HelpPlan.Draft d){
        if(d==null)return null;String key=scope(q,d);reset(d,key);if(key==null)return null;d.spatialOverlapOpen=true;return state(q,d);
    }
    private static void segment(List<String> out,boolean finished,boolean pending,Set<String> toggles,String attempt){
        if(finished){out.add("view:request");out.add("view:finish");}out.addAll(toggles);if(attempt!=null)out.add(attempt);if(pending)out.add("view:request");
    }
    /** Retain a real view request in each phase and successful gates; only superseded attempts are discarded. */
    private static void compact(HelpPlan.Draft d){
        List<String> out=new ArrayList<>();boolean finished=false,pending=false;Set<String> toggles=new LinkedHashSet<>();String attempt=null;
        for(String action:d.spatialOverlapActions){
            if(action.equals("view:request")){pending=true;continue;}
            if(action.equals("view:finish")){pending=false;finished=true;continue;}
            if(action.startsWith("toggle:")){if(!toggles.add(action))toggles.remove(action);attempt=null;continue;}
            if(action.startsWith("try:")){attempt=action;continue;}
            segment(out,finished,pending,toggles,null);out.add(action);finished=false;pending=false;toggles.clear();attempt=null;
        }
        segment(out,finished,pending,toggles,attempt);d.spatialOverlapActions=out;
    }
    private static boolean append(HelpPlan.Draft d,String action){
        if(d.spatialOverlapActions.size()>=MAX_ACTIONS)compact(d);if(d.spatialOverlapActions.size()>=MAX_ACTIONS)return false;
        List<String> copy=new ArrayList<>(d.spatialOverlapActions);copy.add(action);d.spatialOverlapActions=copy;compact(d);return true;
    }
    private static State active(Question q,HelpPlan.Draft d){State s=state(q,d);return s==null||!d.spatialOverlapOpen||s.complete()?null:s;}
    /** Request the animation again without clearing any learner drawing or answer. */
    public static boolean watch(Question q,HelpPlan.Draft d){State s=active(q,d);return s!=null&&!s.viewing&&append(d,"view:request");}
    public static boolean finishView(Question q,HelpPlan.Draft d){State s=active(q,d);return s!=null&&s.viewing&&append(d,"view:finish");}
    public static boolean toggle(Question q,HelpPlan.Draft d,int z){State s=active(q,d);return s!=null&&!s.viewing&&s.phase==1&&z>=0&&z<3&&append(d,"toggle:"+z);}
    public static boolean advance(Question q,HelpPlan.Draft d,String raw){
        State s=active(q,d);if(s==null||s.viewing||raw==null||raw.length()>120)return false;String value=raw.trim();
        if(s.phase==0)return s.ready&&value.equals("ok")&&append(d,"advance:ok");
        if(s.phase==1){
            if(!value.equals("ok"))return false;
            boolean saved=append(d,s.ready?"advance:ok":"try:drawing");return saved&&s.ready;
        }
        if(s.phase!=2||!number(value))return false;boolean correct=sameHeight(model(q,d),value);boolean saved=append(d,(correct?"answer:":"try:number:")+value);return saved&&correct;
    }
    public static String feedback(Question q,HelpPlan.Draft d){
        State s=state(q,d);if(s==null||s.entry.isEmpty())return null;
        if(s.phase==1)return "움직임을 다시 보고, 파란 줄에서 보이는 칸만 표시하세요.";
        return s.phase==2?"그 방향에서 보이는 칸을 아래부터 한 번씩 세세요. 기둥마다 센 수를 모두 더하지 않습니다.":null;
    }
}
