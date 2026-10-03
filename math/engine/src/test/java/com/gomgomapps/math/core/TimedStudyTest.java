package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimedStudyTest {
    private final Random random=new Random(124);
    private final Generator generator=new Generator(random);
    private final LocalDate day=LocalDate.of(2026,9,13);
    private Learning.State state(){Learning.State s=new Learning.State();s.profile.currentSkill="add9";return s;}
    @Test public void durationsAreExplicitAndNotFixedQuestionTargets(){
        Learning.State s=state();Learning.Session a=TimedStudy.begin(s,30,random);
        assertEquals(1_800_000,a.durationMs);assertEquals(0,a.target);
        for(int i=0;i<45;i++){assertNotNull(Learning.ensureQuestion(s,generator,day));Learning.finishQuestion(s,false,day,random);}
        assertFalse(a.finished);assertEquals(45,a.completed);assertEquals(Integer.valueOf(45),s.daily.get(day.toString()));
        Learning.Session b=TimedStudy.begin(s,60,random);assertEquals(3_600_000,b.durationMs);assertSame(a,s.savedSessions.get(a.id));
    }
    @Test public void deadlinePreservesCurrentAnswerThenRecordsOnce(){
        Learning.State s=state();Learning.Session a=TimedStudy.begin(s,30,random);Question q=Learning.ensureQuestion(s,generator,day);
        a.answers.set(0,"4");a.studyMs=a.durationMs;TimedStudy.Clock clock=new TimedStudy.Clock();clock.start(a,0);
        assertSame(q,Learning.ensureQuestion(s,generator,day));assertEquals("4",a.answers.get(0));assertFalse(a.finished);
        Learning.finishQuestion(s,false,day,random);assertTrue(a.finished);assertEquals(1,s.history.size());
        Learning.finishQuestion(s,false,day,random);assertNull(Learning.ensureQuestion(s,generator,day));
        clock.tick(60_000);assertEquals(a.durationMs,a.studyMs);
        assertEquals(1,s.history.size());assertEquals(1,a.completed);assertEquals(a.durationMs,s.history.get(0).studyMs);
    }
    @Test public void pauseAndRestartDoNotCreditAwayTime(){
        Learning.State s=state();Learning.Session a=TimedStudy.begin(s,30,random);TimedStudy.Clock c=new TimedStudy.Clock();
        c.start(a,100);c.tick(10_100);c.pause(12_100);c.tick(900_100);assertEquals(12_000,a.studyMs);
        c.start(a,900_100);c.tick(902_100);assertEquals(14_000,a.studyMs);
        TimedStudy.Clock restored=new TimedStudy.Clock();restored.start(a,10);restored.tick(1010);assertEquals(15_000,a.studyMs);
    }
    @Test public void quietThinkingCountsButUnattendedScreenStops(){
        Learning.Session s=TimedStudy.begin(state(),60,random);TimedStudy.Clock c=new TimedStudy.Clock();c.start(s,0);
        assertFalse(c.tick(180_000));assertEquals(180_000,s.studyMs);
        assertTrue(c.tick(3_600_000));assertEquals(TimedStudy.Clock.IDLE_MS,s.studyMs);assertTrue(s.timedPaused);assertFalse(c.running());
    }
    @Test public void activeInputExtendsThinkingWindowAndClockRollbackDoesNotAddTime(){
        Learning.Session s=TimedStudy.begin(state(),60,random);TimedStudy.Clock c=new TimedStudy.Clock();c.start(s,1000);
        c.input(301_000);assertFalse(c.tick(601_000));assertEquals(600_000,s.studyMs);
        c.tick(500);assertEquals(600_000,s.studyMs);assertTrue(s.timedPaused);
    }
    @Test public void clockIsNotSharedWithAnotherSessionOrDeferredCorrections(){
        Learning.State state=state();Learning.Session s=TimedStudy.begin(state,30,random);TimedStudy.Clock c=new TimedStudy.Clock();c.start(s,0);c.tick(1000);
        Learning.ensureQuestion(state,generator,day);Learning.finishQuestion(state,true,day,random);String id=s.question.id;Learning.ensureQuestion(state,generator,day);Deferred.open(s,id);
        c.pause(1000);c.start(s,2000);c.tick(9000);assertEquals(1000,s.studyMs);
        Learning.beginPractice(state,"practice",List.of("add9"),3,false,random);c.start(state.session,10_000);c.tick(50_000);assertEquals(0,state.session.studyMs);
    }
    @Test public void serializedAndSnapshotStateRetainTimeInputHelpAndPausedStatus()throws Exception{
        Learning.State state=state();Learning.Session s=TimedStudy.begin(state,60,random);Learning.ensureQuestion(state,generator,day);
        s.studyMs=123_456;s.timedPaused=true;s.answers.set(0,"4");Review.helpUsed(state,day);
        Learning.State snapshot=LearningSnapshot.capture(state);s.studyMs=999;s.answers.set(0,"2");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(snapshot);
        Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(123_456,restored.session.studyMs);assertEquals(3_600_000,restored.session.durationMs);assertTrue(restored.session.timedPaused);
        assertEquals("4",restored.session.answers.get(0));assertTrue(restored.session.reviewWork.helpUsed);
    }
    @Test public void oldCountModesAndMixedDailyTotalsRemainCorrect(){
        Learning.State s=state();Learning.beginPractice(s,"practice",List.of("add9"),5,false,random);
        for(int i=0;i<5;i++){Learning.ensureQuestion(s,generator,day);Learning.finishQuestion(s,false,day,random);}assertTrue(s.session.finished);
        TimedStudy.begin(s,30,random);for(int i=0;i<12;i++){Learning.ensureQuestion(s,generator,day);Learning.finishQuestion(s,false,day,random);}
        Learning.beginPractice(s,"homework",List.of("add9"),3,false,random);for(int i=0;i<3;i++){Learning.ensureQuestion(s,generator,day);Learning.finishQuestion(s,false,day,random);}
        assertEquals(Integer.valueOf(20),s.daily.get(day.toString()));assertEquals(2,s.history.size());
    }
    @Test public void crossingMidnightDoesNotResetChosenDuration(){
        Learning.State s=state();TimedStudy.begin(s,30,random);Learning.ensureQuestion(s,generator,day);s.session.studyMs=60_000;
        Learning.finishQuestion(s,false,day.plusDays(1),random);assertEquals(1_740_000,TimedStudy.remaining(s.session));assertNull(s.daily.get(day.toString()));assertEquals(Integer.valueOf(1),s.daily.get(day.plusDays(1).toString()));
    }
    @Test(expected=IllegalArgumentException.class)public void arbitraryDurationsAreNotHiddenModes(){TimedStudy.begin(state(),1,random);}
    @Test public void pausedSessionAllowsDeferredCorrectionWithoutResumingItsClock(){
        Learning.State state=state();Learning.Session s=TimedStudy.begin(state,60,random);
        Question skipped=Learning.ensureQuestion(state,generator,day);s.answers.set(0,"3");
        Learning.finishQuestion(state,true,day,random);Learning.ensureQuestion(state,generator,day);
        String current=s.question.id;s.timedPaused=true;s.studyMs=30_000;
        assertTrue(TimedStudy.needsPauseScreen(s));Deferred.open(s,skipped.id);
        assertFalse(TimedStudy.needsPauseScreen(s));assertEquals("3",s.answers.get(0));
        TimedStudy.Clock clock=new TimedStudy.Clock();clock.start(s,100);clock.tick(90_100);
        assertEquals(30_000,s.studyMs);assertTrue(s.timedPaused);
        Learning.finishQuestion(state,false,day.plusDays(1),random);Deferred.settle(s);
        assertEquals(current,s.question.id);assertTrue(TimedStudy.needsPauseScreen(s));
        assertEquals(30_000,s.studyMs);assertEquals(1,s.corrected);assertEquals(0,Deferred.count(s));
    }
}
