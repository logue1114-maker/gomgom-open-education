package com.gomgomapps.math.core;
import java.math.*;import java.util.*;import java.util.regex.*;import java.io.*;import org.junit.Test;import static org.junit.Assert.*;
public class DecimalFractionRelationsTest {
 private void check(Question q)throws Exception{
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertFalse(p.canTransfer());assertEquals(signature,q.signature());List<String> values;String result;
  if(q.skillId.equals("el_fraction_decimal")){
   Matcher m=Pattern.compile("(\\d+)/(\\d+)의 값을 소수로 나타내세요\\.").matcher(q.prompt);assertTrue(m.matches());BigDecimal n=new BigDecimal(m.group(1)),d=new BigDecimal(m.group(2));result=n.divide(d).toPlainString();if(!result.contains("."))result+=".0";values=List.of(m.group(1),m.group(2),BigDecimal.ONE.divide(d).toPlainString(),result);assertFalse(p.step(3).accepts(m.group(1)+"/"+m.group(2)));
  }else{
   String raw=q.prompt.substring(0,q.prompt.indexOf('의'));int places=raw.length()-raw.indexOf('.')-1;long den=(long)Math.pow(10,places);long num=new BigDecimal(raw).multiply(BigDecimal.valueOf(den)).longValueExact();values=List.of(raw,""+places,""+den,""+num);result=num+"/"+den;assertEquals(new BigDecimal(raw),BigDecimal.valueOf(num).divide(BigDecimal.valueOf(den)));assertFalse(p.step(0).accepts(num+"/"+den));
  }
  assertEquals(values.size(),p.size());for(int i=0;i<p.size();i++){assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*")&&!p.step(i).before.equals("분모 d = 자릿수 p에 맞는 분모 = ")&&!p.step(i).before.equals("한 부분 u = 1 ÷ d = "));assertTrue(values.get(i),p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(new BigDecimal(values.get(i)).add(BigDecimal.ONE).toPlainString()));}
  HelpPlan.Draft draft=new HelpPlan.Draft();draft.stage=p.size();draft.entries=new ArrayList<>(values);assertEquals(result,p.enteredAnswer(draft));
  List<String> expected=q.studyGuide.frames.stream().map(f->f.expected).toList();q.expression="999";q.answers=new String[]{"999"};q.givenNumbers.put("answer","999");HelpPlan.forQuestion(q);assertEquals(expected,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void bothDirectionsHaveMoreThan100DistinctProblemsAndCheckedBlankFrames()throws Exception{Generator g=new Generator(new Random(710210));for(String id:List.of("el_fraction_decimal","el_decimal_fraction")){Set<String> seen=new HashSet<>();for(int i=0;i<4000;i++){Question q=g.next(id,List.of(),false);seen.add(q.prompt);check(q);}assertTrue(id+" "+seen.size(),seen.size()>100);}}
 @Test public void savedGuidesRetainDecimalFormatAndDraftPolicy()throws Exception{for(String id:List.of("el_fraction_decimal","el_decimal_fraction")){Question q=new Generator(new Random(710211)).next(id,List.of(),false);check(q);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();check(saved);HelpPlan p=HelpPlan.forQuestion(saved);HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("old"));d=p.restore(d,q.id);assertEquals(0,d.stage);d.entries.set(0,q.studyGuide.frames.get(0).expected);d.stage=1;d=p.restore(d,q.id);assertEquals(1,d.stage);}}
 @Test public void publicPlaceValueHandlesTrailingZerosAndRejectsWrongForms(){Question q=new Question("el_decimal_fraction","2.50의 값을 분수로 나타내세요.","","5/2");HelpPlan p=HelpPlan.forQuestion(q);assertTrue(p.step(1).accepts("2"));assertTrue(p.step(2).accepts("100"));assertTrue(p.step(3).accepts("250"));for(String text:List.of("1/0의 값을 소수로 나타내세요.","1/3의 값을 소수로 나타내세요.")){Question bad=new Question("el_fraction_decimal",text,"","0");DecimalFractionRelations.attach(bad);assertNull(bad.studyGuide);}}
 @Test public void exactNumeratorLongBoundaryPreservedWithoutUnavailableApi(){
  Question q=new Question("el_decimal_fraction","922337203685477580.7의 값을 분수로 나타내세요.","","");HelpPlan p=HelpPlan.forQuestion(q);assertTrue(p.step(3).accepts("9223372036854775807"));
  try{HelpPlan.forQuestion(new Question("el_decimal_fraction","922337203685477580.8의 값을 분수로 나타내세요.","",""));fail("overflow must be rejected");}catch(ArithmeticException expected){}
 }
}
