package com.gomgomapps.math.core;

import java.util.*;

/** Replayed learner observations and drawings, bound to one public stack and its actual choices. */
public final class KoreanSpatialLesson {
    private KoreanSpatialLesson(){}
    public static final int MAX_ACTIONS=128;
    public record State(int stage,int selected,List<Integer> marked,List<Integer> confirmed,List<Integer> drawn,String entry,boolean ready,boolean complete){
        public State {marked=List.copyOf(marked);confirmed=List.copyOf(confirmed);drawn=List.copyOf(drawn);}
    }
    private static String scope(Question q){
        if(KoreanSpatialSolids.read(q)==null||KoreanSpatialSolids.guide(q)==null||q.id==null||q.id.isBlank())return null;
        return KoreanSpatialSolids.VERSION+"|"+q.id.length()+":"+q.id+"|"+KoreanSpatialSolids.identity(q)+"|"+q.diagram.type+"|"+Arrays.toString(q.diagram.values)+"|"+q.prompt+"|"+q.choices;
    }
    public static boolean isAction(Question q,int stage){StudyGuide h=KoreanSpatialSolids.guide(q);return h!=null&&stage>=0&&stage<h.frames.size()&&KoreanSpatialSolids.ACTION_FORMAT.equals(h.frames.get(stage).inputFormat);}
    public static int lineCount(KoreanSpatialSolids.Givens g){return KoreanSpatialSolids.FRONT.equals(g.kind())?g.width():g.depth();}
    public static List<Integer> lineCells(KoreanSpatialSolids.Givens g,int line){
        if(g==null||line<0||line>=lineCount(g))return List.of();List<Integer> out=new ArrayList<>();
        if(KoreanSpatialSolids.FRONT.equals(g.kind()))for(int y=0;y<g.depth();y++)out.add(y*g.width()+line);
        else for(int x=0;x<g.width();x++)out.add(line*g.width()+x);return List.copyOf(out);
    }
    private static List<Integer> range(int n){List<Integer> out=new ArrayList<>();for(int i=0;i<n;i++)out.add(i);return out;}
    private static boolean count(KoreanSpatialSolids.Givens g,int stage){return KoreanSpatialSolids.COUNT.equals(g.kind())&&stage==0;}
    private static boolean lines(KoreanSpatialSolids.Givens g,int stage){return KoreanSpatialSolids.isView(g.kind())&&stage==0;}
    private static boolean candidates(KoreanSpatialSolids.Givens g,int stage){return KoreanSpatialSolids.RECONSTRUCT.equals(g.kind())&&stage==1;}
    private static List<Integer> required(KoreanSpatialSolids.Givens g,int stage,int selected){
        if(count(g,stage))return selected<0?List.of():range(g.height(selected));
        if(lines(g,stage))return lineCells(g,selected);
        if(KoreanSpatialSolids.RECONSTRUCT.equals(g.kind())&&(stage==0||stage==1&&selected>=0))return range(3);
        return List.of();
    }
    public static List<Integer> required(Question q,HelpPlan.Draft d){State s=state(q,d);return s==null?List.of():required(KoreanSpatialSolids.read(q),s.stage(),s.selected());}
    public static List<Integer> selectable(Question q,HelpPlan.Draft d){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);if(s==null||s.complete())return List.of();
        return count(g,s.stage())?range(g.width()*g.depth()):lines(g,s.stage())?range(lineCount(g)):candidates(g,s.stage())?range(3):List.of();
    }
    private static String expected(Question q,KoreanSpatialSolids.Givens g,int stage,int selected){
        if(selected<0)return null;
        if(count(g,stage))return ""+g.height(selected);
        if(lines(g,stage)){int value=0;for(int cell:lineCells(g,selected))value=KoreanSpatialSolids.TOP.equals(g.kind())?value+(g.height(cell)>0?1:0):Math.max(value,g.height(cell));return ""+value;}
        if(candidates(g,stage)){List<Integer> h=KoreanSpatialSolids.candidate(g,q.choices.get(selected));return KoreanSpatialSolids.matches(g,h)?"fits":"notFits";}
        return null;
    }
    private static boolean validConfirm(KoreanSpatialSolids.Givens g,int stage,String raw){return candidates(g,stage)?Set.of("fits","notFits").contains(raw):raw.matches("[0-9]{1,3}");}
    private static boolean sameEntry(String expected,String raw){return expected.equals(raw)||expected.matches("[0-9]+")&&raw.matches("[0-9]{1,3}")&&Integer.parseInt(expected)==Integer.parseInt(raw);}
    private static boolean ready(KoreanSpatialSolids.Givens g,int stage,int selected,Set<Integer> marked,Set<Integer> confirmed,Set<Integer> drawn){
        if(count(g,stage))return selected<0&&confirmed.size()==g.width()*g.depth();
        if(lines(g,stage))return selected<0&&confirmed.size()==lineCount(g);
        if(KoreanSpatialSolids.isView(g.kind())&&stage==1){var p=KoreanSpatialSolids.target(g);for(int i=0;i<p.bits().size();i++)if(drawn.contains(i)!=(p.bits().get(i)==1))return false;return true;}
        if(KoreanSpatialSolids.RECONSTRUCT.equals(g.kind()))return stage==0?marked.size()==3:stage==1?selected<0&&confirmed.size()==3:true;
        return true;
    }
    /** Pure replay. No arbitrary completion marker can replace counting, comparisons or a correct drawing. */
    public static State state(Question q,HelpPlan.Draft d){
        String key=scope(q);var g=KoreanSpatialSolids.read(q);StudyGuide h=KoreanSpatialSolids.guide(q);
        if(key==null||d==null||!Objects.equals(q.id,d.questionId)||!KoreanSpatialSolids.VERSION.equals(d.teachingVersion)||!key.equals(d.spatialScope)||d.spatialActions==null||d.spatialActions.size()>MAX_ACTIONS)return null;
        int stage=0,selected=-1;Set<Integer> marked=new LinkedHashSet<>(),confirmed=new LinkedHashSet<>(),drawn=new LinkedHashSet<>();String entry="";List<String> committed=new ArrayList<>();
        for(String action:new ArrayList<>(d.spatialActions)){
            if(action==null||stage>=h.frames.size())return null;
            if(action.matches("select:[0-8]")){
                int index=Integer.parseInt(action.substring(7)),size=count(g,stage)?g.width()*g.depth():lines(g,stage)?lineCount(g):candidates(g,stage)?3:0;
                if(selected>=0||index>=size||confirmed.contains(index))return null;selected=index;marked.clear();entry="";
            }else if(action.matches("cube:[0-2]")){
                int z=Integer.parseInt(action.substring(5));if(!count(g,stage)||selected<0||z!=marked.size()||z>=g.height(selected)||!marked.add(z))return null;
            }else if(action.matches("visit:[0-8]")){
                int index=Integer.parseInt(action.substring(6));if(count(g,stage)||!required(g,stage,selected).contains(index)||!marked.add(index))return null;
            }else if(action.matches("toggle:[0-8]")){
                int index=Integer.parseInt(action.substring(7));if(!KoreanSpatialSolids.isView(g.kind())||stage!=1||index>=KoreanSpatialSolids.target(g).bits().size())return null;
                if(!drawn.add(index))drawn.remove(index);
            }else if(action.startsWith("try:")||action.startsWith("confirm:")){
                boolean confirmation=action.startsWith("confirm:");String raw=action.substring(confirmation?8:4),expected=expected(q,g,stage,selected);
                if(expected==null||!validConfirm(g,stage,raw))return null;
                if(!confirmation){if(sameEntry(expected,raw))return null;entry=raw;}
                else {if(!sameEntry(expected,raw)||!marked.containsAll(required(g,stage,selected))||!confirmed.add(selected))return null;selected=-1;marked.clear();entry="";}
            }else if(action.startsWith("advance:")){
                String raw=action.substring(8);if(raw.length()>120)return null;
                if(isAction(q,stage)){if(!raw.equals("ok")||!ready(g,stage,selected,marked,confirmed,drawn))return null;}
                else if(!KoreanSpatialSolids.check(q,List.of(raw)).correct())return null;
                committed.add(raw);stage++;selected=-1;marked.clear();confirmed.clear();drawn.clear();entry="";
            }else return null;
        }
        if(d.stage!=stage||d.entries==null||d.entries.size()<stage)return null;
        for(int i=0;i<stage;i++)if(d.entries.get(i)==null||!committed.get(i).equals(d.entries.get(i).trim()))return null;
        boolean complete=stage==h.frames.size();return new State(stage,selected,new ArrayList<>(marked),new ArrayList<>(confirmed),new ArrayList<>(drawn),entry,complete||ready(g,stage,selected,marked,confirmed,drawn),complete);
    }
    public static State restore(Question q,HelpPlan.Draft d){
        if(d==null)return null;String key=scope(q);if(key==null||!Objects.equals(q.id,d.questionId)||!KoreanSpatialSolids.VERSION.equals(d.teachingVersion))return null;
        State s=state(q,d);if(s!=null)return s;d.spatialScope=key;d.spatialActions=new ArrayList<>();d.stage=0;d.spatialPendingInput=null;
        if(d.entries==null)d.entries=new ArrayList<>();while(d.entries.size()<1)d.entries.add("");return state(q,d);
    }
    /** Retain actual observation proof, the current wrong entry and net drawing; remove only superseded attempts. */
    private static void compact(HelpPlan.Draft d){
        List<String> out=new ArrayList<>();String pendingTry=null;LinkedHashSet<String> toggles=new LinkedHashSet<>();
        for(String action:d.spatialActions){
            if(action.startsWith("try:")){pendingTry=action;continue;}
            if(action.startsWith("toggle:")){if(!toggles.add(action))toggles.remove(action);continue;}
            if(action.startsWith("confirm:")||action.startsWith("advance:"))pendingTry=null;
            if(action.startsWith("advance:")){out.addAll(toggles);toggles.clear();}
            out.add(action);
        }
        out.addAll(toggles);if(pendingTry!=null)out.add(pendingTry);d.spatialActions=out;
    }
    private static boolean append(HelpPlan.Draft d,String action){
        if(d.spatialActions.size()>=MAX_ACTIONS)compact(d);
        if(d.spatialActions.size()>=MAX_ACTIONS)return false;List<String> copy=new ArrayList<>(d.spatialActions);copy.add(action);d.spatialActions=copy;return true;
    }
    public static boolean select(Question q,HelpPlan.Draft d,int index){
        State s=state(q,d);return s!=null&&!s.complete()&&s.selected()<0&&!s.confirmed().contains(index)&&selectable(q,d).contains(index)&&append(d,"select:"+index);
    }
    public static boolean cube(Question q,HelpPlan.Draft d,int z){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);return s!=null&&!s.complete()&&count(g,s.stage())&&s.selected()>=0&&z==s.marked().size()&&z<g.height(s.selected())&&append(d,"cube:"+z);
    }
    public static boolean visit(Question q,HelpPlan.Draft d,int index){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);return s!=null&&!s.complete()&&!count(g,s.stage())&&required(g,s.stage(),s.selected()).contains(index)&&!s.marked().contains(index)&&append(d,"visit:"+index);
    }
    public static boolean toggle(Question q,HelpPlan.Draft d,int index){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);return s!=null&&!s.complete()&&KoreanSpatialSolids.isView(g.kind())&&s.stage()==1&&index>=0&&index<KoreanSpatialSolids.target(g).bits().size()&&append(d,"toggle:"+index);
    }
    public static boolean confirm(Question q,HelpPlan.Draft d,String raw){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);if(s==null||s.complete()||raw==null||raw.length()>120||s.selected()<0)return false;String value=raw.trim();
        if(!validConfirm(g,s.stage(),value))return false;String expected=expected(q,g,s.stage(),s.selected());if(expected==null)return false;
        if(!sameEntry(expected,value)){append(d,"try:"+value);return false;}
        boolean accepted=s.marked().containsAll(required(g,s.stage(),s.selected()))&&append(d,"confirm:"+value);if(accepted)compact(d);return accepted;
    }
    /** Advance alone owns ordinary help entries; a wrong original field remains stored. */
    public static boolean advance(Question q,HelpPlan.Draft d,String raw){
        State s=state(q,d);if(s==null||s.complete()||raw==null||raw.length()>120)return false;while(d.entries.size()<=d.stage)d.entries.add("");d.entries.set(d.stage,raw);String value=raw.trim();
        boolean correct=isAction(q,d.stage)?s.ready()&&value.equals("ok"):KoreanSpatialSolids.check(q,List.of(value)).correct();
        if(!correct||!append(d,"advance:"+value))return false;d.stage++;while(d.entries.size()<=d.stage)d.entries.add("");compact(d);return true;
    }
    /** Explicitly restart just the current observation; prior verified stages and current input remain. */
    public static boolean resetInspection(Question q,HelpPlan.Draft d){
        State s=state(q,d);if(s==null||s.complete()||!isAction(q,s.stage()))return false;int after=0;
        for(int i=0;i<d.spatialActions.size();i++)if(d.spatialActions.get(i).startsWith("advance:"))after=i+1;
        d.spatialActions=new ArrayList<>(d.spatialActions.subList(0,after));return true;
    }
    public static boolean ready(Question q,HelpPlan.Draft d){State s=state(q,d);return s!=null&&s.ready();}
    public static List<Integer> marked(Question q,HelpPlan.Draft d){State s=state(q,d);return s==null?List.of():s.marked();}
    public static String confirmFeedback(Question q,HelpPlan.Draft d){
        State s=state(q,d);var g=KoreanSpatialSolids.read(q);if(s==null||s.entry().isEmpty()||s.selected()<0)return null;
        if(count(g,s.stage()))return "고른 바닥칸의 높이를 보며 나무를 아래부터 한 번씩 세세요. 빈 칸은 0개입니다.";
        if(lines(g,s.stage()))return KoreanSpatialSolids.TOP.equals(g.kind())?"그 줄에서 나무가 있는 바닥칸만 한 번씩 세세요.":"앞뒤로 겹친 기둥의 높이는 더하지 않습니다. 가장 높은 기둥까지 보이는지 비교하세요.";
        List<Integer> h=KoreanSpatialSolids.candidate(g,q.choices.get(s.selected()));return KoreanSpatialSolids.matches(g,h)?"이 후보의 세 모양을 주어진 모양과 한 방향씩 다시 비교하세요.":"이 후보는 주어진 세 모양을 모두 만족하지 않습니다. 다른 방향에서도 같은 모양인지 확인하세요.";
    }
}
