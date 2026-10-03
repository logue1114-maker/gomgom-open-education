package com.gomgomapps.math.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DiagnosisTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private Learning.State student(){Learning.State state=new Learning.State();state.profile.grade=3;state.profile.term=1;state.profile.schoolYear=2026;state.profile.ready=true;return state;}
    private void answer(Learning.State state,Random r,boolean error,boolean skip){Learning.ensureQuestion(state,new Generator(r));if(error)Learning.markError(state);Learning.finishQuestion(state,skip,DAY,r);}
    private int finish(Learning.State state,Random r,boolean error,boolean skip){int count=0;while(!state.session.finished&&count<30){answer(state,r,error,skip);count++;}assertTrue("round terminates",state.session.finished);return count;}

    @Test public void fourIndependentAnswersEndTheFirstRoundWithoutClaimingWholeScopeCoverage(){
        Learning.State state=student();Random r=new Random(73);Learning.beginDiagnostic(state,r);
        assertEquals(4,Diagnosis.displayedTarget(state.session));assertEquals(4,finish(state,r,false,false));
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);assertTrue(plan.initialRoundFinished);assertFalse(plan.untested.isEmpty());assertEquals(3,state.profile.grade);
        assertEquals(2,plan.checks.values().stream().filter(p->p.outcome==Diagnosis.Outcome.RECENTLY_CORRECT).count());
    }
    @Test public void skipsStayUncertainAndStopAtEightWithoutLoweringTheStudent(){
        Learning.State state=student();Random r=new Random(3);Learning.beginDiagnostic(state,r);
        assertEquals(8,finish(state,r,false,true));assertTrue(state.needsPractice.isEmpty());assertEquals(3,state.profile.grade);
        Diagnosis.Plan plan=Diagnosis.currentPlan(state);assertTrue(plan.pending.size()>0);
        assertTrue(plan.checks.values().stream().noneMatch(p->p.outcome==Diagnosis.Outcome.NEEDS_PRACTICE));
        assertTrue(plan.checks.values().stream().allMatch(p->p.reviewGrade==2));
    }
    @Test public void repeatedErrorsFollowOnlyEligibleRelatedFoundationsWithinTheRoundLimit(){
        Learning.State state=student();Random r=new Random(9);Learning.beginDiagnostic(state,r);Diagnosis.Plan plan=Diagnosis.currentPlan(state);
        assertEquals(8,finish(state,r,true,false));assertFalse(state.needsPractice.isEmpty());assertFalse(plan.pending.isEmpty());
        for(Diagnosis.Probe p:plan.checks.values())if(p.followUp&&p.reviewGrade<2){
            assertTrue(plan.scope.contains(p.skillId));assertTrue(Curriculum.grade(Catalog.get(p.skillId),plan.curriculum)<=p.reviewGrade);
            assertTrue(plan.checks.values().stream().anyMatch(parent->parent.reviewGrade==p.reviewGrade+1&&ancestor(parent.skillId,p.skillId)));
        }
        Learning.Session next=Learning.continueDiagnostic(state,r);assertSame(plan,next.diagnosticRun.plan);assertTrue(next.target<=4);
        assertTrue(finish(state,r,false,false)<=8);assertEquals(3,state.profile.grade);
    }
    private boolean ancestor(String parent,String child){return Catalog.foundationOrder(parent).contains(child);}

    @Test public void oneMistakePerTypeGetsConfirmationRatherThanImmediateFoundationFailure(){
        Learning.State state=student();Random r=new Random(35);Learning.beginDiagnostic(state,r);Set<String> encountered=new HashSet<>();
        for(int i=0;i<4;i++){Question q=Learning.ensureQuestion(state,new Generator(r));answer(state,r,encountered.add(q.skillId),false);}
        assertFalse(state.session.finished);assertTrue(state.needsPractice.isEmpty());assertTrue(Diagnosis.additionalBlock(state.session));
        assertEquals(0,Diagnosis.displayedDone(state.session));assertEquals(4,Diagnosis.displayedTarget(state.session));
        assertEquals(4,finish(state,r,false,false));assertTrue(state.needsPractice.isEmpty());
    }
    @Test public void eachDisplayBlockHasAFixedDenominatorAndCompletionIsIdempotent(){
        Learning.State state=student();Random r=new Random(28);Learning.beginDiagnostic(state,r);int phase=1,denominator=4;
        for(int i=0;i<8;i++){
            if(state.session.diagnosticRun.phase==phase)assertEquals(denominator,Diagnosis.displayedTarget(state.session));
            else {phase=state.session.diagnosticRun.phase;denominator=Diagnosis.displayedTarget(state.session);assertEquals(0,Diagnosis.displayedDone(state.session));}
            answer(state,r,false,true);int completed=state.session.completed;Learning.finishQuestion(state,true,DAY,r);assertEquals(completed,state.session.completed);
        }
        assertTrue(state.session.finished);assertEquals(1,state.history.size());assertEquals(8,state.history.get(0).total);
    }
    @Test public void dailyChecksFitInsideTheSelectedDailyQuantity(){
        Learning.State state=student();Random r=new Random(1);Learning.beginDiagnostic(state,r);finish(state,r,false,true);
        Learning.Session daily=Learning.beginPractice(state,"daily",List.of("add100"),10,false,r);
        assertNotNull(daily.diagnosticRun);assertEquals(2,daily.diagnosticRun.scheduled.size());assertEquals(8,daily.queue.size());
        assertEquals(10,finish(state,r,false,true));assertEquals(10,daily.target);assertTrue(Diagnosis.remainingChecks(state)>0);
    }
    @Test public void startingAnotherModeDoesNotStealReservedChecksOrResetTheCurrentQuestion(){
        Learning.State state=student();Random r=new Random(61);Learning.Session original=Learning.beginDiagnostic(state,r);Question q=Learning.ensureQuestion(state,new Generator(r));
        Learning.beginPractice(state,"practice",List.of("add9"),10,false,r);Learning.continueDiagnostic(state,r);
        assertSame(original,state.session);assertEquals(q.id,Learning.ensureQuestion(state,new Generator(r)).id);
    }
    @Test public void storedScopeAndQuestionSurviveProfileChangesAndRestart()throws Exception{
        Learning.State state=student();Random r=new Random(8);Learning.beginDiagnostic(state,r);Question q=Learning.ensureQuestion(state,new Generator(r));Diagnosis.Plan plan=state.session.diagnosticRun.plan;
        state.session.answers.set(0,"12");state.profile.grade=12;state.profile.term=2;state.profile.schoolYear=2027;state.profile.currentSkill="integral";
        Learning.State restored=copy(state);Diagnosis.migrate(restored);assertFalse(Diagnosis.hasInitialRound(restored));
        assertEquals(q.id,Learning.ensureQuestion(restored,new Generator(r)).id);assertEquals("12",restored.session.answers.get(0));assertEquals(plan.scope,restored.session.diagnosticRun.plan.scope);
        assertEquals(2026,restored.session.diagnosticRun.plan.schoolYear);assertEquals(2022,restored.session.diagnosticRun.plan.curriculum);
        while(!restored.session.finished){Question next=Learning.ensureQuestion(restored,new Generator(r));assertTrue(plan.scope.contains(next.skillId));Learning.finishQuestion(restored,false,DAY,r);}
        assertFalse(Diagnosis.hasInitialRound(restored));restored.profile.grade=3;restored.profile.term=1;restored.profile.schoolYear=2026;restored.profile.currentSkill="";assertTrue(Diagnosis.hasInitialRound(restored));
    }
    @Test public void emptyScopeDoesNotDiscardTheExistingStudy(){
        Learning.State state=student();Random r=new Random(1);Learning.Session original=Learning.beginPractice(state,"practice",List.of("add9"),10,false,r);
        for(Catalog.Skill skill:Catalog.ALL)state.profile.excluded.add(skill.id);
        assertThrows(IllegalArgumentException.class,()->Learning.beginDiagnostic(state,r));assertSame(original,state.session);
    }
    @Test public void legacyProgressMigratesWithoutInferringFailuresFromSkips(){
        Learning.State state=student();state.diagnostics=null;state.profile.schoolYear=0;state.profile.diagnosed=true;Random r=new Random(3);
        Learning.Session saved=Learning.beginPractice(state,"practice",List.of("add9"),10,false,r);Question q=Learning.ensureQuestion(state,new Generator(r));saved.answers.set(0,"4");
        Diagnosis.migrate(state);assertEquals(q.id,state.session.question.id);assertEquals("4",state.session.answers.get(0));assertTrue(Diagnosis.hasInitialRound(state));assertTrue(state.needsPractice.isEmpty());
    }
    @Test public void legacyUnfinishedDiagnosisPreservesItsQuestionAndBoundsTheRemainingWork(){
        Learning.State state=student();state.diagnostics=null;Learning.Session s=new Learning.Session();s.mode="diagnostic";s.stageGrade=2;s.completed=6;s.target=15;
        for(Catalog.Skill skill:Learning.diagnosticScope(state.profile))s.diagnosticScope.add(skill.id);
        s.queue.addAll(List.of("add100","sub100","add100","sub100","add100","sub100","add100","sub100"));s.question=new Generator(new Random(2)).next("add100",List.of(),false);s.answers.add("123");state.session=s;
        String id=s.question.id;Diagnosis.migrate(state);assertEquals(id,Learning.ensureQuestion(state,new Generator()).id);assertEquals("123",s.answers.get(0));assertEquals(4,Diagnosis.displayedTarget(s));
        assertTrue(finish(state,new Random(4),false,true)<=8);assertTrue(Diagnosis.hasInitialRound(state));assertTrue(state.needsPractice.isEmpty());
    }
    private Learning.State copy(Learning.State source)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(source);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (Learning.State)in.readObject();}}
}
