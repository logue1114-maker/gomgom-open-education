package com.gomgomapps.math.core;

import java.util.*;

/** Two requested prerequisite problems, then the exact parked question and its own work. */
public final class KoreanFoundationDetour {
    private KoreanFoundationDetour(){}

    private static String scope(Learning.State state){
        if(state==null||state.profile==null||!Diagnosis.foundationAscending(state.profile))return null;
        try{return Diagnosis.scopeKey(state.profile);}catch(IllegalArgumentException invalid){return null;}
    }
    private static boolean studyMode(Learning.Session session){
        return session!=null&&("foundationStudy".equals(session.mode)||"foundationRecheck".equals(session.mode));
    }
    private static Diagnosis.Plan plan(Learning.State state,Learning.Session session){
        String key=scope(state);
        if(key==null||!studyMode(session)||state.diagnostics==null||session.diagnosticRun!=null||
            !"KR".equals(GlobalCurriculum.country(session))||!"kr-national".equals(GlobalCurriculum.system(session)))return null;
        Diagnosis.Plan plan=state.diagnostics.get(session.foundationPlanKey);
        if(plan==null||!Objects.equals(plan.key,session.foundationPlanKey)||!plan.unitScreening||
            !plan.foundationAscending||plan.curriculum!=Curriculum.version(state.profile)||
            !"kr-national".equals(plan.educationSystem)||plan.scope==null||plan.unitScreens==null)return null;
        Diagnosis.UnitScreen screen=plan.unitScreens.get(session.foundationUnitKey);
        if(screen==null||!Objects.equals(screen.key,session.foundationUnitKey)||!Objects.equals(screen.planKey,plan.key))return null;
        for(KoreanFoundationUnits.Unit unit:KoreanFoundationUnits.learned(state.profile))if(unit.key().equals(screen.key))return plan;
        return null;
    }
    private static boolean liveParent(Learning.State state,Learning.Session session){
        Diagnosis.Plan plan=plan(state,session);
        return plan!=null&&session.question!=null&&!session.finished&&!session.advancePending&&!Deferred.active(session)&&
            session.selected!=null&&session.selected.contains(session.question.skillId)&&plan.scope.contains(session.question.skillId)&&
            KoreanFoundationUnits.scope(state.profile).contains(session.question.skillId);
    }
    private static String lower(Learning.State state,Learning.Session session){
        if(!liveParent(state,session))return null;
        Diagnosis.Plan plan=plan(state,session);Set<String> learned=new HashSet<>(KoreanFoundationUnits.scope(state.profile));
        List<String> order=Catalog.foundationOrder(session.question.skillId);
        for(int i=order.size()-1;i>=0;i--)if(plan.scope.contains(order.get(i))&&learned.contains(order.get(i)))return order.get(i);
        return null;
    }
    public static boolean available(Learning.State state){return state!=null&&supplyContext(state,state.session)!=null&&lower(state,state.session)!=null;}
    public static Learning.Session begin(Learning.State state,Random random){
        Learning.Session parent=state==null?null:state.session;String id=available(state)?lower(state,parent):null;
        if(id==null)throw new IllegalArgumentException("이 문제의 개념부터 확인 필요");
        Diagnosis.Plan plan=plan(state,parent);String key=scope(state);
        int grade=parent.selectedGrades.getOrDefault(id,plan.placements.getOrDefault(id,Curriculum.grade(Catalog.get(id),plan.curriculum)));
        Learning.Session child=Learning.beginPractice(state,"foundationStudy",List.of(id),2,false,random,Map.of(id,grade));
        child.queue.clear();child.queue.add(id);child.queue.add(id);child.foundationStudyPath.add(id);
        child.foundationPlanKey=parent.foundationPlanKey;child.foundationUnitKey=parent.foundationUnitKey;
        child.foundationReturnSessionId=parent.id;child.foundationReturnQuestionId=parent.question.id;child.foundationReturnScopeKey=key;
        return child;
    }
    private static boolean bound(Learning.Session child){
        return child!=null&&child.foundationReturnSessionId!=null&&child.foundationReturnQuestionId!=null&&child.foundationReturnScopeKey!=null;
    }
    private static Learning.Session parent(Learning.State state,Learning.Session child){
        if(!bound(child)||!"foundationStudy".equals(child.mode)||plan(state,child)==null||Deferred.active(child)||
            !Objects.equals(scope(state),child.foundationReturnScopeKey)||state.savedSessions==null||
            child.target!=2||child.completed<0||child.completed>2||child.selected==null||child.selected.size()!=1)return null;
        Learning.Session parent=state.savedSessions.get(child.foundationReturnSessionId);
        if(parent==child||!liveParent(state,parent)||!Objects.equals(parent.id,child.foundationReturnSessionId)||
            !Objects.equals(parent.question.id,child.foundationReturnQuestionId)||
            !Objects.equals(parent.foundationPlanKey,child.foundationPlanKey)||!Objects.equals(parent.foundationUnitKey,child.foundationUnitKey))return null;
        String id=child.selected.get(0);
        if(!Objects.equals(lower(state,parent),id)||child.queue==null||child.queue.stream().anyMatch(next->!id.equals(next))||
            child.question!=null&&!id.equals(child.question.skillId))return null;
        return parent;
    }
    public static boolean canReturn(Learning.State state){return state!=null&&parent(state,state.session)!=null;}
    public static String returnTitle(Learning.State state){
        Learning.Session parent=state==null?null:parent(state,state.session);
        return parent==null?null:GlobalCurriculum.title(parent,Catalog.get(parent.question.skillId).id);
    }
    public static boolean returnToQuestion(Learning.State state,boolean completedOnly){
        Learning.Session child=state==null?null:state.session;
        if(child==null||completedOnly&&(!child.finished||child.completed!=child.target))return false;
        Learning.Session parent=parent(state,child);if(parent==null)return false;
        // Keep the link on an unfinished parked child too; its later normal resume can return again.
        // The parent's own link is never changed, so nested prerequisite study unwinds one level.
        Learning.resume(state,parent.id);return true;
    }
    /** Original lesson context for its scoped supply rules, through validated exact-parent links. */
    static Learning.Session supplyContext(Learning.State state,Learning.Session session){
        Set<String> seen=new HashSet<>();Learning.Session current=session;
        while(current!=null){
            if(!seen.add(current.id))return null;
            if(current.foundationReturnSessionId==null&&current.foundationReturnQuestionId==null&&current.foundationReturnScopeKey==null)return current;
            current=parent(state,current);
        }
        return null;
    }
}
