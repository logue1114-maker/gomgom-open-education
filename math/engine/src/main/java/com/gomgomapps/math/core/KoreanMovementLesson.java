package com.gomgomapps.math.core;

import java.util.*;

/** Replays only the learner's actions, separately from the original answer and progress. */
public final class KoreanMovementLesson {
    private KoreanMovementLesson(){}
    public static final int ACTION_STAGE=3;
    private static boolean bound(Question q,HelpPlan.Draft d){
        StudyGuide guide=KoreanPlaneMovement.guide(q);
        return d!=null&&q!=null&&guide!=null&&Objects.equals(q.id,d.questionId)&&Objects.equals(guide.teachingVersion,d.teachingVersion);
    }
    public static List<String> operations(Question q){
        if(q==null)return List.of();
        return switch(q.skillId){
            case "krPlaneSlide"->List.of("left","right","up","down");
            case "krPlaneFlip"->List.of("horizontal","vertical");
            case "krPlaneTurn"->List.of("cw","ccw");
            default->List.of();
        };
    }
    public static List<KoreanPlaneMovement.Cell> placement(Question q,HelpPlan.Draft d){
        if(!bound(q,d))return null;
        var given=KoreanPlaneMovement.read(q);List<KoreanPlaneMovement.Cell> cells=given.source();
        if(d.movementActions==null)return cells;
        if(d.movementActions.size()>64)return null;
        for(String action:d.movementActions){
            if(!operations(q).contains(action))return null;
            cells=KoreanPlaneMovement.apply(cells,action);if(cells==null)return null;
        }
        return cells;
    }
    private static boolean reached(Question q,HelpPlan.Draft d){
        var cells=placement(q,d);var given=KoreanPlaneMovement.read(q);
        return given!=null&&cells!=null&&d.movementActions!=null&&!d.movementActions.isEmpty()&&KoreanPlaneMovement.same(cells,KoreanPlaneMovement.target(given));
    }
    public static void restore(Question q,HelpPlan.Draft d){
        if(!bound(q,d))return;
        if(d.movementActions==null)d.movementActions=new ArrayList<>();
        if(placement(q,d)==null)d.movementActions=new ArrayList<>();
        // A literal saved token alone cannot stand in for doing the movement.
        if(d.stage>ACTION_STAGE&&!reached(q,d)){
            d.stage=ACTION_STAGE;while(d.entries.size()<=ACTION_STAGE)d.entries.add("");d.entries.set(ACTION_STAGE,"");
        }
    }
    public static boolean move(Question q,HelpPlan.Draft d,String action){
        if(!bound(q,d)||d.stage!=ACTION_STAGE||!operations(q).contains(action))return false;
        var cells=placement(q,d);if(cells==null||d.movementActions!=null&&d.movementActions.size()>=64)return false;
        if(KoreanPlaneMovement.apply(cells,action)==null)return false;
        if(d.movementActions==null)d.movementActions=new ArrayList<>();d.movementActions.add(action);return true;
    }
    public static void reset(Question q,HelpPlan.Draft d){if(bound(q,d)&&d.stage==ACTION_STAGE)d.movementActions=new ArrayList<>();}
    public static boolean complete(Question q,HelpPlan.Draft d){return bound(q,d)&&d.stage==ACTION_STAGE&&reached(q,d);}
    public static boolean advance(Question q,HelpPlan.Draft d){
        if(!complete(q,d))return false;
        while(d.entries.size()<=ACTION_STAGE+1)d.entries.add("");d.entries.set(ACTION_STAGE,"ok");d.stage=ACTION_STAGE+1;return true;
    }
}
