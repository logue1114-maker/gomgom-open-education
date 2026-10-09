package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class MoneyBasicStoriesTest {
 @Test public void sampledIndexedConditionsKeepValidPublicPenceAndExactArithmetic(){
  for(String id:List.of(MoneyBasicStories.TOTAL,MoneyBasicStories.CHANGE)){
   int count=MoneyBasicStories.count(id);Set<String> sampled=new HashSet<>();
   for(int index=0;index<count;index+=997){Question q=MoneyBasicStories.at(id,index);int[] v=MoneyBasicStories.read(q);assertNotNull(v);assertTrue(sampled.add(q.signature()));int result=id.equals(MoneyBasicStories.TOTAL)?v[0]+v[1]:v[0]-v[1];assertEquals(result,v[2]);q.answers=new String[]{"999"};q.expression="999";assertTrue(new Checker().check(q,List.of(),List.of(Rational.of(result,100).decimalText())).correct());assertFalse(new Checker().check(q,List.of(),List.of(Rational.of(result+1,100).decimalText())).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(3,h.size());assertFalse(h.canTransfer());for(int step=0;step<3;step++){assertTrue(h.step(step).accepts(Rational.of(v[step],100).decimalText()));assertFalse(h.step(step).before.matches(".*[0-9].*"));}}
   assertTrue(sampled.size()>100);for(int i:new int[]{0,count-1})assertNotNull(MoneyBasicStories.read(MoneyBasicStories.at(id,i)));
  }
 }
 @Test public void selectedGradeFourSuppliesFreshAndEarlierMeasurementsKeepPrecision(){
  Generator g=new Generator(new Random(153));for(String id:List.of(MoneyBasicStories.TOTAL,MoneyBasicStories.CHANGE)){CurriculumLimits limits=GlobalCurriculum.limits("england-primary-2021-v1",id,4);List<String> history=new ArrayList<>();for(int i=0;i<250;i++){Question q=g.next(id,history,false,limits);assertFalse(history.contains(q.signature()));history.add(q.signature());assertNotNull(MoneyBasicStories.read(q));}}
  for(String id:List.of(DecimalMeasureStories.ADD,DecimalMeasureStories.SUB)){assertEquals(2,GlobalCurriculum.limits("england-primary-2021-v1",id,4).decimalPlaces(3));assertEquals(3,GlobalCurriculum.limits("england-primary-2021-v1",id,5).decimalPlaces(1));List<String> recent=new ArrayList<>();Set<String> units=new HashSet<>();for(int i=0;i<150;i++){Question q=g.next(id,recent,false,GlobalCurriculum.limits("england-primary-2021-v1",id,4));assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=DecimalMeasureStories.read(q);assertNotNull(v);units.add(v.unit());assertTrue(new java.math.BigDecimal(v.first()).scale()<=2);assertTrue(new java.math.BigDecimal(v.second()).scale()<=2);java.math.BigDecimal x=new java.math.BigDecimal(v.first()),y=new java.math.BigDecimal(v.second());assertTrue(new Checker().check(q,List.of(),List.of((v.add()?x.add(y):x.subtract(y)).toPlainString())).correct());}assertEquals(Set.of("m","L"),units);}
 }
 @Test public void exactChangeBoundariesAndMalformedStatements(){
  Question zero=MoneyBasicStories.make(MoneyBasicStories.CHANGE,999,999);assertTrue(new Checker().check(zero,List.of(),List.of("0")).correct());assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(zero,List.of(),List.of("0/1")).status);assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(zero,List.of(),List.of("0.000")).status);
  for(int first=0;first<1000;first++){int start=first*(first+1)/2;assertArrayEquals(new int[]{first,0,first},MoneyBasicStories.read(MoneyBasicStories.at(MoneyBasicStories.CHANGE,start)));assertArrayEquals(new int[]{first,first,0},MoneyBasicStories.read(MoneyBasicStories.at(MoneyBasicStories.CHANGE,start+first)));}
  try{MoneyBasicStories.make(MoneyBasicStories.CHANGE,1,2);fail();}catch(IllegalArgumentException expected){}
  zero.prompt="지불한 돈: £0.01\n물건 가격: £0.02\n거스름돈은 몇 파운드인가요?";assertNull(MoneyBasicStories.read(zero));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(zero,List.of(),List.of("999")).status);
 }
}
