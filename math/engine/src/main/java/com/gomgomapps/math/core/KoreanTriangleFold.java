package com.gomgomapps.math.core;

import java.io.Serializable;
import java.util.*;

/** Concrete folding reconstructed from the public triangle and a bounded learner action trace. */
public final class KoreanTriangleFold {
    private KoreanTriangleFold(){}
    public static final String VERSION="korean-triangle-properties-fold-v2";
    public static final int FOLD_STAGE=1,MAX_ACTIONS=96;

    /** A step is 45 degrees of a physical fold; the picture is its projection onto the paper. */
    public record State(String questionId,String version,List<String> actions,int axis,int step,
                        Set<Integer> observed,Set<Integer> checkedAxes,
                        KoreanTriangleClassification.Vertex seamStart,KoreanTriangleClassification.Vertex seamEnd,
                        KoreanTriangleClassification.Vertex movingOriginal,KoreanTriangleClassification.Vertex moving,
                        KoreanTriangleClassification.Vertex fixed,int movingIndex,int fixedIndex,boolean overlap) implements Serializable {
        public State {
            actions=List.copyOf(actions);
            observed=Collections.unmodifiableSet(new LinkedHashSet<>(observed));
            checkedAxes=Collections.unmodifiableSet(new LinkedHashSet<>(checkedAxes));
        }
        public List<KoreanTriangleClassification.Vertex> movingTriangle(){return axis<0?List.of():List.of(seamStart,seamEnd,moving);}
        public List<KoreanTriangleClassification.Vertex> fixedTriangle(){return axis<0?List.of():List.of(seamStart,seamEnd,fixed);}
    }
    public static boolean supports(String id){return KoreanTriangleClassification.PROPERTIES.equals(id);}
    private static KoreanTriangleClassification.Givens givens(Question q){return q!=null&&supports(q.skillId)?KoreanTriangleClassification.read(q):null;}
    public static List<Integer> requiredAxes(Question q){var g=givens(q);return g==null?List.of():g.form().equals("equilateral")?List.of(0,1):List.of(0);}
    private static State geometry(Question q,KoreanTriangleClassification.Givens g,List<String> trace,int axis,int step,Set<Integer> observed,Set<Integer> checked){
        if(axis<0)return new State(q.id,VERSION,trace,-1,0,observed,checked,null,null,null,null,null,-1,-1,false);
        int j=(axis+1)%3,k=(axis+2)%3;var apex=g.vertices().get(axis);var original=g.vertices().get(j);var fixed=g.vertices().get(k);
        var midpoint=new KoreanTriangleClassification.Vertex((original.x()+fixed.x())/2,(original.y()+fixed.y())/2);
        double dx=midpoint.x()-apex.x(),dy=midpoint.y()-apex.y(),lengthSquared=dx*dx+dy*dy;
        double along=((original.x()-apex.x())*dx+(original.y()-apex.y())*dy)/lengthSquared;
        double px=apex.x()+along*dx,py=apex.y()+along*dy;
        double cosine=step==2?0:step==4?-1:step==0?1:Math.cos(step*Math.PI/4);
        var moved=new KoreanTriangleClassification.Vertex(px+(original.x()-px)*cosine,py+(original.y()-py)*cosine);
        double scale=0;for(double length:g.sides())scale=Math.max(scale,length);
        boolean overlap=step==4&&Math.hypot(moved.x()-fixed.x(),moved.y()-fixed.y())<=Math.max(1,scale)*1e-8;
        return new State(q.id,VERSION,trace,axis,step,observed,checked,apex,midpoint,original,moved,fixed,j,k,overlap);
    }
    /** A missing old trace is an untouched tool, never a completed one. Invalid traces fail closed. */
    public static State restore(Question q,String questionId,String version,List<String> actions){
        var g=givens(q);if(g==null||q.id==null||q.id.isBlank()||!Objects.equals(q.id,questionId)||!VERSION.equals(version))return null;
        List<String> trace=actions==null?List.of():new ArrayList<>(actions);if(trace.size()>MAX_ACTIONS)return null;
        int axis=-1,step=0;Set<Integer> observed=new LinkedHashSet<>(),checked=new LinkedHashSet<>();List<String> prefix=new ArrayList<>();
        for(String action:trace){
            if(action==null)return null;
            if(action.matches("axis:[0-2]")){
                int next=action.charAt(5)-'0';if(next==axis)return null;axis=next;step=0;observed.clear();
            }else if(action.equals("fold")){
                if(axis<0||step>=4)return null;step++;observed.clear();
            }else if(action.equals("unfold")){
                if(axis<0||step<=0)return null;step--;observed.clear();
            }else if(action.matches("angle:[0-2]")){
                State current=geometry(q,g,prefix,axis,step,observed,checked);int vertex=action.charAt(6)-'0';
                if(!current.overlap||vertex!=current.movingIndex&&vertex!=current.fixedIndex||!observed.add(vertex))return null;
                if(observed.size()==2)checked.add(axis);
            }else return null;
            prefix.add(action);
        }
        return geometry(q,g,trace,axis,step,observed,checked);
    }
    public static State state(Question q){return q==null?null:restore(q,q.id,VERSION,List.of());}
    /** Does not trust caller-supplied geometry, visited sets, step, or completion flags. */
    public static State state(Question q,State saved){return saved==null?null:restore(q,saved.questionId,saved.version,saved.actions);}
    private static State append(Question q,State saved,String action){State current=state(q,saved);if(current==null||current.actions.size()>=MAX_ACTIONS)return null;List<String> trace=new ArrayList<>(current.actions);trace.add(action);return restore(q,current.questionId,VERSION,trace);}
    public static State selectAxis(Question q,State saved,int axis){return axis<0||axis>2?null:append(q,saved,"axis:"+axis);}
    public static State advanceFold(Question q,State saved){return append(q,saved,"fold");}
    public static State unfold(Question q,State saved){return append(q,saved,"unfold");}
    public static State observe(Question q,State saved,int vertex){return vertex<0||vertex>2?null:append(q,saved,"angle:"+vertex);}
    public static State reset(Question q){return state(q);}
    public static boolean complete(Question q,State saved){State current=state(q,saved);return current!=null&&current.step==4&&current.overlap&&current.observed.size()==2&&current.checkedAxes.containsAll(requiredAxes(q));}
}
