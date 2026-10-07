package com.gomgomapps.math.core;
import java.util.regex.*;
/** Sector perimeter relationships derived exclusively from public givens. */
public final class SectorPerimeterRelations {
 private SectorPerimeterRelations(){}
 public static boolean supports(String id){return "sectorPerimeter".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("반지름: (\\d+(?:\\.\\d+)?(?:/\\d+)?)cm · 중심각: (\\d+(?:\\.\\d+)?(?:/\\d+)?)°\\nπ = (\\d+(?:\\.\\d+)?(?:/\\d+)?) 사용\\n부채꼴의 전체 둘레는 몇 cm인가요\\?").matcher(q.prompt);if(!m.matches())return;
  Rational radius=Expression.number(m.group(1)),angle=Expression.number(m.group(2)),pi=Expression.number(m.group(3));
  Rational circle=radius.mul(Rational.of(2)).mul(pi),arc=circle.mul(angle).div(Rational.of(360));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="sector-perimeter-relations-v1";
  g
   .step("문제에서 반지름을 찾아 쓰세요.","반지름 = "," cm",radius.toString())
   .step("문제에서 중심각을 찾아 쓰세요.","중심각 = ","°",angle.toString())
   .step("문제에서 사용할 원주율을 찾아 쓰세요.","원주율 = ","",pi.toString())
   .step("원 한 바퀴의 길이를 구하세요.","2 × 반지름 × 원주율 = "," cm",circle.toString())
   .step("중심각에 해당하는 호의 길이를 구하세요.","원둘레 × 중심각 ÷ 360 = "," cm",arc.toString())
   .step("호의 길이에 두 반지름을 더해 둘레를 구하세요.","호의 길이 + 2 × 반지름 = "," cm",arc.add(radius.mul(Rational.of(2))).toString());q.studyGuide=g;
 }
}
