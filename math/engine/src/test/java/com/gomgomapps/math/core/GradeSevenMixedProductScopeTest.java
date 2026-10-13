package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class GradeSevenMixedProductScopeTest {
 @Test public void actualGeneralRouteKeepsTwoMixedAndOtherOperandKindsWithPublicChecks(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"NA");GlobalCurriculum.choosePack(p,"na-nied-primary-2024-v1");assertTrue(GlobalCurriculum.pack(p).inGrade("fracMixedMul",7));CurriculumLimits l=GlobalCurriculum.limits("na-nied-primary-2024-v1","fracMixedMul",7);assertFalse(l.wholeFractionMultiplier());Generator g=new Generator(new Random(351));List<String>recent=new ArrayList<>();Set<Integer>mixedCounts=new HashSet<>();Set<Boolean>wholeOther=new HashSet<>();Set<String>operandOrders=new HashSet<>();
  for(int i=0;i<150;i++){Question q=g.next("fracMixedMul",recent,false,l);assertTrue(l.allows(q));assertFalse(recent.contains(q.signature()));recent.add(q.signature());var v=FractionProducts.read(q.prompt);assertNotNull(v);operandOrders.add((v.left.mixed()?"M":v.left.denominator==1?"W":"P")+"/"+(v.right.mixed()?"M":v.right.denominator==1?"W":"P"));int count=(v.left.mixed()?1:0)+(v.right.mixed()?1:0);mixedCounts.add(count);wholeOther.add(v.left.denominator==1||v.right.denominator==1);assertTrue(count>=1);for(var op:List.of(v.left,v.right)){if(op.denominator>1)assertTrue(op.denominator>=2&&op.denominator<=12);if(op.mixed())assertTrue(op.whole>=1&&op.whole<=9);}Rational expected=v.left.value().mul(v.right.value());q.answers=new String[]{"poison"};q.expression="poison";assertTrue(new Checker().check(q,List.of(),List.of(expected.toString())).correct());assertFalse(new Checker().check(q,List.of(),List.of(expected.add(Rational.of(1)).toString())).correct());HelpPlan h=HelpPlan.forQuestion(q);assertEquals(count==2?13:11,h.size());assertFalse(h.canTransfer());
  }assertEquals(Set.of(1,2),mixedCounts);assertEquals(Set.of(false,true),wholeOther);assertEquals(Set.of("M/M","M/P","P/M","M/W","W/M"),operandOrders);
 }
 @Test public void existingEnglandWholeFlagAndDomesticIdRemainSeparate(){
  CurriculumLimits gb=GlobalCurriculum.limits("england-primary-2021-v1","fracMixedMul",5);assertTrue(gb.wholeFractionMultiplier());assertTrue(WholeFractionProducts.selected("fracMixedMul",gb));assertEquals(5,Catalog.get("fracMixedMul").grade);Question q=new Question("fracMixedMul","1 1/2 × 2 1/3","poison","poison");assertFalse(WholeFractionProducts.supports(q));assertTrue(new Checker().check(q,List.of(),List.of("7/2")).correct());assertFalse(HelpPlan.forQuestion(q).canTransfer());
 }
 @Test public void savedAnswerFormatsKeepLegacyGrammarWithPublicPoisonChecks(){
  Question q=new Question("fracMixedMul","1 1/2 × 2 1/3","poison","999");Checker c=new Checker();
  for(String input:List.of("7/2","14/4","3.5"))assertTrue(c.check(q,List.of(),List.of(input)).correct());
  for(String input:List.of("1+2.5","(3.5)","7/2+0"))assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(input)).status);
  q.answerFormat="decimalValue";assertTrue(c.check(q,List.of(),List.of("3.5")).correct());assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("7/2")).status);
  q.answerFormat="hundredthsFraction";assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("")).status);assertTrue(c.check(q,List.of(),List.of("350/100")).correct());assertEquals(Checker.Status.WRONG_ANSWER,c.check(q,List.of(),List.of("7/2")).status);assertFalse(c.check(q,List.of(),List.of("999/100")).correct());
  q.answerFormat="fraction";assertTrue(c.check(q,List.of(),List.of("14/4")).correct());assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("3.5")).status);
  q.answerFormat="decimal";assertTrue(c.check(q,List.of(),List.of("3.5")).correct());assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("7/2")).status);
  q.answerFormat="";q.kind="reduced";assertTrue(c.check(q,List.of(),List.of("7/2")).correct());assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("14/4")).status);assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of("3.5")).status);
 }
}
