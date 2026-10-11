package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class ThreeDigitStepsTest {
 private static int nonzeroDigits(int n){return (int)Integer.toString(n).chars().filter(c->c!='0').count();}
 @Test public void completeGeneratedDomainsUsePublicArithmetic(){
  Checker checker=new Checker();
  for(String id:List.of(ThreeDigitSteps.ADD,ThreeDigitSteps.SUB)){
   int independent=0;Set<String> seen=new HashSet<>();
   for(int a=id.equals(ThreeDigitSteps.ADD)?0:100;a<=500;a++)for(int b=10;b<=500;b++){
    int result=id.equals(ThreeDigitSteps.ADD)?a+b:a-b;if(result<0||result>500||nonzeroDigits(b)<2)continue;independent++;
    Question q=ThreeDigitSteps.make(id,a,b);assertTrue(seen.add(q.signature()));String[] words=q.prompt.split(" ");int first=Integer.parseInt(words[0]),second=Integer.parseInt(words[2]);int expected=words[1].equals("+")?first+second:first-second;
    q.answers=new String[]{"poison"};q.expression="poison";assertTrue(checker.check(q,List.of(),List.of(""+expected)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of(),List.of(""+(expected+1))).status);
   }
   assertEquals(independent,ThreeDigitSteps.count(id,500));assertEquals(independent,seen.size());
   Set<String> indexes=new HashSet<>();for(int index=0;index<independent;index+=127){Question q=ThreeDigitSteps.at(id,500,index);assertTrue(seen.contains(q.signature()));assertTrue(indexes.add(q.signature()));}
   assertNotNull(ThreeDigitSteps.read(ThreeDigitSteps.at(id,500,independent-1)));
  }
  assertNotNull(ThreeDigitSteps.read(ThreeDigitSteps.make(ThreeDigitSteps.ADD,0,111)));assertNotNull(ThreeDigitSteps.read(ThreeDigitSteps.make(ThreeDigitSteps.ADD,99,11)));assertNull(ThreeDigitSteps.read(new Question(ThreeDigitSteps.SUB,"99 − 11","","88")));
  for(String invalid:List.of("501 − 11","100 + 9","490 + 11","100 − 101","100 + 11 = 111")){Question q=new Question(ThreeDigitSteps.ADD,invalid,"","111");assertNull(ThreeDigitSteps.read(q));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("111")).status);}
 }
 @Test public void gradeThreeFreshSupplyBlankSubtotalsAndExistingLargeMinimum(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();
  for(String id:List.of(ThreeDigitSteps.ADD,ThreeDigitSteps.SUB)){
   assertTrue(pack.inGrade(id,3));assertFalse(pack.inGrade(id,2));assertFalse(pack.inGrade(id,4));assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));
   CurriculumLimits limits=GlobalCurriculum.limits(pack.id,id,3);Generator g=new Generator(new Random(205));List<String> recent=new ArrayList<>();Set<Integer> secondWidths=new HashSet<>();
   for(int n=0;n<300;n++){
    Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(limits.allows(q));String[] words=q.prompt.split(" ");int a=Integer.parseInt(words[0]),b=Integer.parseInt(words[2]);assertTrue(a>=(id.equals(ThreeDigitSteps.ADD)?0:100)&&a<=500);assertTrue(nonzeroDigits(b)>=2);secondWidths.add(words[2].length());
    q.answers=new String[]{"poison"};HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));int stage=0,running=a;assertTrue(h.step(stage++).accepts(""+a));
    for(int unit=100;unit>=1;unit/=10){int part=b/unit%10*unit;if(part==0)continue;assertTrue(h.step(stage++).accepts(""+part));running+=id.equals(ThreeDigitSteps.ADD)?part:-part;assertTrue(running>=0&&running<=500);assertTrue(h.step(stage++).accepts(""+running));}
    assertEquals(stage,h.size());assertEquals(1+2*nonzeroDigits(b),h.size());for(int i=0;i<h.size();i++)assertFalse(h.step(i).before.matches(".*[0-9].*"));
   }
   assertEquals(Set.of(2,3),secondWidths);
  }
  assertTrue(GlobalCurriculum.limits(pack.id,ThreeDigitSteps.SUB,3).allows(ThreeDigitSteps.make(ThreeDigitSteps.SUB,499,499)));
  assertNull(LargeMentalDecomposition.read(new Question(LargeMentalDecomposition.ADD,"100 + 111","","211")));
 }
}
