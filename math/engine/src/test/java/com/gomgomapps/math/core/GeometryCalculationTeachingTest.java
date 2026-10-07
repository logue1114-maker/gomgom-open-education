package com.gomgomapps.math.core;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class GeometryCalculationTeachingTest {
 @Test public void normalProductionSeparatesEachOperationAndNeverTransfers(){
  Generator g=new Generator(new Random(202610061801L));for(String id:List.of("el_triangle_area","el_rhombus_area","el_trapezoid_area","el_triangle_angle_sum","el_quadrilateral_angle_sum"))for(int i=0;i<500;i++){
   Question q=g.next(id,List.of(),i%2==0);HelpPlan plan=HelpPlan.forQuestion(q);assertNotNull(plan);assertFalse(plan.canTransfer());assertEquals(id.equals("el_trapezoid_area")?6:Set.of("el_triangle_area","el_rhombus_area").contains(id)?4:id.equals("el_triangle_angle_sum")?4:5,plan.size());HelpPlan.Draft d=new HelpPlan.Draft();
   for(int j=0;j<plan.size();j++){String expected=q.studyGuide.frames.get(j).expected;assertTrue(plan.step(j).accepts(expected));assertFalse(plan.step(j).accepts(Expression.number(expected).add(Rational.ONE).toString()));d.entries.add(expected);}d.stage=plan.size();assertEquals(Expression.number(q.answers[0]),Expression.number(plan.enteredAnswer(d)));
  }
 }
 @Test public void oddProductsRemainExactAndDoNotReadAnswerKey(){
  for(String id:List.of("el_triangle_area","el_rhombus_area")){Question q=new Question(id,"public dimensions","3*5/2","999");GeometryCalculationTeaching.attach(q);assertEquals(List.of("3","5","15","15/2"),q.studyGuide.frames.stream().map(f->f.expected).toList());}
  Question q=new Question("el_trapezoid_area","public dimensions","(2+3)*3/2","999");GeometryCalculationTeaching.attach(q);assertEquals(List.of("2","3","5","3","15","15/2"),q.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void anglesUseGivenTotalAndOnlyPublicAngles(){
  Question q=new Question("el_quadrilateral_angle_sum","public angles","360-70-80-90","999");GeometryCalculationTeaching.attach(q);assertEquals(List.of("70","80","90","240","120"),q.studyGuide.frames.stream().map(f->f.expected).toList());
  Question t=new Question("el_triangle_angle_sum","public angles","180-40-75","999");GeometryCalculationTeaching.attach(t);assertEquals(List.of("40","75","115","65"),t.studyGuide.frames.stream().map(f->f.expected).toList());
 }
 @Test public void halfAreaResultsOfferFractionEntryWithoutLookingAtAnswer(){
  for(String id:List.of("el_triangle_area","el_rhombus_area","el_trapezoid_area")){Question q=new Question(id,"public dimensions","3*5/2","999");assertTrue(FractionInput.available(q));assertFalse(FractionInput.defaultFraction(q));}
 }
}
