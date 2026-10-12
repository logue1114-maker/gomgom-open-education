package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class MixedFractionComparisonTest {
 @Test public void visibleFormsSignsAndConversionHelp(){
  assertEquals(Rational.of(7,3),MixedFractionValue.value("2 1/3"));assertNotEquals(Rational.of(21,3),MixedFractionValue.value("2 1/3"));
  for(String bad:List.of("2 3/3","1 0/2","2/0","1 2/101","10001/2","2  1/3","-2 1/3"))try{MixedFractionValue.value(bad);fail(bad);}catch(IllegalArgumentException expected){}
  for(String[]v:new String[][]{{"2 1/3","7/3","="},{"7/3","2 1/2","<"},{"3 99/100","398/100",">"},{"101/100","1 1/100","="}}){
   Question q=MixedFractionComparison.question(v[0],v[1]);q.answers=new String[]{"poison"};q.expression="poison";
   for(String sign:List.of("<","=",">"))assertEquals(v[2].equals(sign),new Checker().check(q,List.of(),List.of(sign)).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(11,h.size());assertFalse(h.canTransfer());assertTrue(h.restore(null,q.id).entries.stream().allMatch(String::isEmpty));
  }
  Question q=MixedFractionComparison.question("2 1/3","7/3");HelpPlan h=HelpPlan.forQuestion(q);String[]inputs={"2","1","3","6","7","7","3","9","21","21","="};for(int i=0;i<inputs.length;i++)assertTrue(h.step(i).accepts(inputs[i]));assertFalse(h.step(4).accepts("6"));assertArrayEquals(new int[]{4,6},MixedFractionComparison.referenceIndices(q,8));
 }
 @Test public void originalMixedCardsAndBothDirections(){
  for(String dir:List.of("작은","큰")){
   Question q=new Question("mixedFractionOrder",dir+" 분수부터 고르세요.\n2 1/3 · 9/4 · 3 1/2","poison","poison");
   List<String>correct=dir.equals("작은")?List.of("9/4","2 1/3","3 1/2"):List.of("3 1/2","2 1/3","9/4");assertTrue(new Checker().check(q,List.of(),correct).correct());List<String>wrong=new ArrayList<>(correct);Collections.swap(wrong,0,2);assertFalse(new Checker().check(q,List.of(),wrong).correct());wrong=new ArrayList<>(correct);wrong.set(1,"7/3");assertFalse(new Checker().check(q,List.of(),wrong).correct());
   HelpPlan h=HelpPlan.forQuestion(q);assertEquals(3,h.size());assertFalse(h.canTransfer());for(int i=0;i<3;i++)assertTrue(h.step(i).accepts(correct.get(i)));
  }
 }
 @Test public void mixedMagnitudeLimitsUseWholePlusFraction(){Question q=new Question("mixedFractionOrder","작은 분수부터 고르세요.\n1 1/2 · 7/4 · 2 1/3","","1 1/2","7/4","2 1/3");assertTrue(new CurriculumLimits("maxFractionValue=3").allows(q));assertFalse(new CurriculumLimits("maxFractionValue=2").allows(q));}
 @Test public void ordinaryGradeSixBothTopics(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
  for(String id:List.of(MixedFractionComparison.ID,MixedFractionComparison.ORDER)){
   assertTrue(GlobalCurriculum.pack(p).inGrade(id,6));CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1",id,6);Generator g=new Generator(new Random(276));List<String>recent=new ArrayList<>();Set<String>signs=new HashSet<>();boolean large=false,up=false,down=false,sameWholeUnequal=false,unlikeEqual=false;Set<Integer>forms=new HashSet<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,false,l);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(l.allows(q));List<String>raw=id.equals(MixedFractionComparison.ID)?MixedFractionComparison.operands(q):FractionOrdering.givens(q);forms.add((int)raw.stream().filter(v->v.contains(" ")).count());for(String v:raw)large|=MixedFractionValue.read(v).denominator()>12;assertTrue(new Checker().check(q,List.of(),Arrays.asList(q.answers)).correct());if(id.equals(MixedFractionComparison.ID)){var a=MixedFractionValue.read(raw.get(0));var b=MixedFractionValue.read(raw.get(1));sameWholeUnequal|=!q.answers[0].equals("=")&&a.improper()/a.denominator()==b.improper()/b.denominator();unlikeEqual|=q.answers[0].equals("=")&&a.denominator()!=b.denominator();}signs.add(q.answers[0]);up|=q.prompt.startsWith("작은");down|=q.prompt.startsWith("큰");}
   assertTrue(large);assertEquals(id.equals(MixedFractionComparison.ID)?Set.of(0,1,2):Set.of(0,1,2,3),forms);if(id.equals(MixedFractionComparison.ID)){assertEquals(Set.of("<","=",">"),signs);assertTrue(sameWholeUnequal);assertTrue(unlikeEqual);}else assertTrue(up&&down);
  }
 }
}
