package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

/** In-memory reconstruction of the prior empty unit, not an actual-device-save claim. */
public class KoreanCentimeterPlanUpgradeTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.schoolYear=2026;s.profile.curriculum=2022;s.profile.foundationAscending=true;s.profile.ready=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    private Diagnosis.Plan prior(Learning.State s){
        Diagnosis.Plan p=Diagnosis.currentPlan(s);s.diagnostics.remove(p.key);p.scope.removeIf(KoreanCentimeterMeasurement::supports);p.placements.keySet().removeIf(KoreanCentimeterMeasurement::supports);
        Diagnosis.UnitScreen unit=p.unitScreens.get("KR|2022|2|1|4");unit.members.clear();unit.representatives.clear();unit.label="초등 2학년 · 1학기 · 길이 재기 · 핵심 문제 미지원";
        Set<String> remove=new HashSet<>();for(var entry:p.checks.entrySet())if(KoreanCentimeterMeasurement.supports(entry.getValue().skillId))remove.add(entry.getKey());p.checks.keySet().removeAll(remove);p.pending.removeAll(remove);p.untested.removeAll(remove);
        List<String> sorted=new ArrayList<>(p.scope);Collections.sort(sorted);p.key=Curriculum.MAPPING_VERSION+"|"+p.schoolYear+"|"+p.curriculum+"|"+p.actualGrade+"|"+p.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+p.lastLearnedSkill;
        for(Diagnosis.UnitScreen screen:Diagnosis.screens(p))screen.planKey=p.key;s.diagnostics.put(p.key,p);return p;
    }

    @Test public void EmptyLengthUnitAddsThreePendingCoresWithoutChangingAnActiveQuestionAnswersOrHelp(){
        Learning.State s=student();Random random=new Random(3753);Learning.beginDiagnostic(s,random);Question q=Learning.ensureQuestion(s,new Generator(random),DAY);Diagnosis.Plan old=prior(s);
        int remaining=Diagnosis.remainingChecks(old),pending=old.pending.size();Set<String> oldKeys=new LinkedHashSet<>(old.checks.keySet());Diagnosis.Run run=s.session.diagnosticRun;Diagnosis.Probe probe=run.current;List<String> scheduled=run.scheduled.stream().map(p->p.unitKey+"|"+p.skillId).toList();
        s.session.answers.set(0,"123");s.session.steps.add("2+2=4");HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=q.id;draft.entries.add("4");draft.foundationCounted.add(1);s.session.conceptHelp=draft;
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertNotSame(old,next);assertSame(next,run.plan);assertEquals(remaining+3,Diagnosis.remainingChecks(next));assertEquals(pending+3,next.pending.size());assertEquals(oldKeys,old.checks.keySet());assertEquals(remaining,Diagnosis.remainingChecks(old));assertEquals(pending,old.pending.size());assertTrue(old.unitScreens.get("KR|2022|2|1|4").members.isEmpty());
        assertSame(q,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(List.of("2+2=4"),s.session.steps);assertSame(draft,s.session.conceptHelp);assertEquals(List.of(1),draft.foundationCounted);assertNotSame(probe,run.current);assertEquals(probe.skillId,run.current.skillId);assertEquals(probe.reviewGrade,run.current.reviewGrade);assertTrue(run.current.reserved);assertEquals(scheduled,run.scheduled.stream().map(p->p.unitKey+"|"+p.skillId).toList());
        Diagnosis.UnitScreen added=next.unitScreens.get("KR|2022|2|1|4");assertEquals(3,added.representatives.size());for(String id:added.representatives){assertTrue(KoreanCentimeterMeasurement.supports(id));assertTrue(next.pending.contains(added.key+"|"+id));}assertEquals(Diagnosis.UnitStatus.UNCHECKED,added.status);assertTrue(added.reasons.isEmpty());assertTrue(added.completedRepresentatives.isEmpty());
        Diagnosis.migrate(s);assertSame(next,Diagnosis.currentPlan(s));assertEquals(2,s.diagnostics.size());Learning.State cold=LearningSnapshot.capture(s);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(List.of("123"),cold.session.answers);assertEquals(List.of(1),cold.session.conceptHelp.foundationCounted);cold.session.conceptHelp.foundationCounted.clear();assertEquals(List.of(1),draft.foundationCounted);assertNotSame(q,cold.session.question);
    }

    @Test public void FinishedRoundsAndExistingStudyReasonsStayOnTheirOriginalPlanWhileNewChecksRemainUntested(){
        Learning.State s=student();Random random=new Random(3754);Learning.beginDiagnostic(s,random);while(!s.session.finished){Learning.ensureQuestion(s,new Generator(random),DAY);Learning.finishQuestion(s,true,DAY,random);}Learning.Session finished=s.session;Diagnosis.Plan old=prior(s);
        Diagnosis.UnitScreen flagged=old.unitScreens.values().stream().filter(u->!u.reasons.isEmpty()).findFirst().orElseThrow();String id=flagged.reasons.keySet().iterator().next();flagged.freshRechecks.put(id,new LinkedHashSet<>(List.of("prior-fresh-question")));flagged.recoveredSkills.add(id);
        int oldRemaining=Diagnosis.remainingChecks(old);Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertSame(old,finished.diagnosticRun.plan);assertEquals(oldRemaining+3,Diagnosis.remainingChecks(next));assertEquals(oldRemaining,Diagnosis.remainingChecks(old));Diagnosis.UnitScreen copied=next.unitScreens.get(flagged.key);assertEquals(flagged.reasons,copied.reasons);assertEquals(flagged.flaggedAt,copied.flaggedAt);assertEquals(flagged.freshRechecks,copied.freshRechecks);assertEquals(flagged.recoveredSkills,copied.recoveredSkills);assertNotSame(flagged.freshRechecks.get(id),copied.freshRechecks.get(id));assertTrue(Diagnosis.studyUnits(s).stream().anyMatch(u->u.key.equals(flagged.key)));
        Diagnosis.UnitScreen added=next.unitScreens.get("KR|2022|2|1|4");assertEquals(Diagnosis.UnitStatus.UNCHECKED,added.status);assertTrue(added.recoveredSkills.isEmpty());assertTrue(added.flaggedAt.isEmpty());assertTrue(added.freshRechecks.isEmpty());assertTrue(next.scope.contains("krNoCarryAdd"));assertTrue(next.scope.contains("krNoBorrowSub"));
        Learning.State cold=LearningSnapshot.capture(s);assertNotSame(next,Diagnosis.currentPlan(cold));assertNotSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(old.key,cold.session.diagnosticRun.plan.key);
    }

    @Test public void ChangedScopeVersionsEvidenceAndBrokenRunsCannotBeReinterpretedAsAnEmptyUnitUpgrade(){
        for(String boundary:List.of("excluded","endpoint","curriculum","foreign","duplicate","bad-probe","prior-evidence")){
            Learning.State s=student();Random random=new Random(3755);Learning.beginDiagnostic(s,random);Learning.ensureQuestion(s,new Generator(random),DAY);Diagnosis.Plan old=prior(s);Diagnosis.Run run=s.session.diagnosticRun;Diagnosis.Probe current=run.current;
            switch(boundary){case "excluded"->s.profile.excluded.add("add9");case "endpoint"->s.profile.lastLearnedSkill="unit:KR|2022|2|1|3";case "curriculum"->{s.profile.curriculum=2015;s.profile.lastLearnedSkill="unit:KR|2015|6|2|5";}case "foreign"->s.session.countryCode="NA";case "duplicate"->s.diagnostics.put("alias",old);case "bad-probe"->current.unitKey="missing-unit";case "prior-evidence"->old.unitScreens.get("KR|2022|2|1|4").reasons.put("old-content","unable");}
            Diagnosis.migrate(s);assertNull(boundary,Diagnosis.currentPlan(s));assertSame(old,run.plan);assertSame(current,run.current);assertSame(old,s.diagnostics.get(old.key));assertTrue(old.unitScreens.get("KR|2022|2|1|4").members.isEmpty());
        }
    }

    @Test public void ArchivedAncestorAndLatestCompatiblePlanUpgradeFromTheUniqueStrictSuperset(){
        Learning.State s=student();Random random=new Random(3756);Learning.beginDiagnostic(s,random);
        Question helped=Learning.ensureQuestion(s,new Generator(random),DAY);Review.helpUsed(s,DAY);Learning.finishQuestion(s,false,DAY,random);
        Question current=Learning.ensureQuestion(s,new Generator(random),DAY);Diagnosis.Plan latest=prior(s);Diagnosis.Run run=s.session.diagnosticRun;
        Diagnosis.UnitScreen latestEvidence=latest.unitScreens.values().stream().filter(u->u.reasons.containsKey(helped.skillId)).findFirst().orElseThrow();assertEquals("help",latestEvidence.reasons.get(helped.skillId));
        // Reconstruct the preserved pre-v374 ancestor by removing only that increment's two cores.
        Diagnosis.Plan ancestor=LearningSnapshot.copyPlan(latest);ancestor.scope.removeIf(KoreanFirstTermArithmetic::supports);ancestor.placements.keySet().removeIf(KoreanFirstTermArithmetic::supports);
        Diagnosis.UnitScreen arithmetic=ancestor.unitScreens.get("KR|2022|1|2|6");arithmetic.members.clear();arithmetic.representatives.clear();arithmetic.label="초등 1학년 · 2학기 · 덧셈과 뺄셈(3) · 핵심 문제 미지원";
        Set<String> removed=new HashSet<>();for(var entry:ancestor.checks.entrySet())if(KoreanFirstTermArithmetic.supports(entry.getValue().skillId))removed.add(entry.getKey());ancestor.checks.keySet().removeAll(removed);ancestor.pending.removeAll(removed);ancestor.untested.removeAll(removed);
        List<String> sorted=new ArrayList<>(ancestor.scope);Collections.sort(sorted);ancestor.key=Curriculum.MAPPING_VERSION+"|"+ancestor.schoolYear+"|"+ancestor.curriculum+"|"+ancestor.actualGrade+"|"+ancestor.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+ancestor.lastLearnedSkill;for(Diagnosis.UnitScreen u:Diagnosis.screens(ancestor))u.planKey=ancestor.key;
        // Distinct archived evidence makes copying the older record observable as well as a bad run binding.
        ancestor.unitScreens.get(latestEvidence.key).reasons.put(helped.skillId,"unable");
        s.diagnostics.clear();s.diagnostics.put(ancestor.key,ancestor);s.diagnostics.put(latest.key,latest);
        assertEquals(ancestor.scope.size()+2,latest.scope.size());int remaining=Diagnosis.remainingChecks(latest),pending=latest.pending.size(),ancestorRemaining=Diagnosis.remainingChecks(ancestor);Set<String> ancestorChecks=new LinkedHashSet<>(ancestor.checks.keySet());
        s.session.answers.set(0,"123");HelpPlan.Draft draft=new HelpPlan.Draft();draft.questionId=current.id;draft.entries.add("1");s.session.conceptHelp=draft;
        Learning.Session archivedRun=new Learning.Session();archivedRun.mode="diagnostic";archivedRun.countryCode="KR";archivedRun.educationSystem="kr-national";archivedRun.diagnosticRun=new Diagnosis.Run(ancestor,true,Diagnosis.ROUND_LIMIT);archivedRun.diagnosticRun.current=ancestor.checks.get(run.current.unitKey+"|"+run.current.skillId);s.savedSessions.put(archivedRun.id,archivedRun);
        Diagnosis.migrate(s);assertNull(Diagnosis.currentPlan(s));assertSame(latest,run.plan);assertSame(ancestor,archivedRun.diagnosticRun.plan);assertSame(current,s.session.question);assertEquals(2,s.diagnostics.size());assertEquals(remaining,Diagnosis.remainingChecks(latest));
        // A completed archived run keeps its old meaning and no longer blocks the latest active round.
        archivedRun.finished=true;
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertEquals(remaining+3,Diagnosis.remainingChecks(next));assertEquals(pending+3,next.pending.size());assertEquals(latest.scope.size()+3,next.scope.size());assertSame(next,run.plan);assertSame(current,s.session.question);assertEquals(List.of("123"),s.session.answers);assertSame(draft,s.session.conceptHelp);
        assertEquals(latestEvidence.reasons,next.unitScreens.get(latestEvidence.key).reasons);assertEquals(latestEvidence.flaggedAt,next.unitScreens.get(latestEvidence.key).flaggedAt);assertEquals("help",next.unitScreens.get(latestEvidence.key).reasons.get(helped.skillId));
        assertSame(ancestor,s.diagnostics.get(ancestor.key));assertSame(latest,s.diagnostics.get(latest.key));assertSame(ancestor,archivedRun.diagnosticRun.plan);assertEquals(ancestorRemaining,Diagnosis.remainingChecks(ancestor));assertEquals(ancestorChecks,ancestor.checks.keySet());assertEquals("unable",ancestor.unitScreens.get(latestEvidence.key).reasons.get(helped.skillId));assertEquals(remaining,Diagnosis.remainingChecks(latest));assertEquals(3,s.diagnostics.size());
        Diagnosis.migrate(s);assertSame(next,Diagnosis.currentPlan(s));assertEquals(3,s.diagnostics.size());Learning.State cold=LearningSnapshot.capture(s);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(List.of("123"),cold.session.answers);
    }
}
