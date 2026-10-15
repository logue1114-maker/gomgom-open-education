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
        Learning.Session original=state.session;String originalQuestion=original.question.id;
        KoreanFoundationStudy.beginEasier(state,random);Learning.Session first=state.session;
        assertEquals(List.of("join9","join9"),first.queue);assertEquals("join9",next(state,random).skillId);assertEquals(screen.key,first.foundationUnitKey);
        KoreanFoundationStudy.beginEasier(state,random);assertEquals(List.of("count","count"),state.session.queue);assertEquals("count",next(state,random).skillId);assertEquals(screen.key,state.session.foundationUnitKey);
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(first,state.session);
        assertTrue(KoreanFoundationDetour.returnToQuestion(state,false));assertSame(original,state.session);assertEquals(originalQuestion,state.session.question.id);
        assertTrue(Review.track(state.progress("count")).recent);assertFalse(Review.track(state.progress("count")).pending);
    }
    @Test public void explicitEasierRequestStillRespectsTheLearnersExcludedScope(){
        Random random=new Random(37202);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.representatives.contains("add9")).findFirst().orElseThrow();KoreanFoundationStudy.flagged(state,screen,"add9","unable",DAY);
        Review.Track known=Review.track(state.progress("count"));known.recent=true;known.pending=false;known.cleanRun=2;
        state.profile.excluded.add("join9");Diagnosis.beginStudy(state,screen,random);assertEquals("add9",next(state,random).skillId);
        state.profile.excluded.add("count");Learning.Session parent=state.session;List<String> queue=new ArrayList<>(parent.queue);
        assertFalse(KoreanFoundationDetour.available(state));
        try{KoreanFoundationStudy.beginEasier(state,random);fail("Excluded foundations must not return");}catch(IllegalArgumentException expected){}
        assertSame(parent,state.session);assertEquals(queue,parent.queue);assertFalse(state.session.queue.contains("join9"));
    }
    @Test public void normalPracticeHelpWrongAndSkipAddOnlyTheCurrentLearnedUnitAndSurviveColdSnapshot(){
        Random random=new Random(37301);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.members.contains("split9")).findFirst().orElseThrow();assertFalse(screen.representatives.contains("split9"));
        Learning.State archivedCopy=LearningSnapshot.capture(state);Diagnosis.Plan archived=Diagnosis.currentPlan(archivedCopy);archived.key="archived:"+archived.key;for(Diagnosis.UnitScreen old:Diagnosis.screens(archived))old.planKey=archived.key;state.diagnostics.put(archived.key,archived);
        Learning.beginPractice(state,"practice",List.of("split9"),1,false,random);next(state,random);Review.helpUsed(state,DAY);Review.Track alreadyPending=Review.track(state.progress("split9"));long generation=alreadyPending.generation,eligibleAfter=alreadyPending.eligibleAfter;correct(state,random);assertEquals(generation,alreadyPending.generation);assertEquals(eligibleAfter,alreadyPending.eligibleAfter);
        Learning.beginPractice(state,"practice",List.of("sub9"),1,false,random);next(state,random);Learning.markError(state,DAY);correct(state,random);
        Learning.beginPractice(state,"practice",List.of("join9"),1,false,random);next(state,random);Learning.finishQuestion(state,true,DAY,random);
        assertEquals(Map.of("split9","help","sub9","wrong","join9","unable"),screen.reasons);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);assertTrue(Diagnosis.studyUnits(state).contains(screen));
        assertEquals(1,state.progress("split9").assisted);assertEquals(1,state.progress("sub9").corrected);assertEquals(1,state.progress("join9").skipped);assertTrue(Review.track(state.progress("join9")).failed.isEmpty());assertFalse(state.progress("sub9").weak());
        assertTrue(Diagnosis.screens(archived).stream().allMatch(u->u.reasons.isEmpty()));assertTrue(Diagnosis.screens(plan).stream().filter(u->!u.key.equals(screen.key)).allMatch(u->u.reasons.isEmpty()));
        Learning.State cold=LearningSnapshot.capture(state);Diagnosis.UnitScreen restored=Diagnosis.currentPlan(cold).unitScreens.get(screen.key);assertEquals(screen.reasons,restored.reasons);assertEquals(screen.flaggedAt,restored.flaggedAt);assertNotSame(screen,restored);assertTrue(Diagnosis.studyUnits(cold).contains(restored));
    }
    @Test public void aNewNormalPracticeGapStillNeedsTwoFreshSpacedIndependentChecksToRecover(){
        Random random=new Random(37302);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);Diagnosis.UnitScreen screen=Diagnosis.screens(plan).stream().filter(u->u.members.contains("split9")).findFirst().orElseThrow();
        Learning.beginPractice(state,"practice",List.of("split9"),1,false,random);next(state,random);Review.helpUsed(state,DAY);correct(state,random);assertEquals("help",screen.reasons.get("split9"));
        Learning.beginPractice(state,"practice",List.of("split9"),1,false,random);correct(state,random);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);assertFalse(screen.recoveredSkills.contains("split9"));
        Learning.beginPractice(state,"practice",List.of("split9"),1,false,random);next(state,random);Review.helpUsed(state,DAY);correct(state,random);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,screen.status);
        Diagnosis.beginRecheck(state,screen,random);Set<String> signatures=new HashSet<>();while(!state.session.finished){Question q=next(state,random);if(q.skillId.equals("split9"))assertTrue(signatures.add(q.signature()));correct(state,random);}
        assertEquals(2,signatures.size());assertEquals(2,screen.freshRechecks.get("split9").size());assertTrue(screen.recoveredSkills.contains("split9"));assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,screen.status);assertTrue(Diagnosis.studyUnits(state).contains(screen));assertEquals("help",screen.reasons.get("split9"));
    }
    @Test public void normalPracticeCannotAddForeignExcludedFutureOrStaleUnitGaps(){
        for(String boundary:List.of("excluded","future","foreign-system","foreign-country","inactive","secondary","wrong-version","session-old-version","wrong-members","no-current-plan")){
            Random random=new Random(37303);Learning.State state=student();Learning.beginDiagnostic(state,random);Diagnosis.Plan plan=Diagnosis.currentPlan(state);String id=boundary.equals("future")?"add20":"split9";
            Learning.beginPractice(state,"practice",List.of(id),1,false,random);next(state,random);
            switch(boundary){
                case "excluded" -> state.profile.excluded.add(id);
                case "foreign-system" -> state.session.educationSystem="na-nied-primary-2024-v1";
                case "foreign-country" -> state.session.countryCode="NA";
                case "inactive" -> state.profile.foundationAscending=false;
                case "secondary" -> state.profile.grade=7;
                case "wrong-version" -> plan.curriculum=2015;
                case "session-old-version" -> {Diagnosis.Plan origin=new Diagnosis.Plan();origin.curriculum=2015;state.session.diagnosticRun=new Diagnosis.Run(origin,false,1);}
                case "wrong-members" -> {for(Diagnosis.UnitScreen screen:Diagnosis.screens(plan))screen.members.remove(id);}
                case "no-current-plan" -> state.diagnostics.clear();
            }
            Review.helpUsed(state,DAY);correct(state,random);assertTrue(boundary,Diagnosis.screens(plan).stream().allMatch(u->u.reasons.isEmpty()));
            if(boundary.equals("no-current-plan"))assertTrue(state.diagnostics.isEmpty());
        }
    }
}
