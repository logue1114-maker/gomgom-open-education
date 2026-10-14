package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PercentageMeaningTest {
 private static Question question(String raw){return new Question(PercentageMeaning.ID,PercentageMeaning.prompt(raw),"private expression","999","999%");}
 private static Checker.Result check(Question q,String basis,String p){return new Checker().check(q,List.of(),Arrays.asList(basis,p));}
 @Test public void publicPartsPerHundredPreserveRawDecimalAndPermitZeroAndPercentagesAboveHundred(){
  String[][] cases={{"0","0%","0"},{"00000.50","0.50 %","1/200"},{"35","35.0%","7/20"},{"100.0","100%","1"},{"150.5","150.50%","301/200"}};
  for(String[] c:cases){Question q=question(c[0]);var g=PercentageMeaning.read(q);assertNotNull(g);assertEquals(c[0],g.rawPercent());assertEquals(Expression.number(c[2]),g.ratio());assertTrue(check(q,"0100",c[1]).correct());assertTrue(check(q,"100.0",c[1]).correct());assertTrue(CurriculumLimits.NONE.allows(q));}
  assertEquals(Rational.of(35),PercentageMeaning.symbolInput("+35.00 %"));assertEquals(Rational.of(-35),PercentageMeaning.symbolInput("−35%"));assertEquals(Checker.Status.WRONG_ANSWER,check(question("35"),"100","0.35%").status);assertEquals(Checker.Status.WRONG_ANSWER,check(question("35"),"100","-35%").status);
 }
 @Test public void bodyDefinitionAndSymbolErrorsStayInTheirOwnScalarAnswerFields(){
  Question q=question("35");Checker.Result basis=check(q,"10","35%"),badBasis=check(q,"100%","35%"),bare=check(q,"100","35"),wrongPercent=check(q,"100","0.35%");
  assertEquals(Checker.Status.WRONG_ANSWER,basis.status);assertEquals(0,basis.index);assertEquals(Checker.Status.INPUT_NEEDED,badBasis.status);assertEquals(0,badBasis.index);assertEquals(Checker.Status.INPUT_NEEDED,bare.status);assertEquals(1,bare.index);assertEquals("백분율 기호 % 입력 필요",bare.message);assertEquals(Checker.Status.WRONG_ANSWER,wrongPercent.status);assertEquals(1,wrongPercent.index);
  for(Checker.Result r:List.of(basis,badBasis,bare,wrongPercent))assertEquals(Checker.InputTarget.ANSWER,r.inputTarget);
  for(String raw:List.of("35‰","35÷","35%%","35％","35/100%","3.5e1%","35,%","35+0%","(35)%","%35","35. %")){Checker.Result r=check(q,"100",raw);assertEquals(raw,Checker.Status.INPUT_NEEDED,r.status);assertEquals(1,r.index);assertEquals(Checker.InputTarget.ANSWER,r.inputTarget);}
  assertEquals(0,check(q,null,"35%").index);assertEquals(1,check(q,"100",null).index);
 }
 @Test public void publicCheckerAndReadOnlyFourFramesIgnorePrivateKeysWorkAndStoredGuide(){
  Question q=question("00035.00");q.studyGuide=new StudyGuide().step("private","private","","999");StudyGuide stored=q.studyGuide;HelpPlan h=HelpPlan.forQuestion(q);assertEquals(4,h.size());assertFalse(h.canTransfer());StudyGuide visible=PercentageMeaning.guide(q);assertEquals(PercentageMeaning.VERSION,visible.teachingVersion);assertEquals(Set.of("%","‰","÷"),h.step(0).options.keySet());assertEquals("100분의 몇",h.step(1).options.get("100"));assertTrue(h.step(0).accepts("%"));assertFalse(h.step(0).accepts("‰"));assertTrue(h.step(1).accepts("100"));assertFalse(h.step(1).accepts("10"));assertTrue(h.step(2).accepts("35.0"));assertFalse(h.step(2).accepts("35%"));assertTrue(h.step(3).accepts("35.0%"));assertFalse(h.step(3).accepts("35"));assertEquals("percentageNumeric",visible.frames.get(2).inputFormat);assertEquals("percentageSymbol",visible.frames.get(3).inputFormat);assertEquals("00035.00",visible.frames.get(2).expected);assertEquals("00035.00",visible.frames.get(3).expected);
  for(int i=0;i<4;i++){assertEquals("",h.step(i).after);assertFalse(h.step(i).accepts(""));}
  HelpPlan.Draft d=h.restore(null,q.id);assertEquals(PercentageMeaning.VERSION,d.teachingVersion);assertEquals(List.of(""),d.entries);d.stage=4;d.entries=new ArrayList<>(List.of("%","100","35.0","35.00 %"));HelpPlan.Draft restored=h.restore(d.copy(),q.id);assertEquals(4,restored.stage);assertEquals("35.00 %",h.enteredAnswer(restored));assertSame(stored,q.studyGuide);assertEquals("private expression",q.expression);assertArrayEquals(new String[]{"999","999%"},q.answers);
  q.expression=null;q.answers=null;q.stepSupport=true;q.calculationMethod="written";assertTrue(new Checker().check(q,List.of("999=999"),List.of("100","35%"),List.of(Checker.StepKind.FULL)).correct());assertEquals(4,HelpPlan.forQuestion(q).size());assertSame(stored,q.studyGuide);assertNull(q.expression);assertNull(q.answers);
 }
 @Test public void referencesShowOnlyTheCheckedLearnerPartsAndDoNotFillThePercentage(){
  Question q=question("00000.50");assertEquals(Map.of(),PercentageMeaning.references(q,3,List.of()));assertEquals(Map.of(),PercentageMeaning.references(q,3,List.of("%","100","0.05")));assertEquals(Map.of(),PercentageMeaning.references(q,2,List.of("%","100","0.50")));assertEquals(Map.of(),PercentageMeaning.references(q,4,List.of("%","100","0.50","0.50%")));
  assertEquals(Map.of("부분 수 p = ","00.500"),PercentageMeaning.references(q,3,List.of("%","100","00.500")));assertEquals(Map.of(),PercentageMeaning.references(q,3,Arrays.asList("%","100",null)));assertEquals(Map.of(),PercentageMeaning.references(null,3,List.of("%","100","0.50")));
 }
 @Test public void maximumPublicFiniteComponentRemainsReachableWithoutWideningSharedExpression(){
  String raw="99999999999999999999.123456789012";Question q=question(raw);BigInteger n=new BigInteger("99999999999999999999123456789012"),d=BigInteger.TEN.pow(12);Rational expected=new Rational(n,d);assertEquals(expected,PercentageMeaning.read(q).percent());assertEquals(expected.div(Rational.of(100)),PercentageMeaning.read(q).ratio());assertTrue(check(q,"100",raw+"%").correct());HelpPlan h=HelpPlan.forQuestion(q);assertTrue(h.step(2).accepts(raw));assertTrue(h.step(3).accepts(raw+" %"));assertTrue(CurriculumLimits.NONE.allows(q));
  try{Expression.number(raw);fail("Shared30-character component guard widened");}catch(IllegalArgumentException expectedGuard){}try{Rational.decimal("0."+"1".repeat(13));fail("Shared12-place guard widened");}catch(IllegalArgumentException expectedGuard){}
 }
 @Test public void malformedPublicSourceReturnsNoGuideWithoutStoredFallbackAndInputBoundsRemainExact(){
  for(String raw:List.of("-1","+1","1e2","1/2",".5","1.","1".repeat(21),"0."+"1".repeat(13))){Question q=question(raw);q.studyGuide=new StudyGuide().step("private","private","","999");StudyGuide stored=q.studyGuide;assertNull(raw,PercentageMeaning.read(q));assertNull(raw,HelpPlan.forQuestion(q));assertSame(stored,q.studyGuide);assertEquals(Checker.Status.INPUT_NEEDED,check(q,"100","1%").status);assertFalse(CurriculumLimits.NONE.allows(q));}
  Question extra=question("35");extra.prompt+=" extra";assertNull(PercentageMeaning.read(extra));assertNull(PercentageMeaning.read(new Question("percent",PercentageMeaning.prompt("35"),"","35")));assertNull(PercentageMeaning.read(null));assertNull(PercentageMeaning.guide(null));
  for(String raw:List.of("0".repeat(121),BigInteger.ONE.shiftLeft(512).toString(),"1".repeat(21),"0."+"1".repeat(13),"1/2","1e2","1,5","1+0","1 %"))assertNull(raw,PercentageMeaning.numericInput(raw));assertNull(PercentageMeaning.numericInput(null));assertNull(PercentageMeaning.symbolInput(null));assertNull(PercentageMeaning.symbolInput(" ".repeat(116)+"35.0%"));assertNotNull(PercentageMeaning.symbolInput(" ".repeat(115)+"35.0%"));
 }
}
