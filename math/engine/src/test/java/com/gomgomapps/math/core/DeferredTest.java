package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DeferredTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private Learning.State start(int count){Learning.State state=new Learning.State();Learning.beginPractice(state,"homework",List.of("add20"),count,true,new Random(5));Learning.ensureQuestion(state,new Generator(new Random(8)));return state;}
    private Learning.State restore(Learning.State state)throws Exception{Deferred.sync(state);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);Learning.State loaded=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();Deferred.settle(loaded);return loaded;}
    @Test public void snapshotSurvivesNextQuestionAndKeepsChoicesRolesAndInk()throws Exception{
        Learning.State state=start(2);Learning.Session s=state.session;Question q=s.question;
        s.answers.set(0,"9");s.steps.add("4+5=9");s.stepKinds.add(Checker.StepKind.PARTIAL);s.scratch.add(new float[]{.1f,.2f,0,.3f,.4f,100});s.pendingInk.add(new float[]{.2f,.4f,0});s.inkTarget=0;
        Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());s.answers.set(0,"123");s.scratch.add(new float[]{.9f,.8f,0});String nextId=s.question.id;
        state=restore(state);s=state.session;Deferred.open(s,q.id);
        assertEquals(q.id,s.question.id);assertEquals(q.choices,s.question.choices);assertEquals(q.correctChoice,s.question.correctChoice);assertEquals("9",s.answers.get(0));assertEquals(List.of("4+5=9"),s.steps);assertEquals(Checker.StepKind.PARTIAL,s.stepKind(0));assertArrayEquals(new float[]{.1f,.2f,0,.3f,.4f,100},s.scratch.get(0),0);assertEquals(1,s.pendingInk.size());assertEquals(0,s.inkTarget);
        s.answers.set(0,"8");Deferred.leave(s);assertEquals(nextId,s.question.id);assertEquals("123",s.answers.get(0));assertArrayEquals(new float[]{.9f,.8f,0},s.scratch.get(0),0);
        Deferred.open(s,q.id);assertEquals("8",s.answers.get(0));
    }
    @Test public void solvedDeferredIsCountedOnceAndNeverAsIndependent()throws Exception{
        Learning.State state=start(1);Learning.Session s=state.session;String id=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());assertTrue(s.finished);assertEquals(1,s.skipped);assertEquals(1,state.history.size());Deferred.open(s,id);
        Learning.finishQuestion(state,false,DAY.plusDays(1),new Random());Learning.finishQuestion(state,false,DAY.plusDays(1),new Random());assertEquals(1,s.completed);assertEquals(0,s.correct);assertEquals(1,s.corrected);assertEquals(0,s.skipped);assertEquals(0,Deferred.count(s));assertEquals(Integer.valueOf(1),state.daily.get(DAY.plusDays(1).toString()));
        Learning.Progress p=state.progress(s.question.skillId);assertEquals(1,p.attempts);assertEquals(0,p.firstCorrect);assertEquals(1,p.corrected);assertEquals(0,p.skipped);assertEquals(0,p.consecutive);assertEquals(1,state.history.get(0).total);assertEquals(1,state.history.get(0).corrected);assertEquals(0,state.history.get(0).skipped);
        state=restore(state);assertFalse(Deferred.active(state.session));assertTrue(state.session.finished);assertNull(Learning.ensureQuestion(state,new Generator()));Learning.finishQuestion(state,false,DAY.plusDays(1),new Random());assertEquals(1,state.session.corrected);
    }
    @Test public void repeatedDeferralKeepsEditedWorkWithoutInflatingCounts()throws Exception{
        Learning.State state=start(1);Learning.Session s=state.session;String id=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Deferred.open(s,id);s.answers.set(0,"7");Learning.markError(state);Learning.finishQuestion(state,true,DAY,new Random());
        state=restore(state);s=state.session;assertFalse(Deferred.active(s));assertEquals(1,s.completed);assertEquals(1,s.skipped);assertEquals(1,state.progress(s.question.skillId).attempts);Deferred.open(s,id);assertEquals("7",s.answers.get(0));assertTrue(s.hadError);assertEquals(1,Deferred.count(s));assertTrue(state.daily.isEmpty());
    }
    @Test public void reviewInMiddleReturnsToSameQuestionAndPreservesItsInput(){
        Learning.State state=start(3);Learning.Session s=state.session;String old=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());String current=s.question.id;s.answers.set(0,"6");Learning.markError(state);
        Deferred.open(s,old);Learning.finishQuestion(state,false,DAY,new Random());Question restored=Learning.ensureQuestion(state,new Generator());assertEquals(current,restored.id);assertEquals("6",s.answers.get(0));assertTrue(s.hadError);assertEquals(1,s.completed);assertFalse(s.finished);assertEquals(1,s.corrected);assertEquals(2,s.queue.size()+1);
    }
    @Test public void finishedRoundWithPendingWorkRemainsResumableAfterOtherStudy(){
        Learning.State state=start(1);Learning.Session original=state.session;Learning.finishQuestion(state,true,DAY,new Random());Learning.beginPractice(state,"practice",List.of("sub20"),1,false,new Random());Learning.ensureQuestion(state,new Generator());Learning.finishQuestion(state,false,DAY,new Random());assertSame(original,Deferred.resumeCandidate(state));Learning.resume(state,original.id);assertSame(original,state.session);assertEquals(1,Deferred.count(original));assertFalse(state.savedSessions.containsKey(original.id));
    }
    @Test public void switchingBetweenTwoDeferredQuestionsKeepsBothEdits(){
        Learning.State state=start(2);Learning.Session s=state.session;String first=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());String second=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Deferred.open(s,first);s.answers.set(0,"12");Deferred.open(s,second);s.answers.set(0,"13");Deferred.open(s,first);assertEquals("12",s.answers.get(0));Deferred.open(s,second);assertEquals("13",s.answers.get(0));assertEquals(2,s.skipped);assertEquals(2,s.completed);
    }
    @Test public void missingLegacyFieldsDoNotInventOldQuestions()throws Exception{
        Learning.State state=start(1);state.session.deferred=null;state.session.reviewingId=null;ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(state);Learning.State loaded=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(0,Deferred.count(loaded.session));assertFalse(Deferred.active(loaded.session));assertEquals(state.session.question.id,Learning.ensureQuestion(loaded,new Generator()).id);
    }
    @Test public void correctingOldDiagnosticQuestionDoesNotProveMastery(){
        Learning.State state=new Learning.State();state.profile.grade=2;state.profile.term=1;Random r=new Random(8);Learning.beginDiagnostic(state,r);Generator generator=new Generator(r);Learning.ensureQuestion(state,generator);String old=state.session.question.id,skill=state.session.question.skillId;
        Learning.finishQuestion(state,true,DAY,r);Learning.ensureQuestion(state,generator);Deferred.open(state.session,old);Learning.finishQuestion(state,false,DAY,r);Learning.ensureQuestion(state,generator);Diagnosis.Probe probe=state.session.diagnosticRun.plan.checks.get(skill);assertEquals(0,probe.independent);assertEquals(0,probe.errors);assertNotEquals(Diagnosis.Outcome.RECENTLY_CORRECT,probe.outcome);assertEquals(0,state.progress(skill).firstCorrect);
    }
    @Test public void reviewingDuringAnAdvanceRestoresExactlyOneNextQuestion(){
        Learning.State state=start(3);Learning.Session s=state.session;String skipped=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());String justSolved=s.question.id;Learning.finishQuestion(state,false,DAY,new Random());assertTrue(s.advancePending);Deferred.open(s,skipped);Learning.finishQuestion(state,false,DAY,new Random());Question next=Learning.ensureQuestion(state,new Generator());assertNotEquals(justSolved,next.id);assertEquals(2,s.completed);assertTrue(s.queue.isEmpty());assertSame(next,Learning.ensureQuestion(state,new Generator()));
    }
    @Test public void activeReviewSurvivesParkingAndReloadingAnotherMode()throws Exception{
        Learning.State state=start(2);String sessionId=state.session.id,skipped=state.session.question.id;Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());String current=state.session.question.id;state.session.answers.set(0,"6");Deferred.open(state.session,skipped);state.session.answers.set(0,"7");Learning.beginPractice(state,"practice",List.of("sub20"),1,false,new Random());Learning.ensureQuestion(state,new Generator());state=restore(state);Learning.resume(state,sessionId);assertTrue(Deferred.active(state.session));assertEquals("7",state.session.answers.get(0));Deferred.leave(state.session);assertEquals(current,state.session.question.id);assertEquals("6",state.session.answers.get(0));assertEquals(1,state.session.completed);
    }
    @Test public void conceptHelpDraftStaysWithItsQuestionAcrossDeferralAndReload()throws Exception{
        Learning.State state=start(2);Learning.Session s=state.session;String old=s.question.id;
        s.conceptHelp=new HelpPlan.Draft();s.conceptHelp.questionId=old;s.conceptHelp.stage=1;s.conceptHelp.entries.add("2");Review.helpUsed(state,DAY);
        Learning.finishQuestion(state,true,DAY,new Random());Learning.ensureQuestion(state,new Generator());String current=s.question.id;assertNull(s.conceptHelp);
        state=restore(state);s=state.session;Deferred.open(s,old);assertEquals(old,s.conceptHelp.questionId);assertEquals(1,s.conceptHelp.stage);assertEquals(List.of("2"),s.conceptHelp.entries);assertTrue(s.reviewWork.helpUsed);
        s.conceptHelp.entries.set(0,"3");Deferred.leave(s);assertEquals(current,s.question.id);assertNull(s.conceptHelp);
        Deferred.open(s,old);assertEquals(List.of("3"),s.conceptHelp.entries);
    }
    @Test public void assistedDeferredAnswerUsesOneExclusiveCompletionBucket(){
        Learning.State state=start(1);Learning.Session s=state.session;String old=s.question.id;Learning.finishQuestion(state,true,DAY,new Random());Deferred.open(s,old);Review.helpUsed(state,DAY);
        Learning.finishQuestion(state,false,DAY.plusDays(1),new Random());Learning.Progress p=state.progress(s.question.skillId);
        assertEquals(1,s.completed);assertEquals(0,s.correct);assertEquals(0,s.corrected);assertEquals(1,s.assisted);assertEquals(0,s.skipped);
        assertEquals(0,p.firstCorrect);assertEquals(0,p.corrected);assertEquals(1,p.assisted);assertEquals(0,p.skipped);
        assertEquals(1,state.history.get(0).assisted);assertEquals(0,state.history.get(0).corrected);
    }
}
