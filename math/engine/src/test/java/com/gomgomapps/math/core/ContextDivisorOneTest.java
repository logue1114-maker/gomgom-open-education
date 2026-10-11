package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ContextDivisorOneTest {
 @Test public void fullNamibiaContextDomainIncludesOneWithoutLeakingToEngland(){
  Checker checker=new Checker();String pack="na-nied-primary-2024-v1";
  for(String id:List.of(RemainderContexts.GROUPS,RemainderContexts.LENGTH)){
   CurriculumLimits limits=GlobalCurriculum.limits(pack,id,3);
   assertEquals(1,limits.contextDivisorMinimum());assertEquals(10,limits.contextDivisorMaximum());
   for(int n=10;n<=100;n++)for(int d=1;d<=10;d++){
    Question q=new Question(id,String.format(Locale.ROOT,RemainderContexts.template(id),n,d),"","poison");
    q.expression="poison";String expected=id.equals(RemainderContexts.GROUPS)?String.valueOf(n/d):n+"/"+d;
    if(id.equals(RemainderContexts.LENGTH)){q.answerFormat="fraction";q.decimal=true;}
    assertArrayEquals(new int[]{n,d,n/d,n%d},RemainderContexts.read(q));
    // Poisoned answer metadata is irrelevant to the public-condition checker/help.
    assertTrue(checker.check(q,List.of(),List.of(expected)).correct());
    RemainderContexts.attach(q);HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());
    assertTrue(h.step(0).accepts(String.valueOf(n)));assertTrue(h.step(1).accepts(String.valueOf(d)));
    assertTrue(h.step(2).accepts(String.valueOf(n/d)));assertTrue(h.step(3).accepts(String.valueOf(n%d)));
    assertTrue(h.step(h.size()-1).accepts(expected));
    q.answers=new String[]{expected};assertTrue(limits.allows(q));
    if(d==1){assertFalse(checker.check(q,List.of(),List.of(String.valueOf(n+1))).correct());assertFalse(h.step(3).accepts("1"));}
   }
   Generator generator=new Generator(new Random(223));List<String> recent=new ArrayList<>();Set<Integer> divisors=new HashSet<>();
   for(int i=0;i<910;i++){Question q=generator.next(id,recent,false,limits);int[] v=RemainderContexts.read(q);assertNotNull(v);assertTrue(v[0]>=10&&v[0]<=100);assertTrue(limits.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());divisors.add(v[1]);}
   assertEquals(new HashSet<>(List.of(1,2,3,4,5,6,7,8,9,10)),divisors);
   for(int grade:List.of(5,6)){CurriculumLimits english=GlobalCurriculum.limits("england-primary-2021-v1",id,grade);assertEquals(grade==5?2:10,english.contextDivisorMinimum());}
  }
  Question invalid=new Question(RemainderContexts.GROUPS,String.format(Locale.ROOT,RemainderContexts.template(RemainderContexts.GROUPS),10,0),"","10");assertNull(RemainderContexts.read(invalid));
 }
 @Test public void explicitLowerBoundIsEnforcedAndContradictoryRangeIsEmpty(){
  CurriculumLimits limits=new CurriculumLimits("contextDivisorMinimum=3;contextDivisorMaximum=5");
  for(int d=1;d<=6;d++){Question q=new Question(RemainderContexts.GROUPS,String.format(Locale.ROOT,RemainderContexts.template(RemainderContexts.GROUPS),20,d),"",String.valueOf(20/d));assertEquals(d>=3&&d<=5,limits.allows(q));}
  try{RemainderContexts.next(RemainderContexts.GROUPS,new Random(1),new CurriculumLimits("contextDivisorMinimum=5;contextDivisorMaximum=3"),Map.of());fail("empty range accepted");}catch(IllegalArgumentException expected){}
 }
}
