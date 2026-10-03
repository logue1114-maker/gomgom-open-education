package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ReviewTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private final Random random=new Random(38192);
    private final Generator generator=new Generator(random);
    private Learning.State start(int count){Learning.State s=new Learning.State();s.profile.grade=3;s.profile.term=1;Learning.beginPractice(s,"practice",List.of("add100"),count,false,random);return s;}
    private Question question(Learning.State s){return Learning.ensureQuestion(s,generator,DAY);}
    private void answer(Learning.State s,boolean error){question(s);if(error)Learning.markError(s,DAY);Learning.finishQuestion(s,false,DAY,random);}
    private Review.Track track(Learning.State s){return Review.track(s.progress("add100"));}
    private long count(Learning.State s,Review.Outcome outcome){return track(s).events.stream().filter(e->e.outcome==outcome).count();}
    private Learning.State restore(Learning.State s)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(s);Learning.State out=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();Review.migrate(out);return out;}

    @Test public void historicalErrorsDoNotOutweighFreshIndependentRechecks(){
        Learning.State s=start(10);Learning.Progress p=s.progress("add100");p.attempts=100;p.firstCorrect=1;p.corrected=99;p.review=null;Review.migrate(s);
        assertEquals(Review.Status.RECHECK,Review.status(p));assertTrue(track(s).events.isEmpty());
        answer(s,true);assertFalse(p.weak());answer(s,true);assertTrue(p.weak());assertTrue(s.needsPractice.contains("add100"));
        answer(s,false);answer(s,false);question(s);assertTrue(s.session.reviewWork.recheck);answer(s,false);
        assertEquals(1,track(s).rechecked);assertTrue(p.weak());
        answer(s,false);answer(s,false);question(s);assertTrue(s.session.reviewWork.recheck);answer(s,false);
        assertEquals(2,track(s).rechecked);assertEquals(2,s.session.rechecked);assertFalse(p.weak());assertFalse(s.needsPractice.contains("add100"));
        assertEquals(Review.Status.RECENT,Review.status(p));assertEquals(108,p.attempts);assertEquals(7,p.firstCorrect);assertEquals(101,p.corrected);
        assertEquals(8,s.daily.get(DAY.toString()).intValue());assertEquals(10,s.session.target);
        assertEquals(2,count(s,Review.Outcome.RECHECK_CORRECT));assertEquals(2,count(s,Review.Outcome.CORRECTED));
    }
    @Test public void errorAndRecheckPurposeSurviveReloadWithoutDoubleCounting()throws Exception{
        Learning.State s=start(10);String id=question(s).id;Learning.markError(s,DAY);Learning.markError(s,DAY);
        s=restore(s);assertEquals(id,question(s).id);Learning.markError(s,DAY);assertEquals(1,count(s,Review.Outcome.FIRST_WRONG));
        answer(s,true);answer(s,false);answer(s,false);question(s);assertTrue(s.session.reviewWork.recheck);String recheck=s.session.question.id;String source=s.session.reviewWork.sourceId;
        s=restore(s);assertEquals(recheck,question(s).id);assertTrue(s.session.reviewWork.recheck);assertEquals(id,source);
        Learning.finishQuestion(s,false,DAY,random);Learning.finishQuestion(s,false,DAY,random);s=restore(s);Learning.finishQuestion(s,false,DAY,random);
        assertEquals(4,s.session.completed);assertEquals(4,s.reviewSequence);assertEquals(1,track(s).rechecked);assertEquals(4,s.progress("add100").attempts);
    }
    @Test public void inputProblemsAndSkipsAreNotMathematicalWeakness(){
        Learning.State s=start(2);question(s);Review.inputIssue(s,"unreadable",DAY);Review.inputIssue(s,"unreadable",DAY);
        Learning.finishQuestion(s,true,DAY,random);question(s);Learning.finishQuestion(s,true,DAY,random);
        assertEquals(1,count(s,Review.Outcome.INPUT_UNCERTAIN));assertEquals(2,count(s,Review.Outcome.SKIPPED));assertFalse(s.progress("add100").weak());assertTrue(s.needsPractice.isEmpty());assertTrue(s.daily.isEmpty());
        assertEquals(Review.Status.UNKNOWN,Review.status(s.progress("add100")));assertEquals(2,Deferred.count(s.session));
    }
    @Test public void deferredCorrectionCannotEraseNewerIndependentSuccess()throws Exception{
        Learning.State s=start(3);String old=question(s).id;Learning.finishQuestion(s,true,DAY,random);answer(s,false);question(s);String current=s.session.question.id;
        Deferred.open(s.session,old);s=restore(s);assertEquals(old,s.session.question.id);Learning.finishQuestion(s,false,DAY,random);Learning.finishQuestion(s,false,DAY,random);
        assertEquals(0,track(s).rechecked);assertEquals(1,count(s,Review.Outcome.CORRECTED));assertEquals(2,s.reviewSequence);assertEquals(Review.Status.RECENT,Review.status(s.progress("add100")));
        assertEquals(current,question(s).id);assertEquals(current,s.session.reviewWork.questionId);assertEquals(0,s.session.skipped);assertEquals(1,s.session.corrected);
    }
    @Test public void newDaySchedulesFreshSelectedQuestionWithoutChangingScopeOrCount(){
        Learning.State s=start(10);answer(s,true);answer(s,true);for(int i=0;i<6;i++)answer(s,false);assertFalse(track(s).pending);
        assertFalse(Review.due(s,"add100",DAY));assertTrue(Review.due(s,"add100",DAY.plusDays(1)));
        Learning.beginPractice(s,"homework",List.of("sub20","add100"),3,false,random);s.session.queue.clear();s.session.queue.addAll(List.of("sub20","sub20","sub20"));
        Question q=Learning.ensureQuestion(s,generator,DAY.plusDays(1));assertEquals("add100",q.skillId);assertTrue(s.session.reviewWork.recheck);assertEquals(3,s.session.target);assertEquals(2,s.session.queue.size());
        Learning.finishQuestion(s,false,DAY.plusDays(1),random);assertEquals(3,track(s).rechecked);assertFalse(Review.due(s,"add100",DAY.plusDays(1)));
        Learning.beginPractice(s,"practice",List.of("sub20"),1,false,random);assertEquals("sub20",Learning.ensureQuestion(s,generator,DAY.plusDays(14)).skillId);
    }
    @Test public void stalePublishedQuestionDoesNotBecomeRecheckWhenDateOrNeedChanges()throws Exception{
        Learning.State s=start(8);question(s);s.session.reviewWork=null;Learning.Progress p=s.progress("add100");p.review=null;p.attempts=20;p.corrected=20;
        s=restore(s);String existing=s.session.question.id;assertEquals(existing,Learning.ensureQuestion(s,generator,DAY.plusDays(1)).id);assertFalse(s.session.reviewWork.recheck);
        Learning.finishQuestion(s,false,DAY.plusDays(1),random);assertEquals(0,track(s).rechecked);assertEquals(Review.Status.RECHECK,Review.status(s.progress("add100")));
    }
    @Test public void repeatedPromptNeverCountsAsIndependentRecovery(){
        Learning.State s=start(8);Random fixed=new Random(){@Override public int nextInt(int bound){return 0;}};Generator same=new Generator(fixed);
        for(int i=0;i<8;i++){Learning.ensureQuestion(s,same,DAY);if(i<2)Learning.markError(s,DAY);Learning.finishQuestion(s,false,DAY,fixed);}
        assertEquals(1,track(s).failed.size());assertEquals(0,track(s).rechecked);assertTrue(track(s).pending);assertFalse(s.progress("add100").weak());assertEquals(8,s.session.completed);
    }
    @Test public void failedRecheckResetsRecoveryButRepeatedSubmitDoesNotAddFailures(){
        Learning.State s=start(10);answer(s,true);answer(s,true);answer(s,false);answer(s,false);answer(s,false);assertEquals(1,track(s).recoveryChecks);
        answer(s,false);answer(s,false);question(s);assertTrue(s.session.reviewWork.recheck);Learning.markError(s,DAY);Learning.markError(s,DAY);answer(s,false);
        assertEquals(0,track(s).recoveryChecks);assertEquals(1,count(s,Review.Outcome.RECHECK_WRONG));assertTrue(track(s).pending);assertTrue(s.progress("add100").weak());
    }
    @Test public void recommendationsUseRecentStatusAndPreserveActualGrade(){
        Learning.State s=start(10);s.profile.grade=6;s.profile.term=2;answer(s,true);answer(s,true);
        assertEquals("add100",Learning.suggested(s,DAY).get(0));for(int i=0;i<6;i++)answer(s,false);
        assertFalse(Learning.suggested(s,DAY).contains("add100"));assertEquals("add100",Learning.suggested(s,DAY.plusDays(1)).get(0));assertEquals(6,s.profile.grade);assertEquals(2,s.profile.term);
    }
    @Test public void eventWindowIsBoundedWhileTotalsRemain(){
        Learning.State s=start(45);for(int i=0;i<45;i++)answer(s,false);
        assertEquals(Review.EVENT_LIMIT,track(s).events.size());assertEquals(45,s.progress("add100").firstCorrect);assertEquals(45,s.reviewSequence);assertEquals(45,s.daily.get(DAY.toString()).intValue());
    }
    @Test public void oldDiagnosticSuccessDoesNotClearNewerErrors(){
        Learning.State s=start(10);answer(s,false);answer(s,false);assertTrue(Review.diagnosticRecent(s,"add100"));
        answer(s,true);answer(s,true);answer(s,false);assertFalse(Review.diagnosticRecent(s,"add100"));assertTrue(s.progress("add100").weak());
    }
    @Test public void explicitHelpIsAssistedEvidenceWithoutInventingAnError(){
        Learning.State s=start(1);question(s);Review.helpUsed(s,DAY);Review.helpUsed(s,DAY);
        assertFalse(s.session.hadError);Learning.finishQuestion(s,false,DAY,random);
        Learning.Progress p=s.progress("add100");assertEquals(1,p.attempts);assertEquals(0,p.firstCorrect);assertEquals(0,p.corrected);assertEquals(1,p.assisted);assertEquals(0,p.consecutive);
        assertEquals(0,s.session.correct);assertEquals(0,s.session.corrected);assertEquals(1,s.session.assisted);assertEquals(1,s.session.completed);
        assertEquals(1,count(s,Review.Outcome.HELP_USED));assertEquals(1,count(s,Review.Outcome.ASSISTED_CORRECT));assertEquals(0,count(s,Review.Outcome.FIRST_WRONG));assertEquals(0,count(s,Review.Outcome.CORRECTED));
        assertTrue(track(s).pending);assertEquals(1,s.history.size());assertEquals(1,s.history.get(0).total);assertEquals(1,s.history.get(0).assisted);
    }
    @Test public void helpAfterAnErrorKeepsBothTheWrongAttemptAndAssistanceVisible(){
        Learning.State s=start(1);question(s);Learning.markError(s,DAY);Review.helpUsed(s,DAY);Learning.finishQuestion(s,false,DAY,random);
        Learning.Progress p=s.progress("add100");assertEquals(0,p.firstCorrect);assertEquals(0,p.corrected);assertEquals(1,p.assisted);assertTrue(track(s).pending);
        assertEquals(1,s.session.completed);assertEquals(s.session.completed,s.session.correct+s.session.corrected+s.session.assisted+s.session.skipped);
        assertEquals(1,count(s,Review.Outcome.FIRST_WRONG));assertEquals(1,count(s,Review.Outcome.HELP_USED));assertEquals(1,count(s,Review.Outcome.ASSISTED_CORRECT));
    }
    @Test public void assistedFreshCheckCannotCountAsAnIndependentRecheck(){
        Learning.State s=start(8);question(s);Review.helpUsed(s,DAY);Learning.finishQuestion(s,false,DAY,random);
        answer(s,false);answer(s,false);question(s);assertTrue(s.session.reviewWork.recheck);
        Review.helpUsed(s,DAY);Learning.finishQuestion(s,false,DAY,random);
        assertEquals(0,track(s).rechecked);assertEquals(0,s.session.rechecked);assertEquals(2,s.session.assisted);assertTrue(track(s).pending);
        assertEquals(0,count(s,Review.Outcome.RECHECK_CORRECT));assertEquals(2,count(s,Review.Outcome.ASSISTED_CORRECT));
    }
}
