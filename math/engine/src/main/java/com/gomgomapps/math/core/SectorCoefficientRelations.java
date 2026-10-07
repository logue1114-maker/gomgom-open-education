package com.gomgomapps.math.core;
import java.util.*;import java.util.regex.*;
/** Exact pi-coefficient relationships from public radius and central angle. */
public final class SectorCoefficientRelations {
 private SectorCoefficientRelations(){}
 public static boolean supports(String id){return Set.of("sec_sector_arc","sec_sector_area").contains(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  Matcher m=Pattern.compile("반지름이 (\\d+(?:\\.\\d+)?(?:/\\d+)?), 중심각이 (\\d+(?:\\.\\d+)?(?:/\\d+)?)°인 부채꼴의 (호의 길이|넓이)는 \\(□\\)π입니다\\. □는\\?").matcher(q.prompt);if(!m.matches())return;
  boolean arc=q.skillId.equals("sec_sector_arc");if(arc!=m.group(3).equals("호의 길이"))return;
  Rational radius=Expression.number(m.group(1)),angle=Expression.number(m.group(2)),circle=arc?radius.mul(Rational.of(2)):radius.pow(2),coefficient=circle.mul(angle).div(Rational.of(360));
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="sector-coefficient-relations-v1";
  g
   .step("문제에서 반지름을 찾아 쓰세요.","반지름 = ","",radius.toString())
   .step("문제에서 중심각을 찾아 쓰세요.","중심각 = ","°",angle.toString());
  if(arc){g
   .step("원둘레를 (□)π로 나타낼 때 □를 구하세요.","2 × 반지름 = ","",circle.toString())
   .step("원둘레의 π계수에 중심각의 비율을 곱하세요.","원둘레의 π계수 × 중심각 ÷ 360 = ","",coefficient.toString());}else{g
   .step("원의 넓이를 (□)π로 나타낼 때 □를 구하세요.","반지름² = ","",circle.toString())
   .step("원넓이의 π계수에 중심각의 비율을 곱하세요.","원넓이의 π계수 × 중심각 ÷ 360 = ","",coefficient.toString());}
  q.studyGuide=g;
 }
}
