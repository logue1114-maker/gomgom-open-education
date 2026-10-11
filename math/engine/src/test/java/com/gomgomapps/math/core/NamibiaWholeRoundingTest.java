package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class NamibiaWholeRoundingTest {
 private static final String NA="na-nied-primary-2024-v1";
 private static final int[] UNITS={10,100,1000,10000,100000};
 private void verify(Question q){
  RoundingRelations.Givens g=RoundingRelations.read(q);assertNotNull(g);int n=Integer.parseInt(g.first()),unit=g.unit().intValueExact(),base=n/unit*unit,digit=n/(unit/10)%10,extra=digit>=5?unit:0,answer=base+extra;
  assertTrue(n>=0&&n<=1000000);assertTrue(answer<=1000000);q.answers=new String[]{"poison"};Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of(""+answer)).correct());assertFalse(c.check(q,List.of(),List.of(""+(answer+1))).correct());HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());assertEquals(7,h.size());List<String> values=List.of(""+n,""+unit,""+digit,digit>=5?"raise":"keep",""+base,""+extra,""+answer);for(int i=0;i<values.size();i++)assertTrue("step "+i,h.step(i).accepts(values.get(i)));assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
 }
 @Test public void actualSupplyAllFiveUnitsAndFreshPublicHelp(){
  CurriculumLimits limits=GlobalCurriculum.limits(NA,"el_round",6);Generator g=new Generator(new Random(253));List<String> recent=new ArrayList<>();Set<Integer> seen=new HashSet<>();for(int i=0;i<150;i++){Question q=g.next("el_round",recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());seen.add(RoundingRelations.read(q).unit().intValueExact());verify(q);}assertEquals(Set.of(10,100,1000,10000,100000),seen);
 }
 @Test public void endpointsLowNumbersHalfAndCarryAreInsideRealCurriculum(){
  CurriculumLimits limits=GlobalCurriculum.limits(NA,"el_round",6);assertEquals(0,limits.givenMinimum(200000));assertEquals(1000000,limits.wholeMaximum(999999));
  for(int index=0;index<UNITS.length;index++){final int selected=index;for(int value:List.of(0,1,UNITS[index]/2-1,UNITS[index]/2,UNITS[index]-1,1000000-UNITS[index]/2,999999,1000000)){Random boundary=new Random(){int calls;@Override public int nextInt(int bound){return calls++==0?selected:value;}};Question q=ElementaryBasics.create(Catalog.get("el_round"),boundary,limits);assertEquals(value,Integer.parseInt(RoundingRelations.read(q).first()));assertTrue(limits.allows(q));verify(q);}}
  assertEquals(99999,GlobalCurriculum.limits(NA,"el_round",5).wholeMaximum(0));assertEquals(999999,GlobalCurriculum.limits(NA,"el_round",7).wholeMaximum(0));
 }
}
