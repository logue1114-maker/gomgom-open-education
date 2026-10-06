package com.gomgomapps.math.core;
import java.math.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScientificQuantityTest {
 private static final String PACK="br-bncc-fundamental-2017-v1";
 @Test public void hundredMassProductsAreDistinctAndIndependentlySolved(){verify("scientificMassProduct",true);}
 @Test public void hundredSpeedQuotientsAreDistinctAndIndependentlySolved(){verify("scientificSpeedQuotient",false);}
 private void verify(String id,boolean product){
  Generator g=new Generator(new Random(product?20261006911L:20261006912L));Set<String> seen=new HashSet<>();Set<Integer> shifts=new HashSet<>(),signs=new HashSet<>();
  for(int i=0;i<100;i++){
   Question q=g.next(id,seen,i%2==0,GlobalCurriculum.limits(PACK,id,9));assertTrue(seen.add(q.signature()));
   Matcher m=Pattern.compile("([0-9.]+) × 10\\^\\((-?\\d+)\\)").matcher(q.prompt);assertTrue(m.find());
   BigDecimal a=new BigDecimal(m.group(1));int u=Integer.parseInt(m.group(2));assertTrue(m.find());BigDecimal b=new BigDecimal(m.group(1));int v=Integer.parseInt(m.group(2));assertFalse(m.find());
   // Solve the publicly stated full quantities, separately from generator normalization.
   BigDecimal left=a.scaleByPowerOfTen(u),right=b.scaleByPowerOfTen(v),total=product?left.multiply(right):left.divide(right);
   BigDecimal stripped=total.stripTrailingZeros();int n=stripped.precision()-stripped.scale()-1;String coefficient=total.scaleByPowerOfTen(-n).stripTrailingZeros().toPlainString();
   assertArrayEquals(new String[]{coefficient,String.valueOf(n)},q.answers);assertEquals("pair",q.kind);assertTrue(q.choices.isEmpty());
   Checker c=new Checker();assertTrue(c.check(q,List.of(),List.of(coefficient,String.valueOf(n))).correct());
   Checker.Result wrong=c.check(q,List.of(),List.of(coefficient,String.valueOf(n+1)));assertEquals(Checker.Status.WRONG_ANSWER,wrong.status);assertEquals(1,wrong.index);
   assertEquals(Checker.Status.INPUT_NEEDED,c.check(q,List.of(),List.of(coefficient,"")).status);
   assertEquals(4,q.studyGuide.frames.size());assertFalse(q.studyGuide.transfer);assertFalse(HelpPlan.forQuestion(q).canTransfer());
   assertEquals((product?a.multiply(b):a.divide(b)).stripTrailingZeros().toPlainString(),q.studyGuide.frames.get(0).expected);
   assertEquals(String.valueOf(product?u+v:u-v),q.studyGuide.frames.get(1).expected);assertEquals(coefficient,q.studyGuide.frames.get(2).expected);assertEquals(String.valueOf(n),q.studyGuide.frames.get(3).expected);
   shifts.add(n-(product?u+v:u-v));signs.add(Integer.signum(n));
  }
  assertEquals(100,seen.size());assertTrue(shifts.contains(0));assertTrue(shifts.contains(product?1:-1));assertTrue(signs.contains(-1));assertTrue(signs.contains(1));
 }
 @Test public void currentGradeUnitsAreExcludedFromInitialDiagnosis(){
  Learning.Profile p=new Learning.Profile();GlobalCurriculum.chooseCountry(p,"BR");GlobalCurriculum.choosePack(p,PACK);p.grade=9;
  for(var s:ScientificQuantity.SKILLS){assertTrue(GlobalCurriculum.pack(p).inGrade(s.id,9));assertFalse(GlobalCurriculum.scope(p).stream().anyMatch(x->x.id.equals(s.id)));}
 }
 @Test public void questionSerializationPreservesGivensAndBothBlankAnswerSlots() throws Exception {
  Question q=new Generator(new Random(9)).next("scientificSpeedQuotient",List.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);
  Question restored=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();assertEquals(q.id,restored.id);assertEquals(q.prompt,restored.prompt);assertArrayEquals(q.answers,restored.answers);assertEquals(4,restored.studyGuide.frames.size());assertFalse(restored.studyGuide.transfer);
 }
}
