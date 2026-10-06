package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class CountingPatternsTest {
 @Test public void hundredVisibleProblemsPerUnitHaveIndependentAnswersAndLocalFeedback(){
  Generator g=new Generator(new Random(20261006061L));
  for(String id:List.of("groupedCount","numberSteps")){
   Set<String> seen=new LinkedHashSet<>();Set<Integer> answers=new HashSet<>();
   for(int i=0;i<100;i++){
    Question q=g.next(id,seen,false,GlobalCurriculum.limits(BrazilCurriculumTest.PACK,id,1));assertTrue(q.prompt,seen.add(q.signature()));int answer;
    if(id.equals("groupedCount")){double[] v=q.diagram.values;answer=(int)(v[0]*v[1]+v[2]);assertTrue(v[0]>=2&&v[0]<=5&&v[1]>=1&&v[1]<=10&&v[2]>=0&&v[2]<v[0]);assertEquals(5,q.studyGuide.frames.size());}
    else {String[] terms=q.prompt.split(" → ");int first=Integer.parseInt(terms[0]),step=Integer.parseInt(terms[1])-first,blank=Arrays.asList(terms).indexOf("□");answer=first+blank*step;assertTrue(Math.abs(step)>=1&&Math.abs(step)<=5);for(int k=0;k<5;k++)if(k!=blank)assertEquals(first+k*step,Integer.parseInt(terms[k]));assertFalse(q.prompt.contains("×"));assertEquals(2,q.studyGuide.frames.size());}
    assertEquals(String.valueOf(answer),q.answers[0]);assertTrue(new Checker().check(q,List.of(),List.of(""+answer)).correct());assertFalse(new Checker().check(q,List.of(),List.of(""+(answer+1))).correct());answers.add(answer);
    HelpPlan plan=HelpPlan.forQuestion(q);assertFalse(plan.canTransfer());for(int j=0;j<plan.size();j++){String expected=q.studyGuide.frames.get(j).expected;assertTrue(plan.step(j).accepts(expected));assertFalse(plan.step(j).accepts(""+(Integer.parseInt(expected)+1)));}
   }assertTrue(id+answers,answers.size()>15);
  }
 }
 @Test public void newGradeOnePlacementsStayInGradeAndPreviousGradeDiagnosis(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,BrazilCurriculumTest.PACK);p.grade=2;
  for(String id:List.of("groupedCount","numberSteps")){assertTrue(GlobalCurriculum.pack(p).inGrade(id,1));assertFalse(GlobalCurriculum.pack(p).inGrade(id,2));assertTrue(GlobalCurriculum.scope(p).stream().anyMatch(s->s.id.equals(id)));}
 }
 @Test public void actualChoicesShufflePositionsAndKeepDifferentVisibleNumbers(){
  Generator g=new Generator(new Random(20261006062L));for(String id:List.of("groupedCount","numberSteps")){int[] positions=new int[4];
   for(int i=0;i<200;i++){Question q=g.next(id,List.of(),true);assertEquals(4,q.choices.size());assertEquals(4,new HashSet<>(q.choices).size());assertEquals(q.answers[0],q.choices.get(q.correctChoice));positions[q.correctChoice]++;for(String value:q.choices)assertTrue(Integer.parseInt(value)>=0&&Integer.parseInt(value)<=(id.equals("groupedCount")?50:99));}
   for(int n:positions)assertTrue(Arrays.toString(positions),n>25&&n<80);
  }
 }
}
