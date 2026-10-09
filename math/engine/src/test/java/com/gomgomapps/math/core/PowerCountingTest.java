package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PowerCountingTest {
 @Test public void allPowersBoundariesArbitraryStartsAndPublicRelations(){
  assertEquals(6000006,PowerCounting.count(1000000));
  for(String id:List.of(PowerCounting.FORWARD,PowerCounting.BACKWARD))for(int step:new int[]{10,100,1000,10000,100000,1000000})for(int start:new int[]{0,1,step-1,step,999999,1000000}){
   Question q=PowerCounting.make(id,start,step);int delta=id.equals(PowerCounting.FORWARD)?step:-step;int[] v={start,step,start+delta,start+2*delta};assertArrayEquals(v,PowerCounting.read(q));q.answers=new String[]{"999","999"};q.expression="999";
   Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of(""+v[2],""+v[3])).correct());assertEquals(0,c.check(q,List.of(),List.of(""+(v[2]+1),""+v[3])).index);assertEquals(1,c.check(q,List.of(),List.of(""+v[2],""+(v[3]+1))).index);
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(4,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));for(int i=0;i<4;i++){assertTrue(h.step(i).accepts(""+v[i]));assertFalse(h.step(i).before.matches(".*[0-9].*"));}
  }
 }
 @Test public void realMillionSupplyStaysFreshAndSmallSupplyExhaustsBeforeOldest(){
  Generator g=new Generator(new Random(154));for(String id:List.of(PowerCounting.FORWARD,PowerCounting.BACKWARD)){
   List<String> recent=new ArrayList<>();Set<Integer> steps=new HashSet<>();boolean unaligned=false;CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,5);
   for(int i=0;i<500;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());int[] v=PowerCounting.read(q);assertNotNull(v);steps.add(v[1]);unaligned|=v[0]%v[1]!=0;assertTrue(v[0]<=1000000);assertTrue(q.choices.isEmpty());}assertEquals(6,steps.size());assertTrue(unaligned);
   limits=new CurriculumLimits("wholeMaximum=100");recent.clear();for(int i=0;i<202;i++){Question q=g.next(id,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());}assertEquals(recent.get(0),g.next(id,recent,false,limits).signature());
  }
 }
 @Test public void indicesMalformedStatementsAndReviewedScope(){
  for(String id:List.of(PowerCounting.FORWARD,PowerCounting.BACKWARD)){for(int block=0;block<6;block++){int[] low=PowerCounting.read(PowerCounting.at(id,block*1000001,1000000)),high=PowerCounting.read(PowerCounting.at(id,(block+1)*1000001-1,1000000));assertEquals(0,low[0]);assertEquals(1000000,high[0]);assertEquals(low[1],high[1]);}
   assertFalse(Curriculum.inCurriculum(Catalog.get(id),2022));var pack=GlobalCurriculum.packs("GB").stream().filter(x->x.id.equals("england-primary-2021-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(id,5));assertFalse(pack.inGrade(id,4));
  }
  Question q=PowerCounting.make(PowerCounting.BACKWARD,1,10);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("-9/1","-19")).status);q.prompt="3씩 뒤로 두 번 세세요.\n1 → □ → □";assertNull(PowerCounting.read(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("-2","-5")).status);
 }
}
