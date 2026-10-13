package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;

public class GradeSevenThreeStepFractionScopeTest {
 @Test public void normalNaSevenFreshSupplyHasThreeMeaningfulFractionFamiliesAndHonestFormats(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(ThreeStepFractionContexts.ID,7));assertFalse(pack.inGrade(ThreeStepFractionContexts.ID,6));assertEquals(1,Catalog.ALL.stream().filter(s->s.id.equals(ThreeStepFractionContexts.ID)).count());
  var limits=GlobalCurriculum.limits(pack.id,ThreeStepFractionContexts.ID,7);Generator generator=new Generator(new Random(357));List<String>recent=new ArrayList<>();Set<Integer>families=new HashSet<>();boolean rational=false,improperRecipe=false;
  for(int i=0;i<180;i++){
   Question q=generator.next(ThreeStepFractionContexts.ID,recent,false,limits);assertFalse(recent.contains(q.signature()));recent.add(q.signature());assertTrue(limits.allows(q));var g=ThreeStepFractionContexts.read(q);assertNotNull(g);families.add(g.family());rational|=!g.value().isInteger();improperRecipe|=g.family()==2&&g.fraction().compareTo(Rational.ONE)>0;
   assertTrue(g.total().compareTo(Rational.ZERO)>0);assertTrue(g.total().compareTo(Rational.of(100))<=0);assertFalse(g.fraction().equals(Rational.ONE));assertTrue(g.first().compareTo(Rational.ZERO)>0);if(g.family()<2)assertTrue(g.last().compareTo(Rational.ONE)>0);if(g.family()!=1)assertTrue(g.extra().compareTo(Rational.ZERO)>0);if(g.family()==2)assertTrue(g.last().compareTo(Rational.ZERO)>0);
   assertEquals("",q.answerFormat);assertEquals(!g.value().isInteger(),q.decimal);assertEquals(!g.value().isInteger(),FractionInput.defaultFraction(q));assertTrue(q.stepSupport);assertTrue(new Checker().check(q,List.of(),List.of(g.value().toString())).correct());assertEquals(9,HelpPlan.forQuestion(q).size());assertFalse(HelpPlan.forQuestion(q).canTransfer());
  }assertEquals(Set.of(0,1,2),families);assertTrue(rational);assertTrue(improperRecipe);
 }
 @Test public void publicPlanAndResultStayIndependentOfPoisonedSavedExpressionsAndAnswers(){
  Question q=new Question(ThreeStepFractionContexts.ID,ThreeStepFractionContexts.prompt(2,"1000","13","7","1.25","1/3"),"-999","-999");q.studyGuide=new StudyGuide().step("private","private","","-999");var limits=GlobalCurriculum.limits("na-nied-primary-2024-v1",ThreeStepFractionContexts.ID,7);assertTrue(limits.allows(q));assertTrue(new Checker().check(q,List.of(),List.of(ThreeStepFractionContexts.read(q).value().toString())).correct());assertEquals("-999",q.expression);assertArrayEquals(new String[]{"-999"},q.answers);assertEquals("private",q.studyGuide.frames.get(0).instruction);
  q.expression=null;q.answers=null;assertTrue(limits.allows(q));assertEquals(9,HelpPlan.forQuestion(q).size());assertNotNull(ThreeStepFractionContexts.workQuestion(q));
 }
 @Test public void legacyNaSixTwoStepAndNaSevenWholeContextsKeepTheirRoutesAndSavedHelp(){
  var pack=GlobalCurriculum.packs("NA").stream().filter(p->p.id.equals("na-nied-primary-2024-v1")).findFirst().orElseThrow();assertTrue(pack.inGrade(TwoStepFractionStories.ID,6));assertTrue(pack.inGrade(ThreeStepWholeContexts.ID,7));assertEquals("분수의 두 단계 문장제",Catalog.get(TwoStepFractionStories.ID).title);assertEquals("세 단계 문장제",Catalog.get(ThreeStepWholeContexts.ID).title);
  Question old=TwoStepFractionStories.make(12,2,3,0);HelpPlan h=HelpPlan.forQuestion(old);assertEquals(5,h.size());assertFalse(h.canTransfer());HelpPlan.Draft draft=h.restore(null,old.id);draft.stage=5;draft.entries=new ArrayList<>(List.of("12","2","3","8","4"));assertEquals("two-step-fraction-story-v1",draft.teachingVersion);assertEquals(5,h.restore(draft.copy(),old.id).stage);assertEquals("4",h.enteredAnswer(draft));assertEquals("fraction",old.answerFormat);assertFalse(old.stepSupport);assertTrue(new Checker().check(old,List.of(),List.of("4/1")).correct());
  Question whole=ThreeStepWholeContexts.make(1,15,5,4,4);var wh=HelpPlan.forQuestion(whole);assertEquals(9,wh.size());assertEquals("three-step-whole-contexts-v1",wh.restore(null,whole.id).teachingVersion);assertFalse(wh.canTransfer());assertTrue(new Checker().check(whole,List.of(),List.of("4")).correct());assertNull(ThreeStepFractionContexts.read(old));assertNull(ThreeStepFractionContexts.read(whole));
 }
}
