package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class KoreanLargePlanUpgradeTest {
    private Diagnosis.Plan prior(Learning.State s){
        Diagnosis.Plan p=Diagnosis.currentPlan(s);s.diagnostics.remove(p.key);p.scope.removeAll(KoreanLargeIntegrationTest.IDS);p.placements.keySet().removeAll(KoreanLargeIntegrationTest.IDS);Diagnosis.UnitScreen unit=p.unitScreens.get(KoreanLargeIntegrationTest.UNIT);unit.members=new ArrayList<>(List.of("largePlace"));unit.representatives.clear();Set<String> removed=new HashSet<>();for(var e:p.checks.entrySet())if(KoreanLargeIntegrationTest.IDS.contains(e.getValue().skillId))removed.add(e.getKey());p.checks.keySet().removeAll(removed);p.pending.removeAll(removed);p.untested.removeAll(removed);
        List<String> sorted=new ArrayList<>(p.scope);Collections.sort(sorted);p.key=Curriculum.MAPPING_VERSION+"|"+p.schoolYear+"|"+p.curriculum+"|"+p.actualGrade+"|"+p.term+"||"+String.join(",",sorted)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+p.lastLearnedSkill;for(Diagnosis.UnitScreen u:Diagnosis.screens(p))u.planKey=p.key;s.diagnostics.put(p.key,p);return p;
    }
    @Test public void ExistingFiveDigitMemberAndEvidenceAreCopiedWithoutCompletingNewRepresentatives(){
        Learning.State s=KoreanLargeIntegrationTest.student();Random r=new Random(3782);Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),KoreanLargeIntegrationTest.DAY);Diagnosis.Plan old=prior(s);Diagnosis.Run run=s.session.diagnosticRun;Diagnosis.UnitScreen before=old.unitScreens.get(KoreanLargeIntegrationTest.UNIT);before.reasons.put("largePlace","wrong");before.flaggedAt.put("largePlace",17L);before.freshRechecks.put("largePlace",new LinkedHashSet<>(List.of("independent-old")));before.status=Diagnosis.UnitStatus.NEEDS_STUDY;s.session.answers.set(0,"123");int remaining=Diagnosis.remainingChecks(old);Set<String> oldChecks=new LinkedHashSet<>(old.checks.keySet());
        Diagnosis.migrate(s);Diagnosis.Plan next=Diagnosis.currentPlan(s);assertNotNull(next);assertNotSame(old,next);assertSame(next,run.plan);assertSame(q,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(remaining+5,Diagnosis.remainingChecks(next));assertEquals(old.scope.size()+5,next.scope.size());assertEquals(oldChecks,old.checks.keySet());assertEquals(List.of("largePlace"),before.members);assertTrue(before.representatives.isEmpty());assertEquals(remaining,Diagnosis.remainingChecks(old));
        Diagnosis.UnitScreen after=next.unitScreens.get(before.key);assertEquals(before.reasons,after.reasons);assertEquals(before.flaggedAt,after.flaggedAt);assertEquals(before.freshRechecks,after.freshRechecks);assertNotSame(before.freshRechecks.get("largePlace"),after.freshRechecks.get("largePlace"));assertEquals(5,after.representatives.size());assertTrue(after.completedRepresentatives.isEmpty());assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,after.status);Diagnosis.migrate(s);assertSame(next,Diagnosis.currentPlan(s));
        Learning.State cold=LearningSnapshot.capture(s);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);assertEquals(q.id,cold.session.question.id);assertEquals(List.of("123"),cold.session.answers);assertEquals("wrong",Diagnosis.currentPlan(cold).unitScreens.get(before.key).reasons.get("largePlace"));
    }
    @Test public void EndpointExclusionLegacyUnexpectedMembersAndUnknownEvidenceCannotSilentlyRetargetOldPlans(){
        for(String boundary:List.of("endpoint","exclusion","legacy","unknown-member","unknown-evidence","foreign","equal-archive")){
            Learning.State s=KoreanLargeIntegrationTest.student();Random r=new Random(3783);Learning.beginDiagnostic(s,r);Question q=Learning.ensureQuestion(s,new Generator(r),KoreanLargeIntegrationTest.DAY);Diagnosis.Plan old=prior(s);Diagnosis.Run run=s.session.diagnosticRun;var unit=old.unitScreens.get(KoreanLargeIntegrationTest.UNIT);
            switch(boundary){case "endpoint"->s.profile.lastLearnedSkill="unit:KR|2022|3|2|6";case "exclusion"->s.profile.excluded.add("krLargeWrite");case "legacy"->old.legacy=true;case "unknown-member"->unit.members.add("add9");case "unknown-evidence"->unit.reasons.put("unknown","wrong");case "foreign"->s.session.countryCode="NA";case "equal-archive"->s.diagnostics.put("ambiguous-archive",LearningSnapshot.copyPlan(old));}
            Diagnosis.migrate(s);assertNull(boundary,Diagnosis.currentPlan(s));assertSame(old,run.plan);assertSame(q,s.session.question);assertTrue(unit.representatives.isEmpty());
        }
    }
}
