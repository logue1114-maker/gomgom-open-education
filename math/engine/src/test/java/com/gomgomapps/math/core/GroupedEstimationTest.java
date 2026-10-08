package com.gomgomapps.math.core;
import java.util.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class GroupedEstimationTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void guessesAreNeverScoredAndAllPublicCountsAreCheckedIndependently(){
  Generator gen=new Generator(new Random(48));
  for(int grade:List.of(2,3)){
   CurriculumLimits limits=GlobalCurriculum.limits(NA,GroupedEstimation.ID,grade);int max=grade==2?20:30;List<String> seen=new ArrayList<>();Set<Integer> counts=new HashSet<>();
   for(int i=0;i<max*4;i++){
    Question q=gen.next(GroupedEstimation.ID,seen,true,limits);assertFalse(seen.contains(q.signature()));seen.add(q.signature());String identity=q.signature();int n=CollectionGrouping.points(q,0).length;assertTrue(n>=1&&n<=max);counts.add(n);assertTrue(q.choices.isEmpty());
    assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of(""+n)).status);
    Learning.Session session=new Learning.Session();session.question=q;session.answers.add(i%2==0?"0":"999");session.hadError=false;session.durationMs=3600000;session.studyMs=2468000;
    String guess=session.answers.get(0);assertTrue(GroupedEstimation.beginCounting(session));assertEquals(guess,q.learnerEstimate);assertEquals("",session.answers.get(0));assertFalse(session.hadError);assertEquals(2468000,session.studyMs);assertEquals(identity,q.signature());assertEquals(n,CollectionGrouping.points(q,5).length);
    assertTrue(new Checker().check(q,List.of(),List.of(""+n)).correct());Checker.Result wrong=new Checker().check(q,List.of(),List.of(""+(n+1)));assertEquals(Checker.Status.WRONG_ANSWER,wrong.status);assertEquals(0,wrong.index);
    q.answers=new String[]{"99999"};GroupedEstimation.attach(q);assertEquals(List.of(""+(n/5),""+(n%5),""+n),q.studyGuide.frames.stream().map(f->f.expected).toList());assertFalse(HelpPlan.forQuestion(q).canTransfer());assertEquals(guess,q.learnerEstimate);
   }
   assertEquals(max,counts.size());assertEquals(seen.get(0),gen.next(GroupedEstimation.ID,seen,false,limits).signature());
  }
 }
 @Test public void ownGuessCountingDraftAndGroupingSurviveSnapshotAndSerialization()throws Exception{
  Learning.State state=new Learning.State();state.session=new Learning.Session();Learning.Session s=state.session;s.question=new Generator(new Random(481)).next(GroupedEstimation.ID,List.of(),false,GlobalCurriculum.limits(NA,GroupedEstimation.ID,2));String identity=s.question.signature();s.answers.add("00100");assertTrue(GroupedEstimation.beginCounting(s));s.answers.set(0,"8");CollectionGrouping.group(s.question,0);s.studyMs=1234000;
  Learning.State snapshot=LearningSnapshot.capture(state);assertEquals("00100",snapshot.session.question.learnerEstimate);assertEquals(List.of("8"),snapshot.session.answers);assertEquals(0,CollectionGrouping.selected(snapshot.session.question));assertEquals(1234000,snapshot.session.studyMs);assertEquals(identity,snapshot.session.question.signature());
  ByteArrayOutputStream data=new ByteArrayOutputStream();new ObjectOutputStream(data).writeObject(snapshot);Learning.State restored=(Learning.State)new ObjectInputStream(new ByteArrayInputStream(data.toByteArray())).readObject();assertEquals("00100",restored.session.question.learnerEstimate);assertEquals(List.of("8"),restored.session.answers);assertTrue(GroupedEstimation.counting(restored.session.question));assertFalse(GroupedEstimation.beginCounting(restored.session));
 }
 @Test public void invalidInputsAndPriorGradeScopeDoNotBecomeMathErrors(){Learning.Session s=new Learning.Session();s.question=new Generator(new Random(482)).next(GroupedEstimation.ID,List.of(),false);s.answers.add("-2");assertFalse(GroupedEstimation.beginCounting(s));assertNull(s.question.learnerEstimate);assertEquals(List.of("-2"),s.answers);s.answers.set(0,"");assertFalse(GroupedEstimation.beginCounting(s));Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,NA);p.grade=2;assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(skill->skill.id.equals(GroupedEstimation.ID)));p.grade=3;assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(skill->skill.id.equals(GroupedEstimation.ID)));}
}
