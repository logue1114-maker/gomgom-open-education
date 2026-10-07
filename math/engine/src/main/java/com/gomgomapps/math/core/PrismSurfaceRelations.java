package com.gomgomapps.math.core;
import java.util.regex.*;
/** Right-triangle prism surface relationships built only from public dimensions. */
public final class PrismSurfaceRelations {
 private PrismSurfaceRelations(){}
 public static boolean supports(String id){return "triangularPrismSurface".equals(id);}
 public static void attach(Question q){
  if(q==null||!supports(q.skillId)||q.prompt==null)return;
  String n="(\\d+(?:\\.\\d+)?(?:/\\d+)?)";Matcher m=Pattern.compile("곧은 삼각기둥 · 길이 "+n+"cm\\n밑면: 직각삼각형\\n직각의 두 변 "+n+"cm, "+n+"cm · 빗변 "+n+"cm\\n겉넓이는 몇 cm²인가요\\?").matcher(q.prompt);if(!m.matches())return;
  Rational length=Expression.number(m.group(1)),a=Expression.number(m.group(2)),b=Expression.number(m.group(3)),c=Expression.number(m.group(4));
  Rational area=a.mul(b).div(Rational.of(2)),perimeter=a.add(b).add(c),sides=perimeter.mul(length);
  StudyGuide g=new StudyGuide().transfer(false);g.teachingVersion="prism-surface-relations-v1";
  g.step("직각을 이루는 첫째 변의 길이를 찾아 쓰세요.","첫째 변 = "," cm",a.toString())
   .step("직각을 이루는 둘째 변의 길이를 찾아 쓰세요.","둘째 변 = "," cm",b.toString())
   .step("빗변의 길이를 찾아 쓰세요.","빗변 = "," cm",c.toString())
   .step("삼각기둥의 길이를 찾아 쓰세요.","기둥 길이 = "," cm",length.toString())
   .step("밑면 한 개의 넓이를 구하세요.","첫째 변 × 둘째 변 ÷ 2 = "," cm²",area.toString())
   .step("밑면의 둘레를 구하세요.","첫째 변 + 둘째 변 + 빗변 = "," cm",perimeter.toString())
   .step("세 옆면의 넓이 합을 구하세요.","밑면 둘레 × 기둥 길이 = "," cm²",sides.toString())
   .step("삼각기둥의 겉넓이를 구하세요.","밑면 넓이 × 2 + 옆면 넓이 합 = "," cm²",area.mul(Rational.of(2)).add(sides).toString());q.studyGuide=g;
 }
}
