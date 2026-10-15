package com.gomgomapps.math.core;

import java.time.LocalDate;
import java.util.*;

/** Study requests and real spaced evidence for the selected unit's core problems. */
public final class KoreanFoundationStudy {
    private KoreanFoundationStudy(){}

    private static final CurriculumLimits RULE_WARMUP_LIMITS=new CurriculumLimits("maxGiven=20;maxResult=20;nonnegative=true;answerDomain=integer");

    /** Reduce arithmetic load only for new prerequisite questions in this Korean rules lesson. */
    static CurriculumLimits questionLimits(Learning.State state,Learning.Session session,String id,CurriculumLimits baseline){
        if(baseline!=CurriculumLimits.NONE||session==null||!"el_number_pattern".equals(id)||
            !Set.of("foundationStudy","foundationRecheck").contains(session.mode==null?"":session.mode)||
            !"KR".equals(session.countryCode)||!"kr-national".equals(session.educationSystem)||
            !"KR|2022|4|2|3".equals(session.foundationUnitKey)||session.foundationPlanKey==null||
            session.selected==null||!session.selected.contains(id)||state==null||state.diagnostics==null)return baseline;
        Diagnosis.Plan plan=state.diagnostics.get(session.foundationPlanKey);
        if(plan==null||!plan.unitScreening||plan.curriculum!=2022||!"kr-national".equals(plan.educationSystem)||
            !Objects.equals(plan.key,session.foundationPlanKey)||plan.scope==null||!plan.scope.contains(id)||plan.unitScreens==null)return baseline;
        Diagnosis.UnitScreen unit=plan.unitScreens.get(session.foundationUnitKey);
        if(unit==null||!Objects.equals(unit.key,session.foundationUnitKey)||!Objects.equals(unit.planKey,plan.key)||
            unit.members==null||unit.representatives==null)return baseline;
        Learning.Session context=KoreanFoundationDetour.supplyContext(state,session);
        if(context==null)return baseline;
        for(String target:unit.representatives)if(KoreanRulesRelations.supports(target)&&unit.members.contains(target)&&
            plan.scope.contains(target)&&context.selected.contains(target)&&Catalog.foundationOrder(target).contains(id))return RULE_WARMUP_LIMITS;
        return baseline;
    }

    public static void flagged(Learning.State state,Diagnosis.UnitScreen screen,String id,String reason,LocalDate day){
        project(state,screen,id,reason);
        Review.requestRecheck(state,id,reason,day);
    }

    private static void project(Learning.State state,Diagnosis.UnitScreen screen,String id,String reason){
        screen.reasons.put(id,reason);screen.flaggedAt.put(id,state.reviewSequence);
        screen.recoveredSkills.remove(id);screen.freshRechecks.remove(id);screen.status=Diagnosis.UnitStatus.NEEDS_STUDY;
    }

    /** Reflect only the active question's already-recorded intervention, without a new review event. */
    static void recordIntervention(Learning.State state){
        Learning.Session session=state.session;
        if(session==null||session.question==null||session.finished||session.advancePending||Deferred.active(session)||
            "diagnostic".equals(session.mode)||session.diagnosticRun!=null&&session.diagnosticRun.current!=null)return;
        Review.Work work=session.reviewWork;
        if(work==null||work.finished||!Objects.equals(work.questionId,session.question.id)||!work.errorRecorded&&!work.helpUsed)return;
        String id=session.question.skillId;
        Learning.Progress progress=state.progress.get(id);Review.Track track=progress==null?null:progress.review;
        if(track==null||!track.pending||!Objects.equals(track.sourceId,session.question.id))return;
        Diagnosis.UnitScreen screen=currentLearnedUnit(state,session,id);
        if(screen==null)return;
        // Never retarget a restored intervention after the learner changes the learned scope.
        if(!sameProjectionScope(work,screen))return;
        String reason=work.errorRecorded?"wrong":"help";
        if(projected(session,screen,id)&&reason.equals(work.foundationReason))return;
        project(state,screen,id,reason);
        work.foundationPlanKey=screen.planKey;work.foundationUnitKey=screen.key;work.foundationReason=reason;
    }

