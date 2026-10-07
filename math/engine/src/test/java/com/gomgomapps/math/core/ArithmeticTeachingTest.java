package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import java.io.*;import static org.junit.Assert.*;
public class ArithmeticTeachingTest {
 @Test public void generatedReductionAndSignedQuestionsHaveEnteredFramesWithoutMainTransfer(){
  Generator g=new Generator(new Random(20261006821L));for(String id:List.of("reduce","signedAdd","signedMul"))for(int i=0;i<300;i++){
   Question q=g.next(id,List.of(),i%2==0);assertNotNull(id,q.studyGuide);HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertTrue(plan.size()>=3);for(int stage=0;stage<plan.size();stage++){String expected=q.studyGuide.frames.get(stage).expected;assertTrue(id,plan.step(stage).accepts(expected));assertFalse(plan.step(stage).accepts(Expression.number(expected).add(Rational.ONE).toString()));}
  }
 }
 @Test public void allSignsSubtractionCancellationAndExactDivisionPreservePublicProblem(){
  for(int a=-12;a<=12;a++)for(int b=-12;b<=12;b++)for(String op:List.of("+","-","×","÷")){
   if(op.equals("÷")&&(b==0||a%b!=0))continue;String id=op.equals("+")||op.equals("-")?"signedAdd":"signedMul";int result=op.equals("+")?a+b:op.equals("-")?a-b:op.equals("×")?a*b:a/b;
   String expression="("+a+") "+op+" ("+b+")";Question q=new Question(id,expression,expression,""+result);String signature=q.signature();ArithmeticTeaching.attach(q);assertEquals(signature,q.signature());assertNotNull(q.studyGuide);assertEquals(""+result,q.studyGuide.frames.get(q.studyGuide.frames.size()-1).expected);assertFalse(HelpPlan.forQuestion(q).canTransfer());if(result==0&&(id.equals("signedMul")||a==0&&b==0))assertEquals("계산한 절댓값을 결과에 쓰세요.",q.studyGuide.frames.get(q.studyGuide.frames.size()-1).instruction);
  }
 }
 @Test public void reductionUsesUnreducedVisibleOperandsAndSavedFramesRetainTheirSteps()throws Exception{
  Question q=new Question("reduce","18/30을 기약분수로 나타내세요.","3/5","3/5");q.kind="reduced";ArithmeticTeaching.attach(q);assertEquals(List.of("18","30","6","3","5"),q.studyGuide.frames.stream().map(f->f.expected).toList());
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(q);Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(q.signature(),restored.signature());assertFalse(HelpPlan.forQuestion(restored).canTransfer());assertEquals(5,HelpPlan.forQuestion(restored).size());
 }
}
