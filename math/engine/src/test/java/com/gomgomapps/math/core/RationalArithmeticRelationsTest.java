package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class RationalArithmeticRelationsTest {
 private void check(Question q){
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertFalse(p.canTransfer());assertEquals(signature,q.signature());
  List<String> values=q.studyGuide.frames.stream().map(f->f.expected).toList();
  for(int i=0;i<p.size();i++){assertFalse(p.step(i).before.matches(".*[0-9].*"));assertTrue(p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(Expression.number(values.get(i)).add(Rational.ONE).toString()));}
  int n=values.size();assertEquals(Expression.number(q.prompt),Rational.of(Long.parseLong(values.get(n-2)),Long.parseLong(values.get(n-1))));assertTrue(Long.parseLong(values.get(n-1))>0);
  HelpPlan.Draft draft=new HelpPlan.Draft();draft.entries=new ArrayList<>(values);draft.stage=n;assertEquals(Expression.number(q.prompt),Expression.number(p.enteredAnswer(draft)));
  q.expression="999";q.answers=new String[]{"999"};q.givenNumbers.put("answer","999");HelpPlan.forQuestion(q);assertEquals(values,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void fourOperationsHaveDistinctProblemsAndBlankFrames(){
  Generator g=new Generator(new Random(710091));Map<String,Set<String>> problems=new HashMap<>();
  for(int i=0;i<4000;i++){Question q=g.next("rational",List.of(),false);String op=q.prompt.substring(q.prompt.indexOf(")")+1).trim().substring(0,1);problems.computeIfAbsent(op,k->new HashSet<>()).add(q.prompt);check(q);}
  assertEquals(Set.of("+","-","*","/"),problems.keySet());for(String op:problems.keySet())assertTrue(op+" "+problems.get(op).size(),problems.get(op).size()>100);
 }
 @Test public void zeroCancellationSignedDivisionAndDraftReplacement(){
  for(int a:List.of(-6,0,6))for(int c:List.of(-4,0,4))for(String op:List.of("+","-","*","/")){
   if(op.equals("/")&&c==0)continue;String text="("+a+"/3) "+op+" ("+c+"/4)";check(new Question("rational",text,text,"999"));
  }
  Question q=new Question("rational","(-2/3) / (-4/5)","(-2/3) / (-4/5)","5/6");q.studyGuide=new StudyGuide().step("old","2 × 5 = ","","10");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("10"));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);d.entries.set(0,"-2");d.stage=1;p.restore(d,q.id);assertEquals(1,d.stage);
  Question invalid=new Question("rational","(1/2) / (0/3)","","0");RationalArithmeticRelations.attach(invalid);assertNull(invalid.studyGuide);
 }
}
