package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenFractionOrderingScopeTest {
 private static final List<String> IDS=List.of("fractionOrder","likeFractionOrder","mixedFractionOrder");
 @Test public void actualRoutesKeepOriginalCardsBothDirectionsAndSelectedRanges(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
  for(String id:IDS){assertTrue(GlobalCurriculum.pack(p).inGrade(id,7));CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1",id,7);Generator g=new Generator(new Random(348));List<String>recent=new ArrayList<>();Set<Boolean>directions=new HashSet<>();Set<Integer>forms=new HashSet<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,false,l);assertTrue(l.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());directions.add(q.prompt.startsWith("큰"));assertEquals("orderedFractions",q.kind);assertEquals(3,q.labels.length);assertFalse(q.stepSupport);List<String>cards=FractionOrdering.givens(q);assertEquals(3,cards.size());forms.add((int)cards.stream().filter(v->v.contains(" ")).count());if(id.equals("likeFractionOrder"))assertEquals(1,cards.stream().map(v->MixedFractionValue.read(v).denominator()).distinct().count());
    for(String raw:cards){var v=MixedFractionValue.read(raw);assertTrue(v.denominator()>=2&&v.denominator()<=10);if(id.equals("mixedFractionOrder")){int whole=v.improper()/v.denominator();assertTrue(whole>=1&&whole<=6);}else assertTrue(v.value().compareTo(Rational.of(1,1))<=0);}
    List<String>expected=List.copyOf(Arrays.asList(q.answers));assertTrue(cards.containsAll(expected));q.answers=new String[]{"poison","poison","poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),expected).correct());List<String>wrong=new ArrayList<>(expected);Collections.swap(wrong,0,1);assertFalse(new Checker().check(q,List.of(),wrong).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
   }assertEquals(Set.of(false,true),directions);if(id.equals("mixedFractionOrder"))assertEquals(Set.of(0,1,2,3),forms);
  }
 }
 @Test public void oldGradeSixHundredDenominatorsRemainOriginalCardScope(){
  for(String id:IDS){String cards=id.equals("mixedFractionOrder")?"1 1/100 · 102/100 · 1 3/100":"1/100 · 2/100 · 3/100";Question q=new Question(id,"작은 분수부터 고르세요.\n"+cards,"poison","poison","poison","poison");q.kind="orderedFractions";
   assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,6).allows(q));assertFalse(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,7).allows(q));assertTrue(new Checker().check(q,List.of(),FractionOrdering.givens(q)).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }
 }
}
