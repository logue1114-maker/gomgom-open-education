package com.gomgomapps.math.core;
import org.junit.Test;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

public class LearningEvaluationTest {
    private Learning.Progress outcomes(int correct,int total){Learning.Progress p=new Learning.Progress();for(int i=0;i<total;i++)LearningEvaluation.record(p,i<correct);return p;}
    @Test public void boundariesAndMinimumDoNotRoundUp(){
        assertEquals(LearningEvaluation.Level.INSUFFICIENT_RECORDS,LearningEvaluation.level(outcomes(9,9)));
        assertEquals(LearningEvaluation.Level.NEEDS_PRACTICE,LearningEvaluation.level(outcomes(5,10)));
        assertEquals(LearningEvaluation.Level.AVERAGE,LearningEvaluation.level(outcomes(6,10)));
        assertEquals(LearningEvaluation.Level.AVERAGE,LearningEvaluation.level(outcomes(8,10)));
        assertEquals(LearningEvaluation.Level.EXCELLENT,LearningEvaluation.level(outcomes(9,10)));
        assertEquals(LearningEvaluation.Level.AVERAGE,LearningEvaluation.level(outcomes(26,29)));
    }
    @Test public void recentWindowAllowsRecoveryAndLegacyHistoryIsNotInvented(){
        Learning.Progress p=outcomes(0,30);for(int i=0;i<30;i++)LearningEvaluation.record(p,true);
        assertEquals(30,LearningEvaluation.count(p));assertEquals(30,LearningEvaluation.correct(p));
        p.evaluationResults=null;p.attempts=100;p.firstCorrect=100;
        assertEquals(LearningEvaluation.Level.INSUFFICIENT_RECORDS,LearningEvaluation.level(p));
        LearningEvaluation.record(p,false);assertEquals(1,LearningEvaluation.count(p));assertEquals(0,LearningEvaluation.correct(p));
    }
    @Test public void normalFinishSeparatesErrorsAssistanceSkipsAndAvoidsDuplicateRetry()throws Exception{
        Learning.State state=new Learning.State();Random r=new Random(4);LocalDate day=LocalDate.of(2026,10,8);
        Learning.beginPractice(state,"practice",List.of("add9"),10,false,r);Generator generator=new Generator(r);
        for(int i=0;i<10;i++){
            Learning.ensureQuestion(state,generator,day);
            if(i==0)Learning.markError(state,day);
            if(i==1){state.session.reviewWork=new Review.Work();state.session.reviewWork.helpUsed=true;}
            Learning.finishQuestion(state,i==2,day,r);Learning.finishQuestion(state,false,day,r);
        }
        Learning.Progress p=state.progress("add9");assertEquals(10,LearningEvaluation.count(p));assertEquals(7,LearningEvaluation.correct(p));
        String skipped=state.session.deferred.keySet().iterator().next();Deferred.open(state.session,skipped);Learning.finishQuestion(state,false,day,r);
        assertEquals(10,LearningEvaluation.count(p));assertEquals(7,LearningEvaluation.correct(p));
        Learning.State frozen=LearningSnapshot.capture(state);LearningEvaluation.record(p,false);
        assertEquals(10,LearningEvaluation.count(frozen.progress("add9")));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(frozen);
        Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        assertEquals(LearningEvaluation.Level.AVERAGE,LearningEvaluation.level(restored.progress("add9")));
        assertEquals(7,LearningEvaluation.correct(restored.progress("add9")));
        assertEquals(0,LearningEvaluation.count(restored.progress.get("sub9")));
    }
}
