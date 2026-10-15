package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class CountArithmeticBreakTest {
    private static final LocalDate DAY=LocalDate.of(2026,10,15);
    private static Learning.State original(boolean subtract){
        Learning.State state=KoreanLargeIntegrationTest.student();Random random=new Random(379);
        Learning.beginDiagnostic(state,random);
        Learning.Session s=Learning.beginPractice(state,"practice",List.of(KoreanLargeNumbers.COUNT),20,false,random,Map.of(KoreanLargeNumbers.COUNT,4));
        s.question=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,subtract?1463400:1113400,0,1,70000,subtract));
        s.answers=new ArrayList<>(List.of("1253401"));Review.openFresh(state,DAY);Review.helpUsed(state,DAY);
        HelpPlan plan=HelpPlan.forQuestion(s.question);s.conceptHelp=plan.restore(null,s.question.id);
        for(int i=0;i<4;i++)s.conceptHelp.entries.add("");
        for(int i=0;i<4;i++)s.conceptHelp.entries.set(i,KoreanLargeCountTeaching.guide(s.question).frames.get(i).expected);
        s.conceptHelp.stage=4;s.conceptHelp.entries.set(4,"999");return state;
    }
    private static Learning.State cold(Learning.State s)throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(data)){out.writeObject(s);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(data.toByteArray()))){return (Learning.State)in.readObject();}
    }
    private static void finish(Learning.State state,Random random,boolean helped,boolean skipped){
        Question q=Learning.ensureQuestion(state,new Generator(random),DAY);
        if(helped)Review.helpUsed(state,DAY);
        if(!skipped){state.session.answers.set(0,q.answers[0]);assertTrue(new Checker().check(q,List.of(),state.session.answers).correct());}
        Learning.finishQuestion(state,skipped,DAY,random);
    }
    @Test public void bothOperationsReturnToExactOriginalQuestionBodyWorkAndStageWithoutCountingTheOriginal()throws Exception{
        for(boolean sub:List.of(false,true)){
            Learning.State state=original(sub);Learning.Session parent=state.session;String parentId=parent.id,qid=parent.question.id;
            HelpPlan.Draft draft=parent.conceptHelp;Review.Work work=parent.reviewWork;int events=state.progress(KoreanLargeNumbers.COUNT).review.events.size();
            Random random=new Random(3792);Learning.Session child=CountArithmeticBreak.begin(state,random);
            assertEquals(sub?"sub9":"add9",child.selected.get(0));assertEquals(2,child.target);assertNull(child.foundationPlanKey);assertNull(child.foundationUnitKey);
            assertSame(parent,state.savedSessions.get(parentId));assertFalse(CountArithmeticBreak.returnToQuestion(state,true));
            finish(state,random,false,false);state=LearningSnapshot.capture(cold(state));
            assertEquals(1,state.session.completed);finish(state,random,true,false);state=cold(state);
            assertTrue(CountArithmeticBreak.returnToQuestion(state,true));assertEquals(parentId,state.session.id);assertEquals(qid,state.session.question.id);
            assertEquals(List.of("1253401"),state.session.answers);assertEquals(draft.entries,state.session.conceptHelp.entries);assertEquals(4,state.session.conceptHelp.stage);
            assertEquals(work.questionId,state.session.reviewWork.questionId);assertTrue(state.session.reviewWork.helpUsed);
            assertEquals(0,state.session.completed);assertEquals(0,state.progress(KoreanLargeNumbers.COUNT).attempts);assertEquals(events,state.progress(KoreanLargeNumbers.COUNT).review.events.size());
            assertEquals(Diagnosis.UnitStatus.NEEDS_STUDY,Diagnosis.currentPlan(state).unitScreens.get(KoreanLargeIntegrationTest.UNIT).status);
            assertEquals(0,state.progress(KoreanLargeNumbers.COUNT).review.recoveryChecks);
            state=cold(state);assertTrue(CountArithmeticBreak.helpReturnPending(state));assertTrue(CountArithmeticBreak.consumeHelpReturn(state));assertFalse(CountArithmeticBreak.consumeHelpReturn(state));
            assertFalse(CountArithmeticBreak.returnToQuestion(state,true));
        }
    }
    @Test public void returnPendingCannotFollowAChangedQuestionOrNewQuestionGeneration(){
        Learning.State state=original(false);Random random=new Random(3793);CountArithmeticBreak.begin(state,random);assertTrue(CountArithmeticBreak.returnToQuestion(state,false));
        assertTrue(CountArithmeticBreak.helpReturnPending(state));state.session.question=KoreanLargeNumbers.make(new KoreanLargeNumbers.Givens(KoreanLargeNumbers.COUNT,200000,0,1,10000,false));assertFalse(CountArithmeticBreak.helpReturnPending(state));
        state=original(false);CountArithmeticBreak.begin(state,random);assertTrue(CountArithmeticBreak.returnToQuestion(state,false));
        state.session.advancePending=true;Learning.ensureQuestion(state,new Generator(random),DAY);assertFalse(state.session.countReturnHelpPending);assertNull(state.session.countReturnHelpQuestionId);
    }
    @Test public void noReturnMayChangeCountryOrLearnedScopeToMakeItselfPossible(){
        List<Consumer<Learning.Profile>> changes=List.of(p->p.countryCode="NA",p->p.educationSystem="na-national",p->p.grade=5,p->p.term=1,p->p.schoolYear=2025,p->p.curriculum=2015,p->p.lastLearnedSkill="unit:KR|2022|3|2|6",p->p.excluded.add(KoreanLargeNumbers.COUNT));
        for(Consumer<Learning.Profile> change:changes){Learning.State state=original(false);Random random=new Random(3794);Learning.Session parent=state.session;CountArithmeticBreak.begin(state,random);Learning.Session child=state.session;change.accept(state.profile);String country=state.profile.countryCode,system=state.profile.educationSystem;assertFalse(CountArithmeticBreak.returnToQuestion(state,false));assertSame(child,state.session);assertSame(parent,state.savedSessions.get(parent.id));assertEquals(country,state.profile.countryCode);assertEquals(system,state.profile.educationSystem);}
    }
    @Test public void staleOrFinishedOriginalAndNestedDetoursAreRejected(){
        for(int kind=0;kind<3;kind++){
            Learning.State state=original(false);Random random=new Random(3795);Learning.Session parent=state.session;CountArithmeticBreak.begin(state,random);
            assertNull(CountArithmeticBreak.skill(state));
            if(kind==0)parent.advancePending=true;else if(kind==1)parent.finished=true;else parent.question.id="different-question";
            assertFalse(CountArithmeticBreak.canReturn(state));assertFalse(CountArithmeticBreak.returnToQuestion(state,false));
        }
        Learning.State state=original(false);state.session.conceptHelp.stage=3;assertNull(CountArithmeticBreak.skill(state));
        state.session.conceptHelp.stage=4;state.profile.excluded.add("add9");assertNull(CountArithmeticBreak.skill(state));
    }
    @Test public void explicitEarlyReturnKeepsUnfinishedChildAndConsumesOnlyItsReturnLink(){
        Learning.State state=original(false);Random random=new Random(3796);Learning.Session parent=state.session;Learning.Session child=CountArithmeticBreak.begin(state,random);Question q=Learning.ensureQuestion(state,new Generator(random),DAY);child.answers.set(0,"1");
        assertTrue(CountArithmeticBreak.returnToQuestion(state,false));assertSame(parent,state.session);assertSame(child,state.savedSessions.get(child.id));assertEquals(q.id,child.question.id);assertEquals(List.of("1"),child.answers);assertEquals(0,child.completed);assertNull(child.countReturnSessionId);
        Learning.resume(state,child.id);assertFalse(CountArithmeticBreak.canReturn(state));assertFalse(CountArithmeticBreak.returnToQuestion(state,false));
    }
    @Test public void twoSkipsRetainDeferredWorkWithoutAwardingOriginalSuccess(){
        Learning.State state=original(false);Random random=new Random(3797);Learning.Session parent=state.session;Learning.Session child=CountArithmeticBreak.begin(state,random);
        finish(state,random,false,true);finish(state,random,false,true);assertEquals(2,child.deferred.size());assertEquals(0,child.correct);assertTrue(child.finished);
        assertTrue(CountArithmeticBreak.returnToQuestion(state,true));assertSame(parent,state.session);assertSame(child,state.savedSessions.get(child.id));assertEquals(0,parent.completed);assertEquals(0,state.progress(KoreanLargeNumbers.COUNT).attempts);assertEquals(2,child.deferred.size());
    }
    @Test public void snapshotBeforeFirstAndAfterCompletionRetainsOnlyRealAccounting()throws Exception{
        Learning.State state=original(false);Random random=new Random(3798);CountArithmeticBreak.begin(state,random);state=cold(LearningSnapshot.capture(state));assertTrue(CountArithmeticBreak.canReturn(state));
        finish(state,random,false,false);finish(state,random,false,false);int attempts=state.progress("add9").attempts,sequence=(int)state.reviewSequence;Learning.Session child=state.session;
        Learning.finishQuestion(state,false,DAY,random);assertEquals(attempts,state.progress("add9").attempts);assertEquals(sequence,state.reviewSequence);
        assertTrue(CountArithmeticBreak.returnToQuestion(state,true));assertFalse(CountArithmeticBreak.returnToQuestion(state,true));assertEquals(attempts,state.progress("add9").attempts);assertEquals(sequence,state.reviewSequence);assertTrue(child.finished);
    }
}
