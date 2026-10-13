package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenMixedArithmeticScopeTest {
 @Test public void actualGradeSevenRoutesUsePublicMixedOperands(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");
  for(String id:List.of("fracMixedAdd","fracMixedSub")){assertTrue(GlobalCurriculum.pack(p).inGrade(id,7));CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1",id,7);Generator g=new Generator(new Random(349));List<String>recent=new ArrayList<>();
   for(int i=0;i<100;i++){Question q=g.next(id,recent,false,l);assertTrue(l.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=MixedFractions.read(q.prompt);assertNotNull(v);assertTrue(v.wholeLeft>=1&&v.wholeLeft<=9);assertTrue(v.wholeRight>=1&&v.wholeRight<=9);assertTrue(v.denominatorLeft>=2&&v.denominatorLeft<=12);assertTrue(v.denominatorRight>=2&&v.denominatorRight<=12);
    Rational a=Rational.of((long)v.wholeLeft*v.denominatorLeft+v.numeratorLeft,v.denominatorLeft),b=Rational.of((long)v.wholeRight*v.denominatorRight+v.numeratorRight,v.denominatorRight);Rational expected=id.equals("fracMixedAdd")?a.add(b):a.sub(b);q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(17,h.size());assertFalse(h.canTransfer());
   }
  }
 }
 @Test public void originalTwelveAndDerivedLcm132KeepNa6RelatedScopeSeparate(){
  for(String id:List.of("fracMixedAdd","fracMixedSub")){String op=id.equals("fracMixedAdd")?" + ":" - ";Question q=new Question(id,"3 1/12"+op+"1 1/11","poison","1");assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,7).allows(q));assertFalse(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,6).allows(q));HelpPlan h=HelpPlan.forQuestion(q);assertEquals(17,h.size());assertTrue(h.step(8).accepts("132"));assertFalse(h.canTransfer());Question related=new Question(id,"3 1/12"+op+"1 1/6","poison","1");assertTrue(GlobalCurriculum.limits("na-nied-primary-2024-v1",id,6).allows(related));}
 }
}
