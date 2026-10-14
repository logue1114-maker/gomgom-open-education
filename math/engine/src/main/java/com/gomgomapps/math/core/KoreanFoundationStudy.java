package com.gomgomapps.math.core;

import java.time.LocalDate;
import java.util.*;

/** Study requests and real spaced evidence for the selected unit's core problems. */
public final class KoreanFoundationStudy {
    private KoreanFoundationStudy(){}

    public static void flagged(Learning.State state,Diagnosis.UnitScreen screen,String id,String reason,LocalDate day){
        screen.reasons.put(id,reason);screen.flaggedAt.put(id,state.reviewSequence);
        screen.recoveredSkills.remove(id);screen.freshRechecks.remove(id);screen.status=Diagnosis.UnitStatus.NEEDS_STUDY;
        Review.requestRecheck(state,id,reason,day);
    }

    public static Learning.Session beginStudy(Learning.State state,Diagnosis.UnitScreen screen,Random random){
        return begin(state,screen,random,false,null);
    }
    public static Learning.Session beginRecheck(Learning.State state,Diagnosis.UnitScreen screen,Random random){
        return begin(state,screen,random,true,null);
    }
    public static Learning.Session beginEasier(Learning.State state,Random random){
        Learning.Session current=state.session;
        if(current==null||current.foundationPlanKey==null||current.question==null)throw new IllegalArgumentException("공부할 단원 선택 필요");
        Diagnosis.Plan plan=state.diagnostics.get(current.foundationPlanKey);
        Diagnosis.UnitScreen screen=plan==null?null:plan.unitScreens.get(current.foundationUnitKey);
        if(screen==null)throw new IllegalArgumentException("공부할 단원 선택 필요");
        String lower=foundation(state,plan,current.question.skillId,true);
        if(lower==null)throw new IllegalArgumentException("이 문제의 개념부터 확인 필요");
        return begin(state,screen,random,false,lower);
    }

    private static Learning.Session begin(Learning.State state,Diagnosis.UnitScreen screen,Random random,boolean recheck,String easier){
        Diagnosis.Plan plan=Diagnosis.planFor(state,screen);
        if(plan==null)throw new IllegalArgumentException("저장한 단원 점검을 찾을 수 없음");
        Set<String> learned=new HashSet<>(KoreanFoundationUnits.scope(state.profile));
        List<String> targets=new ArrayList<>();
        for(String id:screen.reasons.keySet())if(!screen.recoveredSkills.contains(id)&&plan.scope.contains(id)&&learned.contains(id))targets.add(id);
        if(targets.isEmpty())for(String id:screen.representatives)if(learned.contains(id))targets.add(id);
        if(targets.isEmpty())throw new IllegalArgumentException("공부할 단원의 배운 범위 확인 필요");
        List<String> queue=new ArrayList<>();
        for(String target:targets){
            String lower=easier==null?foundation(state,plan,target):easier;
            String warm=lower==null?target:lower;
            // Actual study opportunities, then fresh target questions with a gap between checks.
            queue.add(warm);queue.add(warm);queue.add(target);
            queue.add(warm);queue.add(warm);queue.add(target);
        }
        LinkedHashSet<String> selected=new LinkedHashSet<>(queue);
        Map<String,Integer> grades=new LinkedHashMap<>();
        for(String id:selected)grades.put(id,screen.members.contains(id)&&!Set.of("count","compare").contains(id)?screen.reviewGrade:plan.placements.getOrDefault(id,Curriculum.grade(Catalog.get(id),plan.curriculum)));
        Learning.Session s=Learning.beginPractice(state,recheck?"foundationRecheck":"foundationStudy",selected,queue.size(),false,random,grades);
        s.queue.clear();s.queue.addAll(queue);s.foundationPlanKey=plan.key;s.foundationUnitKey=screen.key;
        s.foundationRecheck=recheck;s.foundationStudyPath.addAll(selected);
        return s;
    }

    private static String foundation(Learning.State state,Diagnosis.Plan plan,String target){
        return foundation(state,plan,target,false);
    }
    private static String foundation(Learning.State state,Diagnosis.Plan plan,String target,boolean learnerRequested){
        List<String> prerequisites=Catalog.foundationOrder(target);
        Set<String> learned=new HashSet<>(KoreanFoundationUnits.scope(state.profile));
        for(int i=prerequisites.size()-1;i>=0;i--){
            String id=prerequisites.get(i);if(!plan.scope.contains(id)||!learned.contains(id))continue;
            Learning.Progress p=state.progress.get(id);
            if(!learnerRequested&&p!=null&&p.review!=null&&p.review.recent&&!p.review.pending&&p.review.cleanRun>=2)continue;
            return id;
        }
        return null;
    }

    public static void recordFinish(Learning.State state,Learning.Session session,boolean independent,boolean hadError,boolean skip,LocalDate day){
        if(session.question==null||state.diagnostics==null)return;
        String id=session.question.skillId;
        for(Diagnosis.Plan plan:state.diagnostics.values()){
            if(!plan.unitScreening||plan.unitScreens==null||!Objects.equals(plan.educationSystem,session.educationSystem))continue;
            for(Diagnosis.UnitScreen screen:plan.unitScreens.values()){
                boolean ownStudy=Objects.equals(plan.key,session.foundationPlanKey)&&Objects.equals(screen.key,session.foundationUnitKey);
                if(!screen.reasons.containsKey(id)&&!ownStudy)continue;
                if(hadError||skip||!independent){
                    if(screen.flaggedAt.getOrDefault(id,-1L)!=state.reviewSequence)flagged(state,screen,id,hadError?"wrong":skip?"unable":"help",day);
                    continue;
                }
                if(!screen.reasons.containsKey(id))continue;
                Learning.Progress progress=state.progress.get(id);Review.Track track=progress==null?null:progress.review;
                if(track==null)continue;
                long since=screen.flaggedAt.getOrDefault(id,Long.MAX_VALUE);
                Set<String> fresh=screen.freshRechecks.computeIfAbsent(id,ignored->new LinkedHashSet<>());
                for(Review.Event event:track.events)if(event.sequence>since&&event.questionId.equals(session.question.id)&&event.outcome==Review.Outcome.RECHECK_CORRECT)fresh.add(event.signature);
                if(track.pending||!track.recent||track.recoveryChecks<Review.RECOVERY_CHECKS||fresh.size()<Review.RECOVERY_CHECKS)continue;
                screen.recoveredSkills.add(id);
                if(screen.recoveredSkills.containsAll(screen.reasons.keySet()))screen.status=Diagnosis.UnitStatus.RECENTLY_CHECKED;
            }
        }
    }
}
