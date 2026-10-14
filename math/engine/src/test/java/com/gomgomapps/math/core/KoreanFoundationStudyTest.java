package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanFoundationStudyTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Learning.State student(){
        Learning.State state=new Learning.State();state.profile.grade=6;state.profile.term=2;
        state.profile.curriculum=2022;state.profile.schoolYear=2026;state.profile.ready=true;state.profile.foundationAscending=true;
        state.profile.lastLearnedSkill="unit:KR|2022|1|1|3";return state;
    }
    private Diagnosis.UnitScreen unable(Learning.State state,Random random){
        Learning.beginDiagnostic(state,random);Question q=Learning.ensureQuestion(state,new Generator(random),DAY);
        Learning.finishQuestion(state,true,DAY,random);
        return Diagnosis.screens(state.session.diagnosticRun.plan).stream().filter(u->u.reasons.containsKey(q.skillId)).findFirst().orElseThrow();
    }
    private Question next(Learning.State state,Random random){return Learning.ensureQuestion(state,new Generator(random),DAY);}
    private void correct(Learning.State state,Random random){
        Question q=next(state,random);state.session.answers.clear();state.session.answers.addAll(Arrays.asList(q.answers));
        assertTrue(new Checker().check(q,state.session.steps,state.session.answers,state.session.stepKinds).correct());
        Learning.finishQuestion(state,false,DAY,random);
    }
    @Test public void unablePersistsWithoutInventedMathErrorsAndColdSnapshotKeepsTheRequest(){
        Random random=new Random(37051);Learning.State state=student();Diagnosis.UnitScreen screen=unable(state,random);
        String id=screen.reasons.keySet().iterator().next();Review.Track track=Review.track(state.progress(id));
        assertTrue(track.pending);assertTrue(track.failed.isEmpty());assertFalse(track.practice);assertFalse(state.progress(id).weak());
        assertEquals(0,state.session.deferred.size());assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
        assertTrue(Diagnosis.studyUnits(state).stream().anyMatch(u->u.key.equals(screen.key)));
        Learning.State cold=LearningSnapshot.capture(state);Diagnosis.UnitScreen restored=Diagnosis.screens(cold.diagnostics.get(screen.planKey)).stream().filter(u->u.key.equals(screen.key)).findFirst().orElseThrow();
        assertEquals(screen.reasons,restored.reasons);assertEquals(screen.flaggedAt,restored.flaggedAt);assertNotSame(screen,restored);
        assertSame(cold.diagnostics.get(screen.planKey),cold.session.diagnosticRun.plan);
        screen.freshRechecks.put(id,new LinkedHashSet<>(List.of("published-question")));
        Learning.State detached=LearningSnapshot.capture(state);Diagnosis.UnitScreen detachedUnit=detached.diagnostics.get(screen.planKey).unitScreens.get(screen.key);
        detachedUnit.freshRechecks.get(id).add("later-question");detachedUnit.recoveredSkills.add(id);detachedUnit.reasons.put(id,"help");
        assertEquals(Set.of("published-question"),screen.freshRechecks.get(id));assertTrue(screen.recoveredSkills.isEmpty());assertEquals("unable",screen.reasons.get(id));
    }
    @Test public void oneCorrectAndHelpedCorrectDoNotRemoveTheStudyRequest(){
        Random random=new Random(37052);Learning.State state=student();Diagnosis.UnitScreen screen=unable(state,random);
        Diagnosis.beginRecheck(state,screen,random);correct(state,random);
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
        Question q=next(state,random);Review.helpUsed(state,DAY);correct(state,random);
        assertEquals(1,state.progress(q.skillId).assisted);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
        assertTrue(screen.recoveredSkills.isEmpty());
    }
    @Test public void actualSpacedFreshChecksRecoverButKeepTheUnitAndItsFutureReviewVisible(){
        Random random=new Random(37053);Learning.State state=student();Diagnosis.UnitScreen screen=unable(state,random);
        String id=screen.reasons.keySet().iterator().next();Diagnosis.beginRecheck(state,screen,random);
        Set<String> signatures=new HashSet<>();while(!state.session.finished){Question q=next(state,random);assertTrue(signatures.add(q.signature()));correct(state,random);}
        assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,screen.status);assertTrue(screen.recoveredSkills.contains(id));
        assertEquals(2,screen.freshRechecks.get(id).size());assertTrue(Review.track(state.progress(id)).dueDay>DAY.toEpochDay());
        assertTrue(Diagnosis.studyUnits(state).stream().anyMatch(u->u.key.equals(screen.key)));
    }
    @Test public void prescribedFoundationPathReturnsToItsAnchorAndFurtherDifficultyIsRecorded(){
        Random random=new Random(37054);Learning.State state=student();Learning.beginDiagnostic(state,random);
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();
        KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);Diagnosis.beginStudy(state,screen,random);
        List<String> queue=new ArrayList<>(state.session.queue);assertEquals("add9",queue.get(2));assertEquals("add9",queue.get(5));
        assertNotEquals("add9",queue.get(0));Question lower=next(state,random);assertEquals(queue.get(0),lower.skillId);
        Learning.markError(state,DAY);Learning.finishQuestion(state,true,DAY,random);
        assertEquals("wrong",screen.reasons.get(lower.skillId));assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
        assertEquals(queue.get(1),next(state,random).skillId);
    }
    @Test public void foreignPracticeCannotClearTheKoreanUnitEvidence(){
        Random random=new Random(37055);Learning.State state=student();Diagnosis.UnitScreen screen=unable(state,random);
        Diagnosis.beginRecheck(state,screen,random);state.session.educationSystem="na-nied-primary-2024-v1";
        while(!state.session.finished)correct(state,random);
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);assertTrue(screen.freshRechecks.isEmpty());assertTrue(screen.recoveredSkills.isEmpty());
    }
    @Test public void changedLearnedScopeCannotForceAnExcludedPrerequisiteIntoStudy(){
        Random random=new Random(37056);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);
        Diagnosis.beginStudy(state,screen,random);String priorWarm=state.session.queue.get(0);assertNotEquals("add9",priorWarm);state.profile.excluded.add(priorWarm);
        Diagnosis.beginStudy(state,screen,random);assertFalse(state.session.queue.contains(priorWarm));assertEquals("add9",state.session.queue.get(2));assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
    }
    @Test public void learnerCanReturnToRecentlyConfirmedFoundationsWithoutLosingTheOriginalTarget(){
        Random random=new Random(37201);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);
        for(String id:List.of("count","join9")){Review.Track track=Review.track(state.progress(id));track.recent=true;track.pending=false;track.cleanRun=2;}
        Diagnosis.beginStudy(state,screen,random);assertEquals("add9",next(state,random).skillId);
        KoreanFoundationStudy.beginEasier(state,random);assertEquals("add9",state.session.queue.get(2));assertEquals("join9",next(state,random).skillId);assertEquals(screen.key,state.session.foundationUnitKey);
        KoreanFoundationStudy.beginEasier(state,random);assertEquals("add9",state.session.queue.get(2));assertEquals("count",next(state,random).skillId);assertEquals(screen.key,state.session.foundationUnitKey);
        assertTrue(Review.track(state.progress("count")).recent);assertFalse(Review.track(state.progress("count")).pending);
    }
    @Test public void explicitEasierRequestStillRespectsTheLearnersExcludedScope(){
        Random random=new Random(37202);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);
        Review.Track known=Review.track(state.progress("count"));known.recent=true;known.pending=false;known.cleanRun=2;
        state.profile.excluded.add("join9");Diagnosis.beginStudy(state,screen,random);assertEquals("add9",next(state,random).skillId);
        state.profile.excluded.add("count");
        try{KoreanFoundationStudy.beginEasier(state,random);fail("Excluded foundations must not return");}catch(IllegalArgumentException expected){}
        assertFalse(state.session.queue.contains("join9"));
    }
}
