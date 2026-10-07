package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class ElementaryAreaRelationTest {
 @Test public void publicMeasurementsDetermineEveryLearnerEntry(){
  Generator g=new Generator(new Random(1271));boolean half=false;
  for(String id:List.of("el_triangle_area","el_rhombus_area","el_trapezoid_area"))for(int i=0;i<500;i++){
   Question q=g.create(Catalog.get(id));List<Long> v=new ArrayList<>();Matcher m=Pattern.compile("(\\d+)cm").matcher(q.prompt);while(m.find())v.add(Long.valueOf(m.group(1)));boolean trap=id.equals("el_trapezoid_area");assertEquals(trap?3:2,v.size());
   long product=(trap?v.get(0)+v.get(1):v.get(0))*v.get(trap?2:1);Rational area=Rational.of(product,2);half|=!area.isInteger();
   List<String> entries=trap?List.of(""+v.get(0),""+v.get(1),""+(v.get(0)+v.get(1)),""+v.get(2),""+product,area.toString()):List.of(""+v.get(0),""+v.get(1),""+product,area.toString());
   Arrays.fill(q.answers,"999999");HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(entries.size(),plan.size());assertFalse(plan.canTransfer());HelpPlan.Draft d=plan.restore(null,q.id);
   for(int k=0;k<entries.size();k++){assertTrue(plan.step(k).accepts(entries.get(k)));assertFalse(plan.step(k).accepts(Expression.number(entries.get(k)).add(Rational.ONE).toString()));assertFalse(plan.step(k).before.replace("÷ 2","").matches("(?s).*\\d.*"));d.entries.set(k,entries.get(k));d.stage++;d.entries.add("");}
   assertEquals(area,Expression.number(plan.enteredAnswer(d)));
  }assertTrue(half);
 }
 @Test public void migrationDiscardsOldIntermediateValueButPreservesNewEnteredDraft(){
  Question q=new Question("el_triangle_area","밑변 3cm, 높이 5cm인 삼각형의 넓이는?","3*5/2","15/2");q.studyGuide=new StudyGuide().step("old","3 × 5 = ","","15");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("15",""));
  HelpPlan plan=HelpPlan.forQuestion(q);plan.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));d.entries.set(0,"3");d.stage=1;d.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("3",restored.entries.get(0));assertEquals("",restored.entries.get(1));
 }
}
