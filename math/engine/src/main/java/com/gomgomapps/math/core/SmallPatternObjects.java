package com.gomgomapps.math.core;

import java.util.*;

/** An optional counting detour; only its bounded action trace changes, never the lesson answers. */
public final class SmallPatternObjects {
    private SmallPatternObjects(){}
    public static final String VERSION="small-pattern-objects-v1";
    public static final int MAX_ACTIONS=64;
    private static final String INSTRUCTION="수의 규칙을 찾아 빈 수를 구하세요.";

    public record Givens(int first,int second,int delta,int blank,List<Integer> terms) {
        public Givens {terms=List.copyOf(terms);}
    }
    public record State(int phase,List<Integer> marked,boolean ready,boolean complete) {
        public State {marked=List.copyOf(marked);}
    }

    /** Read the visible five-term array. Stored answers, expression and guide are irrelevant. */
    public static Givens read(Question q){
        if(q==null||!"el_number_pattern".equals(q.skillId)||q.prompt==null||q.prompt.length()>160)return null;
        String[] lines=q.prompt.split("\\R",-1);
        if(lines.length!=2||!INSTRUCTION.equals(lines[1]))return null;
        String[] tokens=lines[0].split(" → ",-1);if(tokens.length!=5)return null;
        int[] shown=new int[5];int blank=-1;
        for(int i=0;i<5;i++){
            if(tokens[i].equals("□")){if(blank>=0)return null;blank=i;}
            else {if(!tokens[i].matches("(?:[1-9]|1[0-9]|20)"))return null;shown[i]=Integer.parseInt(tokens[i]);}
        }
        if(blank<2)return null;
        int delta=shown[1]-shown[0];if(delta<1||delta>4)return null;
        List<Integer> terms=new ArrayList<>();
        for(int i=0;i<5;i++){
            int value=shown[0]+i*delta;
            if(value<1||value>20||i!=blank&&shown[i]!=value)return null;
            terms.add(value);
        }
        return new Givens(shown[0],shown[1],delta,blank,terms);
    }

    private static String scope(Question q){
        return read(q)==null||q.id==null||q.id.isBlank()||q.id.length()>128?null:
                VERSION+"|"+q.id.length()+":"+q.id+"|"+q.prompt;
    }
    private static int count(Givens g,int phase){
        return phase==0?g.first():phase==1?g.second():phase==2?g.delta():0;
    }
    /** A pure replay: an invalid prefix, duplicate, premature next or extra action invalidates all. */
    public static State state(Question q,HelpPlan.Draft draft){
        Givens g=read(q);String key=scope(q);
        if(g==null||key==null||draft==null||!key.equals(draft.patternObjectsScope))return null;
        List<String> actions=draft.patternObjectActions==null?List.of():new ArrayList<>(draft.patternObjectActions);
        if(actions.size()>MAX_ACTIONS)return null;
        int phase=0;Set<Integer> marked=new LinkedHashSet<>();
        for(String action:actions){
            if(phase==3||action==null)return null;
            if(action.equals("next")){
                if(marked.size()!=count(g,phase))return null;
                phase++;marked.clear();
            }else if(action.matches("m:(?:0|[1-9][0-9]?)")){
                int index=Integer.parseInt(action.substring(2));
                if(index>=count(g,phase)||!marked.add(index))return null;
            }else return null;
        }
        return new State(phase,new ArrayList<>(marked),phase==3||marked.size()==count(g,phase),phase==3);
    }

    private static void reset(HelpPlan.Draft draft,String key){
        draft.patternObjectsScope=key;draft.patternObjectsOpen=false;draft.patternObjectActions=new ArrayList<>();
    }
    /** Restore only the tool fields; the parent's version, stage and numeric entries stay untouched. */
    public static State restore(Question q,HelpPlan.Draft draft){
        if(draft==null)return null;
        String key=scope(q);
        if(key==null){reset(draft,null);return null;}
        State restored=state(q,draft);
        if(restored==null){reset(draft,key);restored=state(q,draft);}
        return restored;
    }
    public static boolean open(Question q,HelpPlan.Draft draft){
        if(draft==null)return false;
        String key=scope(q);if(key==null){restore(q,draft);return false;}
        boolean corrupted=key.equals(draft.patternObjectsScope)&&state(q,draft)==null;
        State restored=restore(q,draft);
        if(restored==null||corrupted)return false;
        draft.patternObjectsOpen=true;return true;
    }
    private static State active(Question q,HelpPlan.Draft draft){
        State current=state(q,draft);
        if(current==null){restore(q,draft);return null;}
        return draft.patternObjectsOpen?current:null;
    }
    private static boolean append(HelpPlan.Draft draft,String action){
        if(draft.patternObjectActions!=null&&draft.patternObjectActions.size()>=MAX_ACTIONS)return false;
        List<String> trace=draft.patternObjectActions==null?new ArrayList<>():new ArrayList<>(draft.patternObjectActions);
        trace.add(action);draft.patternObjectActions=trace;return true;
    }
    /** Indices are local to the visible group; phase2 has just delta extra objects, indexed from0. */
    public static boolean mark(Question q,HelpPlan.Draft draft,int index){
        State current=active(q,draft);Givens g=read(q);
        if(current==null||current.complete()||index<0||index>=count(g,current.phase())||current.marked().contains(index))return false;
        return append(draft,"m:"+index);
    }
    public static boolean next(Question q,HelpPlan.Draft draft){
        State current=active(q,draft);
        return current!=null&&current.ready()&&!current.complete()&&append(draft,"next");
    }
    public static void close(Question q,HelpPlan.Draft draft){if(draft!=null)draft.patternObjectsOpen=false;}
    public static boolean restart(Question q,HelpPlan.Draft draft){
        if(draft==null)return false;
        String key=scope(q);reset(draft,key);
        if(key==null)return false;
        draft.patternObjectsOpen=true;return true;
    }
}
