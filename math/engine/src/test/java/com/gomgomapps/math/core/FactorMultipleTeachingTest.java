package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.util.regex.*;import static org.junit.Assert.*;
public class FactorMultipleTeachingTest {
 private int solve(String id,String prompt){Matcher m=Pattern.compile("\\d+").matcher(prompt);List<Integer> n=new ArrayList<>();while(m.find())n.add(Integer.parseInt(m.group()));int a=n.get(0),b=n.get(1);
  if(id.equals("el_multiple"))return a*b;
  if(id.equals("el_divisor")){int k=0;for(int d=1;d<=a;d++)if(a%d==0&&++k==b)return d;}
  if(id.equals("el_common_divisor")){int k=0;for(int d=1;d<=Math.min(a,b);d++)if(a%d==0&&b%d==0&&++k==2)return d;}
  if(id.equals("el_common_multiple")){int k=0;for(int v=1;v<=a*b*n.get(2);v++)if(v%a==0&&v%b==0&&++k==n.get(2))return v;}throw new AssertionError(prompt);
 }
 @Test public void actualModuleAndNormalGeneratorBothUseThePublicSearch(){
  Random random=new Random(20261006411L);Generator generator=new Generator(random);for(String id:List.of("el_divisor","el_multiple","el_common_divisor","el_common_multiple"))for(int i=0;i<500;i++){
   Question q=i%2==0?ElementaryBasics.create(Catalog.get(id),random):generator.next(id,List.of(),false);HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());int answer=solve(id,q.prompt);assertEquals(Integer.toString(answer),q.answers[0]);assertEquals(Integer.toString(answer),q.studyGuide.frames.get(plan.size()-1).expected);
   for(int j=0;j<plan.size();j++){StudyGuide.Frame f=q.studyGuide.frames.get(j);assertTrue(plan.step(j).accepts(f.expected));assertFalse(plan.step(j).accepts(Integer.toString(Integer.parseInt(f.expected)+1)));}
  }
 }
 @Test public void recentHundredCommonFactorsNeverRepeatWhileUnseenQuestionsRemain(){
  Generator g=new Generator(new Random(202610064515L));List<String> recent=new ArrayList<>();Set<String> all=new HashSet<>();for(int i=0;i<1000;i++){Question q=g.next("el_common_divisor",recent,i%2==0);assertFalse(recent.contains(q.signature()));assertEquals(Integer.toString(solve(q.skillId,q.prompt)),q.answers[0]);recent.add(q.signature());all.add(q.signature());if(recent.size()>100)recent.remove(0);}assertEquals(110,all.size());
 }
 @Test public void exhaustedSmallDomainChoosesTheOldestWithoutChangingItsContract(){
  Random random=new Random(1);Map<String,Integer> previous=new LinkedHashMap<>();Question oldest=null;for(int i=0;i<110;i++){Question q=CommonFactorSupply.next(random,CurriculumLimits.NONE,previous);if(i==0)oldest=q;assertFalse(previous.containsKey(q.signature()));previous.put(q.signature(),i);}Question repeated=CommonFactorSupply.next(random,CurriculumLimits.NONE,previous);assertEquals(oldest.signature(),repeated.signature());assertFalse(HelpPlan.forQuestion(repeated).canTransfer());
 }
 @Test public void commonMultipleDoesNotStartWithAnUnenteredLeastCommonMultiple(){
  Question q=new Question("el_common_multiple","4과 6의 3번째 공배수는?","999*3","999");FactorMultipleTeaching.attach(q);assertEquals("6 × 1 = ",q.studyGuide.frames.get(0).before);assertEquals("6",q.studyGuide.frames.get(0).expected);assertEquals("36",q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);assertEquals("999",q.answers[0]);
 }
 @Test public void divisorScansNonFactorsAsWellAsFactorsWithoutReverseAnswerHints(){
  Question q=new Question("el_divisor","25의 약수 중 2번째로 작은 수는?","25/999","999");FactorMultipleTeaching.attach(q);assertEquals(16,q.studyGuide.frames.size());assertEquals("25 ÷ 2 = ",q.studyGuide.frames.get(3).before);assertEquals("1",q.studyGuide.frames.get(5).expected);assertEquals("5",q.studyGuide.frames.get(15).expected);assertFalse(q.studyGuide.transfer);
 }
 @Test public void commonDivisorChecksBothNumbersAndIncludesRejectedCandidates(){
  Question q=new Question("el_common_divisor","14과 21의 공약수 중 두 번째로 작은 수는?","14/999","999");FactorMultipleTeaching.attach(q);assertEquals(43,q.studyGuide.frames.size());assertEquals("21 ÷ 2 = ",q.studyGuide.frames.get(9).before);assertEquals("1",q.studyGuide.frames.get(11).expected);assertEquals("7",q.studyGuide.frames.get(42).expected);assertFalse(q.studyGuide.transfer);
 }
}
