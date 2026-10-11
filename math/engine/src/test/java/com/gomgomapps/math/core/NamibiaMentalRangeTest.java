package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class NamibiaMentalRangeTest {
 private static final String NA="na-nied-primary-2024-v1";
 @Test public void boundedAdditionEnumeratesEveryPermittedPublicPair(){
  for(int min=0;min<=2;min++)for(int max=min;max<=6;max++)for(int cap=0;cap<=2*max;cap++){
   Set<String> expected=new HashSet<>(),actual=new HashSet<>();
   for(int a=min;a<=max;a++)for(int b=min;b<=max;b++)if(a+b<=cap)expected.add(a+" + "+b);
   long count=LargeMentalDecomposition.boundedCount(LargeMentalDecomposition.ADD,min,max,cap);assertEquals(expected.size(),count);
   for(long i=0;i<count;i++)assertTrue(actual.add(LargeMentalDecomposition.bounded(LargeMentalDecomposition.ADD,min,max,cap,i).prompt));
   assertEquals(expected,actual);
  }
 }
 @Test public void normalGradeSixRangeAndIndependentPublicArithmetic(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals(NA)).findFirst().orElseThrow();
  Generator generator=new Generator(new Random(243));
  for(String id:List.of(LargeMentalDecomposition.ADD,LargeMentalDecomposition.SUB)){
   assertTrue(pack.inGrade(id,6));CurriculumLimits limits=GlobalCurriculum.limits(NA,id,6);List<String> recent=new ArrayList<>();
   for(int i=0;i<300;i++){
    Question q=generator.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());
    String[] words=q.prompt.split(" ");int a=Integer.parseInt(words[0]),b=Integer.parseInt(words[2]);int answer=id.equals(LargeMentalDecomposition.ADD)?a+b:a-b;
    assertTrue(a>=0&&a<=1000000&&b>=0&&b<=1000000&&answer>=0&&answer<=1000000);verify(q,a,b,answer);
   }
  }
 }
 private void verify(Question q,int a,int b,int answer){
  q.answers=new String[]{"poison"};q.expression="poison";Checker checker=new Checker();
  assertTrue(checker.check(q,List.of(),List.of(""+answer)).correct());assertFalse(checker.check(q,List.of(),List.of(""+(answer+1))).correct());
  HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());int stage=0,running=a;assertTrue(h.step(stage++).accepts(""+a));
  for(int power=1000000;power>=1;power/=10){int part=b/power%10*power;if(part==0&&!(b==0&&power==1))continue;assertTrue(h.step(stage++).accepts(""+part));running+=q.skillId.equals(LargeMentalDecomposition.ADD)?part:-part;assertTrue(h.step(stage++).accepts(""+running));}
  assertEquals(answer,running);assertEquals(stage,h.size());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
 }
 @Test public void zeroSmallAndMillionBoundariesHaveRealBlankCalculations(){
  for(int[] v:new int[][]{{0,0},{0,1},{999,1},{1000000,0},{0,1000000},{1000000,1000000},{1000000,1}}){
   for(String id:List.of(LargeMentalDecomposition.ADD,LargeMentalDecomposition.SUB)){
    if(id.equals(LargeMentalDecomposition.SUB)&&v[0]<v[1])continue;
    Question q=LargeMentalDecomposition.make(id,v[0],v[1]);int answer=id.equals(LargeMentalDecomposition.ADD)?v[0]+v[1]:v[0]-v[1];
    assertEquals(answer<=1000000,GlobalCurriculum.limits(NA,id,6).allows(q));verify(q,v[0],v[1],answer);
   }
  }
  assertEquals(500001500001L,LargeMentalDecomposition.boundedCount(LargeMentalDecomposition.ADD,0,1000000,1000000));
  assertFalse(GlobalCurriculum.limits("england-primary-2021-v1",LargeMentalDecomposition.ADD,5).allows(LargeMentalDecomposition.make(LargeMentalDecomposition.ADD,0,1000)));
 }
}
