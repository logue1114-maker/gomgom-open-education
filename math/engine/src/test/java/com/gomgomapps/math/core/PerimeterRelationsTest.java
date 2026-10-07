package com.gomgomapps.math.core;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class PerimeterRelationsTest {
 @Test public void legacyRadiusResultCannotBecomeTheDiameterInput(){Question q=new Question("el_circle_radius","지름이 8cm인 원의 반지름은?","8/2","4");q.studyGuide=new StudyGuide().step("old","8 ÷ 2 = ","cm","4");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("4",""));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));d.entries.set(0,"8");d.stage=1;d.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("8",restored.entries.get(0));assertEquals("",restored.entries.get(1));}
 @Test public void publicDecimalLengthsKeepExactAnswersAndUnits(){
  List<Question> qs=List.of(new Question("el_circle_radius","지름 5cm","5/2","2.5"),new Question("el_rectangle_perimeter","가로 2.5cm, 세로 3.5cm","2*(2.5+3.5)","12"),new Question("el_triangle_perimeter","세 변 2.5cm, 3.5cm, 4cm","2.5+3.5+4","10"));List<List<String>> expected=List.of(List.of("5","5/2"),List.of("5/2","7/2","6","12"),List.of("5/2","7/2","4","10"));for(int i=0;i<qs.size();i++){Question q=qs.get(i);Arrays.fill(q.answers,"999999");HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());assertEquals(expected.get(i).size(),p.size());for(int k=0;k<p.size();k++){assertTrue(p.step(k).accepts(expected.get(i).get(k)));assertEquals(" cm",p.step(k).after);}}
 }
}
