package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;import org.junit.Test;import static org.junit.Assert.*;
public class SimpleGeometryRelationsTest {
 @Test public void everyEntryFollowsPublicMeasurementsWithoutReadingAnswerKeys(){
  Generator g=new Generator(new Random(1371));for(String id:SimpleGeometryRelations.IDS)for(int i=0;i<500;i++){
   Question q=g.create(Catalog.get(id));List<Rational> v=new ArrayList<>();Matcher m=Pattern.compile("(\\d+)\\s*cm").matcher(q.prompt);while(m.find())v.add(Expression.number(m.group(1)));Rational a=v.get(0),b=v.size()>1?v.get(1):a,h=v.size()>2?v.get(2):a,p=a.mul(b),square=a.mul(a);List<Rational> expected;
   switch(id){
    case "el_circle_diameter":expected=List.of(a,a.mul(Rational.of(2)));break;
    case "el_circle_radius":expected=List.of(a,a.div(Rational.of(2)));break;
    case "el_rectangle_perimeter":case "el_parallelogram_perimeter":expected=List.of(a,b,a.add(b),a.add(b).mul(Rational.of(2)));break;
    case "el_square_perimeter":expected=List.of(a,a.mul(Rational.of(4)));break;
    case "el_triangle_perimeter":expected=List.of(a,b,h,a.add(b).add(h));break;
    case "el_rectangle_area":case "el_parallelogram_area":expected=List.of(a,b,p);break;
    case "el_square_area":expected=List.of(a,square);break;
    case "el_circle_area":expected=List.of(a,square,Rational.of(157,50).mul(square));break;
    case "el_circle_circumference":expected=List.of(a,a.mul(Rational.of(2)),a.mul(Rational.of(157,25)));break;
    case "el_rect_prism_volume":expected=List.of(a,b,p,h,p.mul(h));break;
    case "el_rect_prism_surface":Rational front=a.mul(h),side=b.mul(h),sum=p.add(front).add(side);expected=List.of(a,b,h,p,front,side,sum,sum.mul(Rational.of(2)));break;
    case "el_cube_volume":expected=List.of(a,square,a.pow(3));break;
    case "el_cube_surface":expected=List.of(a,square,square.mul(Rational.of(6)));break;
    default:throw new AssertionError(id);
   }
   assertTrue(new Checker().check(q,List.of(),List.of(expected.get(expected.size()-1).toString())).correct());String signature=q.signature();Arrays.fill(q.answers,"999999");HelpPlan plan=HelpPlan.forQuestion(q);assertEquals(signature,q.signature());assertEquals(expected.size(),plan.size());assertFalse(plan.canTransfer());HelpPlan.Draft d=plan.restore(null,q.id);
   for(int k=0;k<expected.size();k++){assertTrue(plan.step(k).accepts(expected.get(k).toString()));assertFalse(plan.step(k).accepts(expected.get(k).add(Rational.ONE).toString()));String before=plan.step(k).before.replace("3.14","").replace("× 2","").replace("÷ 2","").replace("× 4","").replace("× 6","");assertFalse(before,before.matches("(?s).*\\d.*"));d.entries.set(k,expected.get(k).toString());d.stage++;d.entries.add("");}
   assertEquals(expected.get(expected.size()-1),Expression.number(plan.enteredAnswer(d)));
  }
 }
 @Test public void oldGuideValuesAreClearedAndConfirmedNewMeasurementsSurvive(){
  Question q=new Question("el_rect_prism_volume","가로 3cm, 세로 4cm, 높이 5cm인 직육면체의 부피는?","3*4*5","60");q.studyGuide=new StudyGuide().step("old","3 × 4 = ","","12");HelpPlan.Draft d=new HelpPlan.Draft();d.questionId=q.id;d.stage=1;d.entries=new ArrayList<>(List.of("12",""));HelpPlan p=HelpPlan.forQuestion(q);p.restore(d,q.id);assertEquals(0,d.stage);assertEquals("",d.entries.get(0));d.entries.set(0,"3");d.stage=1;d.entries.add("");HelpPlan.Draft restored=HelpPlan.forQuestion(q).restore(d.copy(),q.id);assertEquals(1,restored.stage);assertEquals("3",restored.entries.get(0));assertEquals("",restored.entries.get(1));assertEquals("simple-geometry-relations-v1",restored.teachingVersion);
 }
}
