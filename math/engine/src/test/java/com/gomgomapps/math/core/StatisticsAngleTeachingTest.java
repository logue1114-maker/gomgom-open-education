package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class StatisticsAngleTeachingTest {
 @Test public void normalProductionChecksEnteredStagesWithoutTransfer(){
  Generator g=new Generator(new Random(202610061501L));
  for(String id:List.of("mean","median","angles"))for(int i=0;i<500;i++){
   Question q=g.next(id,List.of(),i%2==0);assertNotNull(id,q.studyGuide);HelpPlan p=HelpPlan.forQuestion(q);assertFalse(p.canTransfer());int size=q.prompt.contains("의 중앙값")?q.prompt.substring(0,q.prompt.indexOf("의")).split(",").length:0;assertEquals(id.equals("mean")?3:id.equals("median")?size+(size%2==1?1:4):4,p.size());
   HelpPlan.Draft d=new HelpPlan.Draft();
   for(int j=0;j<p.size();j++){String expected=q.studyGuide.frames.get(j).expected;assertTrue(p.step(j).accepts(expected));assertFalse(p.step(j).accepts(Expression.number(expected).add(Rational.ONE).toString()));d.entries.add(expected);}d.stage=p.size();assertEquals(Expression.number(q.answers[0]),Expression.number(p.enteredAnswer(d)));
  }
 }
 @Test public void meanUsesPublicDataAndPreservesFraction(){
  Question q=new Question("mean","1, 2, 3, 5의 평균은?","999","999");StatisticsAngleTeaching.attach(q);assertEquals(List.of("11","4","11/4"),q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void medianSortsDuplicatesWithoutRevealingSortedAnswer(){
  Question q=new Question("median","9, 2, 2, 8, 1의 중앙값은?","999","999");StatisticsAngleTeaching.attach(q);assertEquals(List.of("1","2","2","8","9","2"),q.studyGuide.frames.stream().map(f->f.expected).toList());
  for(StudyGuide.Frame f:q.studyGuide.frames)assertFalse(f.before.contains("1, 2, 2, 8, 9"));
 }
 @Test public void angleUsesGivensAndRejectsDegenerateTriangle(){
  Question q=new Question("angles","삼각형","180-40-75","999");StatisticsAngleTeaching.attach(q);assertEquals(List.of("40","75","115","65"),q.studyGuide.frames.stream().map(f->f.expected).toList());
  Question invalid=new Question("angles","삼각형","180-90-90","999");StatisticsAngleTeaching.attach(invalid);assertNull(invalid.studyGuide);
 }
}
