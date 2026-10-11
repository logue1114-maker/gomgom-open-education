package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class WholePowerTenSupplyTest {
 @Test public void normalSessionsReachEveryScaleWithoutRepeatingSmallDomains(){
  String pack="na-nied-primary-2024-v1";
  for(String id:List.of(WholePowerTen.MUL,WholePowerTen.DIV)){
   Map<Integer,Integer> totals=new HashMap<>();for(int seed=0;seed<24;seed++){Generator g=new Generator(new Random(seed));List<String> recent=new ArrayList<>();Set<Integer> scales=new HashSet<>();for(int i=0;i<100;i++){Question q=g.next(id,recent,false,GlobalCurriculum.limits(pack,id,6));assertFalse(recent.contains(q.signature()));recent.add(q.signature());int power=WholePowerTen.read(q)[1];scales.add(power);totals.merge(power,1,Integer::sum);}assertEquals(6,scales.size());}
   assertEquals(6,totals.size());assertTrue(totals.get(1000)>100);assertTrue(totals.get(10000)>100);assertTrue(totals.get(100000)>100);if(id.equals(WholePowerTen.MUL))assertEquals(Integer.valueOf(48),totals.get(1000000));else assertTrue(totals.get(1000000)>100);
  }
 }
 @Test public void exhaustedMillionScaleDoesNotRepeatWhileOtherScalesRemain(){
  Map<String,Integer> recent=new LinkedHashMap<>();for(int a=0;a<=1;a++){Question q=WholePowerTen.make(WholePowerTen.MUL,a,1000000);recent.put(q.signature(),recent.size());}
  for(int i=0;i<200;i++){Question q=WholePowerTen.next(WholePowerTen.MUL,new Random(i),CurriculumLimits.NONE,recent);assertFalse(recent.containsKey(q.signature()));assertNotEquals(1000000,WholePowerTen.read(q)[1]);recent.put(q.signature(),recent.size());}
 }
 @Test public void narrowLimitsKeepTheAllowedScalesAndOnlyRepeatAfterFullExhaustion(){
  CurriculumLimits limits=new CurriculumLimits("maxGiven=10;maxResult=10;nonnegative=true");Map<String,Integer> recent=new LinkedHashMap<>();Random r=new Random(239);
  for(int i=0;i<2;i++){Question q=WholePowerTen.next(WholePowerTen.MUL,r,limits,recent);assertTrue(limits.allows(q));assertEquals(10,WholePowerTen.read(q)[1]);assertFalse(recent.containsKey(q.signature()));recent.put(q.signature(),i);}
  Question repeated=WholePowerTen.next(WholePowerTen.MUL,r,limits,recent);assertTrue(recent.containsKey(repeated.signature()));assertTrue(limits.allows(repeated));
 }
}
