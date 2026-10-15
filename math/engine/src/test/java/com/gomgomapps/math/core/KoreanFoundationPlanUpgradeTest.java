package com.gomgomapps.math.core;

import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

public class KoreanFoundationPlanUpgradeTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.foundationAscending=true;s.profile.ready=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    /** Reconstruct the exact prior effective mapping, not a claim about an actual device save. */
    private Diagnosis.Plan prior(Learning.State s){
        Diagnosis.Plan p=Diagnosis.currentPlan(s);s.diagnostics.remove(p.key);
        p.scope.removeIf(KoreanFirstTermArithmetic::supports);p.placements.keySet().removeIf(KoreanFirstTermArithmetic::supports);
        for(Diagnosis.UnitScreen u:Diagnosis.screens(p))if(u.members.stream().anyMatch(KoreanFirstTermArithmetic::supports)){u.members.clear();u.representatives.clear();u.label="초등 1학년 · 2학기 · 덧셈과 뺄셈(3) · 핵심 문제 미지원";}
        Set<String> remove=new HashSet<>();for(var entry:p.checks.entrySet())if(KoreanFirstTermArithmetic.supports(entry.getValue().skillId))remove.add(entry.getKey());
        p.checks.keySet().removeAll(remove);p.pending.removeAll(remove);p.untested.removeAll(remove);
        List<String> ids=new ArrayList<>(p.scope);Collections.sort(ids);
        p.key=Curriculum.MAPPING_VERSION+"|"+p.schoolYear+"|"+p.curriculum+"|"+p.actualGrade+"|"+p.term+"||"+String.join(",",ids)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+p.lastLearnedSkill;
        for(Diagnosis.UnitScreen u:Diagnosis.screens(p))u.planKey=p.key;s.diagnostics.put(p.key,p);return p;
    }
    @Test public void additiveUpgradeKeepsARealRunningRoundAndRebindsItsExactProbe(){
        Learning.State s=student();Random r=new Random(374);Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),DAY);
        Diagnosis.Plan old=prior(s);int count=Diagnosis.remainingChecks(old),pending=old.pending.size();Diagnosis.Probe original=s.session.diagnosticRun.current;List<Diagnosis.Probe> scheduled=new ArrayList<>(s.session.diagnosticRun.scheduled);
        s.session.answers.set(0,"123");HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.entries.add("4");s.session.conceptHelp=draft;
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertNotSame(old,next);assertEquals(count+2,Diagnosis.remainingChecks(next));assertEquals(count,Diagnosis.remainingChecks(old));assertEquals(pending,old.pending.size());
        assertSame(q,s.session.question);assertEquals("123",s.session.answers.get(0));assertSame(draft,s.session.conceptHelp);assertSame(next,s.session.diagnosticRun.plan);assertNotSame(original,s.session.diagnosticRun.current);assertEquals(original.skillId,s.session.diagnosticRun.current.skillId);assertEquals(original.reviewGrade,s.session.diagnosticRun.current.reviewGrade);
        assertTrue(s.session.diagnosticRun.current.reserved);assertFalse(next.pending.contains(s.session.diagnosticRun.current.unitKey+"|"+original.skillId));assertEquals(scheduled.stream().map(x->x.unitKey+"|"+x.skillId).toList(),s.session.diagnosticRun.scheduled.stream().map(x->x.unitKey+"|"+x.skillId).toList());
        int oldCompleted=old.unitScreens.get(original.unitKey).completedRepresentatives.size();Learning.finishQuestion(s,true,DAY,r);
        assertEquals(oldCompleted,old.unitScreens.get(original.unitKey).completedRepresentatives.size());assertTrue(next.unitScreens.get(original.unitKey).completedRepresentatives.contains(original.skillId));assertEquals(2,s.diagnostics.size());
        Diagnosis.migrate(s);assertSame(next,Diagnosis.currentPlan(s));assertEquals(2,s.diagnostics.size());
        Learning.State cold=LearningSnapshot.capture(s);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertNotSame(next,Diagnosis.currentPlan(cold));
    }
    @Test public void priorStudyEvidenceSurvivesWhileNewCoresRemainUncheckedAndOldFinishedRunsStayFrozen(){
        Learning.State s=student();Random r=new Random(375);Learning.beginDiagnostic(s,r);while(!s.session.finished){Learning.ensureQuestion(s,new Generator(r),DAY);Learning.finishQuestion(s,true,DAY,r);}
        Learning.Session finished=s.session;Diagnosis.Plan old=prior(s);Diagnosis.UnitScreen screen=old.unitScreens.values().stream().filter(x->!x.reasons.isEmpty()).findFirst().orElseThrow();
        String reason=screen.reasons.keySet().iterator().next();screen.freshRechecks.put(reason,new LinkedHashSet<>(List.of("actual-fixture-signature")));screen.recoveredSkills.add(reason);
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertSame(old,finished.diagnosticRun.plan);
        Diagnosis.UnitScreen copy=next.unitScreens.get(screen.key);assertEquals(screen.reasons,copy.reasons);assertEquals(screen.flaggedAt,copy.flaggedAt);assertEquals(screen.freshRechecks,copy.freshRechecks);assertEquals(screen.recoveredSkills,copy.recoveredSkills);assertNotSame(screen.freshRechecks.get(reason),copy.freshRechecks.get(reason));assertEquals(next.key,copy.planKey);assertSame(next,Diagnosis.planFor(s,copy));
        Diagnosis.UnitScreen added=next.unitScreens.get("KR|2022|1|2|6");assertEquals(List.of("krNoCarryAdd","krNoBorrowSub"),added.representatives);assertEquals(Diagnosis.UnitStatus.UNCHECKED,added.status);assertTrue(added.completedRepresentatives.isEmpty());assertTrue(added.reasons.isEmpty());assertTrue(Diagnosis.studyUnits(s).stream().anyMatch(x->x.key.equals(screen.key)));
    }
    @Test public void aChangedEffectiveScopeOrInvalidProbeDoesNotPartiallyRebind(){
        for(String boundary:List.of("excluded","endpoint","year","bad-probe","foreign","duplicate")){
            Learning.State s=student();Random r=new Random(376);Learning.beginDiagnostic(s,r);Learning.ensureQuestion(s,new Generator(r),DAY);Diagnosis.Plan old=prior(s);Diagnosis.Run run=s.session.diagnosticRun;Diagnosis.Probe before=run.current;
            switch(boundary){case "excluded"->s.profile.excluded.add("add9");case "endpoint"->s.profile.lastLearnedSkill="unit:KR|2022|5|2|6";case "year"->s.profile.schoolYear++;case "bad-probe"->before.unitKey="invalid";case "foreign"->s.session.countryCode="NA";case "duplicate"->s.diagnostics.put("alias",old);}
            Diagnosis.migrate(s);assertNull(boundary,Diagnosis.currentPlan(s));assertSame(boundary,old,run.plan);assertSame(before,run.current);assertSame(old,s.diagnostics.get(old.key));
        }
    }
    @Test public void currentUnitGapsTrackTheNewPracticeWithoutChangingOtherCountriesOrOldCurricula(){
        Learning.State s=student();Random r=new Random(377);Learning.beginDiagnostic(s,r);prior(s);Diagnosis.migrate(s);
        Learning.beginPractice(s,"practice",List.of("krNoCarryAdd"),1,false,r);Learning.ensureQuestion(s,new Generator(r),DAY);Review.helpUsed(s,DAY);Learning.finishQuestion(s,false,DAY,r);
        assertEquals("help",Diagnosis.currentPlan(s).unitScreens.get("KR|2022|1|2|6").reasons.get("krNoCarryAdd"));assertFalse(Curriculum.inCurriculum(Catalog.get("krNoCarryAdd"),2015));
        s.profile.curriculum=2015;assertTrue(KoreanFoundationUnits.available(s.profile).stream().noneMatch(u->u.members().contains("krNoCarryAdd")));
    }

    @Test public void roundSolidAdditionKeepsFinishedRoundAndActiveQuestionWhileAddingOnlyThreeChecks(){
        Learning.State s=student();s.profile.lastLearnedSkill="unit:KR|2022|6|2|6";Random r=new Random(394);
        Learning.beginDiagnostic(s,r);while(!s.session.finished){Learning.ensureQuestion(s,new Generator(r),DAY);Learning.finishQuestion(s,true,DAY,r);}
        Learning.Session finished=s.session;Diagnosis.Plan finishedPlan=finished.diagnosticRun.plan;int finishedDone=finished.completed;
        Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),DAY);Diagnosis.Run run=s.session.diagnosticRun;Diagnosis.Plan old=run.plan;Diagnosis.Probe active=run.current;assertNotSame("Restart creates a new active plan, leaving the finished run frozen",finishedPlan,old);
        // Reconstruct the v393 empty last unit with the exact same endpoint; not a claim about device bytes.
        s.diagnostics.remove(old.key);old.scope.removeIf(KoreanRoundSolids::supports);old.placements.keySet().removeIf(KoreanRoundSolids::supports);
        Diagnosis.UnitScreen empty=old.unitScreens.get("KR|2022|6|2|6");empty.members.clear();empty.representatives.clear();empty.label="초등 6학년 · 2학기 · 원기둥·원뿔·구 · 핵심 문제 미지원";
        Set<String> removed=new HashSet<>();for(var e:old.checks.entrySet())if(KoreanRoundSolids.supports(e.getValue().skillId))removed.add(e.getKey());old.checks.keySet().removeAll(removed);old.pending.removeAll(removed);old.untested.removeAll(removed);
        List<String> ids=new ArrayList<>(old.scope);Collections.sort(ids);old.key=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",ids)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;
        for(Diagnosis.UnitScreen unit:Diagnosis.screens(old))unit.planKey=old.key;s.diagnostics.put(old.key,old);
        int remaining=Diagnosis.remainingChecks(old),history=s.history.size();s.session.answers.set(0,"888");s.session.steps.add("1=2");s.session.stepKinds.add(Checker.StepKind.FULL);
        HelpPlan.Draft help=new HelpPlan.Draft();help.questionId=q.id;help.entries.add("777");s.session.conceptHelp=help;Learning.markError(s,DAY);Review.Track track=Review.track(s.progress(q.skillId));long generation=track.generation;Review.Work work=s.session.reviewWork;
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertNotSame(old,next);assertEquals(remaining+3,Diagnosis.remainingChecks(next));assertEquals(remaining,Diagnosis.remainingChecks(old));assertSame("Completed run keeps its own pre-restart plan",finishedPlan,finished.diagnosticRun.plan);assertEquals("Completed count is not rewritten",finishedDone,finished.completed);
        assertSame(q,s.session.question);assertEquals("888",s.session.answers.get(0));assertEquals(List.of("1=2"),s.session.steps);assertSame(help,s.session.conceptHelp);assertEquals(List.of("777"),help.entries);assertSame(work,s.session.reviewWork);assertSame(track,Review.track(s.progress(q.skillId)));assertEquals(generation,track.generation);assertTrue(track.pending);assertEquals(history,s.history.size());
        assertSame(next,run.plan);assertNotSame(active,run.current);assertEquals(active.skillId,run.current.skillId);assertTrue(run.current.reserved);assertSame(old,s.diagnostics.get(old.key));
        Diagnosis.UnitScreen added=next.unitScreens.get("KR|2022|6|2|6");assertEquals(KoreanRoundSolids.IDS,added.members);assertEquals(KoreanRoundSolids.IDS,added.representatives);assertTrue(added.completedRepresentatives.isEmpty());assertTrue(added.reasons.isEmpty());assertEquals(Diagnosis.UnitStatus.UNCHECKED,added.status);
        Learning.State cold=LearningSnapshot.capture(s);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals("888",cold.session.answers.get(0));assertEquals(List.of("777"),cold.session.conceptHelp.entries);Diagnosis.migrate(cold);assertEquals(remaining+3,Diagnosis.remainingChecks(Diagnosis.currentPlan(cold)));
    }
}
