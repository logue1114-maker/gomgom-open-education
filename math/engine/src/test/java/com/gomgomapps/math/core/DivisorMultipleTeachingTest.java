package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class DivisorMultipleTeachingTest {
 private int common(int a,int b){for(int d=Math.min(a,b);d>=1;d--)if(a%d==0&&b%d==0)return d;throw new AssertionError();}
 @Test public void normalGeneratedGuidesEndAtAnIndependentlyFoundDivisorOrMultiple(){
  Generator g=new Generator(new Random(202610062501L));for(String id:List.of("gcd","lcm"))for(int i=0;i<1000;i++){
   Question q=g.next(id,List.of(),false);java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)와 (\\d+).*",java.util.regex.Pattern.DOTALL).matcher(q.prompt);assertTrue(m.matches());int a=Integer.parseInt(m.group(1)),b=Integer.parseInt(m.group(2)),d=common(a,b);HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());HelpPlan.Draft draft=new HelpPlan.Draft();
   for(int j=0;j<plan.size();j++){String value=q.studyGuide.frames.get(j).expected;assertTrue(plan.step(j).accepts(value));assertFalse(plan.step(j).accepts(Expression.number(value).add(Rational.ONE).toString()));draft.entries.add(value);}draft.stage=plan.size();assertEquals(Rational.of(id.equals("gcd")?d:a/d*b),Expression.number(plan.enteredAnswer(draft)));
  }
 }
 @Test public void everyDivisionIsSeparatedIntoQuotientProductAndRemainder(){
  Question q=new Question("gcd","55와 34의 최대공약수는?","","999");DivisorMultipleTeaching.attach(q);List<String> expected=q.studyGuide.frames.stream().map(f->f.expected).toList();assertEquals(List.of("1","34","21","1","21","13","1","13","8","1","8","5","1","5","3","1","3","2","1","2","1","2","2","0","1"),expected);assertEquals("999",q.answers[0]);
 }
 @Test public void equalAndDivisibleInputsKeepTheZeroRemainder(){
  Question q=new Question("gcd","12와 12의 최대공약수는?","","999");DivisorMultipleTeaching.attach(q);assertEquals(List.of("1","12","0","12"),q.studyGuide.frames.stream().map(f->f.expected).toList());
  q=new Question("lcm","8와 24의 최소공배수는?","","999");DivisorMultipleTeaching.attach(q);assertEquals(List.of("3","24","0","8","1","24"),q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void coprimeNumbersProduceOneAsTheCommonDivisor(){
  Question q=new Question("lcm","7와 11의 최소공배수는?","","999");DivisorMultipleTeaching.attach(q);List<String> entries=q.studyGuide.frames.stream().map(f->f.expected).toList();assertEquals("1",entries.get(entries.size()-3));assertEquals("7",entries.get(entries.size()-2));assertEquals("77",entries.get(entries.size()-1));assertFalse(q.studyGuide.transfer);
 }
}
