package com.gomgomapps.math.core;

import java.util.*;

/** Keeps concrete comparisons separate from the original answer and review evidence. */
public final class KoreanTriangleLesson {
    private KoreanTriangleLesson(){}
    public static int inspectionStage(Question q){
        if(KoreanTriangleClassification.read(q)==null)return -1;
        return KoreanTriangleClassification.SIDES.equals(q.skillId)?0:KoreanTriangleClassification.ANGLES.equals(q.skillId)?1:-1;
    }
    private static boolean bound(Question q,HelpPlan.Draft d){
        StudyGuide h=KoreanTriangleClassification.guide(q);
        return h!=null&&d!=null&&Objects.equals(q.id,d.questionId)&&Objects.equals(h.teachingVersion,d.teachingVersion);
    }
    public static boolean inspected(Question q,HelpPlan.Draft d){
        int stage=inspectionStage(q);
        return bound(q,d)&&stage>=0&&d.triangleVisitStage==stage&&d.triangleVisited!=null&&
            d.triangleVisited.size()==3&&new HashSet<>(d.triangleVisited).equals(Set.of(0,1,2));
    }
    public static void restore(Question q,HelpPlan.Draft d){
        if(!bound(q,d))return;
        int gate=inspectionStage(q);
        if(d.triangleVisited==null)d.triangleVisited=new ArrayList<>();
        if(d.triangleVisitStage!=gate||d.triangleVisited.size()>3||d.triangleVisited.stream().anyMatch(n->n==null||n<0||n>2)||new HashSet<>(d.triangleVisited).size()!=d.triangleVisited.size()){
            d.triangleVisitStage=gate;d.triangleVisited=new ArrayList<>();d.triangleVertex=-1;
        }
        if(d.triangleVertex<0||d.triangleVertex>2||!d.triangleVisited.contains(d.triangleVertex))d.triangleVertex=-1;
        if(gate>=0&&d.stage>gate&&!inspected(q,d)){
            d.stage=gate;while(d.entries.size()<=gate)d.entries.add("");d.entries.set(gate,"");
        }
    }
    public static boolean visit(Question q,HelpPlan.Draft d,int index){
        if(!bound(q,d)||inspectionStage(q)<0||d.stage!=inspectionStage(q)||index<0||index>2)return false;
        restore(q,d);d.triangleVertex=index;
        if(!d.triangleVisited.contains(index))d.triangleVisited.add(index);
        return true;
    }
    public static boolean advance(Question q,HelpPlan.Draft d,String value){
        if(!bound(q,d))return false;
        HelpPlan plan=HelpPlan.forQuestion(q);int stage=d.stage;
        if(stage<0||stage>=plan.size())return false;
        while(d.entries.size()<=stage)d.entries.add("");d.entries.set(stage,value==null?"":value);
        if(stage==inspectionStage(q)&&!inspected(q,d)||!plan.step(stage).accepts(value))return false;
        d.stage++;while(d.entries.size()<=d.stage)d.entries.add("");return true;
    }
}
