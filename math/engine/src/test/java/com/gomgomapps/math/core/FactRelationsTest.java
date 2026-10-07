package com.gomgomapps.math.core;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.io.*;
public class FactRelationsTest {
 private void verify(Question q){
  String[] sides=q.prompt.split(" = "),terms=sides[0].split(" [×÷] ");boolean mul=q.prompt.contains("×"),left=terms[0].equals("□"),right=terms[1].equals("□");
  int a=left?0:Integer.parseInt(terms[0]),b=right?0:Integer.parseInt(terms[1]),total=sides.length==2?Integer.parseInt(sides[1]):0;
  int answer=left?(mul?total/b:total*b):right?(mul?total/a:a/total):(mul?a*b:a/b);
  List<Integer> expected=left?(mul?List.of(total,b,answer):List.of(b,total,answer)):right?List.of(mul?total:a,mul?a:total,answer):List.of(a,b,answer);
  String[] key=q.answers.clone();String signature=q.signature();HelpPlan p=HelpPlan.forQuestion(q);assertNotNull(p);assertEquals(3,p.size());assertFalse(p.canTransfer());assertEquals("fact-relations-v1",q.studyGuide.teachingVersion);
  HelpPlan.Draft d=p.restore(null,q.id);
  for(int i=0;i<3;i++){assertTrue(p.step(i).accepts(String.valueOf(expected.get(i))));assertFalse(p.step(i).accepts(""));assertFalse(p.step(i).accepts(String.valueOf(expected.get(i)+1)));assertEquals("",p.step(i).after);assertFalse(p.step(i).before.matches(".*[0-9].*"));d.entries.set(i,String.valueOf(expected.get(i)));d.stage++;d.entries.add("");}
  assertEquals(String.valueOf(answer),p.enteredAnswer(d));assertArrayEquals(key,q.answers);assertEquals(signature,q.signature());
 }
 @Test public void normalAndMissingTermFactsAreDerivedOnlyFromVisibleEquations(){
  for(String id:List.of("tables","divide"))for(String prompt:id.equals("tables")?List.of("3 × 4","3 × □ = 12","□ × 4 = 12","0 × 9","9 × 0"):List.of("12 ÷ 4","□ ÷ 4 = 3","12 ÷ □ = 3","0 ÷ 4")){
   Question q=new Question(id,prompt,"99999","99999");q.studyGuide=new StudyGuide().step("old","999 = ","999","999");verify(q);
  }
  Generator g=new Generator(new Random(710814));
  for(String id:List.of("tables","divide")){
   Set<String> prompts=new HashSet<>();for(int i=0;i<1500;i++){Question q=g.next(id,List.of(),i%2==0);prompts.add(q.prompt);assertNotNull(q.studyGuide);verify(q);verify(new Question(id,q.prompt,"wrong expression","wrong answer"));}
   assertEquals(id.equals("tables")?100:72,prompts.size());
   for(int grade:List.of(2,3)){List<String> recent=new ArrayList<>();Set<String> distinct=new HashSet<>();Set<String> forms=new HashSet<>();CurriculumLimits limits=GlobalCurriculum.limits("sg-moe-primary-2021-v1",id,grade);
    for(int i=0;i<100;i++){Question q=g.next(id,recent,false,limits);assertNotNull(q.studyGuide);assertTrue(limits.allows(q));verify(q);assertEquals(String.valueOf(FactFormsTest.solve(q.prompt)),q.answers[0]);assertTrue(distinct.add(q.prompt));forms.add(q.prompt.startsWith("□")?"left":q.prompt.contains("□")?"right":"direct");recent.add(q.signature());}
    assertEquals(Set.of("left","right","direct"),forms);
   }
  }
 }
 @Test public void oldNumericGuideAndDraftRefreshWhileNewPartialDraftSurvivesSerialization()throws Exception{
  Question q=new Question("tables","3 × □ = 12","999","999");q.studyGuide=new StudyGuide().step("old","12 ÷ 3 = ","","4");HelpPlan p=HelpPlan.forQuestion(q);HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("4"));HelpPlan.Draft d=p.restore(old,q.id);assertEquals(0,d.stage);d.entries.set(0,"12");d.stage=1;
  ByteArrayOutputStream out=new ByteArrayOutputStream();new ObjectOutputStream(out).writeObject(d);HelpPlan.Draft saved=(HelpPlan.Draft)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();assertEquals(1,p.restore(saved,q.id).stage);assertEquals("12",p.restore(saved,q.id).entries.get(0));
 }
 @Test public void invalidAmbiguousAndUnrelatedEquationsAreNotReinterpreted(){
  for(String prompt:List.of("□ × 0 = 0","0 × □ = 0","□ × □ = 12","3 × □ = 10","3 ÷ 0","3 ÷ 2","12 ÷ □ = 0","0 ÷ □ = 4")){Question q=new Question(prompt.contains("×")?"tables":"divide",prompt,"","0");FactRelations.attach(q);assertNull(q.studyGuide);}
  Question q=new Question("mul100","12 × 3","","36");FactRelations.attach(q);assertNull(q.studyGuide);
 }
}
