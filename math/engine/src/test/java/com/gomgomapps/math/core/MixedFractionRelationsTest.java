package com.gomgomapps.math.core;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class MixedFractionRelationsTest {
 private void verify(Question q){
  Matcher m=Pattern.compile("(\\d+) (\\d+)/(\\d+) ([+−-]) (\\d+) (\\d+)/(\\d+)").matcher(q.prompt);assertTrue(m.matches());
  BigInteger w=new BigInteger(m.group(1)),a=new BigInteger(m.group(2)),b=new BigInteger(m.group(3)),z=new BigInteger(m.group(5)),c=new BigInteger(m.group(6)),d=new BigInteger(m.group(7));
  BigInteger u=w.multiply(b).add(a),v=z.multiply(d).add(c),den=b.divide(b.gcd(d)).multiply(d),h=den.divide(b),k=den.divide(d),left=u.multiply(h),right=v.multiply(k),num=m.group(4).equals("+")?left.add(right):left.subtract(right),common=num.gcd(den);
  List<String> values=List.of(w,a,b,u,z,c,d,v,den,h,k,left,right,num,common,num.divide(common),den.divide(common)).stream().map(BigInteger::toString).toList();
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertEquals(17,p.size());assertFalse(p.canTransfer());
  for(int i=0;i<p.size();i++){assertTrue(values.get(i),p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(new BigInteger(values.get(i)).add(BigInteger.ONE).toString()));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*"));}
  HelpPlan.Draft draft=p.restore(null,q.id);draft.stage=17;draft.entries=new ArrayList<>(values);
  Rational expected=new Rational(num,den);assertEquals(expected,Expression.number(p.enteredAnswer(draft)));assertEquals(expected,Expression.number(q.answers[0]));
  List<String> original=q.studyGuide.frames.stream().map(frame->frame.expected).toList();q.expression="999";q.answers[0]="999";q.givenNumbers.put("answer","999");HelpPlan.forQuestion(q);assertEquals(original,q.studyGuide.frames.stream().map(frame->frame.expected).toList());
 }
 @Test public void generatedProblemsCoverDistinctMixedNumberArithmetic(){Generator generator=new Generator(new Random(710810));for(String id:List.of("fracMixedAdd","fracMixedSub")){Set<String> seen=new HashSet<>();for(int i=0;i<4000;i++){Question q=generator.next(id,List.of(),i%2==0);seen.add(q.prompt);verify(q);}assertTrue(id,seen.size()>100);}}
 @Test public void borrowingEqualityAndWholeResultsUseTheSameBlankFrames(){for(String raw:List.of("3 1/4 - 2 3/4","2 1/3 - 2 1/3","1 1/2 + 2 1/2","2 1/3 + 3 1/4")){MixedFractions.Givens p=MixedFractions.read(raw);Rational answer=raw.contains(" - ")?p.left().sub(p.right()):p.left().add(p.right());verify(new Question(raw.contains(" - ")?"fracMixedSub":"fracMixedAdd",raw,"999",answer.toString()));}}
 @Test public void serializedQuestionAndVersionedDraftsRestore()throws Exception{
  Question q=new Generator(new Random(710811)).next("fracMixedAdd",List.of(),false);ByteArrayOutputStream bytes=new ByteArrayOutputStream();new ObjectOutputStream(bytes).writeObject(q);
  Question saved=(Question)new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();HelpPlan p=HelpPlan.forQuestion(saved);
  HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("3/2"));old=p.restore(old,q.id);assertEquals(0,old.stage);assertTrue(old.entries.stream().allMatch(String::isEmpty));
  old.entries.set(0,saved.studyGuide.frames.get(0).expected);old.stage=1;assertEquals(1,p.restore(old,q.id).stage);verify(saved);
 }
}
