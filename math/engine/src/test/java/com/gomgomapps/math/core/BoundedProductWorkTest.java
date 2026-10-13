package com.gomgomapps.math.core;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class BoundedProductWorkTest {
 @Test public void fullAlternativeWorkAndTransferUsePublicFactors(){
  Question q=BoundedWholeProduct.make(167,572);q.expression="poison";q.answers=new String[]{"poison"};Checker c=new Checker();
  List<Checker.StepKind> full=List.of(Checker.StepKind.FULL);
  List<String> lines=List.of("(160+7)*572=95524");
  assertTrue(c.checkSteps(q,lines,full).correct());assertEquals(List.of("95524"),WorkAnswer.writtenAnswers(q,lines,full));
  assertEquals(Checker.Status.WRONG_STEP,c.checkSteps(q,List.of("(160+7)*572=95525"),full).status);
  assertEquals("",WorkAnswer.writtenAnswer(q,List.of("1+1=2"),full));
  assertTrue(c.checkSteps(q,List.of("1+1=2"),List.of(Checker.StepKind.PARTIAL)).correct());
  assertEquals("",WorkAnswer.writtenAnswer(q,List.of("1+1=2"),List.of(Checker.StepKind.PARTIAL)));
 }
 @Test public void boundariesAndMalformedPublicProblemsPreserveWorkSafety(){
  Checker c=new Checker();List<Checker.StepKind> full=List.of(Checker.StepKind.FULL);
  for(int[] a:List.of(new int[]{0,1000000},new int[]{1000000,1},new int[]{1000,1000})){
   Question q=BoundedWholeProduct.make(a[0],a[1]);String value=""+((long)a[0]*a[1]);q.expression="poison";q.answers=new String[]{"poison"};
   List<String> line=List.of(a[0]+"*"+a[1]+"="+value);assertTrue(c.checkSteps(q,line,full).correct());assertEquals(value,WorkAnswer.writtenAnswer(q,line,full));
  }
  Question q=BoundedWholeProduct.make(167,572);q.prompt="1000000 × 2";
  assertEquals(Checker.Status.INPUT_NEEDED,c.checkSteps(q,List.of("2*1000000=2000000"),full).status);
 }
}
