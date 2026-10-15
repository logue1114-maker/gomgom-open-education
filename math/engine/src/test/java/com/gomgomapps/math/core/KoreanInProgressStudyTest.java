package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class KoreanInProgressStudyTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private Learning.State student(String endpoint){
        Learning.State state=new Learning.State();state.profile.grade=6;state.profile.term=2;
        state.profile.curriculum=2022;state.profile.schoolYear=2026;state.profile.ready=true;
        state.profile.foundationAscending=true;state.profile.lastLearnedSkill=endpoint;
        Learning.beginDiagnostic(state,new Random(37701));return state;
    }
    private Question practice(Learning.State state,String id,Random random){
        Learning.beginPractice(state,"practice",List.of(id),1,false,random);
        return Learning.ensureQuestion(state,new Generator(random),DAY);
    }
    private Diagnosis.UnitScreen screen(Learning.State state,String id){
        return Diagnosis.screens(Diagnosis.currentPlan(state)).stream().filter(u->u.members.contains(id)).findFirst().orElseThrow();
    }
    private void finish(Learning.State state,Random random){
        state.session.answers.clear();state.session.answers.addAll(Arrays.asList(state.session.question.answers));
        assertTrue(new Checker().check(state.session.question,state.session.steps,state.session.answers,state.session.stepKinds).correct());
        Learning.finishQuestion(state,false,DAY,random);
    }
    private Learning.State legacyWrong(){
        Learning.State state=student("unit:KR|2022|3|1|1");practice(state,"add1000",new Random(37702));Learning.markError(state,DAY);
        Diagnosis.UnitScreen unit=screen(state,"add1000");unit.reasons.clear();unit.flaggedAt.clear();unit.status=Diagnosis.UnitStatus.UNCHECKED;
        Review.Work work=state.session.reviewWork;work.foundationPlanKey=null;work.foundationUnitKey=null;work.foundationReason=null;
        return state;
    }
    private Learning.State serialized(Learning.State state)throws Exception{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(state);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Learning.State)in.readObject();}
    }

    @Test public void unfinishedRealWrongImmediatelyAppearsWithoutSubmissionOrInventedRequest(){
        Random random=new Random(37703);Learning.State state=student("unit:KR|2022|3|1|1");
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);Learning.State archivedCopy=LearningSnapshot.capture(state);
        Diagnosis.Plan archived=Diagnosis.currentPlan(archivedCopy);archived.key="archive:"+archived.key;
        for(Diagnosis.UnitScreen unit:Diagnosis.screens(archived))unit.planKey=archived.key;
        state.diagnostics.put(archived.key,archived);
        Question question=practice(state,"add1000",random);int history=state.history.size(),saved=state.savedSessions.size();long sequence=state.reviewSequence;
        assertFalse(new Checker().check(question,List.of(),List.of("-1")).correct());Learning.markError(state,DAY);
        Diagnosis.UnitScreen unit=screen(state,"add1000");Review.Track track=Review.track(state.progress("add1000"));
        assertEquals("wrong",unit.reasons.get("add1000"));assertTrue(Diagnosis.studyUnits(state).contains(unit));
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertTrue(track.pending);assertEquals(question.id,track.sourceId);
        assertEquals(sequence,state.reviewSequence);assertEquals(0,state.progress("add1000").attempts);assertTrue(state.daily.isEmpty());
        assertEquals(history,state.history.size());assertEquals(saved,state.savedSessions.size());assertEquals(0,state.session.completed);
        assertEquals(1,track.events.size());assertEquals(Review.Outcome.FIRST_WRONG,track.events.get(0).outcome);
        assertTrue(Diagnosis.screens(archived).stream().allMatch(u->u.reasons.isEmpty()));
        assertTrue(Diagnosis.screens(plan).stream().filter(u->u!=unit).allMatch(u->u.reasons.isEmpty()));
    }

    @Test public void helpThenFirstWrongUpgradesOnceAndFinishDoesNotResetTheSameGap(){
        Random random=new Random(37704);Learning.State state=student("unit:KR|2022|1|1|3");practice(state,"split9",random);
        Review.helpUsed(state,DAY);Diagnosis.UnitScreen unit=screen(state,"split9");Review.Track track=Review.track(state.progress("split9"));
        assertEquals("help",unit.reasons.get("split9"));assertTrue(track.failed.isEmpty());long generation=track.generation;
        unit.freshRechecks.put("split9",new LinkedHashSet<>(List.of("prior-check")));unit.recoveredSkills.add("split9");
        Review.helpUsed(state,DAY);Review.open(state,DAY);Review.migrate(state);
        assertEquals(generation,track.generation);assertEquals(Set.of("prior-check"),unit.freshRechecks.get("split9"));
        Learning.markError(state,DAY);assertEquals(generation+1,track.generation);assertEquals("wrong",unit.reasons.get("split9"));
        assertFalse(unit.freshRechecks.containsKey("split9"));assertFalse(unit.recoveredSkills.contains("split9"));
        unit.freshRechecks.put("split9",new LinkedHashSet<>(List.of("keep-on-repeat")));
        long flagged=unit.flaggedAt.get("split9"),eligible=track.eligibleAfter,due=track.dueDay;
        Learning.markError(state,DAY);Review.helpUsed(state,DAY);Review.open(state,DAY);finish(state,random);
        assertEquals("wrong",unit.reasons.get("split9"));assertEquals(generation+1,track.generation);
        assertEquals(flagged,(long)unit.flaggedAt.get("split9"));assertEquals(eligible,track.eligibleAfter);assertEquals(due,track.dueDay);
        assertEquals(Set.of("keep-on-repeat"),unit.freshRechecks.get("split9"));assertEquals(1,state.progress("split9").assisted);
        assertEquals(0,track.events.stream().filter(e->e.outcome==Review.Outcome.STUDY_REQUESTED).count());
        // The inverse order keeps the actual mathematical-error reason when help is opened later.
        practice(state,"sub9",random);Learning.markError(state,DAY);Review.helpUsed(state,DAY);
        assertEquals("wrong",unit.reasons.get("sub9"));
    }

    @Test public void currentConfirmedLegacyWorkRestoresOnMigrationAndOpenWithoutReviewSideEffects()throws Exception{
        Learning.State state=serialized(legacyWrong());Review.Track track=Review.track(state.progress("add1000"));
        int events=track.events.size(),history=state.history.size(),saved=state.savedSessions.size();long generation=track.generation,eligible=track.eligibleAfter,sequence=state.reviewSequence;
        Review.migrate(state);Diagnosis.UnitScreen unit=screen(state,"add1000");
        assertEquals("wrong",unit.reasons.get("add1000"));assertEquals(unit.planKey,state.session.reviewWork.foundationPlanKey);
        assertEquals(unit.key,state.session.reviewWork.foundationUnitKey);assertEquals("wrong",state.session.reviewWork.foundationReason);
        Learning.State snapshot=LearningSnapshot.capture(state);Learning.State cold=serialized(snapshot);Review.open(cold,DAY);Review.migrate(cold);
        assertEquals(unit.reasons,screen(cold,"add1000").reasons);assertEquals(unit.flaggedAt,screen(cold,"add1000").flaggedAt);
        Review.Track restored=Review.track(cold.progress("add1000"));assertEquals(events,restored.events.size());assertEquals(generation,restored.generation);
        assertEquals(eligible,restored.eligibleAfter);assertEquals(sequence,cold.reviewSequence);assertEquals(history,cold.history.size());assertEquals(saved,cold.savedSessions.size());
        assertEquals(0,cold.progress("add1000").attempts);assertTrue(cold.daily.isEmpty());
        // A matching stamp is not a substitute for an actual persisted unit reason.
        screen(cold,"add1000").reasons.clear();Review.open(cold,DAY);assertEquals("wrong",screen(cold,"add1000").reasons.get("add1000"));
        Learning.State helped=student("unit:KR|2022|1|1|3");practice(helped,"split9",new Random(37707));Review.helpUsed(helped,DAY);
        Diagnosis.UnitScreen helpUnit=screen(helped,"split9");helpUnit.reasons.clear();helpUnit.flaggedAt.clear();
        helped.session.reviewWork.foundationPlanKey=null;helped.session.reviewWork.foundationUnitKey=null;helped.session.reviewWork.foundationReason=null;
        Learning.State restoredHelp=serialized(helped);Review.open(restoredHelp,DAY);
        assertEquals("help",screen(restoredHelp,"split9").reasons.get("split9"));
        assertEquals(1,Review.track(restoredHelp.progress("split9")).events.size());assertEquals(0,restoredHelp.progress("split9").attempts);
    }

    @Test public void restorationNeverGuessesHistoricalRecoveredDeferredOrDifferentScopeEvidence(){
        for(String boundary:List.of("recovered","newer-source","wrong-work","finished-work","finished-session","advance","deferred","had-error-only","foreign","excluded","scope-change","stamped-plan","stamped-unit","diagnostic","saved-only")){
            Learning.State state=legacyWrong();Diagnosis.Plan original=Diagnosis.currentPlan(state);Review.Track track=Review.track(state.progress("add1000"));
            switch(boundary){
                case "recovered" -> {track.pending=false;track.recent=true;}
                case "newer-source" -> track.sourceId="different-published-question";
                case "wrong-work" -> state.session.reviewWork.questionId="different-published-question";
                case "finished-work" -> state.session.reviewWork.finished=true;
                case "finished-session" -> state.session.finished=true;
                case "advance" -> state.session.advancePending=true;
                case "deferred" -> state.session.reviewingId="deferred-question";
                case "had-error-only" -> {state.session.reviewWork.errorRecorded=false;state.session.hadError=true;}
                case "foreign" -> state.session.educationSystem="na-nied-primary-2024-v1";
                case "excluded" -> state.profile.excluded.add("add1000");
                case "scope-change" -> state.profile.lastLearnedSkill="unit:KR|2022|1|1|3";
                case "stamped-plan" -> state.session.reviewWork.foundationPlanKey="old-plan-key";
                case "stamped-unit" -> state.session.reviewWork.foundationUnitKey="old-unit-key";
                case "diagnostic" -> state.session.mode="diagnostic";
                case "saved-only" -> {state.savedSessions.put(state.session.id,state.session);state.session=null;}
            }
            int events=track.events.size();long generation=track.generation;Review.migrate(state);Review.open(state,DAY);
            assertTrue(boundary,Diagnosis.screens(original).stream().allMatch(u->u.reasons.isEmpty()));
            assertEquals(boundary,events,track.events.size());assertEquals(boundary,generation,track.generation);
        }
    }

    @Test public void liveProjectionStillRejectsUnlearnedExcludedForeignDiagnosticAndUnlinkedPlans(){
        for(String boundary:List.of("future","excluded","foreign","wrong-version","diagnostic-version","probing","no-plan","members")){
            Learning.State state=student("unit:KR|2022|1|1|3");Diagnosis.Plan original=Diagnosis.currentPlan(state);
            String id=boundary.equals("future")?"add1000":"split9";practice(state,id,new Random(37705));
            switch(boundary){
                case "excluded" -> state.profile.excluded.add(id);
                case "foreign" -> state.session.countryCode="NA";
                case "wrong-version" -> original.curriculum=2015;
                case "diagnostic-version" -> {Diagnosis.Plan origin=new Diagnosis.Plan();origin.curriculum=2015;state.session.diagnosticRun=new Diagnosis.Run(origin,false,1);}
                case "probing" -> {state.session.diagnosticRun=new Diagnosis.Run(original,false,1);state.session.diagnosticRun.current=new Diagnosis.Probe(id,1,false);}
                case "no-plan" -> state.diagnostics.clear();
                case "members" -> Diagnosis.screens(original).forEach(unit->unit.members.remove(id));
            }
            Review.helpUsed(state,DAY);Learning.markError(state,DAY);
            assertTrue(boundary,Diagnosis.screens(original).stream().allMatch(u->u.reasons.isEmpty()));
        }
    }

    @Test public void changingTheLearnedEndpointDoesNotRetargetAnAlreadyStampedUnfinishedGap(){
        Random random=new Random(37708);Learning.State state=student("unit:KR|2022|3|1|1");practice(state,"add1000",random);Learning.markError(state,DAY);
        Diagnosis.UnitScreen original=screen(state,"add1000");String originalKey=original.planKey;
        Learning.State wider=student("unit:KR|2022|3|1|2");Diagnosis.Plan nextPlan=Diagnosis.currentPlan(wider);
        state.profile.lastLearnedSkill=wider.profile.lastLearnedSkill;state.diagnostics.put(nextPlan.key,nextPlan);
        assertNotEquals(originalKey,nextPlan.key);Review.open(state,DAY);Review.migrate(state);finish(state,random);
        assertEquals(originalKey,state.session.reviewWork.foundationPlanKey);assertEquals("wrong",original.reasons.get("add1000"));
        assertTrue(Diagnosis.screens(nextPlan).stream().allMatch(unit->unit.reasons.isEmpty()));
        assertEquals(0,Review.track(state.progress("add1000")).events.stream().filter(event->event.outcome==Review.Outcome.STUDY_REQUESTED).count());
    }

    @Test public void stampedFinishCannotResetCopiedArchiveOrExistingWiderScopeEvidence(){
        for(boolean widerScope:List.of(false,true)){
            Random random=new Random(37709);Learning.State state=student("unit:KR|2022|3|1|1");practice(state,"add1000",random);Learning.markError(state,DAY);
            Diagnosis.Plan other;
            if(widerScope){Learning.State wider=student("unit:KR|2022|3|1|2");other=Diagnosis.currentPlan(wider);state.profile.lastLearnedSkill=wider.profile.lastLearnedSkill;}
            else {other=Diagnosis.currentPlan(LearningSnapshot.capture(state));other.key="archive:"+other.key;for(Diagnosis.UnitScreen unit:Diagnosis.screens(other))unit.planKey=other.key;}
            state.diagnostics.put(other.key,other);
            Diagnosis.UnitScreen kept=Diagnosis.screens(other).stream().filter(u->u.members.contains("add1000")).findFirst().orElseThrow();
            kept.reasons.put("add1000","wrong");kept.flaggedAt.put("add1000",state.reviewSequence);kept.freshRechecks.put("add1000",new LinkedHashSet<>(List.of("preserved-independent-evidence")));kept.recoveredSkills.add("add1000");kept.status=Diagnosis.UnitStatus.RECENTLY_CHECKED;
            long flaggedAt=kept.flaggedAt.get("add1000");finish(state,random);
            assertEquals(flaggedAt,(long)kept.flaggedAt.get("add1000"));assertEquals(Set.of("preserved-independent-evidence"),kept.freshRechecks.get("add1000"));assertTrue(kept.recoveredSkills.contains("add1000"));assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,kept.status);
            assertEquals(0,Review.track(state.progress("add1000")).events.stream().filter(event->event.outcome==Review.Outcome.STUDY_REQUESTED).count());
        }
    }

    @Test public void immediateHelpRemainsActionableUntilTwoFreshSpacedIndependentChecks(){
        Random random=new Random(37706);Learning.State state=student("unit:KR|2022|1|1|3");practice(state,"split9",random);Review.helpUsed(state,DAY);
        Diagnosis.UnitScreen unit=screen(state,"split9");assertTrue(Diagnosis.studyUnits(state).contains(unit));finish(state,random);
        assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);assertTrue(unit.recoveredSkills.isEmpty());
        practice(state,"split9",random);finish(state,random);assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,unit.status);
        Diagnosis.beginRecheck(state,unit,random);Set<String> signatures=new LinkedHashSet<>();
        while(!state.session.finished){
            Question question=Learning.ensureQuestion(state,new Generator(random),DAY);
            if(question.skillId.equals("split9"))assertTrue(signatures.add(question.signature()));finish(state,random);
        }
        assertEquals(2,signatures.size());assertEquals(2,unit.freshRechecks.get("split9").size());
        assertEquals(Diagnosis.UnitStatus.RECENTLY_CHECKED,unit.status);assertEquals("help",unit.reasons.get("split9"));
        assertTrue(Diagnosis.studyUnits(state).contains(unit));assertTrue(Review.track(state.progress("split9")).dueDay>DAY.toEpochDay());
    }
}