    private static boolean projected(Learning.Session session,Diagnosis.UnitScreen screen,String id){
        Review.Work work=session.reviewWork;
        return work!=null&&Objects.equals(work.questionId,session.question.id)&&
            Objects.equals(work.foundationPlanKey,screen.planKey)&&Objects.equals(work.foundationUnitKey,screen.key)&&
            work.foundationReason!=null&&work.foundationReason.equals(screen.reasons.get(id))&&screen.flaggedAt.containsKey(id);
    }

    private static boolean sameProjectionScope(Review.Work work,Diagnosis.UnitScreen screen){
        return work==null||((work.foundationPlanKey==null||work.foundationPlanKey.isBlank()||work.foundationPlanKey.equals(screen.planKey))&&
            (work.foundationUnitKey==null||work.foundationUnitKey.isBlank()||work.foundationUnitKey.equals(screen.key)));
    }

    public static Learning.Session beginStudy(Learning.State state,Diagnosis.UnitScreen screen,Random random){
        return begin(state,screen,random,false,null);
    }
    public static Learning.Session beginRecheck(Learning.State state,Diagnosis.UnitScreen screen,Random random){
        return begin(state,screen,random,true,null);
    }
    public static Learning.Session beginEasier(Learning.State state,Random random){
        return KoreanFoundationDetour.begin(state,random);
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
        if(hadError||skip||!independent){
            Diagnosis.UnitScreen current=currentLearnedUnit(state,session,id);
            if(current!=null&&sameProjectionScope(session.reviewWork,current)&&!current.reasons.containsKey(id))flagged(state,current,id,hadError?"wrong":skip?"unable":"help",day);
        }
        for(Diagnosis.Plan plan:state.diagnostics.values()){
            if(!plan.unitScreening||plan.unitScreens==null||!Objects.equals(plan.educationSystem,session.educationSystem))continue;
            for(Diagnosis.UnitScreen screen:plan.unitScreens.values()){
                boolean ownStudy=Objects.equals(plan.key,session.foundationPlanKey)&&Objects.equals(screen.key,session.foundationUnitKey);
                if(!screen.reasons.containsKey(id)&&!ownStudy)continue;
                if(hadError||skip||!independent){
                    // Review.finish increments the sequence; the same question is not a new gap.
                    if(!sameProjectionScope(session.reviewWork,screen))continue;
                    if(projected(session,screen,id))continue;
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

    /** A new ordinary-practice gap belongs only to the current frozen, still-learned unit. */
    private static Diagnosis.UnitScreen currentLearnedUnit(Learning.State state,Learning.Session session,String id){
        if("foundationStudy".equals(session.mode)||"foundationRecheck".equals(session.mode))return null;
        if(!Diagnosis.foundationAscending(state.profile)||!"KR".equals(GlobalCurriculum.country(session))||!"kr-national".equals(GlobalCurriculum.system(session)))return null;
        if(session.diagnosticRun!=null&&session.diagnosticRun.plan!=null&&session.diagnosticRun.plan.curriculum!=Curriculum.version(state.profile))return null;
        try{
            Diagnosis.Plan plan=Diagnosis.currentPlan(state);
            if(plan==null||!plan.unitScreening||!plan.foundationAscending||plan.unitScreens==null||
                plan.curriculum!=Curriculum.version(state.profile)||!Objects.equals(plan.key,Diagnosis.scopeKey(state.profile))||!plan.scope.contains(id))return null;
            String system=plan.educationSystem==null||plan.educationSystem.isBlank()?"kr-national":plan.educationSystem;
            if(!system.equals(GlobalCurriculum.system(session)))return null;
            for(KoreanFoundationUnits.Unit unit:KoreanFoundationUnits.learned(state.profile))if(unit.members().contains(id)){
                Diagnosis.UnitScreen screen=plan.unitScreens.get(unit.key());
                if(screen!=null&&Objects.equals(screen.planKey,plan.key)&&screen.members.contains(id))return screen;
            }
        }catch(IllegalArgumentException invalidScope){
            // An invalid/stale endpoint must not relabel the problem or block its normal submission.
        }
        return null;
    }
}
