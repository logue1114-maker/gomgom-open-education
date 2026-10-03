package com.gomgomapps.math.core;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrerequisiteTest {
    private static final LocalDate DAY=LocalDate.of(2026,9,8);
    private Learning.State student(int grade,int term){Learning.State s=new Learning.State();s.profile.grade=grade;s.profile.term=term;s.profile.schoolYear=2026;s.profile.ready=true;return s;}
    private void answer(Learning.State s,Random r,boolean wrong,boolean skip){Learning.ensureQuestion(s,new Generator(r),DAY);if(wrong)Learning.markError(s,DAY);Learning.finishQuestion(s,skip,DAY,r);}
    private Learning.State focused(String id,int grade,int term,Random r){
        Learning.State s=student(grade,term);int highest=Learning.diagnosticScope(s.profile).stream().mapToInt(skill->Curriculum.grade(skill,Curriculum.version(s.profile))).max().orElseThrow();
        for(Catalog.Skill skill:Learning.diagnosticScope(s.profile))if(Curriculum.grade(skill,Curriculum.version(s.profile))==highest&&!skill.id.equals(id))s.profile.excluded.add(skill.id);
        Learning.beginDiagnostic(s,r);return s;
    }
    private void weak(Learning.State s,String id,Random r){Learning.beginPractice(s,"practice",List.of(id),2,false,r);answer(s,r,true,false);answer(s,r,true,false);assertTrue(s.progress(id).weak());}

    @Test public void catalogConnectsDistinctArithmeticFoundationsWithoutCyclesOrMissingIds(){
        assertTrue(Catalog.get("mul22").prerequisites.containsAll(List.of("mul2","add1000")));
        assertTrue(Catalog.get("fracAdd").prerequisites.containsAll(List.of("fracAddLike","lcm","reduce")));
        assertTrue(Catalog.get("rational").prerequisites.containsAll(List.of("fracAdd","fracSub","fracMul","fracDiv","signedAdd","signedMul")));
        for(Catalog.Skill skill:Catalog.ALL){List<String> all=Catalog.foundationOrder(skill.id);assertFalse(all.contains(skill.id));assertEquals(all.size(),new HashSet<>(all).size());for(String id:all)assertNotNull(Catalog.get(id));}
    }
    @Test public void repeatedMultiplicationErrorsQueueBothProductAndAdditionChecks(){
        Random r=new Random(12);Learning.State s=focused("mul22",4,2,r);assertEquals(2,s.session.target);
        answer(s,r,true,false);answer(s,r,true,false);Diagnosis.Plan p=Diagnosis.currentPlan(s);
        assertTrue(p.checks.containsKey("mul2"));assertTrue(p.checks.containsKey("add1000"));
        assertEquals(3,p.checks.get("mul2").reviewGrade);assertEquals(3,p.checks.get("add1000").reviewGrade);
        assertFalse(s.progress("mul2").weak());assertFalse(s.progress("add1000").weak());
        int total=2;while(!s.session.finished){answer(s,r,false,false);total++;assertTrue(total<=8);}
        assertEquals(6,total);assertEquals(4,s.profile.grade);assertEquals(2,s.profile.term);
    }
    @Test public void broadDiagnosticsKeepFourQuestionBlocksAndEightQuestionCeilingAcrossRandomOrders(){
        for(int seed=0;seed<24;seed++){
            Random r=new Random(seed);Learning.State s=student(4,2);Learning.beginDiagnostic(s,r);assertEquals(4,Diagnosis.displayedTarget(s.session));
            for(int i=0;i<4;i++)answer(s,r,true,false);Diagnosis.Plan p=Diagnosis.currentPlan(s);
            assertTrue(p.checks.values().stream().anyMatch(probe->probe.followUp));
            assertEquals(4,Diagnosis.displayedTarget(s.session));while(!s.session.finished){answer(s,r,false,false);assertTrue(s.session.completed<=8);}
            assertTrue(s.session.completed>=4&&s.session.completed<=8);assertEquals(4,s.profile.grade);
            for(Diagnosis.Probe probe:p.checks.values())assertTrue(p.scope.contains(probe.skillId));
        }
    }
    @Test public void exclusionsStayExcludedWhileOtherEligibleBranchesRemainAvailable(){
        Random r=new Random(8);Learning.State s=student(4,2);s.profile.excluded.addAll(List.of("largePlace","divide2","mul2","add1000"));
        // Deliberately exercise the multiplication branch with its nearest foundations excluded.
        for(Catalog.Skill skill:Learning.diagnosticScope(s.profile))if(skill.grade==4&&!skill.id.equals("mul22"))s.profile.excluded.add(skill.id);
        Learning.beginDiagnostic(s,r);assertEquals(2,s.session.target);
        answer(s,r,true,false);answer(s,r,true,false);Diagnosis.Plan p=Diagnosis.currentPlan(s);
        for(String excluded:s.profile.excluded)assertFalse(p.checks.containsKey(excluded));
        assertTrue(p.checks.values().stream().anyMatch(probe->probe.followUp&&p.scope.contains(probe.skillId)));
        for(Diagnosis.Probe probe:p.checks.values())if(probe.followUp){assertTrue(p.scope.contains(probe.skillId));assertTrue(Curriculum.grade(Catalog.get(probe.skillId),p.curriculum)<=3);}
    }
    @Test public void sharedFoundationsAreNotReservedOrQueuedMoreThanOnce(){
        Random r=new Random(5);Learning.State s=focused("rational",8,1,r);answer(s,r,true,false);answer(s,r,true,false);Diagnosis.Plan p=Diagnosis.currentPlan(s);
        assertTrue(p.checks.keySet().containsAll(List.of("fracAdd","fracSub","fracMul","fracDiv","tables","divide","sub100")));
        assertEquals(p.pending.size(),new HashSet<>(p.pending).size());Map<String,Integer> scheduled=new HashMap<>();for(Diagnosis.Probe probe:s.session.diagnosticRun.scheduled){scheduled.merge(probe.skillId,1,Integer::sum);assertFalse(p.pending.contains(probe.skillId));}
        for(int count:scheduled.values())assertEquals(2,count);
        assertTrue(p.pending.size()>0);while(!s.session.finished)answer(s,r,false,false);assertTrue(s.session.completed<=8);
        Learning.Session next=Learning.continueDiagnostic(s,r);assertSame(p,next.diagnosticRun.plan);assertTrue(next.target<=4);
    }
    @Test public void oldPlanRefreshPreservesPublishedQuestionAndReservations()throws Exception{
        Random r=new Random(12);Learning.State s=focused("mul22",4,2,r);answer(s,r,true,false);answer(s,r,true,false);
        Learning.ensureQuestion(s,new Generator(r),DAY);String q=s.session.question.id;s.session.answers.set(0,"17");Diagnosis.Plan p=Diagnosis.currentPlan(s);
        // An older catalog left one branch absent; preserve the currently published branch.
        String absent=s.session.question.skillId.equals("mul2")?"add1000":"mul2";
        int removed=(int)s.session.diagnosticRun.scheduled.stream().filter(probe->probe.skillId.equals(absent)).count();
        s.session.diagnosticRun.scheduled.removeIf(probe->probe.skillId.equals(absent));s.session.diagnosticRun.phaseSize-=removed;s.session.target-=removed;p.checks.remove(absent);p.pending.remove(absent);int target=s.session.target;
        s=copy(s);Learning.continueDiagnostic(s,r);assertEquals(q,s.session.question.id);assertEquals("17",s.session.answers.get(0));assertEquals(target,s.session.target);p=Diagnosis.currentPlan(s);assertTrue(p.pending.contains(absent));assertTrue(p.checks.get(absent).followUp);
        Learning.continueDiagnostic(s,r);assertEquals(1,Collections.frequency(p.pending,absent));assertEquals(q,Learning.ensureQuestion(s,new Generator(r),DAY).id);
    }
    @Test public void currentIndependentSuccessStopsAnOldFailureFromOpeningDeeperChecks(){
        Random r=new Random(12);Learning.State s=focused("mul22",4,2,r);answer(s,r,true,false);answer(s,r,true,false);Diagnosis.Plan p=Diagnosis.currentPlan(s);
        // Historical label and current review evidence intentionally disagree.
        Diagnosis.Probe old=p.checks.get("mul2");old.outcome=Diagnosis.Outcome.NEEDS_PRACTICE;Learning.Progress progress=s.progress("mul2");progress.review.recent=true;progress.review.cleanRun=2;
        Learning.continueDiagnostic(s,r);assertFalse(p.checks.containsKey("tables"));assertFalse(progress.weak());assertFalse(Diagnosis.practiceSkills(s,p).contains("mul2"));assertTrue(Diagnosis.practiceSkills(s,p).contains("mul22"));
    }
    @Test public void suggestionsPrioritizeBothKnownFractionGapsWithoutInferringUnseenFailures(){
        Learning.State s=student(6,1);Random r=new Random(28);weak(s,"fracAdd",r);weak(s,"lcm",r);weak(s,"reduce",r);
        List<String> ids=Learning.suggested(s,DAY);assertTrue(ids.indexOf("lcm")>=0&&ids.indexOf("lcm")<ids.indexOf("fracAdd"));assertTrue(ids.indexOf("reduce")>=0&&ids.indexOf("reduce")<ids.indexOf("fracAdd"));
        assertEquals(Set.of("fracAdd","lcm","reduce"),s.needsPractice);assertFalse(s.progress.containsKey("gcd"));
        s.profile.excluded.add("lcm");assertFalse(Learning.suggested(s,DAY).contains("lcm"));assertEquals(6,s.profile.grade);
    }
    @Test public void voluntaryPracticeDoesNotAcquirePrerequisiteUnlocksOrExtraQuestions(){
        Learning.State s=student(2,1);Random r=new Random(66);Learning.Session practice=Learning.beginPractice(s,"homework",List.of("fracAdd"),10,false,r);
        assertEquals(List.of("fracAdd"),practice.selected);for(int i=0;i<10;i++){assertEquals("fracAdd",Learning.ensureQuestion(s,new Generator(r),DAY).skillId);answer(s,r,false,false);}
        assertEquals(10,practice.completed);assertTrue(practice.finished);assertEquals(2,s.profile.grade);
    }
    @Test public void dailyGivesTwoFreshFoundationQuestionsThenReturnsToCurrentUnit(){
        Learning.State s=student(4,2);s.profile.currentSkill="mul22";Learning.Progress foundation=s.progress("mul2");Review.Track track=Review.track(foundation);track.pending=true;track.dueDay=DAY.toEpochDay();
        Random r=new Random(71);Learning.Session daily=Learning.beginPractice(s,"daily",List.of("mul2","mul22"),5,false,r);
        assertEquals(List.of("mul2","mul2","mul22"),daily.queue.subList(0,3));
        Question first=Learning.ensureQuestion(s,new Generator(r),DAY);assertEquals("mul2",first.skillId);Learning.finishQuestion(s,false,DAY,r);
        Question second=Learning.ensureQuestion(s,new Generator(r),DAY);assertEquals("mul2",second.skillId);assertNotEquals(first.signature(),second.signature());Learning.finishQuestion(s,false,DAY,r);
        assertEquals("mul22",Learning.ensureQuestion(s,new Generator(r),DAY).skillId);assertEquals(4,s.profile.grade);assertEquals("mul22",s.profile.currentSkill);
    }
    private Learning.State copy(Learning.State s)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(s);return (Learning.State)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();}
}
