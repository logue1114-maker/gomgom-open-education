package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class FractionReductionRelationsTest {
 private void check(Question q){
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertEquals(5,p.size());assertFalse(p.canTransfer());assertEquals(signature,q.signature());
  Matcher m=Pattern.compile("(\\d+)/(\\d+)을 기약분수로 나타내세요\\.").matcher(q.prompt);assertTrue(m.matches());long n=Long.parseLong(m.group(1)),d=Long.parseLong(m.group(2));
  List<String> values=q.studyGuide.frames.stream().map(f->f.expected).toList();assertEquals(""+n,values.get(0));assertEquals(""+d,values.get(1));long g=Long.parseLong(values.get(2)),rn=Long.parseLong(values.get(3)),rd=Long.parseLong(values.get(4));
  assertTrue(g>0);assertEquals(0,n%g);assertEquals(0,d%g);assertEquals(n,rn*g);assertEquals(d,rd*g);assertEquals(n*rd,d*rn);assertTrue(rd>0);
  for(long divisor=2;divisor<=Math.min(rn,rd);divisor++)assertFalse(rn%divisor==0&&rd%divisor==0);
  for(int i=0;i<5;i++){assertFalse(p.step(i).before.matches(".*[0-9].*"));assertEquals("",p.step(i).after);assertTrue(p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(Expression.number(values.get(i)).add(Rational.ONE).toString()));}
  q.expression="999";q.answers=new String[]{"999"};HelpPlan.forQuestion(q);assertEquals(values,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void distinctProblemsAndPublicOperandChecks(){Generator g=new Generator(new Random(710107));Set<String> prompts=new HashSet<>();for(int i=0;i<4000;i++){Question q=g.next("reduce",List.of(),i%2==0);prompts.add(q.prompt);check(q);}assertTrue(prompts.size()>100);}
 @Test public void oldDraftResetsAndNewDraftRestores(){Question q=new Question("reduce","18/30을 기약분수로 나타내세요.","3/5","3/5");q.studyGuide=new StudyGuide().step("old","18 ÷ 6 = ","","3");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("3"));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);d.entries.set(0,"18");d.stage=1;p.restore(d,q.id);assertEquals(1,d.stage);assertEquals("18",d.entries.get(0));}
 @Test public void zeroNumeratorAndInvalidDenominator(){Question q=new Question("reduce","0/30을 기약분수로 나타내세요.","0","0");check(q);assertEquals("1",q.studyGuide.frames.get(4).expected);Question invalid=new Question("reduce","18/0을 기약분수로 나타내세요.","","0");FractionReductionRelations.attach(invalid);assertNull(invalid.studyGuide);}
}
