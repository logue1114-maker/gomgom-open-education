package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class BasicAlgebraRelationsTest {
 @Test public void publicGivensBlankFramesAndDistinctProblems(){
  Generator generator=new Generator(new Random(71007));
  for(String id:List.of("signedAdd","signedMul","likeTerms")){
   Set<String> problems=new HashSet<>();
   for(int i=0;i<3000;i++){
    Question q=generator.next(id,List.of(),false);problems.add(q.prompt);String signature=q.signature();
    HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(id,plan);assertFalse(plan.canTransfer());
    List<String> entries=q.studyGuide.frames.stream().map(f->f.expected).toList();
    for(int j=0;j<plan.size();j++){HelpPlan.Step step=plan.step(j);assertFalse(step.before,step.before.matches(".*\\d.*"));assertFalse(step.after,step.after.matches(".*\\d.*"));assertTrue(step.accepts(entries.get(j)));assertFalse(step.accepts(""));assertFalse(step.accepts(Expression.number(entries.get(j)).add(Rational.ONE).toString()));}
    if(id.equals("likeTerms"))assertEquals(Expression.parse(q.prompt).toString(),Expression.parse(entries.get(3)+"x+("+entries.get(2)+")").toString());
    else assertEquals(Expression.number(q.prompt).toString(),entries.get(entries.size()-1));
    q.expression="999";q.answers=new String[]{"999"};HelpPlan.forQuestion(q);assertEquals(entries,q.studyGuide.frames.stream().map(f->f.expected).toList());
    assertNotNull(signature);
   }
   assertTrue(id+" distinct problems "+problems.size(),problems.size()>100);
  }
 }
 @Test public void oldGuidesAreReplacedAndNewDraftsRestore(){
  Question q=new Question("signedAdd","(-7) - (-7)","(-7) - (-7)","0");String signature=q.signature();
  q.studyGuide=new StudyGuide().step("old","7 + 7 = ","","14");HelpPlan.Draft old=new HelpPlan.Draft();old.questionId=q.id;old.stage=1;old.entries=new ArrayList<>(List.of("14"));
  HelpPlan plan=HelpPlan.forQuestion(q);plan.restore(old,q.id);assertEquals(0,old.stage);assertEquals(List.of(""),old.entries);assertEquals(signature,q.signature());assertEquals("0",q.studyGuide.frames.get(plan.size()-1).expected);
  old.entries.set(0,"-7");old.stage=1;plan.restore(old,q.id);assertEquals(1,old.stage);assertEquals(List.of("-7",""),old.entries);
 }
}
