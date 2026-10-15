package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanSpatialPlanUpgradeTest {
    private static final String UNIT="KR|2022|6|2|2";
    private Learning.State student(){Learning.State s=new Learning.State();s.profile.grade=6;s.profile.term=2;s.profile.curriculum=2022;s.profile.schoolYear=2026;s.profile.ready=true;s.profile.foundationAscending=true;s.profile.lastLearnedSkill="unit:KR|2022|6|2|5";return s;}
    @Test public void countFactEquivalenceCannotMoveObservedCubeProofToAnotherPublicPosition(){
        Question q=KoreanSpatialSolids.create(KoreanSpatialSolids.COUNT,new Random(39052));q.diagram=KoreanSpatialSolids.heightDiagram(2,2,List.of(1,2,0,1));String identity=q.signature();HelpPlan.Draft d=HelpPlan.forQuestion(q).restore(null,q.id);assertNotNull(KoreanSpatialLesson.restore(q,d));
        assertTrue(KoreanSpatialLesson.select(q,d,0));assertTrue(KoreanSpatialLesson.cube(q,d,0));q.diagram=KoreanSpatialSolids.heightDiagram(2,2,List.of(2,1,0,1));assertEquals(identity,q.signature());assertNull(KoreanSpatialLesson.state(q,d));assertNotNull(KoreanSpatialLesson.restore(q,d));assertEquals(-1,KoreanSpatialLesson.state(q,d).selected());assertTrue(d.spatialActions.isEmpty());
    }
    @Test public void newlySupportedEmptyUnitAddsFiveChecksWithoutRewritingTheSavedQuestionOrHelp(){
        Learning.State s=student();Random random=new Random(39051);Learning.beginDiagnostic(s,random);Question q=Learning.ensureQuestion(s,new Generator(random),LocalDate.of(2026,10,15));
        Diagnosis.Plan old=Diagnosis.currentPlan(s);s.diagnostics.remove(old.key);var oldUnit=old.unitScreens.get(UNIT);old.scope.removeIf(KoreanSpatialSolids::supports);old.placements.keySet().removeIf(KoreanSpatialSolids::supports);oldUnit.members.clear();oldUnit.representatives.clear();oldUnit.label="이전 미지원 단원";
        Set<String> removed=new HashSet<>();for(var x:old.checks.entrySet())if(KoreanSpatialSolids.supports(x.getValue().skillId))removed.add(x.getKey());old.checks.keySet().removeAll(removed);old.pending.removeAll(removed);old.untested.removeAll(removed);
        List<String> ids=new ArrayList<>(old.scope);Collections.sort(ids);old.key=Curriculum.MAPPING_VERSION+"|"+old.schoolYear+"|"+old.curriculum+"|"+old.actualGrade+"|"+old.term+"||"+String.join(",",ids)+"|foundation-unit-screen-v2|units-"+KoreanFoundationUnits.MAPPING_VERSION+"|"+old.lastLearnedSkill;
        for(var unit:old.unitScreens.values())unit.planKey=old.key;s.diagnostics.put(old.key,old);int remaining=Diagnosis.remainingChecks(old),scope=old.scope.size();long sequence=s.reviewSequence;
        s.session.answers.set(0,"123");s.session.conceptHelp=new HelpPlan.Draft();s.session.conceptHelp.questionId=q.id;s.session.conceptHelp.entries.add("888");s.session.conceptHelp.spatialPendingInput="unsubmitted display";s.session.conceptHelp.planeVisited.add(1);
        Diagnosis.migrate(s);Diagnosis.Plan current=Diagnosis.currentPlan(s);assertNotSame(old,current);assertEquals(scope+5,current.scope.size());assertEquals(remaining+5,Diagnosis.remainingChecks(current));assertEquals(remaining,Diagnosis.remainingChecks(old));assertEquals(KoreanSpatialSolids.IDS,current.unitScreens.get(UNIT).representatives);assertTrue(oldUnit.representatives.isEmpty());assertTrue(s.diagnostics.containsValue(old));
        assertSame(q,s.session.question);assertEquals(List.of("123"),s.session.answers);assertEquals(List.of("888"),s.session.conceptHelp.entries);assertEquals(List.of(1),s.session.conceptHelp.planeVisited);assertEquals("unsubmitted display",s.session.conceptHelp.spatialPendingInput);assertEquals(sequence,s.reviewSequence);assertSame(current,s.session.diagnosticRun.plan);
        Diagnosis.migrate(s);assertSame(current,Diagnosis.currentPlan(s));Learning.State cold=LearningSnapshot.capture(s);assertEquals(q.id,cold.session.question.id);assertEquals(List.of("123"),cold.session.answers);assertEquals("unsubmitted display",cold.session.conceptHelp.spatialPendingInput);assertSame(Diagnosis.currentPlan(cold),cold.session.diagnosticRun.plan);
    }
    @Test public void learnedEndpointAndExclusionsBoundTheNewScopeAndOlderAndForeignScopesStaySeparate(){
        Learning.Profile p=student().profile;p.lastLearnedSkill="unit:KR|2022|6|2|1";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(KoreanSpatialSolids::supports));
        p.lastLearnedSkill="unit:"+UNIT;var unit=KoreanFoundationUnits.learned(p).stream().filter(x->x.key().equals(UNIT)).findFirst().orElseThrow();assertEquals(KoreanSpatialSolids.IDS,unit.representatives());
        p.excluded.add(KoreanSpatialSolids.FRONT);assertFalse(KoreanFoundationUnits.scope(p).contains(KoreanSpatialSolids.FRONT));p.excluded.clear();assertTrue(KoreanFoundationUnits.scope(p).contains(KoreanSpatialSolids.FRONT));
        p.lastLearnedSkill="unit:KR|2022|6|2|5";assertTrue(KoreanFoundationUnits.missingRepresentatives(p).isEmpty());p.lastLearnedSkill="unit:KR|2022|6|2|6";assertEquals(List.of("KR|2022|6|2|6"),KoreanFoundationUnits.missingRepresentatives(p).stream().map(KoreanFoundationUnits.Unit::key).toList());
        p.curriculum=2015;p.lastLearnedSkill="unit:KR|2015|6|2|3";assertTrue(KoreanFoundationUnits.scope(p).stream().noneMatch(KoreanSpatialSolids::supports));
        p.curriculum=2022;p.countryCode="NA";p.educationSystem="na-nied-primary-2024-v1";assertTrue(KoreanFoundationUnits.available(p).isEmpty());
    }
}
