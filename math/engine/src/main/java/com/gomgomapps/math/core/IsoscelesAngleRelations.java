package com.gomgomapps.math.core;
import java.util.regex.*;
/** Equal base angles from the published apex angle. */
public final class IsoscelesAngleRelations {
 private IsoscelesAngleRelations(){}
 public static boolean supports(String id){return "sec_isosceles_angle".equals(id);}
 public static StudyDiagram diagram(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return null;
  Matcher m=Pattern.compile("꼭지각이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)°인 이등변삼각형의 한 밑각은\\?").matcher(q.prompt);if(!m.matches())return null;
  Rational a=Expression.number(m.group(1));if(a.compareTo(Rational.ZERO)<=0||a.compareTo(Rational.of(180))>=0)return null;
  return new StudyDiagram("isoscelesAngles",new double[]{a.n.doubleValue()/a.d.doubleValue()},m.group(1)+"°","그림은 비례하지 않음");
 }
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("꼭지각이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)°인 이등변삼각형의 한 밑각은\\?").matcher(q.prompt);if(!m.matches())return;
  Rational apex=Expression.number(m.group(1));if(apex.compareTo(Rational.ZERO)<=0||apex.compareTo(Rational.of(180))>=0)return;Rational sum=Rational.of(180).sub(apex);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="isosceles-angle-relations-v1";
  g.step("문제에서 꼭지각의 크기를 찾아 쓰세요.","꼭지각 = ","°",apex.toString())
   .step("삼각형의 내각의 합은 180°입니다. 두 밑각의 합을 구하세요.","180° − 꼭지각 = ","°",sum.toString())
   .step("두 밑각의 크기는 같습니다. 두 밑각의 합을 2로 나누세요.","두 밑각의 합 ÷ 2 = ","°",sum.div(Rational.of(2)).toString());q.studyGuide=g;
 }
}
