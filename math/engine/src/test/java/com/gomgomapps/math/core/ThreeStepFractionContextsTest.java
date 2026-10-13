package com.gomgomapps.math.core;
import java.util.*;import java.math.BigInteger;import org.junit.Test;import static org.junit.Assert.*;

public class ThreeStepFractionContextsTest {
 private static Question question(int f,String t,String n,String d,String e,String k){return new Question(ThreeStepFractionContexts.ID,ThreeStepFractionContexts.prompt(f,t,n,d,e,k),"poison","999");}
 @Test public void threeDifferentStoriesHaveThreeDependentExactQuantityOperations(){
  Question[] questions={question(0,"7","3","4","0.5","2"),question(1,"7","2","3","","2"),question(2,"4","3","2","1/3","1/2")};
  String[][] values={{"21/4","23/4","23/8"},{"14/3","7/3","7/6"},{"6","19/3","35/6"}};
  String[][] operations={{"*","+","/"},{"*","-","/"},{"*","+","-"}};
  for(int f=0;f<questions.length;f++){
   var g=ThreeStepFractionContexts.read(questions[f]);assertNotNull(g);assertEquals(f,g.family());assertEquals(f==1?"m":"L",g.unit());
   for(int stage=0;stage<3;stage++){assertEquals(Expression.number(values[f][stage]),g.intermediates()[stage]);assertEquals(operations[f][stage],g.operation(stage));}
   assertEquals(g.total().mul(g.fraction()),g.first());assertEquals(f==1?g.total().sub(g.first()):g.first().add(g.extra()),g.second());assertEquals(f==2?g.second().sub(g.last()):g.second().div(g.last()),g.value());
   assertTrue(new Checker().check(questions[f],List.of(),List.of(values[f][2])).correct());assertFalse(new Checker().check(questions[f],List.of(),List.of("999")).correct());
  }
  assertTrue(questions[0].prompt.contains("빈 통에 옮기고"));
 }
 @Test public void publicOriginalsAndNineBlankOperationStrategyFramesIgnorePrivateKeys(){
  Question q=question(0,"0.50","6","12","0.25","2.0");q.studyGuide=new StudyGuide().step("private","private","","999");StudyGuide stored=q.studyGuide;String prompt=q.prompt;
  var g=ThreeStepFractionContexts.read(q);assertNotNull(g);assertEquals("0.50",g.totalRaw());assertEquals("6",g.numeratorRaw());assertEquals("12",g.denominatorRaw());assertEquals("2.0",g.lastRaw());
  HelpPlan h=HelpPlan.forQuestion(q);assertNotNull(h);assertEquals(9,h.size());assertFalse(h.canTransfer());HelpPlan.Draft draft=h.restore(null,q.id);assertEquals(ThreeStepFractionContexts.VERSION,draft.teachingVersion);
  List<String> entries=new ArrayList<>();for(int stage=0;stage<3;stage++){
   var op=h.step(stage*3);assertEquals(Set.of("+","-","*","/"),op.options.keySet());assertTrue(op.accepts(g.operation(stage)));assertFalse(op.accepts(g.operation(stage).equals("+")?"-":"+"));
   var strategy=h.step(stage*3+1);assertTrue(strategy.accepts("mental"));assertTrue(strategy.accepts("written"));assertFalse(strategy.accepts("automatic"));assertEquals(Set.of("mental","written"),strategy.options.keySet());
   var numeric=h.step(stage*3+2);assertTrue(numeric.options.isEmpty());assertEquals("",numeric.after);assertTrue(numeric.before.contains(g.formula(stage)));assertTrue(numeric.before.contains("(L)"));assertTrue(numeric.accepts(g.intermediates()[stage].toString()));assertFalse(numeric.accepts(""));assertFalse(numeric.accepts("999"));
   entries.add(g.operation(stage));entries.add(stage%2==0?"mental":"written");entries.add(g.intermediates()[stage].toString());
  }
  assertTrue(h.step(2).before.contains("0.50"));assertTrue(h.step(2).before.contains("6/12"));assertFalse(h.step(2).before.contains("1/4"));assertSame(stored,q.studyGuide);assertEquals("999",q.studyGuide.frames.get(0).expected);assertEquals(prompt,q.prompt);assertEquals("poison",q.expression);assertArrayEquals(new String[]{"999"},q.answers);
  draft.stage=9;draft.entries=entries;HelpPlan.Draft restored=h.restore(draft.copy(),q.id);assertEquals(9,restored.stage);assertEquals(entries,restored.entries.subList(0,9));assertEquals(g.value().toString(),h.enteredAnswer(restored));assertFalse(h.canTransfer());
 }
 @Test public void zeroEqualAndImproperRecipeAreMathematicsNotSelectedSupplyCaps(){
  for(Question q:List.of(question(0,"0","3","3","0","1"),question(1,"7","3","3","","2"),question(1,"7","0","3","","2"),question(2,"(1/2)","5","3","0.50","1/4"),question(2,"1000","13","7","1.25","1/3"))){
   var g=ThreeStepFractionContexts.read(q);assertNotNull(q.prompt,g);assertTrue(new Checker().check(q,List.of(),List.of(g.value().toString())).correct());assertEquals(9,HelpPlan.forQuestion(q).size());
  }
  assertNull(ThreeStepFractionContexts.read(question(0,"10","4","3","2","2")));assertNull(ThreeStepFractionContexts.read(question(1,"10","4","3","","2")));assertNotNull(ThreeStepFractionContexts.read(question(2,"10","4","3","2","2")));
 }
 @Test public void mentalAndWrittenHaveIndependentManualPublicWorkAndFinalChecks(){
  Question q=question(0,"7","3","4","0.5","2");Checker checker=new Checker();q.expression="999=999";q.answers=new String[]{"999"};q.calculationMethod="mental";assertTrue(checker.check(q,List.of(),List.of("23/8")).correct());
  q.calculationMethod="written";q.stepSupport=false;assertTrue(ThreeStepFractionContexts.workQuestion(q).stepSupport);assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("23/8")).status);var wrong=checker.check(q,List.of("999=999"),List.of("23/8"),List.of(Checker.StepKind.FULL));assertEquals(Checker.Status.WRONG_STEP,wrong.status);assertEquals(Checker.InputTarget.WORK,wrong.inputTarget);assertFalse(q.stepSupport);
  assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of("999=999"),List.of("23/8"),List.of(Checker.StepKind.PARTIAL)).status);
  assertTrue(checker.check(q,List.of("((7*(3/4))+0.5)/2=23/8"),List.of("23/8"),List.of(Checker.StepKind.FULL)).correct());assertEquals(Checker.Status.WRONG_ANSWER,checker.check(q,List.of("((7*(3/4))+0.5)/2=23/8"),List.of("3"),List.of(Checker.StepKind.FULL)).status);
  assertTrue(checker.checkSteps(q,List.of("((7*(3/4))+0.5)/2"),List.of(Checker.StepKind.FULL)).correct());assertTrue(checker.checkSteps(q,List.of("1/2=2/4"),List.of(Checker.StepKind.PARTIAL)).correct());assertEquals("999=999",q.expression);assertArrayEquals(new String[]{"999"},q.answers);
 }
 @Test public void defaultAndExplicitAnswerFormsAndPartErrorsRemainSeparate(){
  Question q=question(0,"7","3","4","0.5","2");Checker checker=new Checker();assertTrue(FractionInput.defaultFraction(q));assertTrue(checker.check(q,List.of(),List.of("2.875")).correct());assertTrue(checker.check(q,List.of(),List.of("46/16")).correct());assertEquals(1,FractionInput.errorPart(q,"24/8"));assertEquals(2,FractionInput.errorPart(q,"23/9"));assertEquals(0,FractionInput.errorPart(q,"46/16"));assertEquals(0,FractionInput.errorPart(q,"24/9"));
  q.answerFormat="fraction";assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("2.875")).status);assertTrue(checker.check(q,List.of(),List.of("23/8")).correct());q.kind="reduced";assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("46/16")).status);q.kind="number";q.answerFormat="decimal";assertEquals(Checker.Status.INPUT_NEEDED,checker.check(q,List.of(),List.of("23/8")).status);assertTrue(checker.check(q,List.of(),List.of("2.875")).correct());
  Question whole=question(0,"8","1","2","4","2");assertFalse(FractionInput.defaultFraction(whole));assertTrue(checker.check(whole,List.of(),List.of("4")).correct());assertTrue(checker.check(whole,List.of(),List.of("4.0")).correct());assertTrue(checker.check(whole,List.of(),List.of("4/1")).correct());whole.answerFormat="fraction";assertTrue(FractionInput.defaultFraction(whole));assertEquals(Checker.Status.INPUT_NEEDED,checker.check(whole,List.of(),List.of("4")).status);
 }
 @Test public void invalidPublicSemanticsAndSourceTokensDoNotUseStoredFallback(){
  List<Question> invalid=List.of(question(0,"7","1","0","1","2"),question(0,"7","1","3","1","0"),question(0,"7","1","3","1","1.5"),question(2,"1","1","2","0","1"),question(2,"-1","1","2","0","0"),question(2,"1e2","1","2","0","0"),question(2,"0.0000000000001","1","2","0","0"),question(2,"1".repeat(31),"1","2","0","0"),question(2,"1","1".repeat(31),"2","0","0"),question(2,"1","1","1".repeat(31),"0","0"),question(2,"1","1","2","1".repeat(31),"0"),question(2,"1","1","2","0","1".repeat(31)));
  for(Question q:invalid){q.studyGuide=new StudyGuide().step("private","private","","999");assertNull(q.prompt,ThreeStepFractionContexts.read(q));assertNull(HelpPlan.forQuestion(q));assertNull(ThreeStepFractionContexts.workQuestion(q));assertEquals(Checker.Status.INPUT_NEEDED,new Checker().check(q,List.of(),List.of("999")).status);}
  Question trailing=question(0,"7","1","2","1","2");trailing.prompt+=" trailing";assertNull(ThreeStepFractionContexts.read(trailing));assertNull(ThreeStepFractionContexts.read(null));assertNull(ThreeStepFractionContexts.guide(null));assertEquals(0,ThreeStepFractionContexts.errorPart(null,"1/2"));
 }
 @Test public void legitimate241CharacterOutputsAndEveryLargeStageAreReachableWithoutWideningOldGuards(){
  Question q=question(2,"999999999999999999999999999923/900000000000000000000000000045","999999999999999999999999999985","900000000000000000000000000041","999999999999999999999999999962/900000000000000000000000000005","999999999999999999999999999941/900000000000000000000000000061");
  String expected="162000000000000000000000000009109799999999999999999999999986876000000000000000000000004431459999999999999999999999323968/131220000000000000000000000022161600000000000000000000001267812000000000000000000000026640000000000000000000000000112545";
  var g=ThreeStepFractionContexts.read(q);assertNotNull(g);assertEquals(expected,g.value().toString());assertEquals(241,expected.length());assertTrue(g.value().n.bitLength()<=512);assertTrue(g.value().d.bitLength()<=512);assertNull(QuantityFractionCheck.numericInput(expected));assertEquals(g.value(),ThreeStepFractionContexts.numericInput(expected));assertTrue(new Checker().check(q,List.of(),List.of(expected)).correct());assertEquals(0,FractionInput.errorPart(q,expected));
  HelpPlan h=HelpPlan.forQuestion(q);for(int stage=0;stage<3;stage++)assertTrue(h.step(stage*3+2).accepts(g.intermediates()[stage].toString()));assertTrue(g.expression().length()>240);assertFalse(new Checker().checkSteps(q,List.of(g.expression()),List.of(Checker.StepKind.FULL)).correct());
  String a="9".repeat(30);Question integer=question(2,a,a,"1","0","0");assertEquals(60,ThreeStepFractionContexts.read(integer).value().toString().length());assertTrue(new Checker().check(integer,List.of(),List.of(ThreeStepFractionContexts.read(integer).value().toString())).correct());
  for(String raw:List.of(BigInteger.ONE.shiftLeft(512).toString(),"1".repeat(313),"0".repeat(156),"1e2","1+1","((1))","0.0000000000001","1/0"))assertNull(raw,ThreeStepFractionContexts.numericInput(raw));assertEquals(Rational.of(-1,2),ThreeStepFractionContexts.numericInput("−1/2"));
  Question old=new Question("fracMul","(1/2) × (1/2)","1/2*1/2","1/4");assertFalse(HelpPlan.forQuestion(old).step(0).accepts("0".repeat(31)+"1"));
 }
}
