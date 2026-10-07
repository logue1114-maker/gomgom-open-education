package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class LinearEquationRelationsTest {
 private void check(Question q){
  String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertEquals(6,p.size());assertFalse(p.canTransfer());assertEquals(signature,q.signature());
  List<String> values=q.studyGuide.frames.stream().map(f->f.expected).toList();
  for(int i=0;i<6;i++){assertFalse(p.step(i).before.matches(".*[0-9].*"));assertTrue(p.step(i).accepts(values.get(i)));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(Expression.number(values.get(i)).add(Rational.ONE).toString()));}
  Matcher m=Pattern.compile("(?:x/|)(-?\\d+)(?:x|) \\+ \\((-?\\d+)\\) = (-?\\d+)").matcher(q.prompt.split("\\n")[0]);assertTrue(m.matches());Rational a=Expression.number(m.group(1)),b=Expression.number(m.group(2)),c=Expression.number(m.group(3)),x=Expression.number(values.get(5));
  assertEquals(c,q.skillId.equals("linearFraction")?x.div(a).add(b):x.mul(a).add(b));
  q.expression="999";q.answers=new String[]{"999"};q.givenNumbers.put("solution","999");HelpPlan.forQuestion(q);assertEquals(values,q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void generatedQuestionsIncludeZerosAndDiversity(){
  Generator g=new Generator(new Random(710101));for(String id:List.of("linear","linearFraction")){
   Set<String> prompts=new HashSet<>();boolean zeroConstant=false,zeroSolution=false;
   for(int i=0;i<4000;i++){Question q=g.next(id,List.of(),i%2==0);prompts.add(q.prompt);assertTrue(q.choices.isEmpty());check(q);zeroConstant|=q.studyGuide.frames.get(1).expected.equals("0");zeroSolution|=q.studyGuide.frames.get(5).expected.equals("0");}
   assertTrue(id,prompts.size()>100);assertTrue(zeroConstant);assertTrue(zeroSolution);
  }
 }
 @Test public void fractionalSolutionAndSavedGuideDraftsUseTheSameRelations(){
  Question q=new Question("linear","-2x + (0) = 1","-2x + (0) = 1","-1/2");q.kind="equation";check(q);assertEquals("-1/2",q.studyGuide.frames.get(5).expected);
  q.studyGuide=new StudyGuide().step("old","1 ÷ (-2) = ","","-1/2");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("-1/2"));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals(List.of(""),d.entries);d.entries.set(0,"-2");d.stage=1;p.restore(d,q.id);assertEquals(1,d.stage);
  for(String id:List.of("linear","linearFraction")){Question invalid=new Question(id,id.equals("linear")?"0x + (2) = 2":"x/0 + (2) = 2","","0");LinearEquationRelations.attach(invalid);assertNull(invalid.studyGuide);}
 }
}
