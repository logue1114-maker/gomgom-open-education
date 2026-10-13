package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
/** Selected NA7 connection, preserving the existing comparator and NA6 scope. */
public class GradeSevenMixedFractionCompareScopeTest {
 @Test public void normalPackRouteUsesOriginalDenominatorsTwoToTen(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
  assertTrue(GlobalCurriculum.pack(p).inGrade(MixedFractionComparison.ID,7));
  CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1",MixedFractionComparison.ID,7);
  Generator g=new Generator(new Random(347));List<String>recent=new ArrayList<>();Set<Integer>forms=new HashSet<>();Set<String>signs=new HashSet<>();
  for(int i=0;i<100;i++){Question q=g.next(MixedFractionComparison.ID,recent,false,l);assertTrue(l.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());forms.add((int)MixedFractionComparison.operands(q).stream().filter(v->v.contains(" ")).count());signs.add(q.answers[0]);assertEquals("symbol",q.kind);assertFalse(q.stepSupport);for(String raw:MixedFractionComparison.operands(q)){int d=MixedFractionValue.read(raw).denominator();assertTrue(d>=2&&d<=10);}}
  assertEquals(Set.of(0,1,2),forms);assertEquals(Set.of("<","=",">"),signs);
 }
 @Test public void originalTenAndCommonDenominatorHundredAreDifferentContracts(){
  CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1",MixedFractionComparison.ID,7);
  Question q=MixedFractionComparison.question("1 1/10","12/10");q.answers=new String[]{">"};q.expression="poison";
  assertTrue(l.allows(q));assertTrue(new Checker().check(q,List.of(),List.of("<")).correct());assertFalse(new Checker().check(q,List.of(),List.of(">")).correct());
  HelpPlan h=HelpPlan.forQuestion(q);assertFalse(h.canTransfer());
  boolean hundred=false;for(int i=0;i<h.size();i++)if(h.step(i).before.equals("공통 분모 = ")){assertTrue(h.step(i).accepts("100"));hundred=true;}assertTrue(hundred);
  assertFalse(l.allows(MixedFractionComparison.question("1 1/11","12/11")));
 }
 @Test public void oldGradeSixDenominatorHundredRemainsAllowed(){
  CurriculumLimits old=GlobalCurriculum.limits("na-nied-primary-2024-v1",MixedFractionComparison.ID,6);
  CurriculumLimits next=GlobalCurriculum.limits("na-nied-primary-2024-v1",MixedFractionComparison.ID,7);
  Question q=MixedFractionComparison.question("6 1/100","602/100");
  assertTrue(old.allows(q));assertFalse(next.allows(q));
  assertTrue(new Checker().check(q,List.of(),List.of("<")).correct());
 }
}
